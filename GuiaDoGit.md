
# Guia Prático de Git e GitHub para Projetos

> ## **[  LEIA A DOCUMENTAÇÃO DO PROJETO !!!  ]**
>
> [!NOTE]
> **Como Visualizar Este Arquivo Corretamente**
>
> Este é um arquivo escrito em **Markdown** (`.md`), uma linguagem de formatação de texto. Para a melhor experiência de leitura, com títulos, links e imagens formatados, é recomendado abri-lo no modo de **Pré-Visualização** (Preview).
>
> ### No Visual Studio Code
>
> * **Usando Atalhos de Teclado (Recomendado):**
>   * **Pré-Visualização Lado a Lado:** Pressione `Ctrl + K` e, em seguida, `V`. (ou `Cmd + K` e `V` no macOS).
>   * **Pré-Visualização em Nova Aba:** Pressione `Ctrl + Shift + V`. (ou `Cmd + Shift + V` no macOS).
>
> * **Usando a Paleta de Comandos:**
>     1. Pressione `Ctrl + Shift + P` para abrir a Paleta de Comandos.
>     2. Digite `Markdown`.
>     3. Selecione a opção **"Markdown: Abrir Pré-Visualização ao Lado"** (*Markdown: Open Preview to the Side*).

---

````md
# Guia Prático de Git e GitHub para Projetos

Este guia está organizado para seguir o fluxo de trabalho real de um projeto, desde a configuração inicial até o ciclo diário de desenvolvimento.
A lógica é: **1. Preparar o ambiente** -\> **2. Iniciar o projeto** -\> **3. O ciclo diário de trabalho** -\> **4. Comandos úteis/avançados para consulta**.

---

## Parte 1: Configuração Inicial (Você só faz isso uma vez)

Antes de tudo, configure seu nome de usuário e e-mail. Isso identificará seus commits.

```bash
# Use seu usuário do GitHub
git config --global user.name "AlineAntuarte"

# Use o mesmo e-mail da sua conta do GitHub
git config --global user.email "aline.ba@aluno.ifsc.edu.br"
````

Opcional, mas muito útil, é configurar o Git para salvar sua senha por um tempo.

```bash
# Salva suas credenciais em cache por 1 hora (3600 segundos)
git config --global credential.helper 'cache --timeout=3600'
```

---

## Parte 2: Começando um Projeto

Você vai começar um projeto de duas formas: clonando um que já existe (mais comum) ou iniciando um do zero.

```bash
# Cenário A: Baixar um projeto que já existe no GitHub
git clone <url-do-repositorio>

# Cenário B: Iniciar um repositório em uma pasta local que ainda não tem Git
git init
```

---

## Parte 3: O Ciclo de Trabalho Principal (Seu Dia a Dia)

Este é o fluxo que você repetirá toda vez que for desenvolver uma nova funcionalidade ou corrigir um problema.

### Passo 1: Sincronize seu Repositório

Sempre comece garantindo que seu ambiente local está atualizado com a versão mais recente do projeto no GitHub.

```bash
# Garante que seu Git local conheça todas as branches do remoto
git fetch origin

# Vá para a branch principal
git checkout main

# Atualize sua 'main' local com a versão do GitHub
git pull origin main
```

### Passo 2: Crie ou Mude para sua Branch de Trabalho

Nunca trabalhe diretamente na branch `main`. Crie uma branch separada para sua tarefa.

```bash
# Cenário A: Criando uma branch NOVA para uma nova funcionalidade
git checkout -b Movimentos

# Cenário B: Voltando a trabalhar em uma branch que JÁ EXISTE
git checkout Movimentos
```

### Passo 3: Trabalhe no Código

Agora você está na sua branch. Programe, altere e crie arquivos. A qualquer momento, use estes comandos para ver o que está acontecendo:

```bash
# Veja quais arquivos foram modificados, criados ou deletados
git status

# Veja em detalhes o que foi alterado dentro dos arquivos
git diff
```

### Passo 4: Salve seu Progresso (Stage e Commit)

Quando atingir um ponto estável no seu trabalho, salve as alterações em um "pacote" chamado commit.

```bash
# Adicione todos os arquivos modificados à "área de preparação" (stage)
git add .

# Registre as alterações com uma mensagem clara e descritiva
git commit -m "Implementa movimentação inicial do robô"
```

### Passo 5: Envie suas Alterações para o GitHub

Envie seus commits para a branch remota no GitHub, para que outros possam ver seu trabalho e para ter um backup seguro.

```bash
# Envia os commits da sua branch local para a branch de mesmo nome no GitHub
git push
```

> **Dica:** Se for a primeira vez que você envia essa branch, o Git pode pedir para você usar um comando mais completo: `git push --set-upstream origin Movimentos`. Apenas copie, cole e execute.

### Passo 6: Crie um Pull Request (PR) e Faça o Merge

Quando sua funcionalidade estiver pronta, é hora de integrá-la à branch `main` através de um Pull Request. **Isso é feito no site do GitHub.**

1. **Abra o PR:** Na página do seu repositório no GitHub, um aviso amarelo **"Compare & pull request"** aparecerá. Clique nele.
2. **Descreva:** Dê um bom título e descrição para suas alterações.
3. **Merge:** Após a revisão (se houver), clique no botão verde **"Merge pull request"** para integrar seu código.
4. **Limpeza:** Após o merge, o GitHub dará a opção **"Delete branch"**. É uma boa prática apagar a branch que não será mais usada.

### Passo 7: Sincronize e Limpe Localmente

Após o merge, sua branch de trabalho já cumpriu seu papel. Volte para a `main` e apague a branch localmente.

```bash
# Volte para a branch principal
git checkout main

# Atualize sua 'main' com o código que você acabou de mergear
git pull origin main

# Apague a branch local que não é mais necessária
git branch -d Movimentos
```

E o ciclo recomeça no Passo 1 para a próxima tarefa\!

---

## Parte 4: Comandos Úteis (Para Consulta)

Comandos importantes que você pode precisar em situações específicas.

### Gerenciando Branches

```bash
# Lista todas as branches locais
git branch

# Apaga uma branch no GitHub
git push origin --delete nome-da-branch
```

### Desfazendo Alterações

```bash
# Tira um arquivo da "área de preparação" (stage)
git reset HEAD <arquivo>

# Descarta completamente as alterações em um arquivo (CUIDADO!)
git checkout -- <arquivo>

# Desfaz um commit específico criando um novo commit reverso (SEGURO)
git revert <hash-do-commit>

# Guarda alterações temporariamente para trabalhar em outra coisa
git stash

# Aplica as alterações guardadas e remove da lista
git stash pop
```
