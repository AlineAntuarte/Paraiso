package Paraiso;

import robocode.*;
import java.awt.Color;
import java.awt.geom.*;
import java.util.*;
import robocode.util.Utils;

//LittleBoy - autores (Aline, Thalia e Thaissa)

public class LittleBoy extends AdvancedRobot {
	// ------------------------------
	// VARIÁVEIS PARA WAVE SURFING
	// ------------------------------
	static double surfDirection = 1;
	static double enemyEnergy = 100;
	ArrayList<EnemyWave> enemyWaves = new ArrayList<>();

	// --- [CORREÇÃO EVASÃO] ---
	// A memória de longo prazo (mapa de perigo) agora usa Double
	// para armazenar nuances de perigo (ex: 0.9, 0.4).
	ArrayList<Double> surfStats = new ArrayList<Double>(Collections.nCopies(47, 0.0));

	// --- [Método Anti Gravidade] ---
	// De acordo com a pesquisa, o robô deve repelir as paredes e inimigos como se
	// fossem o sol, o que explica o seu nome.
	Point2D.Double lastEnemyLocation;

	// ------------------------------
	// run: Comportamento padrão do LittleBoy, tanto mover tanque quanto arma.
	// ------------------------------
	public void run() {
		// Inicialização do Radar
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		// Inicialização da Arma
		setTurnGunRight(Double.POSITIVE_INFINITY);

		setBodyColor(Color.black);
		setRadarColor(Color.blue);

		// Loop principal do robô
		while (true) {
			// Settings Radar
			if (getRadarTurnRemaining() == 0.0)
				setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

			// Settings Movimentação: MOVIMENTO ANTIGRAVIDADE

			// A lógica de evasão (Wave Surfing) tem prioridade total.
			if (!enemyWaves.isEmpty()) {
				// Se HÁ ondas de tiro para se esquivar, use o Wave Surfing.
				updateWaves();
				doSurfing();
			} else {
				// Se NÃO há ondas, use o movimento AntiGravidade padrão.
				// Isso substitui o "vai-e-vem" e evita colisões.
				doAntiGravity();
			}
			// A antiga lógica de 'counter' e 'setAhead/setBack' foi removida.

			// Atualiza as ondas inimigas e realiza o movimento evasivo
			updateWaves();
			doSurfing();

			// --- [CORREÇÃO EVASÃO] ---
			// Chamamos o novo método para "esquecer" perigos antigos a cada turno.
			decayDangerMap();

			execute();
		}
	}

	// onScannedRobot: O que fazer ao ver outro robô
	public void onScannedRobot(ScannedRobotEvent e) {
		shoot(e);

		// Quando Radar encontra um inimigo
		double angleToEnemy = getHeadingRadians() + e.getBearingRadians();
		// Nova linha para memorizar onde vimos o inimigo (A. Gravidade)
		// Calcula e salva a localização absoluta (x, y) do inimigo.
		// Usamos isso para saber de qual coordenada devemos "fugir".
		double enemyX = getX() + Math.sin(angleToEnemy) * e.getDistance();
		double enemyY = getY() + Math.cos(angleToEnemy) * e.getDistance();
		lastEnemyLocation = new Point2D.Double(enemyX, enemyY);

		double turnToEnemy = Utils.normalRelativeAngle(angleToEnemy - getRadarHeadingRadians());
		double extraTurn = Math.atan(36.0 / e.getDistance()) * (turnToEnemy >= 0 ? 1 : -1);
		setTurnRadarRightRadians(turnToEnemy + extraTurn);

		// ------------------------------
		// DETECÇÃO DE TIRO INIMIGO
		// ------------------------------
		double changeInEnergy = enemyEnergy - e.getEnergy();
		if (changeInEnergy > 0 && changeInEnergy <= 3.0) {
			EnemyWave ew = new EnemyWave();
			ew.fireTime = getTime();
			ew.bulletVelocity = bulletVelocity(changeInEnergy);
			ew.fireLocation = new Point2D.Double(getX() + Math.sin(angleToEnemy) * e.getDistance(),
					getY() + Math.cos(angleToEnemy) * e.getDistance());
			ew.directAngle = angleToEnemy;
			ew.direction = (Math.sin(e.getBearingRadians()) * getVelocity()) >= 0 ? 1 : -1;
			enemyWaves.add(ew);
		}
		enemyEnergy = e.getEnergy();
	}

	// Configuração da Mira
	public void shoot(ScannedRobotEvent e) {
		double absoluteBearing = e.getBearingRadians() + getHeadingRadians();
		double gunTurn = absoluteBearing - getGunHeadingRadians();
		setTurnGunRightRadians(Utils.normalRelativeAngle(gunTurn));
		// Força do Tiro
		double firePower = decideFirePower(e);
		setFire(firePower);
	}

