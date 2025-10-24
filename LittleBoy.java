package Paraiso;

import robocode.*;
import java.awt.Color;
import java.awt.geom.*;
import java.util.*;
import robocode.util.Utils;

/**
 * LittleBoy Ultra-Potente - Autores: Aline, Thalia e Thaissa
 *
 * Funcionalidades:
 * - Wave Surfing avançado
 * - Mira preditiva dinâmica
 * - Anti-Gravity Movement agressivo
 * - Radar inteligente
 * - Aprendizado de padrões inimigos (DangerMap)
 */
public class LittleBoy extends AdvancedRobot {

    // ------------------------------
    // VARIÁVEIS DE SURFING
    // ------------------------------
    static double surfDirection = 1;          // Direção lateral do Wave Surfing (-1 ou 1)
    static double enemyEnergy = 100;          // Guarda energia do inimigo para detectar tiros
    ArrayList<EnemyWave> enemyWaves = new ArrayList<>();  // Lista de ondas de balas inimigas
    ArrayList<MyWave> myWaves = new ArrayList<>();        // Lista de ondas das nossas balas

    // ------------------------------
    // RASTREAMENTO DO INIMIGO
    // ------------------------------
    String currentTarget = null;              // Nome do inimigo que estamos atacando
    double enemyHeading = 0;                  // Direção do inimigo
    double enemyVelocity = 0;                 // Velocidade do inimigo
    double enemyDistance = 0;                 // Distância até o inimigo
    double enemyAbsoluteBearing = 0;          // Ângulo absoluto para o inimigo
    Point2D.Double lastEnemyLocation;         // Última posição conhecida do inimigo

    // ------------------------------
    // CONTROLLERS
    // ------------------------------
    private DangerMapManager dangerMapManager; // Gerencia DangerMaps
    private GunController gunController;       // Controla mira e disparos
    private MovementController movementController; // Controla movimento anti-gravidade
    private RadarController radarController;   // Controla radar

    // ------------------------------
    // MÉTODO PRINCIPAL run()
    // ------------------------------
    public void run() {
        setAdjustRadarForGunTurn(true);  // Radar independente do canhão
        setAdjustRadarForRobotTurn(true); // Radar independente do corpo
        setTurnGunRight(Double.POSITIVE_INFINITY); // Gira o canhão infinitamente

        // Inicializa controllers
        dangerMapManager = new DangerMapManager(3, 101, 0.995, Math.max(getBattleFieldWidth(), getBattleFieldHeight()));
        gunController = new GunController(this, dangerMapManager);
        movementController = new MovementController(this);
        radarController = new RadarController(this);

        // Tenta carregar DangerMap de batalhas anteriores
        try { dangerMapManager.loadAll(getDataFile("dangermap")); } catch(Exception ex){}

        // Configura cores do robô
        setBodyColor(Color.black);
        setRadarColor(Color.blue);
        setGunColor(Color.red);

        // Loop principal
        while(true){
            // Gira radar continuamente
            if(getRadarTurnRemaining() == 0.0)
                setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

            // Atualiza ondas inimigas (para Wave Surfing)
            updateWaves();

            // Movimento: Wave Surfing se houver ondas, senão Anti-Gravity
            if(!enemyWaves.isEmpty())
                doSurfingDynamic();
            else
                movementController.antiGravityMove(lastEnemyLocation);

            // Decaimento do DangerMap
            decayDangerMap();

            execute(); // Executa todos os comandos pendentes
        }
    }

    // ------------------------------
    // Evento: escaneou outro robô
    // ------------------------------
    public void onScannedRobot(ScannedRobotEvent e){
        // Atualiza dados do inimigo
        enemyHeading = e.getHeadingRadians();
        enemyVelocity = e.getVelocity();
        enemyDistance = e.getDistance();
        enemyAbsoluteBearing = getHeadingRadians() + e.getBearingRadians();
        currentTarget = e.getName();

        // Trava radar no inimigo
        radarController.lockRadar(enemyAbsoluteBearing);

        // Disparo preditivo (GuessFactor + DangerMap)
        MyWave mw = gunController.fireWithGuess(e);
        if(mw != null) myWaves.add(mw);

        // Calcula posição inimiga para Anti-Gravity
        double enemyX = getX() + Math.sin(enemyAbsoluteBearing) * e.getDistance();
        double enemyY = getY() + Math.cos(enemyAbsoluteBearing) * e.getDistance();
        lastEnemyLocation = new Point2D.Double(enemyX, enemyY);

        // Detecta disparo inimigo (queda de energia)
        double change = enemyEnergy - e.getEnergy();
        if(change > 0 && change <= 3.0){
            EnemyWave ew = new EnemyWave();
            ew.fireTime = getTime();
            ew.bulletVelocity = 20 - 3*change; // Calcula velocidade da bala
            ew.fireLocation = new Point2D.Double(enemyX, enemyY);
            ew.directAngle = enemyAbsoluteBearing;
            ew.direction = (Math.sin(e.getBearingRadians())*getVelocity()>=0?1:-1);
            enemyWaves.add(ew); // Adiciona onda inimiga
        }
        enemyEnergy = e.getEnergy();
    }

