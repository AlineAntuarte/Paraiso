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

	// onHitByBullet com lógica de aprendizado, lembra onde foi atingido e "ensina"
	// o robô para evitar esses pontos no futuro.

	public void onHitByBullet(HitByBulletEvent e) {
		
		// Se não houver ondas para analisar, não há o que fazer.
		if (!enemyWaves.isEmpty()) {
			// enemyWave = ArrayList que guarda os rastros de tiros inimigos, atingindos ou
			// que ainda atingiram.
			// .inEmpty() = true e a lista está vazia, false e há ao menos um elemento na
			// lista. Sendo uma lista de rastros de tiros.
			// ! = inverte, se a lista não estiver vazia, entra no if.

			Point2D.Double hitBulletLocation = new Point2D.Double(e.getBullet().getX(), e.getBullet().getY());
			EnemyWave hitWave = null;

			// Essa linha de código cria um ponto que marca a localização onde a bala
			// inimiga nos atingiu.

			// Point2D.Double = classe que armazena uma coordenada (x,y) de ponto flutuante.
			// hitBulletLocation = nome da variável que guarda a localização do tiro que nos
			// atingiu.
			// new Point2D.Double(...) = cria um novo ponto com as coordenadas x e y da bala
			// inimiga.
			// e = evento que contém informações sobre o tiro que nos atingiu.
			// .getBullet() = método que retorna o objeto Bullet (bala) do evento que nos
			// atingiu.
			// .getX() e .getY() = métodos que retornam as coordenadas x e y da bala no
			// momento do impacto.

			for (EnemyWave ew : enemyWaves) { // EnemyWave ew = Isso diz "Para cada volta no laço, pegue um item da
												// lista enemyWaves e chame-o temporariamente de ew".
				/*
				 * Analogia: Imagine que você tem uma lista de suspeitos (enemyWaves). Este laço
				 * pega o primeiro suspeito da lista, chama-o de ew, permite que você o
				 * investigue, e então passa para o próximo suspeito até a lista acabar.
				 */
				if (Math.abs(ew.distanceTraveled - hitBulletLocation.distance(ew.fireLocation)) < 50) {
					hitWave = ew; // Se a distância percorrida pela onda (ew.distanceTraveled) for
									// aproximadamente igual à diferença entre o local onde a onda foi
									// disparada (ew.fireLocation) e o local onde a bala nos atingiu
									// (hitBulletLocation), então encontramos a onda que nos atingiu.
					break;
				}
			}

			if (hitWave != null) {
				/*
				 * Esta linha é uma verificação de segurança final.
				 * O que faz? Ela pergunta:
				 * "O nosso trabalho de detetive no laço for anterior realmente encontrou a onda que nos acertou?"
				 * hitWave: É a variável que deveria guardar a onda culpada.
				 * != null: Significa "não é nulo" ou "não está vazio".
				 * Tendo algo (não vazio/nulo) em hitWave significa que encontramos a onda
				 * culpada por nos acertar.
				 * Se hitWave fosse null, isso indicaria que não conseguimos identificar qual
				 * onda nos atingiu.
				 * Portanto, este if garante que só prosseguiremos com o aprendizado se tivermos
				 * certeza.
				 */
				int index = (int) (getFactorIndex(hitWave, hitWave.direction) * (surfStats.size() - 1));
				/*
				 * Esta linha calcula em qual parte do seu
				 * mapa de rastros de tiro você estava quando foi atingido.
				 * 
				 * getFactorIndex(...): Calcula o "Guess Factor".
				 * Pense no Guess Factor como uma nota de -1 a 1 que descreve sua posição em
				 * relação à onda da bala.
				 * 
				 * Um fator de 0 significa que você estava bem no meio do caminho, na mira
				 * perfeita.
				 * 
				 * Um fator de 1 significa que você se esquivou o máximo que podia para um lado.
				 * 
				 * Um fator de -1 significa que você se esquivou o máximo que podia para o outro
				 * lado.
				 */

				/*
				 * * (surfStats.size() - 1): Esta parte converte a "nota" (de -1 a 1) para um
				 * índice que corresponde a uma posição no seu array surfStats. Se seu surfStats
				 * tem 47 posições (índices de 0 a 46), este cálculo mapeia o Guess Factor para
				 * um número entre 0 e 46.
				 * 
				 * (int): Converte o resultado final (que pode ser um número decimal) em um
				 * número inteiro, para que possa ser usado como um índice de array.
				 */

				for (int i = 0; i < surfStats.size(); i++) {
					surfStats.set(i, surfStats.get(i) + (int) Math.max(0, 1.0 / (Math.abs(index - i) + 1) - 0.1));
				}
				/*
				 * for (int i = 0; i < surfStats.size(); i++): Este laço passa por todas as
				 * posições do seu mapa de perigo (surfStats), uma de cada vez.
				 * 
				 * surfStats.set(i, ...): Para cada posição i, este comando vai atualizar o
				 * valor de perigo dela.
				 * 
				 * surfStats.get(i) + ...: A lógica é:
				 * "pegue o valor de perigo antigo e some um novo valor a ele".
				 * 
				 * 1.0 / (Math.abs(index - i) + 1): Esta é a fórmula matemática que cria uma
				 * "zona de perigo".
				 * 
				 * Math.abs(index - i) calcula a distância entre a posição i (que estamos
				 * olhando agora) e o index (onde fomos atingidos).
				 * 
				 * Quando i é igual a index, a distância é 0. A fórmula se torna 1.0 / (0 + 1) =
				 * 1.0. Este é o maior valor de perigo adicionado.
				 * 
				 * Quando i está a uma posição de distância, a fórmula se torna 1.0 / (1 + 1) =
				 * 0.5. Um valor de perigo menor.
				 * 
				 * Quanto mais longe i estiver do index, menor será o valor adicionado. Isso
				 * cria uma "colina" de perigo, com o pico no local exato do erro.
				 * 
				 * - 0.1 e Math.max(0, ...): São ajustes finos para garantir que o perigo
				 * adicionado diminua mais rapidamente e nunca seja um número negativo.
				 */

				enemyWaves.remove(hitWave); // # enemyWaves.remove(hitWave) = Remove a onda que te acertou da sua lista
											// de ameaças ativas (enemyWaves).
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