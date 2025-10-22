package Paraiso;

import robocode.*;           // Importa as classes básicas do Robocode
import robocode.util.Utils;   // Importa utilitários úteis, como normalRelativeAngle

// Classe responsável pelo controle do radar do robô
// Permite travar o radar em um inimigo e fazer pequenas previsões de movimento
public class RadarController {
    private AdvancedRobot robot; // Referência ao robô que está usando este controlador

    // Construtor que recebe o robô que vai usar o RadarController
    public RadarController(AdvancedRobot robot) {
        this.robot = robot;
    }

    // Método para travar o radar em um ângulo absoluto (absoluteBearing)
    // "Lock with overshoot": adiciona uma margem para compensar o movimento do robô
    public void lockRadar(double absoluteBearing) {
        // Calcula a diferença entre o ângulo desejado e a posição atual do radar
        double turn = Utils.normalRelativeAngle(absoluteBearing - robot.getRadarHeadingRadians());
        
        // Aplica o dobro da diferença para "overshoot", mantendo o radar preso ao inimigo
        robot.setTurnRadarRightRadians(Utils.normalRelativeAngle(turn) * 2);
    }
}
