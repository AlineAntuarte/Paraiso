package NewBoy;

import robocode.*;
import robocode.util.Utils;
import java.awt.geom.*;
import java.awt.Color;
import java.io.*;
import java.util.zip.*;

public class LittleBoy extends AdvancedRobot {

    static final double BEST_DISTANCE = 525;
    static boolean flat = true;
    static double bearingDirection = 1, lastLatVel, lastVelocity, lastReverseTime, circleDir = 1, enemyFirePower,
            enemyEnergy, enemyDistance, lastVChangeTime, enemyLatVel, enemyVelocity, enemyFireTime, numBadHits;
    static Point2D.Double enemyLocation;
    static final int GF_ZERO = 15;
    static final int GF_ONE = 30;
    static String enemyName;
    static int[][][][][][] guessFactors = new int[3][5][3][3][8][GF_ONE + 1];
    static double numWins;

    public void run() {
        setColors(Color.black, Color.black, Color.yellow);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        double battlefieldWidth = getBattleFieldWidth();
        double battlefieldHeight = getBattleFieldHeight();

        out.println("Battlefield Width: " + battlefieldWidth);
        out.println("Battlefield Height: " + battlefieldHeight);

        // Move para uma posição segura (não exatamente o centro)
        double safeX = battlefieldWidth * 0.6;
        double safeY = battlefieldHeight * 0.6;
        goTo(safeX, safeY);

        while (true) {
            turnRadarRightRadians(Double.POSITIVE_INFINITY);
        }
    }

