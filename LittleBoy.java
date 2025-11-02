package Paraiso;

import robocode.*;
import java.awt.Color;
import java.awt.geom.*;
import java.util.*;
import robocode.util.Utils;

// LittleBoy - autores (Aline, Thalia e Thaissa)

public class LittleBoy extends AdvancedRobot {
    /**
     * run: Comportamento padrão do LittleBoy, tanto mover tanque quanto arma.
     */

    // ------------------------------
    // VARIÁVEIS PARA WAVE SURFING
    // ------------------------------
    static double surfDirection = 1;
    static double enemyEnergy = 100;
    ArrayList<EnemyWave> enemyWaves = new ArrayList<>();
    ArrayList<Integer> surfStats = new ArrayList<>(Collections.nCopies(47, 0)); // Mapa de perigo (47 GuessFactors)
    
    // ------------------------------
    // VARIÁVEIS PARA ANTI-GRAVITY MOVEMENT
    // ------------------------------
    static Point2D.Double[] enemyPoints = new Point2D.Double[20]; // Posições dos inimigos detectados
    int count; // Contador para armazenar múltiplos inimigos
    double gravityStrength = 5000; // Força base de repulsão dos inimigos
    double wallRepulsion = 8000;   // Força de repulsão das paredes

    // Variáveis auxiliares para suavização (momentum)
    double prevXForce = 0;
    double prevYForce = 0;

    public void run() {
        // A inicialização do robô deve ser colocada aqui
        setAdjustRadarForGunTurn(true);
        setAdjustRadarForRobotTurn(true);
        setTurnGunRight(Double.POSITIVE_INFINITY);

        setBodyColor(Color.black); // Define a cor do corpo
        setRadarColor(Color.blue); // Define a cor do radar

        while (true) {
            // Radar contínuo
            if (getRadarTurnRemaining() == 0.0)
                setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

            // ------------------------------
            // MOVIMENTO ANTI-GRAVITY APRIMORADO
            // ------------------------------
            doAntiGravityMovement(); // Chama o movimento anti-gravity ajustado

            // Atualiza as ondas inimigas e realiza o movimento evasivo (wave surfing)
            updateWaves();
            doSurfing();

            execute();
        }
    }
        
    public Point2D.Double wallSmoothing(Point2D.Double location, Point2D.Double destination, int circleDirection, double wallStick) {
        double battleFieldWidth = getBattleFieldWidth();
        double battleFieldHeight = getBattleFieldHeight();
        Rectangle2D.Double battleField = new Rectangle2D.Double(18, 18, battleFieldWidth - 36, battleFieldHeight - 36);
        Point2D.Double p = new Point2D.Double(destination.x, destination.y);

        for (int i = 0; !battleField.contains(p) && i < 4; i++) {
            if (p.x < 18) {
                p.x = 18;
                double a = location.x - 18;
                p.y = location.y + circleDirection * Math.sqrt(wallStick * wallStick - a * a);
            } else if (p.y > battleFieldHeight - 18) {
                p.y = battleFieldHeight - 18;
                double a = battleFieldHeight - 18 - location.y;
                p.x = location.x + circleDirection * Math.sqrt(wallStick * wallStick - a * a);
            } else if (p.x > battleFieldWidth - 18) {
                p.x = battleFieldWidth - 18;
                double a = battleFieldWidth - 18 - location.x;
                p.y = location.y - circleDirection * Math.sqrt(wallStick * wallStick - a * a);
            } else if (p.y < 18) {
                p.y = 18;
                double a = location.y - 18;
                p.x = location.x - circleDirection * Math.sqrt(wallStick * wallStick - a * a);
            }
        }
        return p;
    }

