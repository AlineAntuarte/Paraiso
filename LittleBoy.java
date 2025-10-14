package Paraiso;

import robocode.*;
//importar java.awt.Color;

//LittleBoy - autores (Aline, Thalia e Thaissa)

public class LittleBoy extends Robot {
	/**
	 * run: Comportamento padrão do LittleBoy, tanto mover tanque quanto arma.
	 */
	public void run() {
		// A inicialização do robô deve ser colocada aqui

		// Depois de testar seu robô, tente descomentar a importação no topo e a próxima
		// linha:

		// setColors(Color.red,Color.blue,Color.green); // body,gun,radar

		// Loop principal do robô
		while (true) {
			/*
			 * Substitua as próximas 4 linhas por qualquer comportamento que você queira.
			 */
			ahead(100); // Anda 100 pixels para frente
			turnGunRight(360); // Gira a arma em 360°
			back(100); // Anda 100 pixels para trás
			turnGunRight(360); // Gira a arma novamente em 360°
		}
	}

	// onScannedRobot: O que fazer ao ver outro robô

	public void onScannedRobot(ScannedRobotEvent e) {
		// Substitua a próxima linha por qualquer comportamento desejado
		fire(1);
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
