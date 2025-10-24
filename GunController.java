package Paraiso;

import robocode.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import robocode.util.Utils;

/**
 * GunController
 *
 * Responsável por gerenciar a mira e os disparos do robô.
 * Utiliza predição linear + sistema de "GuessFactor" com o DangerMap
 * para escolher o melhor ângulo de disparo com base em aprendizado.
 */
public class GunController {
    private AdvancedRobot robot; // Referência ao robô principal
    private DangerMapManager dangerMapManager; // Mapas de perigo (histórico dos melhores ângulos)

    // Construtor: recebe o robô e o gerenciador de DangerMaps
    public GunController(AdvancedRobot robot, DangerMapManager dangerMapManager) {
        this.robot = robot;
        this.dangerMapManager = dangerMapManager;
    }

    // TESTE DE CORREÇÃO

    /**
     * Chamado quando nossa bala acerta o inimigo.
     * Usamos isso para treinar nosso DangerMap (aprender).
     */
    public void onBulletHit(BulletHitEvent e, ArrayList<MyWave> myWaves, DangerMapManager dangerMapManager) {
        // Lógica para encontrar a "onda" do tiro que acertou
        // e registrar no DangerMap que aquele foi um bom tiro.
        // (Esta lógica precisa ser criada ou movida para cá)

        // Exemplo simples (provavelmente precisa de mais lógica):
        System.out.println("Acertei o inimigo com potência: " + e.getBullet().getPower());
    }

    /**
     * Chamado quando nossa bala erra o inimigo.
     * Usamos isso para treinar nosso DangerMap (aprender).
     */
    public void onBulletMissed(BulletMissedEvent e, ArrayList<MyWave> myWaves, DangerMapManager dangerMapManager) {
        // Lógica para encontrar a "onda" do tiro que errou
        // e talvez registrar no DangerMap que aquele foi um tiro ruim.

        // Exemplo simples:
        System.out.println("Errei o tiro.");
    }

    /**
     * Mira e dispara no inimigo com base na predição + DangerMap.
     */
    public void aimAndFire(ScannedRobotEvent e) {
        // Não dispara se o canhão ainda está quente ou se a energia está baixa
        if (robot.getGunHeat() > 0.2 || robot.getEnergy() < 0.5)
            return;

        double firePower = decideFirePower(e); // Escolhe a força do tiro
        double bulletSpeed = 20 - 3 * firePower; // Fórmula padrão do Robocode para velocidade do projétil

        // Previsão linear: calcula onde o inimigo provavelmente estará
        Point2D.Double pred = predict(e, bulletSpeed);
        double linearAim = Math.atan2(pred.x - robot.getX(), pred.y - robot.getY());

        // Acessa o mapa de perigo adequado à distância
        DangerMap dangerMap = null;
        if (dangerMapManager != null)
            dangerMap = dangerMapManager.getMapForDistance(e.getDistance());

        // Obtém o índice mais seguro (com menor risco)
        int bestIdx = 0;
        double gf = 0; // Guess Factor normalizado entre -1 e 1
        if (dangerMap != null) {
            bestIdx = dangerMap.bestIndex();
            gf = ((double) bestIdx / (dangerMap.buckets() - 1)) * 2.0 - 1.0; // -1..1
        }

        // Calcula direção lateral do inimigo para definir o sentido do disparo
        double absoluteBearing = robot.getHeadingRadians() + e.getBearingRadians();
        double lateral = e.getVelocity() * Math.sin(e.getHeadingRadians() - absoluteBearing);
        int direction = lateral >= 0 ? 1 : -1;

        // Ângulo máximo de evasão (baseado na velocidade máxima da bala)
        double escapeAngle = Math.asin(8.0 / bulletSpeed);
        double gfAim = absoluteBearing + direction * gf * escapeAngle;

        // Combina a mira linear (previsão) com a mira GF (aprendizado)
        double aim = linearAim * 0.4 + gfAim * 0.6;

        // Ajusta o canhão para mirar no alvo
        double gunTurn = Utils.normalRelativeAngle(aim - robot.getGunHeadingRadians());
        robot.setTurnGunRightRadians(gunTurn);

        // Dispara se o canhão já estiver quase alinhado
        if (Math.abs(Utils.normalRelativeAngle(aim - robot.getGunHeadingRadians())) < 0.25) {
            robot.setFireBullet(Math.max(0.1, Math.min(firePower, robot.getEnergy() - 0.1)));
            // Não registra aqui — o
            // robô principal pode
            // fazer isso
        }
    }

