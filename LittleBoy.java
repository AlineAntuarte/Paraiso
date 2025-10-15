package Paraiso;

import robocode.*;
import java.awt.Color;
import robocode.util.Utils;

//LittleBoy - autores (Aline, Thalia e Thaissa)

public class LittleBoy extends AdvancedRobot {
	/**
	 * run: Comportamento padrão do LittleBoy, tanto mover tanque quanto arma.
	 */
	public void run() {
		// A inicialização do robô deve ser colocada aqui
		// Inicialização do Radar
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		// Inicialização da Arma
		setTurnGunRight (Double.POSITIVE_INFINITY);
		int counter = 0;
		// Depois de testar seu robô, tente descomentar a importação no topo e a próxima linha:

		setBodyColor(Color.black); // Define e a cor do corpo como vermelho
		setRadarColor(Color.red); // Define e a cor do radar como verde

		// Loop principal do robô
		while (true) {
			// Settings Radar
			if (getRadarTurnRemaining() == 0.0)
				setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
			// Settings Movimentação
			if (counter <16)
				setAhead(100);
			else
				setBack(100);
			counter = (counter + 1) % 32;
			execute ();
		}
	}

	// onScannedRobot: O que fazer ao ver outro robô

	public void onScannedRobot(ScannedRobotEvent e) {
		setFire (1);
		// Quando Radar encontra um inimigo
		double angleToEnemy =
			getHeadingRadians() + e.getBearingRadians();
		double turnToEnemy = Utils.normalRelativeAngle(angleToEnemy - getRadarHeadingRadians());
		double extraTurn = Math.atan(36.0 / e.getDistance()) * (turnToEnemy >= 0 ? 1 : -1);
		
		setTurnRadarRightRadians(turnToEnemy + extraTurn);
		
	}

	/**
	 * onHitByBullet: O que fazer ao ser atingido por uma bala
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		// Substitua a próxima linha por qualquer comportamento desejado
		back(10);
	}

	/**
	 * onHitWall: O que fazer ao bater em uma parede
	 */
	public void onHitWall(HitWallEvent e) {
		// Substitua a próxima linha por qualquer comportamento desejado
		back(20);
	}
}
