# LittleBoy / EnhancedLittleBoy 🤖

Bem-vindo ao nosso robocode **LittleBoy**!  

<img src="https://github.com/user-attachments/assets/5a998993-ad8b-4549-b1c0-ebf556491d3a" alt="LittleBoy" width="300"/>

Este projeto está sendo desenvolvido por nós três:

- **Thalia de Lara Barbosa** – larathalia003@gmail.com  
- **Aline Barbosa Antuarte** – antuartepsn@gmail.com  
- **Thaissa Cintra de Mattos** – thaissa.mcintra@gmail.com  

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

## 🎉 Bom combate!

Prepare-se para ver o **LittleBoy** esquivar, mirar e atacar como um verdadeiro ninja do Robocode! ⚔️
