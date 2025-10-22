package Paraiso;

import java.util.*;
import java.io.*;

/**
 * Classe DangerMap
 * 
 * Representa um "mapa de perigo" usado para armazenar e atualizar informações
 * sobre regiões perigosas — geralmente usada em Robocode para decidir
 * para onde o robô deve ou não se mover.
 * 
 * Cada "bucket" é como uma célula do mapa que acumula um valor de perigo.
 * O robô usa esses valores para evitar áreas onde ele já levou tiros.
 */
public class DangerMap {
    // Número total de buckets (divisões do mapa)
    private final int buckets;

    // Vetor com os valores de perigo de cada bucket
    private final double[] stats;

    // Fator de decaimento (para reduzir o peso de perigos antigos)
    private final double decay;

    /**
     * Construtor do mapa de perigo.
     * 
     * @param buckets número de divisões do mapa (resolução)
     * @param decay fator de decaimento (entre 0 e 1) que suaviza os valores com o tempo
     */
    public DangerMap(int buckets, double decay) {
        this.buckets = buckets;
        this.decay = decay;
        this.stats = new double[buckets];
        Arrays.fill(this.stats, 0.0); // Inicializa todos os perigos com 0
    }

    /** Retorna o número de buckets. */
    public int buckets() { return buckets; }

    /**
     * Aplica o decaimento sobre todos os valores de perigo.
     * Isso faz os perigos antigos perderem importância com o tempo.
     */
    public void decay() {
        for (int i = 0; i < buckets; i++)
            stats[i] *= decay;
    }

    /**
     * Adiciona uma certa quantidade de perigo a um índice específico.
     * O índice é limitado aos limites do vetor.
     */
    public void addDanger(int index, double amount) {
        index = Math.max(0, Math.min(buckets - 1, index)); // garante que não sai do limite
        stats[index] += amount;
    }

    /**
     * Registra um acerto (hit) em uma determinada posição,
     * distribuindo o perigo em torno do índice com um peso decrescente.
     * 
     * Essa técnica é chamada de "suavização por kernel" — o impacto do tiro
     * afeta o entorno, não só um ponto específico.
     */
    public void registerHit(int index, double power) {
        int radius = Math.max(1, buckets / 20); // define o raio de influência do hit
        for (int i = -radius; i <= radius; i++) {
            int idx = index + i;
            if (idx < 0 || idx >= buckets) continue; // ignora índices fora do vetor
            double w = 1.0 / (Math.abs(i) + 1); // peso inversamente proporcional à distância
            stats[idx] += power * w;
        }
    }

    /**
     * Salva o estado atual do mapa em um arquivo binário.
     * Cada bucket é gravado com seu valor de perigo.
     */
    public void saveToFile(File f) {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(f))) {
            out.writeInt(buckets);
            for (double v : stats) out.writeDouble(v);
        } catch (IOException ex) {
            // Se der erro, apenas ignora (não interrompe o robô)
        }
    }

    /**
     * Carrega os dados de um arquivo salvo anteriormente,
     * restaurando os valores de perigo nos buckets.
     */
    public void loadFromFile(File f) {
        if (!f.exists()) return;
        try (DataInputStream in = new DataInputStream(new FileInputStream(f))) {
            int b = in.readInt();
            for (int i = 0; i < Math.min(b, buckets); i++) stats[i] = in.readDouble();
        } catch (IOException ex) {
            // Ignora erros de leitura
        }
    }

    /**
     * Retorna o valor de perigo em um determinado índice.
     * Garante que o índice esteja dentro dos limites válidos.
     */
    public double get(int index) {
        index = Math.max(0, Math.min(buckets - 1, index));
        return stats[index];
    }

    /**
     * Converte um GuessFactor (valor entre -1 e +1)
     * para um índice do vetor (0 até buckets - 1).
     * 
     * GuessFactor é uma métrica usada em mira preditiva no Robocode.
     */
    public int indexFromGuessFactor(double gf) {
        double v = (gf + 1.0) / 2.0; // normaliza o valor para o intervalo 0..1
        return (int) Math.round(v * (buckets - 1));
    }

    /**
     * Retorna o índice com o menor valor de perigo,
     * ou seja, a posição mais segura para se mover.
     */
    public int bestIndex() {
        double best = Double.MAX_VALUE;
        int bi = 0;
        for (int i = 0; i < buckets; i++) {
            if (stats[i] < best) {
                best = stats[i];
                bi = i;
            }
        }
        return bi;
    }
}
