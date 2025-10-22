package Paraiso;

import robocode.*;
import java.awt.geom.Point2D;
import robocode.util.Utils;

// Classe responsável pelo controle de movimento do robô
public class MovementController {
    private AdvancedRobot robot; // Referência ao robô que será controlado

    // Construtor recebe o robô e guarda a referência
    public MovementController(AdvancedRobot robot) {
        this.robot = robot;
    }

    // ------------------------------
    // Movimento Anti-Gravity
    // ------------------------------
    public void antiGravityMove(Point2D.Double enemyLoc) {
        double xForce = 0; // Força resultante no eixo X
        double yForce = 0; // Força resultante no eixo Y
        double power = 1000; // Potência das forças de repulsão
        double margin = 60; // Margem de distância das paredes

        // ------------------------------
        // Forças de repulsão das paredes
        // ------------------------------
        // Paredes laterais
        xForce += power / Math.max(1, Math.pow(Math.max(robot.getX() - margin, 1), 2)); // Força da parede esquerda
        xForce -= power / Math.max(1, Math.pow(Math.max(robot.getBattleFieldWidth() - robot.getX() - margin, 1), 2)); // Força da parede direita
        // Paredes superior/inferior
        yForce += power / Math.max(1, Math.pow(Math.max(robot.getY() - margin, 1), 2)); // Força da parede inferior
        yForce -= power / Math.max(1, Math.pow(Math.max(robot.getBattleFieldHeight() - robot.getY() - margin, 1), 2)); // Força da parede superior

        // ------------------------------
        // Força de repulsão do inimigo
        // ------------------------------
        if (enemyLoc != null) { // Se posição do inimigo conhecida
            double dx = robot.getX() - enemyLoc.x; // Diferença X
            double dy = robot.getY() - enemyLoc.y; // Diferença Y
            double distanceSq = (dx * dx) + (dy * dy); // Distância ao quadrado
            // Aplica força proporcional inversa à distância ao quadrado
            xForce += (dx / Math.max(1, distanceSq)) * power * 2;
            yForce += (dy / Math.max(1, distanceSq)) * power * 2;
        }

        // ------------------------------
        // Aleatoriedade para reduzir previsibilidade
        // ------------------------------
        xForce += (Math.random() - 0.5) * 20; // Pequeno deslocamento aleatório X
        yForce += (Math.random() - 0.5) * 20; // Pequeno deslocamento aleatório Y

        // ------------------------------
        // Calcula o ângulo final de movimento
        // ------------------------------
        double goAngle = Math.atan2(xForce, yForce); // Ângulo a seguir baseado nas forças
        double smoothed = wallSmoothing(robot.getX(), robot.getY(), goAngle); // Ajuste para não colidir nas paredes
        setBackAsFront(robot, smoothed); // Move o robô na direção desejada (frente ou ré)
    }

    // ------------------------------
    // Decide se o robô vai frente ou ré baseado no ângulo
    // ------------------------------
    public static void setBackAsFront(AdvancedRobot robot, double goAngle) {
        double angle = Utils.normalRelativeAngle(goAngle - robot.getHeadingRadians()); // Normaliza ângulo entre -π e π
        if (Math.abs(angle) > (Math.PI / 2)) { // Se ângulo > 90°, é melhor ir de ré
            if (angle < 0)
                robot.setTurnRightRadians(Math.PI + angle); // Ajusta ângulo para ré
            else
                robot.setTurnRightRadians(-Math.PI + angle);
            robot.setBack(100); // Move para trás
        } else {
            robot.setTurnRightRadians(angle); // Ajusta ângulo normalmente
            robot.setAhead(100); // Move para frente
        }
    }

    // ------------------------------
    // Wall Smoothing - evita colisão com paredes
    // ------------------------------
    private double wallSmoothing(double x, double y, double desiredAngle) {
        double angle = desiredAngle; // Ângulo inicial desejado
        double margin = 60; // Margem mínima da parede
        double bx = robot.getBattleFieldWidth(); // Largura do campo
        double by = robot.getBattleFieldHeight(); // Altura do campo

        // Tenta ajustar ângulo para não encostar nas paredes
        for (int i = 0; i < 6; i++) {
            double nx = x + Math.sin(angle) * margin; // Posição X simulada
            double ny = y + Math.cos(angle) * margin; // Posição Y simulada
            if (nx < 18 || nx > bx - 18 || ny < 18 || ny > by - 18) { // Se muito perto da parede
                angle += 0.25; // Gira o ângulo levemente
            } else {
                break; // Ângulo seguro
            }
        }
        return angle; // Retorna ângulo ajustado
    }
}
