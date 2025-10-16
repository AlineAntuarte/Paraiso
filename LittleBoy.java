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
		// Cor do robô
		setBodyColor(Color.black); // Define e a cor do corpo como preto
		setRadarColor(Color.blue); // Define e a cor do radar como azul

		// Inicialização do Radar
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);

		// Inicialização da Arma
		setTurnGunRight(Double.POSITIVE_INFINITY);
		setTurnRadarRight(Double.POSITIVE_INFINITY);
		// Arma e o radar girando constantemente para procurar por alvos.
		// A movimentação é controlada pelo onScannedRobot.

		// Loop principal do robô
		while (true) {
			execute();
			// Ele executa todos os comandos 'set' que foram dados desde a última chamada.
		}
	}

	// onScannedRobot: O que fazer ao ver outro robô

	public void onScannedRobot(ScannedRobotEvent e) { // --- LÓGICA DE MIRA E TIRO ---
		shoot(e);
		// Chama seu método personalizado para mirar a arma e decidir a potência do
		// tiro.
		// Passamos 'e' para que o método tenha os dados do inimigo.

		// O setFire() que estava aqui era redundante, pois o método shoot() já faz
		// isso.

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

		double turnAngle = e.getBearingRadians() + (Math.PI / 2);
		setTurnRightRadians(Utils.normalRelativeAngle(turnAngle));
		// O objetivo é se mover de lado em relação ao inimigo, a mira está perfeita mas
		// o preço é ficar na mira do inimigo. Assim travamos a mira mas fugimos.
		// Para isso, viramos o corpo do nosso robô 90 graus (PI/2 radianos)
		// em relação ao inimigo e andamos para frente.
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