    // onScannedRobot: O que fazer ao ver outro robô
    public void onScannedRobot(ScannedRobotEvent e) {
        double absBearing = e.getBearingRadians() + getHeadingRadians();

        // Armazena a posição do inimigo para o movimento anti-gravity
        enemyPoints[count] = new Point2D.Double(
            getX() + e.getDistance() * Math.sin(absBearing),
            getY() + e.getDistance() * Math.cos(absBearing)
        );
        if (++count >= getOthers()) count = 0;

        shoot(e); // Chama o método de tiro
        setFire(1); // Tiro padrão

        // Quando Radar encontra um inimigo
        double angleToEnemy = getHeadingRadians() + e.getBearingRadians();
        double turnToEnemy = Utils.normalRelativeAngle(angleToEnemy - getRadarHeadingRadians());
        double extraTurn = Math.atan(36.0 / e.getDistance()) * (turnToEnemy >= 0 ? 1 : -1);
        setTurnRadarRightRadians(turnToEnemy + extraTurn);

        // ------------------------------
        // DETECÇÃO DE TIRO INIMIGO
        // ------------------------------
        double changeInEnergy = enemyEnergy - e.getEnergy();
        if (changeInEnergy > 0 && changeInEnergy <= 3.0) {
            EnemyWave ew = new EnemyWave();
            ew.fireTime = getTime();
            ew.bulletVelocity = bulletVelocity(changeInEnergy);
            ew.fireLocation = new Point2D.Double(
                getX() + Math.sin(angleToEnemy) * e.getDistance(),
                getY() + Math.cos(angleToEnemy) * e.getDistance()
            );
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

        if (e.getDistance() > 400)
            firePower = 1.0;
        else if (e.getDistance() < 200)
            firePower = 3.0;

        if (getEnergy() < 1)
            firePower = 0.1;
        else if (getEnergy() < 10)
            firePower = 1.0;

        return Math.min(e.getEnergy() / 4, firePower);
    }

    public void onHitByBullet(HitByBulletEvent e) {
        back(10);
    }

    public void onHitWall(HitWallEvent e) {
        System.out.println("WARNING: I hit a wall (" + getTime() + ").");
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
        int index = (int) ((getFactorIndex(surfWave, direction)) * (surfStats.size() - 1));
        return surfStats.get(Math.max(0, Math.min(index, surfStats.size() - 1))) + Math.random();
    }

    public double getFactorIndex(EnemyWave surfWave, int direction) {
        Point2D.Double enemyFireLocation = surfWave.fireLocation;
        double offsetAngle = Utils.normalRelativeAngle(Math.atan2(
            getX() - enemyFireLocation.x, getY() - enemyFireLocation.y
        ) - surfWave.directAngle);
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

    // ------------------------------
    // NOVO MÉTODO: ANTI-GRAVITY MOVEMENT (APRIMORADO)
    // ------------------------------
    // Agora com repulsão adaptativa, atração ao centro, suavização e velocidade dinâmica
    public void doAntiGravityMovement() {
        double xForce = 0;
        double yForce = 0;

        // Força repulsiva dos inimigos
        for (int i = 0; i < enemyPoints.length; i++) {
            Point2D.Double p = enemyPoints[i];
            if (p != null) {
                double dx = getX() - p.x;
                double dy = getY() - p.y;
                double distanceSq = dx * dx + dy * dy;
                if (distanceSq > 0.0001) {
                    double force = gravityStrength / distanceSq;
                    double angle = Math.atan2(dy, dx);
                    xForce += Math.cos(angle) * force;
                    yForce += Math.sin(angle) * force;
                }
            }
        }

        // ------------------------------
        // Repulsão adaptativa das paredes com limite para evitar cantos
        // ------------------------------
        double distToLeft = getX();
        double distToRight = getBattleFieldWidth() - getX();
        double distToTop = getBattleFieldHeight() - getY();
        double distToBottom = getY();
        double wallMaxForce = 1500; // Força máxima das paredes
        xForce += Math.min(wallRepulsion / Math.pow(Math.max(1, distToLeft - 30), 2), wallMaxForce);
        xForce -= Math.min(wallRepulsion / Math.pow(Math.max(1, distToRight - 30), 2), wallMaxForce);
        yForce += Math.min(wallRepulsion / Math.pow(Math.max(1, distToBottom - 30), 2), wallMaxForce);
        yForce -= Math.min(wallRepulsion / Math.pow(Math.max(1, distToTop - 30), 2), wallMaxForce);

        // ------------------------------
        // Atração ao centro (mais forte e suavizada)
        // ------------------------------
        Point2D.Double safePoint = new Point2D.Double(getBattleFieldWidth() / 2, getBattleFieldHeight() / 2);
        double dxSafe = safePoint.x - getX();
        double dySafe = safePoint.y - getY();
        double distanceSafeSq = dxSafe * dxSafe + dySafe * dySafe;
        double attraction = 4000 / distanceSafeSq; // aumento da força central
        xForce += Math.cos(Math.atan2(dySafe, dxSafe)) * attraction * 0.7; // suavização da atração
        yForce += Math.sin(Math.atan2(dySafe, dxSafe)) * attraction * 0.7;

        // Suavização (momentum)
        if (prevXForce != 0 || prevYForce != 0) {
            double smoothFactor = 0.7;
            xForce = (xForce * (1 - smoothFactor)) + (prevXForce * smoothFactor);
            yForce = (yForce * (1 - smoothFactor)) + (prevYForce * smoothFactor);
        }
        prevXForce = xForce;
        prevYForce = yForce;

        // Normalização do vetor
        double magnitude = Math.sqrt(xForce * xForce + yForce * yForce);
        if (magnitude > 0) {
            xForce /= magnitude;
            yForce /= magnitude;
        }

        // Direção e movimento
        double moveAngle = Math.atan2(yForce, xForce);
        setTurnRightRadians(Utils.normalRelativeAngle(moveAngle - getHeadingRadians()));
        setAhead(100);

        // Velocidade dinâmica
        setMaxVelocity(8 - Math.abs(getTurnRemaining()) / 40);
    }

    // Classe auxiliar para representar a onda inimiga
    class EnemyWave {
        Point2D.Double fireLocation;
        long fireTime;
        double bulletVelocity, directAngle, distanceTraveled;
        int direction;
    }
}
