
# LittleBoy 2.0 – Robocode Bot

<img src="https://github.com/user-attachments/assets/5a998993-ad8b-4549-b1c0-ebf556491d3a" alt="LittleBoy" width="300"/>

## 🔰 Visão Geral

**LittleBoy** é um robô avançado do Robocode projetado para duelos 1x1, combinando técnicas de *Guess Factor Targeting* (GFT) para mira e movimento circular adaptativo para esquiva. Seu foco é antecipar o inimigo e manter posicionamento estratégico com alta evasão.

## 🌟 Estratégia

O robô utiliza lógica estatística e dinâmica para atirar e se movimentar com precisão.

### 🔫 Mira – Guess Factor Targeting

O sistema de mira utiliza **ondas virtuais de tiro** para prever movimentos do inimigo.

* **Ondas (MicroWave):** Cada tiro cria uma onda virtual que registra onde o inimigo estava quando o tiro "chegaria" nele.
* **Armazenamento Estatístico:** Os dados são segmentados por condições de movimento do inimigo:

  * Aceleração lateral
  * Velocidade lateral
  * Proximidade da parede
  * Distância
* **Tomada de Decisão:** Mira no ângulo com maior probabilidade de acerto baseado em dados históricos.

### 🚶 Movimento – Órbita Circular Adaptativa

* Mantém distância ideal do adversário (**BEST_DISTANCE = 525**)
* Se move perpendicular ao inimigo
* Sistema anti-wall para evitar colisões ou ficar preso em cantos

## 📚 Aprendizado Contínuo

O robô salva e carrega estatísticas por oponente, acumulando conhecimento ao longo das batalhas.

## 🛠 Estrutura do Projeto

* **LittleBoy.java** – lógica principal de movimento, radar e eventos
* **MicroWave (classe interna)** – sistema de ondas e registro dos guess factors

## 👥 Equipe

| Nome                     | E-mail                                                              |
| ------------------------ | ------------------------------------------------------------------- |
| Aline Barbosa Antuarte   | [aline.ba@aluno.ifsc.edu.br](mailto:aline.ba@aluno.ifsc.edu.br)     |
| Thaissa Cintra de Mattos | [thaissa.c@aluno.ifsc.edu.br](mailto:thaissa.c@aluno.ifsc.edu.br)   |
| Thalia de Lara Barbosa   | [thalia.b13@aluno.ifsc.edu.br](mailto:thalia.b13@aluno.ifsc.edu.br) |

## 💻 Contexto Acadêmico

Desenvolvido no curso de **Análise e Desenvolvimento de Sistemas (ADS)**, na disciplina de **Introdução à Computação**, sob orientação do professor **Diego**.

## 🚀 Como Usar

1. Clone o repositório
2. Copie o pacote `NewBoy` para `Robocode/robots`
3. Compile (IDE do Robocode faz automaticamente)
4. Abra o Robocode e adicione `NewBoy.LittleBoy` na batalha

## 🤝 Contribuições

Pull requests e sugestões são bem-vindas, principalmente para melhorias em:

* Segmentação da mira
* Movimento evasivo
* Otimizações de performance

---

🔥 *LittleBoy — Inteligência, estratégia e precisão no campo de batalha Robocode!*
