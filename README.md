# LittleBoy / EnhancedLittleBoy 🤖

Bem-vindo ao nosso robocode **LittleBoy**!  

<img src="https://github.com/user-attachments/assets/5a998993-ad8b-4549-b1c0-ebf556491d3a" width="50%" alt="LittleBoy">

Este projeto está sendo desenvolvido por nós três:

- **Thalia de Lara Barbosa** – <larathalia003@gmail.com>  
- **Aline Barbosa Antuarte** – <aline.ba@aluno.ifsc.edu.br>
- **Thaissa Cintra de Mattos** – <thaissa.c@aluno.ifsc.edu.br>

Tudo isso no nosso curso de **ADS**, na matéria de **Introdução à Computação**, com o professor Diego.  
Estamos criando o nosso **LittleBoy** e sua versão turbinada **EnhancedLittleBoy**! 🚀

---

## 🔹 Estrutura de Arquivos / Classes

| Arquivo / Classe           | Função Principal                                                                                     | Observações                                                                                  |
|----------------------------|----------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------|
| LittleBoy.java             | Robô principal, controla radar, mira, movimento e Wave Surfing                                     | Integra todos os módulos: GunController, MovementController, RadarController, DangerMapManager |
| EnhancedLittleBoy.java     | Versão turbinada do LittleBoy 🔥                                                                    | Melhor decisão de direção, mira mais eficiente e previsão linear refinada                   |
| GunController.java         | Mira e atira no inimigo                                                                              | Usa MyWave e DangerMap para aprender padrões de tiro                                         |
| MovementController.java    | Movimentação evasiva e Anti-Gravity Movement                                                       | Recebe dados do DangerMap, EnemyWave e RadarController para calcular rotas seguras         |
| RadarController.java       | Mantém radar travado no inimigo                                                                     | Ajusta varredura de acordo com a distância do alvo                                          |
| EnemyWave.java             | Representa onda de bala do inimigo                                                                  | Usada para Wave Surfing e movimentação segura                                               |
| MyWave.java                | Representa onda de bala do robô                                                                     | Permite analisar acertos e ajustar tiros futuros                                           |
| DangerMapManager.java      | Gerencia mapas de perigo                                                                             | Salva e carrega mapas, dá suporte ao MovementController e Wave Surfing                      |
| DangerMap.java             | Mapa de perigo específico                                                                            | Atualiza valores de risco baseado nos tiros recebidos                                       |

---

## ✨ Funcionalidades Legais

- **Wave Surfing** – esquiva das balas inimigas como um ninja 🥷  
- **Anti-Gravity Movement** – movimentação estratégica para evitar perigo e colisões ⚡  
- **Mira preditiva** – acerta o inimigo mesmo antes dele se mover 🎯  
- **Aprendizado de perigo** – DangerMap aprende onde é mais seguro se mover 📊  
- **EnhancedLittleBoy** – versão mais inteligente, ágil e poderosa 💥  

---

## ⚡ Como Rodar

1. Abra o Robocode.  
2. Copie todas as classes `.java` para a pasta `robots/Paraiso`.  
3. Compile e execute **LittleBoy** ou **EnhancedLittleBoy**.  
4. Veja o robô em ação e divirta-se! 😎  

---

## 📝 Observações

- Arquivos `.class` **não precisam ser versionados**.  
- Cada módulo foi adicionado em um **commit separado**, para manter o histórico organizado.  
- Para futuras melhorias, crie **branches separadas** e abra Pull Requests.  

---

## 🎉 Bom combate

Prepare-se para ver o **LittleBoy** esquivar, mirar e atacar como um verdadeiro ninja do Robocode! ⚔️

## LittleBoy 2.0 – Robocode Bot

## 🔰 Visão Geral

**LittleBoy** é um robô avançado do Robocode projetado para duelos 1x1, combinando técnicas de *Guess Factor Targeting* (GFT) para mira e movimento circular adaptativo para esquiva. Seu foco é antecipar o inimigo e manter posicionamento estratégico com alta evasão.

## 🌟 Estratégia

O robô utiliza lógica estatística e dinâmica para atirar e se movimentar com precisão.

### 🔫 Mira – Guess Factor Targeting

O sistema de mira utiliza **ondas virtuais de tiro** para prever movimentos do inimigo.

- **Ondas (MicroWave):** Cada tiro cria uma onda virtual que registra onde o inimigo estava quando o tiro "chegaria" nele.
- **Armazenamento Estatístico:** Os dados são segmentados por condições de movimento do inimigo:

  - Aceleração lateral
  - Velocidade lateral
  - Proximidade da parede
  - Distância
- **Tomada de Decisão:** Mira no ângulo com maior probabilidade de acerto baseado em dados históricos.

### 🚶 Movimento – Órbita Circular Adaptativa

- Mantém distância ideal do adversário (**BEST_DISTANCE = 525**)
- Se move perpendicular ao inimigo
- Sistema anti-wall para evitar colisões ou ficar preso em cantos

## 📚 Aprendizado Contínuo

O robô salva e carrega estatísticas por oponente, acumulando conhecimento ao longo das batalhas.

## 🛠 Estrutura do Projeto

- **LittleBoy.java** – lógica principal de movimento, radar e eventos
- **MicroWave (classe interna)** – sistema de ondas e registro dos guess factors

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

- Segmentação da mira
- Movimento evasivo
- Otimizações de performance

---

🔥 *LittleBoy — Inteligência, estratégia e precisão no campo de batalha Robocode!*
