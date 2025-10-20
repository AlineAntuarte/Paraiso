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
	Point2D.Double lastEnemyLocation;

	// ------------------------------
	// run: Comportamento padrão do LittleBoy.
	// ------------------------------
	public void run() {
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		setTurnGunRight(Double.POSITIVE_INFINITY);

		setBodyColor(Color.black);
		setRadarColor(Color.blue);

		while (true) {
			// Radar: varredura contínua
			if (getRadarTurnRemaining() == 0.0)
				setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

			// Atualiza ondas (curto prazo)
			updateWaves();

			// Se há ondas ativas, prioriza Wave Surfing; senão, faz AntiGravity
			if (!enemyWaves.isEmpty()) {
				doSurfing();
			} else {
				doAntiGravity();
			}

			// Decaimento do mapa de perigo (esquecer lentamente eventos antigos)
			decayDangerMap();

			execute();
		}
	}

	// onScannedRobot: O que fazer ao ver outro robô
	public void onScannedRobot(ScannedRobotEvent e) {
		shoot(e);

		double angleToEnemy = getHeadingRadians() + e.getBearingRadians();
		double enemyX = getX() + Math.sin(angleToEnemy) * e.getDistance();
		double enemyY = getY() + Math.cos(angleToEnemy) * e.getDistance();
		lastEnemyLocation = new Point2D.Double(enemyX, enemyY);

		double turnToEnemy = Utils.normalRelativeAngle(angleToEnemy - getRadarHeadingRadians());
		double extraTurn = Math.atan(36.0 / e.getDistance()) * (turnToEnemy >= 0 ? 1 : -1);
		setTurnRadarRightRadians(turnToEnemy + extraTurn);

		// DETECÇÃO DE TIRO INIMIGO
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
				double guessFactor = getFactorIndex(hitWave, hitWave.direction);
				int index = (int) Math.round(((guessFactor + 1.0) / 2.0) * (surfStats.size() - 1));

				for (int i = 0; i < surfStats.size(); i++) {
					double perigoAdicional = Math.max(0, 1.0 / (Math.abs(index - i) + 1) - 0.1);
					surfStats.set(i, surfStats.get(i) + perigoAdicional);
				}
				enemyWaves.remove(hitWave);
			}
		}
	}

	/**
	 * onHitWall: manobra de pânico (versão original).
	 */
	public void onHitWall(HitWallEvent e) {
		double wallAngle = e.getBearingRadians();
		double turnTo = Utils.normalRelativeAngle(wallAngle + Math.PI / 2);
		setTurnRightRadians(turnTo);
		// Ré agressivo para se distanciar da parede (original)
		setBack(500);
	}

	public void decayDangerMap() {
		for (int i = 0; i < surfStats.size(); i++) {
			surfStats.set(i, surfStats.get(i) * 0.999);
		}
	}

	public void doAntiGravity() {
		double xForce = 0;
		double yForce = 0;

		double power = 1000;

		// Paredes
		yForce += power / Math.max(1, getY() * getY());
		yForce -= power / Math.max(1, Math.pow(getBattleFieldHeight() - getY(), 2));
		xForce += power / Math.max(1, getX() * getX());
		xForce -= power / Math.max(1, Math.pow(getBattleFieldWidth() - getX(), 2));

		// Inimigo
		if (lastEnemyLocation != null) {
			double dx = getX() - lastEnemyLocation.x;
			double dy = getY() - lastEnemyLocation.y;
			double distanceSq = (dx * dx) + (dy * dy);
			xForce += (dx / Math.max(1, distanceSq)) * power;
			yForce += (dy / Math.max(1, distanceSq)) * power;
		}

		double goAngle = Math.atan2(xForce, yForce);
		double turnAngle = Utils.normalRelativeAngle(goAngle - getHeadingRadians());

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