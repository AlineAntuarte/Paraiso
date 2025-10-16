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
		setRadarColor(Color.blue); // Define e a cor do radar como azul

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
		shoot (e);
		setFire (1);
		// Quando Radar encontra um inimigo
		double angleToEnemy =
			getHeadingRadians() + e.getBearingRadians();
		double turnToEnemy = Utils.normalRelativeAngle(angleToEnemy - getRadarHeadingRadians());
		double extraTurn = Math.atan(36.0 / e.getDistance()) * (turnToEnemy >= 0 ? 1 : -1);
		
		setTurnRadarRightRadians(turnToEnemy + extraTurn);
			
	}
	
	// Configuração da Mira
	public void shoot (ScannedRobotEvent e) {
		double absoluteBearing = e.getBearingRadians() + getHeadingRadians();
		double gunTurn = absoluteBearing - getGunHeadingRadians();
		setTurnGunRightRadians(Utils.normalRelativeAngle(gunTurn));
		// Força do Tiro
		double firePower = decideFirePower(e);
		setFire(firePower);
	}
	public double decideFirePower(ScannedRobotEvent e) {
		double firePower = getOthers() == 1 ? 2.0 : 3.0;
		
		if(e.getDistance() > 400) {
			firePower = 1.0;
		} else if (e.getDistance() < 200) {
			firePower = 3.0;
		}
		
		if (getEnergy() <1) {
			firePower = 0.1;
		} else if (getEnergy() <10) {
			firePower = 1.0;
		}
		
		return Math.min(e.getEnergy() / 4, firePower);
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
