package Paraiso;

import java.awt.geom.Point2D;

/**
 * Classe EnemyWave
 * 
 * Representa uma "onda" de tiro disparada pelo inimigo.
 * 
 * No Robocode, uma onda (wave) é uma maneira de modelar o movimento
 * do projétil do inimigo — mesmo que o tiro real ainda não tenha te atingido.
 * 
 * Essa técnica é usada em estratégias como o "wave surfing", onde o robô
 * tenta prever a trajetória do tiro inimigo e se mover para evitar o impacto.
 */
public class EnemyWave {

    // Posição (x, y) de onde o inimigo disparou o tiro
    public Point2D.Double fireLocation;

    // Momento (tick) do jogo em que o inimigo disparou
    public long fireTime;

    // Velocidade do projétil do inimigo (depende da potência do tiro)
    public double bulletVelocity;

    // Ângulo direto (bearing) do tiro — a direção em que o inimigo atirou
    public double directAngle;

    // Distância já percorrida pela "onda" (quanto o tiro avançou até agora)
    public double distanceTraveled;

    // Direção do tiro em relação ao nosso movimento (1 = horário, -1 = anti-horário)
    public int direction;

    /**
     * Construtor vazio
     * 
     * As propriedades são preenchidas dinamicamente no momento em que
     * o robô detecta que o inimigo provavelmente disparou.
     */
    public EnemyWave() { }
}