	public double decideFirePower(ScannedRobotEvent e) {
		double firePower = getOthers() == 1 ? 2.0 : 3.0;

		if (e.getDistance() > 400) {
			firePower = 1.0;
		} else if (e.getDistance() < 200) {
			firePower = 3.0;
		}

		if (getEnergy() < 1) {
			firePower = 0.1;
		} else if (getEnergy() < 10) {
			firePower = 1.0;
		}

		return Math.min(e.getEnergy() / 4, firePower);
	}

	/**
	 * onHitByBullet: Lógica de "aprendizado" do Wave Surfing.
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		if (!enemyWaves.isEmpty()) {
			Point2D.Double hitBulletLocation = new Point2D.Double(e.getBullet().getX(), e.getBullet().getY());
			EnemyWave hitWave = null;

			for (EnemyWave ew : enemyWaves) {
				if (Math.abs(ew.distanceTraveled - hitBulletLocation.distance(ew.fireLocation)) < 50) {
					hitWave = ew;
					break;
				}
			}

			if (hitWave != null) {
				// --- [CORREÇÃO EVASÃO] ---
				// Fórmula de normalização correta para mapear
				// o Guess Factor [-1, 1] para o índice do array [0, 46].
				double guessFactor = getFactorIndex(hitWave, hitWave.direction);
				int index = (int) Math.round(((guessFactor + 1.0) / 2.0) * (surfStats.size() - 1));

				// 4. O APRENDIZADO: ATUALIZAR O MAPA DE PERIGO
				for (int i = 0; i < surfStats.size(); i++) {

					// --- [CORREÇÃO EVASÃO] ---
					// 'perigoAdicional' agora é 'double' e armazena o valor decimal
					// (0.9, 0.4, etc.), permitindo o aprendizado com nuances.
					double perigoAdicional = Math.max(0, 1.0 / (Math.abs(index - i) + 1) - 0.1);

					surfStats.set(i, surfStats.get(i) + perigoAdicional);
				}

				// Remove a onda da memória de CURTO PRAZO (ameaças ativas).
				enemyWaves.remove(hitWave);
			}
		}
	}

	/**
	 * onHitWall: O que fazer ao bater em uma parede.
	 * Esta é uma manobra de pânico para "desgrudar" da parede.
	 */
	public void onHitWall(HitWallEvent e) {
		// Pega o ângulo da parede que batemos (em radianos)
		double wallAngle = e.getBearingRadians();

		// Calcula um novo ângulo para virar, que seja 90 graus
		// PERPENDICULAR à parede. Isso aponta nosso robô
		// diretamente para longe dela.
		double turnTo = Utils.normalRelativeAngle(wallAngle + Math.PI / 2);

		// Gira o robô para esse novo ângulo
		setTurnRightRadians(turnTo);

		// E dá ré AGRESSIVAMENTE para sair de perto da parede.
		// 150 iniciais não eram, mas 500 pixels deve ser o suficiente para o
		// doAntiGravity()
		// recalcular uma rota segura no próximo turno.
		setBack(500);
		// Depois de testes, talvez 500 seja muito. Podemos ajustar isso mais tarde.
		// Para quem ler isso, tente focar na lógica de onHitWall funcionar em sincronia
		// com Anti Gravidade. A briga deles pela linha de comando da tremeliques no
		// robô.
	}

	// --- [CORREÇÃO EVASÃO] ---
	/**
	 * Este método é chamado a cada turno para "esquecer" levemente
	 * os perigos antigos. Isso mantém o mapa focado nas ameaças recentes.
	 */
	public void decayDangerMap() {
		// Multiplica o perigo de todos os 47 slots por 0.999
		for (int i = 0; i < surfStats.size(); i++) {
			surfStats.set(i, surfStats.get(i) * 0.999);
		}
	}

