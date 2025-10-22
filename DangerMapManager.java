package Paraiso;

import java.io.File;

/**
 * Classe DangerMapManager
 * 
 * Essa classe gerencia vários mapas de perigo (DangerMap),
 * divididos por faixas de distância entre o robô e o inimigo.
 * 
 * A ideia é que o comportamento de esquiva do robô possa variar conforme
 * a distância: ele pode reagir diferente se o inimigo está perto ou longe.
 */
public class DangerMapManager {
    // Vetor com vários mapas de perigo, um para cada "banda" de distância
    private final DangerMap[] maps;

    // Distância máxima considerada (usada para determinar qual mapa usar)
    private final double maxDistance;

    /**
     * Construtor
     * 
     * @param bands       número de faixas de distância (ex: curta, média, longa)
     * @param buckets     número de divisões internas de cada DangerMap
     * @param decay       fator de decaimento (para suavizar perigos antigos)
     * @param maxDistance distância máxima usada para normalizar o cálculo
     */
    public DangerMapManager(int bands, int buckets, double decay, double maxDistance) {
        this.maps = new DangerMap[bands]; // Cria o vetor com a quantidade de faixas
        for (int i = 0; i < bands; i++) {
            // Cria um DangerMap independente para cada faixa
            maps[i] = new DangerMap(buckets, decay);
        }
        this.maxDistance = maxDistance;
    }

    /**
     * Retorna o mapa de perigo correspondente à distância atual do inimigo.
     * 
     * O robô usa essa função para decidir qual mapa consultar ou atualizar,
     * conforme o quão longe o inimigo está.
     */
    public DangerMap getMapForDistance(double distance) {
        // Converte a distância em um índice dentro do vetor de mapas
        int idx = (int) Math.floor((distance / maxDistance) * maps.length);

        // Garante que o índice nunca saia dos limites do vetor
        if (idx < 0) idx = 0;
        if (idx >= maps.length) idx = maps.length - 1;

        // Retorna o mapa correspondente àquela faixa de distância
        return maps[idx];
    }

    /**
     * Salva todos os mapas de perigo em arquivos separados.
     * 
     * Cada mapa é salvo com um sufixo numérico diferente (_0, _1, _2, ...),
     * para que possam ser restaurados depois.
     */
    public void saveAll(File baseFile) {
        for (int i = 0; i < maps.length; i++) {
            // Cria um arquivo nomeado conforme o índice da faixa
            File f = new File(baseFile.getAbsolutePath() + "_" + i + ".dat");
            // Salva o DangerMap correspondente
            maps[i].saveToFile(f);
        }
    }

    /**
     * Carrega os mapas de perigo salvos anteriormente.
     * 
     * Isso permite que o robô "lembre" onde levou tiros ou encontrou perigo
     * em batalhas anteriores.
     */
    public void loadAll(File baseFile) {
        for (int i = 0; i < maps.length; i++) {
            // Localiza o arquivo correspondente à faixa de distância
            File f = new File(baseFile.getAbsolutePath() + "_" + i + ".dat");
            // Carrega o DangerMap daquele arquivo
            maps[i].loadFromFile(f);
        }
    }

    /**
     * Aplica o decaimento em todos os mapas.
     * 
     * Isso reduz gradualmente o valor dos perigos antigos,
     * mantendo o mapa sempre atualizado com base nos eventos mais recentes.
     */
    public void decayAll() {
        for (DangerMap m : maps)
            m.decay();
    }

    /** Retorna quantas bandas (faixas de distância) existem. */
    public int bands() { return maps.length; }
}
