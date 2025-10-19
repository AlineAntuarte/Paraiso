package Paraiso;

import robocode.*;
import java.awt.Color;
import java.awt.geom.*;
import java.util.*;
import robocode.util.Utils;

//LittleBoy - autores (Aline, Thalia e Thaissa)

public class LittleBoy extends AdvancedRobot {
	/**
	 * run: Comportamento padrão do LittleBoy, tanto mover tanque quanto arma.
	 */

	// ------------------------------
	// VARIÁVEIS PARA WAVE SURFING
	// ------------------------------
	static double surfDirection = 1;
	static double enemyEnergy = 100;
	ArrayList<EnemyWave> enemyWaves = new ArrayList<>();
	ArrayList<Integer> surfStats = new ArrayList<>(Collections.nCopies(47, 0)); // Mapa de perigo (47 GuessFactors)

	public void run() {
		// A inicialização do robô deve ser colocada aqui
		// Inicialização do Radar
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		// Inicialização da Arma
		setTurnGunRight(Double.POSITIVE_INFINITY);
		int counter = 0;

		setBodyColor(Color.black); // Define e a cor do corpo como vermelho
		setRadarColor(Color.blue); // Define e a cor do radar como azul

		// Loop principal do robô
		while (true) {
			// Settings Radar
			if (getRadarTurnRemaining() == 0.0)
				setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
			// Settings Movimentação
			if (counter < 16)
				setAhead(100);
			else
				setBack(100);
			counter = (counter + 1) % 32;

			// Atualiza as ondas inimigas e realiza o movimento evasivo
			updateWaves();
			doSurfing();

			execute();
		}
	}

	// onScannedRobot: O que fazer ao ver outro robô
	public void onScannedRobot(ScannedRobotEvent e) {
		shoot(e);
		setFire(1);

		// Quando Radar encontra um inimigo
		double angleToEnemy = getHeadingRadians() + e.getBearingRadians();
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
	 * onHitByBullet: Este método é chamado sempre que nosso robô é atingido por uma
	 * bala.
	 * É aqui que implementamos a lógica de "aprendizado" do Wave Surfing.
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		// 1. VERIFICAÇÃO INICIAL
		// Primeiro, verificamos se a lista de ondas inimigas não está vazia.
		// Se não estivermos rastreando nenhuma onda, não há como aprender nada.
		if (!enemyWaves.isEmpty()) {

			// 2. PREPARAÇÃO DA ANÁLISE
			// Cria um ponto (x, y) para marcar o local exato onde a bala nos atingiu.
			Point2D.Double hitBulletLocation = new Point2D.Double(e.getBullet().getX(), e.getBullet().getY());

			// Prepara uma variável para guardar a onda específica que nos acertou.
			// Começamos com 'null' (vazio) porque ainda não a encontramos.
			EnemyWave hitWave = null;

			// 3. O TRABALHO DE DETETIVE: ENCONTRAR A ONDA CULPADA
			// Fazemos um loop para analisar cada onda inimiga que estamos rastreando.
			for (EnemyWave ew : enemyWaves) {
				// A lógica principal: comparamos a distância que a onda JÁ PERCORREU (baseado
				// no tempo)
				// com a distância REAL entre o local do tiro e o local do impacto.
				// Se a diferença entre essas duas distâncias for muito pequena (aqui, < 50
				// pixels),
				// significa que esta é a onda que nos acertou.
				if (Math.abs(ew.distanceTraveled - hitBulletLocation.distance(ew.fireLocation)) < 50) {
					// Encontrando esta, a guardamos na nossa variável 'hitWave' <-- ew.
					hitWave = ew;
					// Interrompemos o loop, pois não precisamos mais procurar.
					break;
				}
			}

			// 4. O APRENDIZADO: ATUALIZAR O MAPA DE PERIGO
			// Após a busca, verificamos se realmente encontramos uma onda correspondente.
			if (hitWave != null) {
				// Calculamos o "Guess Factor", que é uma "nota" que representa nossa posição
				// em relação à onda no momento do impacto.
				// Em seguida, convertemos essa "nota" para um índice no nosso array
				// 'surfStats'.
				// Este 'index' é o local exato do "crime" no nosso mapa de perigo.
				
				//CORREÇÃO DE BUG ANTERIOR
				// Primeiro, pegamos o Fator de Adivinhação
				double guessFactor = getFactorIndex(hitWave, hitWave.direction);

				// Agora, aplicamos a fórmula
				int index = (int) Math.round(((guessFactor + 1.0) / 2.0) * (surfStats.size() - 1));

				// Agora, vamos "ensinar" o robô, marcando aquele local como perigoso.
				// Percorremos todo o mapa de perigo ('surfStats')...
				for (int i = 0; i < surfStats.size(); i++) {
					// ...e aumentamos o nível de perigo em cada posição.
					// A fórmula abaixo cria uma "colina" de perigo: o aumento é máximo no 'index'
					// exato do impacto
					// e diminui gradualmente para as posições vizinhas.
					int perigoAdicional = (int) Math.round (Math.max(0, 1.0 / (Math.abs(index - i) + 1) - 0.1));
					surfStats.set(i, surfStats.get(i) + perigoAdicional);
				}

				// 5. LIMPEZA FINAL
				// A onda já nos atingiu, então ela não é mais uma ameaça.
				// Removemos ela da lista para manter nosso rastreamento eficiente e limpo.
				enemyWaves.remove(hitWave);
			}
		}
	}

	/**
	 * onHitWall: O que fazer ao bater em uma parede
	 */
	public void onHitWall(HitWallEvent e) {
		back(20);
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
		int index = (int) ((getFactorIndex(surfWave, direction)) * (surfStats.size() - 1));
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