	/**
	 * Este é o nosso movimento padrão (quando não estamos em "modo de esquiva").
	 * Ele calcula uma "força de repulsão" das paredes e do inimigo
	 * para nos manter em um local seguro.
	 */
	public void doAntiGravity() {
		double xForce = 0;
		double yForce = 0;

		// Um multiplicador para ajustar o quão "forte" é a repulsão.
		// Com testes poderemos diminuir esse valor se o robô ficar muito "nervoso".
		double power = 1000;

		// 1. FORÇA DE REPULSÃO DAS PAREDES

		// Parede de Baixo (y=0) - Empurra para CIMA (y positivo)
		yForce += power / (getY() * getY());

		// Parede de Cima (y=getBattleFieldHeight()) - Empurra para BAIXO (y negativo)
		yForce -= power / Math.pow(getBattleFieldHeight() - getY(), 2);

		// Parede Esquerda (x=0) - Empurra para DIREITA (x positivo)
		xForce += power / (getX() * getX());

		// Parede Direita (x=getBattleFieldWidth()) - Empurra para ESQUERDA (x negativo)
		xForce -= power / Math.pow(getBattleFieldWidth() - getX(), 2);

		// 2. FORÇA DE REPULSÃO DO INIMIGO
		// Se já vimos um inimigo (com base em localização), vai nos afastar dele também
		// para evitar dano de colisões.
		// OBS: Como usa coordenadas, talvez no futuro tenhamos que mudar esse código de
		// novo. Pode fugir do inimigo tarde demais.
		if (lastEnemyLocation != null) {
			double dx = getX() - lastEnemyLocation.x;
			double dy = getY() - lastEnemyLocation.y;
			double distanceSq = (dx * dx) + (dy * dy); // Distância ao quadrado

			// Adiciona a força de repulsão do inimigo ao nosso vetor (x, y)
			xForce += (dx / distanceSq) * power;
			yForce += (dy / distanceSq) * power;
		}

		// 3. CALCULAR O ÂNGULO E MOVER

		// Math.atan2(x, y) calcula o ângulo final do nosso vetor de força (xForce,
		// yForce)
		// O Robocode usa (0,0) no canto inferior esquerdo, mas atan2 espera (0,0) no
		// centro.
		// Passando (xForce, yForce) nos dá o ângulo correto para onde fugir.
		double goAngle = Math.atan2(xForce, yForce);

		// setBackAsFront é uma ótima função, mas para anti-gravidade é mais simples
		// apenas apontar e mover. Vamos calcular o giro mais curto.
		double turnAngle = Utils.normalRelativeAngle(goAngle - getHeadingRadians());

		setTurnRightRadians(turnAngle);

		// Move-se para frente. Se o ângulo for para trás (ex: > 90 graus),
		// o Robocode é inteligente o suficiente para girar e depois andar.
		// Mas para anti-gravidade, é comum ir de ré.
		// Vamos usar a mesma lógica do setBackAsFront para sermos eficientes.
		if (Math.abs(turnAngle) > (Math.PI / 2)) {
			setTurnRightRadians(Utils.normalRelativeAngle(turnAngle + Math.PI));
			setBack(100);
		} else {
			setTurnRightRadians(turnAngle);
			setAhead(100);
		}
	}

	// ------------------------------
	// MÉTODOS DE WAVE SURFING
	// ------------------------------

	public static double bulletVelocity(double power) {
		return 20 - 3 * power;
	}

	public void updateWaves() {
		for (int i = 0; i < enemyWaves.size(); i++) {
			EnemyWave ew = enemyWaves.get(i);
			ew.distanceTraveled = (getTime() - ew.fireTime) * ew.bulletVelocity;
			if (ew.distanceTraveled > Point2D.distance(getX(), getY(), ew.fireLocation.x, ew.fireLocation.y) + 50) {
				enemyWaves.remove(i);
				i--;
			}
		}
	}

	public void doSurfing() {
		if (enemyWaves.isEmpty())
			return;

		EnemyWave surfWave = enemyWaves.get(0);
		double dangerLeft = checkDanger(surfWave, -1);
		double dangerRight = checkDanger(surfWave, 1);

		if (dangerLeft < dangerRight)
			surfDirection = -1;
		else
			surfDirection = 1;

		double goAngle = Utils.normalRelativeAngle(surfWave.directAngle + (surfDirection * Math.PI / 2));
		setBackAsFront(this, goAngle);
	}

	public double checkDanger(EnemyWave surfWave, int direction) {
		// --- [CORREÇÃO EVASÃO] ---
		// Corrigido o bug sutil aqui. A fórmula do 'index' deve ser
		// a mesma fórmula de normalização usada no onHitByBullet
		// para "ler" o mapa de perigo da maneira correta.
		double guessFactor = getFactorIndex(surfWave, direction);
		int index = (int) Math.round(((guessFactor + 1.0) / 2.0) * (surfStats.size() - 1));

		return surfStats.get(Math.max(0, Math.min(index, surfStats.size() - 1))) + Math.random();
	}

	public double getFactorIndex(EnemyWave surfWave, int direction) {
		Point2D.Double enemyFireLocation = surfWave.fireLocation;
		double offsetAngle = Utils
				.normalRelativeAngle(Math.atan2(getX() - enemyFireLocation.x, getY() - enemyFireLocation.y)
						- surfWave.directAngle);
		double factor = offsetAngle / maxEscapeAngle(surfWave.bulletVelocity) * direction;
		return limit(-1, factor, 1);
	}

	public double maxEscapeAngle(double velocity) {
		return Math.asin(8.0 / velocity);
	}

	public static double limit(double min, double value, double max) {
		return Math.max(min, Math.min(value, max));
	}

	public static void setBackAsFront(AdvancedRobot robot, double goAngle) {
		double angle = Utils.normalRelativeAngle(goAngle - robot.getHeadingRadians());
		if (Math.abs(angle) > (Math.PI / 2)) {
			if (angle < 0)
				robot.setTurnRightRadians(Math.PI + angle);
			else
				robot.setTurnRightRadians(-Math.PI + angle);
			robot.setBack(100);
		} else {
			robot.setTurnRightRadians(angle);
			robot.setAhead(100);
		}
	}

	// Classe auxiliar que representa a "onda" da bala inimiga
	class EnemyWave {
		Point2D.Double fireLocation;
		long fireTime;
		double bulletVelocity, directAngle, distanceTraveled;
		int direction;
	}
}