    // ------------------------------
    // Atualiza ondas inimigas
    // ------------------------------
    public void updateWaves(){
        for(int i=0;i<enemyWaves.size();i++){
            EnemyWave ew = enemyWaves.get(i);
            ew.distanceTraveled = (getTime() - ew.fireTime) * ew.bulletVelocity;
            // Remove ondas que já passaram do robô
            if(ew.distanceTraveled > Point2D.distance(getX(), getY(), ew.fireLocation.x, ew.fireLocation.y) + 50){
                enemyWaves.remove(i); i--;
            }
        }
    }

    // ------------------------------
    // Wave Surfing Dinâmico
    // ------------------------------
    public void doSurfingDynamic(){
        if(enemyWaves.isEmpty()) return;

        // Escolhe a onda mais perigosa
        EnemyWave best = null;
        double bestDelta = Double.MAX_VALUE;
        for(EnemyWave ew:enemyWaves){
            double dist = Point2D.distance(getX(), getY(), ew.fireLocation.x, ew.fireLocation.y);
            double delta = Math.abs(ew.distanceTraveled - dist);
            if(delta<bestDelta){ bestDelta = delta; best = ew; }
        }
        if(best==null) return;

        // Calcula perigo à esquerda e à direita
        double dangerLeft = checkDanger(best,-1);
        double dangerRight = checkDanger(best,1);

        // Define direção do surf
        surfDirection = (dangerLeft<dangerRight)?-1:1;

        // Pequena aleatoriedade para confundir inimigo
        if(Math.random()<0.05) surfDirection*=-1;

        // Calcula ângulo de movimento e vai para frente ou ré
        double goAngle = Utils.normalRelativeAngle(best.directAngle + surfDirection*Math.PI/2);
        MovementController.setBackAsFront(this, goAngle);
    }

    // ------------------------------
    // Checa perigo baseado no DangerMap
    // ------------------------------
    private double checkDanger(EnemyWave wave,int direction){
        double myX=getX(), myY=getY(), danger=0;
        double distanceToFire = Point2D.distance(myX,myY,wave.fireLocation.x,wave.fireLocation.y);
        double timeToImpact = Math.max(0,(distanceToFire-wave.distanceTraveled)/wave.bulletVelocity);
        DangerMap map = dangerMapManager!=null?dangerMapManager.getMapForDistance(distanceToFire):null;

        for(int step=0; step<Math.min(80,(int)Math.ceil(timeToImpact+1)); step++){
            double moveAngle = wave.directAngle+(direction*Math.PI/2);
            myX += Math.sin(moveAngle)*Math.abs(getVelocity());
            myY += Math.cos(moveAngle)*Math.abs(getVelocity());
            myX = Math.max(18,Math.min(getBattleFieldWidth()-18,myX));
            myY = Math.max(18,Math.min(getBattleFieldHeight()-18,myY));

            double offsetAngle = Utils.normalRelativeAngle(Math.atan2(myX-wave.fireLocation.x,myY-wave.fireLocation.y)-wave.directAngle);
            double guessFactor = offsetAngle/Math.asin(8.0/wave.bulletVelocity)*direction;
            guessFactor = Math.max(-1,Math.min(1,guessFactor));
            if(map!=null) danger += map.get(map.indexFromGuessFactor(guessFactor));
        }

        return danger + Math.random()*0.02; // Aleatoriedade pequena
    }

    // ------------------------------
    // Decaimento do DangerMap
    // ------------------------------
    public void decayDangerMap(){ if(dangerMapManager!=null) dangerMapManager.decayAll(); }

    // ------------------------------
    // Eventos
    // ------------------------------
    public void onHitByBullet(HitByBulletEvent e){ movementController.antiGravityMove(lastEnemyLocation); }
    public void onHitWall(HitWallEvent e){ movementController.antiGravityMove(lastEnemyLocation); }
    public void onBulletHit(BulletHitEvent e){ gunController.onBulletHit(e,myWaves,dangerMapManager); }
    public void onBulletMissed(BulletMissedEvent e){ gunController.onBulletMissed(e,myWaves,dangerMapManager); }
    public void onWin(WinEvent e){ saveDangerMap(); setBodyColor(Color.green); setRadarColor(Color.white); }
    public void onDeath(DeathEvent e){ saveDangerMap(); }

    // ------------------------------
    // Salva mapas de perigo
    // ------------------------------
    private void saveDangerMap(){ try{ dangerMapManager.saveAll(getDataFile("dangermap")); }catch(Exception ex){} }

    // ------------------------------
    // Utilitários
    // ------------------------------
    public static double bulletVelocity(double power){ return 20-3*power; }
    public static double limit(double min,double value,double max){ return Math.max(min,Math.min(value,max)); }
    public static void setBackAsFront(AdvancedRobot robot,double goAngle){
        double angle = Utils.normalRelativeAngle(goAngle-robot.getHeadingRadians());
        if(Math.abs(angle)>(Math.PI/2)){
            if(angle<0) robot.setTurnRightRadians(Math.PI+angle);
            else robot.setTurnRightRadians(-Math.PI+angle);
            robot.setBack(100);
        }else{
            robot.setTurnRightRadians(angle);
            robot.setAhead(100);
        }
    }
}