    private void goTo(double x, double y) {
        double dx = x - getX();
        double dy = y - getY();
        double angle = Math.atan2(dx, dy);
        double distance = Math.hypot(dx, dy);

        setTurnRightRadians(Utils.normalRelativeAngle(angle - getHeadingRadians()));
        setAhead(distance);
        execute();
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        if (enemyName == null) {
            enemyName = e.getName();
            restoreData();
        }

        Point2D.Double robotLocation = new Point2D.Double(getX(), getY());
        double theta;
        double enemyAbsoluteBearing = getHeadingRadians() + e.getBearingRadians();
        enemyDistance = e.getDistance();
        enemyLocation = projectMotion(robotLocation, enemyAbsoluteBearing, enemyDistance);

        if ((enemyEnergy -= e.getEnergy()) >= 0.1 && enemyEnergy <= 3.0) {
            enemyFirePower = enemyEnergy;
            enemyFireTime = getTime();
        }

        enemyEnergy = e.getEnergy();

        // Arena adaptável
        double margin = 36;
        Rectangle2D.Double BF = new Rectangle2D.Double(
                margin,
                margin,
                getBattleFieldWidth() - 2 * margin,
                getBattleFieldHeight() - 2 * margin);

        Point2D.Double newDestination = null;
        double moveDistance = Math.min(250, Math.max(170, enemyDistance * 0.3));
        int attempts = 0;
        double angleOffset = 0.02 + Math.PI / 2 + (enemyDistance > BEST_DISTANCE ? -0.1 : 0.5);

        while (attempts++ < 100) {
            double angle = enemyAbsoluteBearing + circleDir * (angleOffset -= 0.02);
            newDestination = projectMotion(robotLocation, angle, moveDistance);
            if (BF.contains(newDestination))
                break;
        }

        theta = absoluteBearing(robotLocation, newDestination) - getHeadingRadians();
        setAhead(Math.cos(theta) * 100);
        setTurnRightRadians(Math.tan(theta));

        MicroWave w = new MicroWave();

        lastLatVel = enemyLatVel;
        lastVelocity = enemyVelocity;
        enemyLatVel = (enemyVelocity = e.getVelocity()) * Math.sin(e.getHeadingRadians() - enemyAbsoluteBearing);

        int distanceIndex = (int) enemyDistance / 140;

        double bulletPower = distanceIndex == 0 ? 3 : 2;
        theta = Math.min(getEnergy() / 4, Math.min(enemyEnergy / 4, bulletPower));
        if (theta == bulletPower)
            addCustomEvent(w);
        bulletPower = theta;
        w.bulletVelocity = 20D - 3D * bulletPower;

        int accelIndex = (int) Math.round(Math.abs(enemyLatVel) - Math.abs(lastLatVel));

        if (enemyLatVel != 0)
            bearingDirection = enemyLatVel > 0 ? 1 : -1;
        w.bearingDirection = bearingDirection * Math.asin(8D / w.bulletVelocity) / GF_ZERO;

        double moveTime = w.bulletVelocity * lastVChangeTime++ / enemyDistance;
        int bestGF = moveTime < .1 ? 1 : moveTime < .3 ? 2 : moveTime < 1 ? 3 : 4;

        int vIndex = (int) Math.abs(enemyLatVel / 3);

        if (Math.abs(Math.abs(enemyVelocity) - Math.abs(lastVelocity)) > .6) {
            lastVChangeTime = 0;
            bestGF = 0;

            accelIndex = (int) Math.round(Math.abs(enemyVelocity) - Math.abs(lastVelocity));
            vIndex = (int) Math.abs(enemyVelocity / 3);
        }

        if (accelIndex != 0)
            accelIndex = accelIndex > 0 ? 1 : 2;

        w.firePosition = robotLocation;
        w.enemyAbsBearing = enemyAbsoluteBearing;
        w.waveGuessFactors = guessFactors[accelIndex][bestGF][vIndex][BF.contains(
                projectMotion(robotLocation, enemyAbsoluteBearing + w.bearingDirection * GF_ZERO, enemyDistance))
                        ? 0
                        : BF.contains(projectMotion(robotLocation,
                                enemyAbsoluteBearing + .5 * w.bearingDirection * GF_ZERO, enemyDistance)) ? 1
                                        : 2][distanceIndex];

        bestGF = GF_ZERO;

        for (int gf = GF_ONE; gf >= 0 && enemyEnergy > 0; gf--)
            if (w.waveGuessFactors[gf] > w.waveGuessFactors[bestGF])
                bestGF = gf;

        setTurnGunRightRadians(Utils.normalRelativeAngle(
                enemyAbsoluteBearing - getGunHeadingRadians() + w.bearingDirection * (bestGF - GF_ZERO)));

        if (getEnergy() > 1 || distanceIndex == 0)
            setFire(bulletPower);

        setTurnRadarRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing - getRadarHeadingRadians()) * 2);
    }

    public void onHitByBullet(HitByBulletEvent e) {
        if ((double) (getTime() - lastReverseTime) > enemyDistance / e.getVelocity() && enemyDistance > 200 && !flat)
            flat = (++numBadHits / (getRoundNum() + 1) > 1.1);
    }

    private static Point2D.Double projectMotion(Point2D.Double loc, double heading, double distance) {
        return new Point2D.Double(loc.x + distance * Math.sin(heading), loc.y + distance * Math.cos(heading));
    }

    private static double absoluteBearing(Point2D.Double source, Point2D.Double target) {
        return Math.atan2(target.x - source.x, target.y - source.y);
    }

    public void onWin(WinEvent e) {
        numWins++;
        saveData();
    }

    public void onDeath(DeathEvent e) {
        saveData();
    }

    private void restoreData() {
        try {
            ObjectInputStream in = new ObjectInputStream(
                    new GZIPInputStream(new FileInputStream(getDataFile(enemyName))));
            guessFactors = (int[][][][][][]) in.readObject();
            in.close();
        } catch (Exception ex) {
            flat = false;
        }
    }

    private void saveData() {
        if (flat && numWins / (getRoundNum() + 1) < .7 && getNumRounds() == getRoundNum() + 1)
            try {
                ObjectOutputStream out = new ObjectOutputStream(
                        new GZIPOutputStream(new RobocodeFileOutputStream(getDataFile(enemyName))));
                out.writeObject(guessFactors);
                out.close();
            } catch (IOException ex) {
            }
    }

    class MicroWave extends Condition {
        Point2D.Double firePosition;
        int[] waveGuessFactors;
        double enemyAbsBearing, distance, bearingDirection, bulletVelocity;

        public boolean test() {
            if ((LittleBoy.enemyLocation).distance(firePosition) <= (distance += bulletVelocity) + bulletVelocity) {
                try {
                    waveGuessFactors[(int) Math.round((Utils.normalRelativeAngle(
                            absoluteBearing(firePosition, LittleBoy.enemyLocation) - enemyAbsBearing))
                            / bearingDirection + GF_ZERO)]++;
                } catch (ArrayIndexOutOfBoundsException e) {
                }
                removeCustomEvent(this);
            }
            return false;
        }
    }
}
