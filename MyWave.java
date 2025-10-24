package Paraiso;

import robocode.Bullet; // Importa a classe Bullet do Robocode para rastrear balas
// import java.awt.geom.Point2D; // Importa Point2D (não usado aqui, mas possivelmente útil futuramente)

// Classe que representa uma onda de bala disparada pelo nosso robô
// Utilizada para o aprendizado do robô (Wave Surfing / ajuste de tiro)
public class MyWave {
    public Bullet bullet;      // A bala associada a esta onda
    public int guessIndex;     // Índice do "bucket" usado no mapa de perigo (guess factor)
    public double fireDistance; // Distância do disparo quando a onda foi criada

    // Construtor da classe MyWave
    public MyWave(Bullet b, int guessIndex, double fireDistance) {
        this.bullet = b;             // Armazena a bala que disparamos
        this.guessIndex = guessIndex; // Armazena o índice do guess factor usado para aprender
        this.fireDistance = fireDistance; // Armazena a distância do disparo (para análise posterior)
    }
}