    /**
     * Versão alternativa que retorna uma MyWave ao atirar,
     * usada para treinar o DangerMap (aprendizado).
     */
    public MyWave fireWithGuess(ScannedRobotEvent e) {
        if (robot.getGunHeat() > 0.2 || robot.getEnergy() < 0.5)
            return null;

        double firePower = decideFirePower(e);
        double bulletSpeed = 20 - 3 * firePower;
        Point2D.Double pred = predict(e, bulletSpeed);
        double linearAim = Math.atan2(pred.x - robot.getX(), pred.y - robot.getY());

        DangerMap dangerMap = null;
        if (dangerMapManager != null)
            dangerMap = dangerMapManager.getMapForDistance(e.getDistance());

        int bestIdx = 0;
        double gf = 0;
        if (dangerMap != null) {
            bestIdx = dangerMap.bestIndex();
            gf = ((double) bestIdx / (dangerMap.buckets() - 1)) * 2.0 - 1.0;
        }

        double absoluteBearing = robot.getHeadingRadians() + e.getBearingRadians();
        double lateral = e.getVelocity() * Math.sin(e.getHeadingRadians() - absoluteBearing);
        int direction = lateral >= 0 ? 1 : -1;
        double escapeAngle = Math.asin(8.0 / bulletSpeed);
        double gfAim = absoluteBearing + direction * gf * escapeAngle;

        double aim = linearAim * 0.4 + gfAim * 0.6;

        double gunTurn = Utils.normalRelativeAngle(aim - robot.getGunHeadingRadians());
        robot.setTurnGunRightRadians(gunTurn);

        // Se o canhão estiver alinhado o suficiente, dispara
        if (Math.abs(Utils.normalRelativeAngle(aim - robot.getGunHeadingRadians())) < 0.25) {
            Bullet b = robot.setFireBullet(Math.max(0.1, Math.min(firePower, robot.getEnergy() - 0.1)));
            // Retorna uma "onda" de tiro (para treinar o acerto depois)
            if (b != null && dangerMap != null) {
                return new MyWave(b, bestIdx, e.getDistance());
            }
        }
        return null;
    }

    /**
     * Faz uma predição linear da posição futura do inimigo.
     * O loop incrementa o tempo até que a bala alcance a posição prevista.
     */
    private Point2D.Double predict(ScannedRobotEvent e, double bulletSpeed) {
        double absoluteBearing = robot.getHeadingRadians() + e.getBearingRadians();
        double ex = robot.getX() + Math.sin(absoluteBearing) * e.getDistance();
        double ey = robot.getY() + Math.cos(absoluteBearing) * e.getDistance();

        double heading = e.getHeadingRadians();
        double velocity = e.getVelocity();

        double px = ex, py = ey;
        double dt = 0;

        // Simula o movimento do inimigo até o ponto de impacto
        while (dt * bulletSpeed < Point2D.distance(robot.getX(), robot.getY(), px, py) && dt < 100) {
            dt += 1;
            px += Math.sin(heading) * velocity;
            py += Math.cos(heading) * velocity;

            // Impede que a predição saia do campo de batalha
            px = Math.max(18, Math.min(robot.getBattleFieldWidth() - 18, px));
            py = Math.max(18, Math.min(robot.getBattleFieldHeight() - 18, py));
        }

        return new Point2D.Double(px, py);
    }

    /**
     * Define a força do tiro com base na distância e energia atual.
     * Equilíbrio entre dano e economia de energia.
     */
    private double decideFirePower(ScannedRobotEvent e) {
        double d = e.getDistance();
        double firePower;

        // Ajuste de potência por distância
        if (d < 150)
            firePower = 3.0;
        else if (d < 300)
            firePower = 2.0;
        else if (d < 500)
            firePower = 1.4;
        else
            firePower = 1.0;

        // Reduz a potência quando a energia do robô está baixa
        if (robot.getEnergy() < 15)
            firePower = Math.min(firePower, 1.0);
        if (robot.getEnergy() < 6)
            firePower = 0.4;

        // Também considera a energia do inimigo (não desperdiça)
        firePower = Math.min(firePower, Math.max(0.1, e.getEnergy() / 4.0));

        // Garante que fique dentro dos limites válidos
        return Math.max(0.1, Math.min(3.0, firePower));
    }
}
