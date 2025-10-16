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
		setTurnGunRight(Double.POSITIVE_INFINITY);
		int counter = 0;

		setBodyColor(Color.black); // Define e a cor do corpo como vermelho
		setRadarColor(Color.red); // Define e a cor do radar como verde

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
			execute();
		}
	}

	// onScannedRobot: O que fazer ao ver outro robô

	public void onScannedRobot(ScannedRobotEvent e) { // --- LÓGICA DE MIRA E TIRO ---
		shoot(e);
		// Chama seu método personalizado para mirar a arma e decidir a potência do tiro.
		// Passamos 'e' para que o método tenha os dados do inimigo.

		setFire(1);
		// Quando Radar encontra um inimigo
		double angleToEnemy = getHeadingRadians() + e.getBearingRadians();
		// Calcula o ângulo absoluto do inimigo no mapa.
		// (minha direção + a direção relativa do inimigo = direção absoluta do inimigo)

		double turnToEnemy = Utils.normalRelativeAngle(angleToEnemy - getRadarHeadingRadians());
		// Calcula o caminho mais curto para virar o radar até o inimigo.
		// Utils.normalRelativeAngle converte ângulos grandes (ex: 350°) no equivalente
		// curto (ex: -10°).

		double extraTurn = Math.atan(36.0 / e.getDistance()) * (turnToEnemy >= 0 ? 1 : -1);
		// Um truque para manter o radar travado: viramos o radar um pouco a mais
		// para "prever" o movimento do inimigo e não perdê-lo de vista.

		setTurnRadarRightRadians(turnToEnemy + extraTurn);
		// Define o comando para virar o radar para a nova posição calculada.

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

	public double decideFirePower(ScannedRobotEvent e) { // Método de tiro baseado na energia do inimigo e distância
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
