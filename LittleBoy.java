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
	 * onHitByBullet: O que fazer ao ser atingido por uma bala
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		back(10);
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
		double offsetAngle = Utils.normalRelativeAngle(Math.atan2(getX() - enemyFireLocation.x, getY() - enemyFireLocation.y)
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