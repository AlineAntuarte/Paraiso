package Paraiso;

import robocode.HitWallEvent;
import robocode.util.Utils;

/**
 * EnhancedLittleBoy
 *
 * Versão aprimorada do robô LittleBoy.
 * 
 * Essa classe herda de LittleBoy e melhora a movimentação com:
 *  - Sistema anti-gravidade mais equilibrado:
 *      → paredes têm força de repulsão mais forte
 *      → inimigo tem força de repulsão limitada, com leve desvio tangencial
 *  - Reação ao bater na parede menos brusca, proporcional ao tamanho do campo.
 */
public class EnhancedLittleBoy extends LittleBoy {

    /**
     * Método chamado automaticamente quando o robô encosta em uma parede.
     * 
     * Aqui, ele não dá uma ré total. Em vez disso:
     *  - Calcula o ângulo da parede
     *  - Gira suavemente 90° para sair de raspão
     *  - Recuo proporcional ao tamanho do campo (evita exagero em mapas pequenos)
     */
    @Override
    public void onHitWall(HitWallEvent e) {
        // Ângulo da parede onde bateu
        double wallAngle = e.getBearingRadians();

        // Calcula um ângulo de saída de 90° da parede (evita travar)
        double turnTo = Utils.normalRelativeAngle(wallAngle + Math.PI / 2);
        setTurnRightRadians(turnTo);

        // Distância segura de recuo, adaptada ao tamanho da arena
        double safeBack = Math.min(200, Math.max(100,
                Math.max(getBattleFieldWidth(), getBattleFieldHeight()) / 6));
        setBack(safeBack);
    }

    /**
     * Sistema de movimentação anti-gravidade.
     * 
     * A ideia é simular forças que empurram o robô:
     *  - Paredes empurram fortemente (para evitar ficar preso)
     *  - Inimigo empurra com força proporcional à distância
     *  - Pequeno desvio tangencial cria movimento curvo (dificulta acertos)
     */
    public void doAntiGravity() {
        double xForce = 0;
        double yForce = 0;

        // Parâmetros de controle das forças
        double wallPower = 1200;           // força base das paredes
        double enemyBasePower = 600;       // força base do inimigo
        double enemyEffectiveRange = 300;  // alcance em que a repulsão do inimigo atua
        double minWallDistance = 80;       // distância mínima crítica da parede
        double margin = 36;                // margem de segurança do campo

        // ==================== PAREDES ====================

        // Distância até o fundo do campo
        double distBottom = getY();
        double bottomForce = wallPower / Math.max(1, distBottom * distBottom);
        if (distBottom < minWallDistance) bottomForce *= 3; // reforça se estiver colado
        yForce += bottomForce;

        // Distância até o topo
        double distTop = getBattleFieldHeight() - getY();
        double topForce = wallPower / Math.max(1, distTop * distTop);
        if (distTop < minWallDistance) topForce *= 3;
        yForce -= topForce;

        // Distância até a parede esquerda
        double distLeft = getX();
        double leftForce = wallPower / Math.max(1, distLeft * distLeft);
        if (distLeft < minWallDistance) leftForce *= 3;
        xForce += leftForce;

        // Distância até a parede direita
        double distRight = getBattleFieldWidth() - getX();
        double rightForce = wallPower / Math.max(1, distRight * distRight);
        if (distRight < minWallDistance) rightForce *= 3;
        xForce -= rightForce;

        // ==================== INIMIGO ====================
        // Se já temos a última posição conhecida do inimigo
        if (lastEnemyLocation != null) {
            double dx = getX() - lastEnemyLocation.x;
            double dy = getY() - lastEnemyLocation.y;
            double distance = Math.hypot(dx, dy);
            double distanceSq = Math.max(1, distance * distance);

            // Só aplica repulsão se o inimigo estiver dentro da faixa de influência
            if (distance < enemyEffectiveRange) {
                // Fator decrescente com a distância (0 a 1)
                double factor = (enemyEffectiveRange - distance) / enemyEffectiveRange;

                // Força proporcional ao fator
                double enemyPower = enemyBasePower * factor;

                // Repulsão direta (afasta o robô do inimigo)
                xForce += (dx / distanceSq) * enemyPower;
                yForce += (dy / distanceSq) * enemyPower;

                // Componente tangencial: empurra lateralmente, gerando curvas
                double tangential = 150 * factor;
                xForce += (-dy / (distance + 1)) * tangential;
                yForce += (dx / (distance + 1)) * tangential;
            }
        }

        // ==================== AJUSTES NAS BORDAS ====================
        // Evita que as forças puxem o robô para fora da área útil do mapa
        if (getX() < margin && xForce < 0) xForce += wallPower * 0.5;
        if (getX() > getBattleFieldWidth() - margin && xForce > 0) xForce -= wallPower * 0.5;
        if (getY() < margin && yForce < 0) yForce += wallPower * 0.5;
        if (getY() > getBattleFieldHeight() - margin && yForce > 0) yForce -= wallPower * 0.5;

        // ==================== MOVIMENTO FINAL ====================

        // Calcula o ângulo resultante das forças
        double goAngle = Math.atan2(xForce, yForce);

        // Ajusta o ângulo em relação à direção atual do robô
        double turnAngle = Utils.normalRelativeAngle(goAngle - getHeadingRadians());

        // Se o ângulo for muito grande (maior que 90°), é mais eficiente ir de ré
        if (Math.abs(turnAngle) > (Math.PI / 2)) {
            setTurnRightRadians(Utils.normalRelativeAngle(turnAngle + Math.PI));
            setBack(120);
        } else {
            setTurnRightRadians(turnAngle);
            setAhead(120);
        }
    }
}
