# 1. Configuração (primeira vez)
```bash
git config --global user.name "AlineAntuarte" //Use seu usuário
git config --global user.email "aline.ba@aluno.ifsc.edu.br" // Use seu email
```

## 1.1 Configuração para não repetir senha
```bash
git config --global credential.helper cache
git config --global credential.helper 'cache --timeout=3600'
```
> Neste exemplo, as credenciais seriam armazenadas por 1 hora (3600 segundos).


# 2. Criar ou clonar repositório
```bash
git init                   # inicia um repositório local
git clone <url-do-repo>    # clona um repositório existente
```


# 3. Trabalhar no código
```bash
git status                 # ver alterações feitas
git diff                   # ver detalhes das alterações
```


# 4. Preparar (stage) e confirmar (commit)
```bash
git add <arquivo>          # adiciona arquivo específico
git add .                  # adiciona todos os arquivos modificados
git commit -m "mensagem clara"   # registra alterações com mensagem
```


# 5. Sincronizar com o remoto
```bash
git pull origin main       # busca e mescla alterações do remoto
git push -u origin main    # envia alterações e define seguimento
git push origin main       # nos usos seguintes é só `git push`
```


# 6. Trabalhar com branches
```bash
git branch                         # mostra branches locais
git checkout -b nome-branch        # cria e muda para nova branch
git checkout nome-branch           # muda para uma branch existente
git merge nome-branch              # mescla outra branch na branch atual
git branch -d nome-branch          # apaga a branch local quando encerrada
git push origin --delete nome-branch  # apaga a branch no GitHub
```


# 7. Desfazer ou guardar mudanças
```bash
git reset HEAD <arquivo>           # tira do stage
git checkout -- <arquivo>          # descarta alterações não comitadas
git reset --hard                   # retorna tudo ao último commit
git revert <hash>                  # desfaz um commit criando um novo
git stash                          # guarda alterações temporariamente
git stash pop                      # recupera alterações guardadas
```

Claro, aqui está a seção completa, formatada em Markdown para ser copiada e colada diretamente em um arquivo `.md`.

-----

````md
# 8. Fluxo de Trabalho com Pull Request (PR) e Merge

Este é um fluxo completo para trabalhar em uma funcionalidade específica (como `Movimentos`, `Radar` ou `Ataque`) em uma branch separada e depois integrá-la à branch principal (`main`) de forma segura.

### Passo 8.1: Iniciar o trabalho em uma nova branch

Sempre comece a partir da branch `main` atualizada.

```bash
# 1. Mude para a branch principal
git checkout main

# 2. Garanta que ela está atualizada com o repositório remoto
git pull origin main

# 3. Crie sua nova branch e já mude para ela.
# (Use 'Movimentos', 'Radar' ou 'Ataque' como nome-da-feature)
git checkout -b Movimentos
````

### Passo 8.2: Desenvolver e fazer o commit

Agora você está na branch `Movimentos`. Trabalhe no seu código normalmente, adicionando e comitando as alterações.

```bash
# Adicione os arquivos modificados
git add .

# Crie um commit com uma mensagem clara sobre o que foi feito
git commit -m "Implementa lógica de movimentação do robô"
```

### Passo 8.3: Enviar sua branch para o GitHub

Envie sua nova branch para o repositório remoto (`origin`).

```bash
# A opção '-u' faz com que sua branch local "siga" a remota.
# Na primeira vez que enviar, use esta opção.
git push -u origin Movimentos
```

### Passo 8.4: Abrir o Pull Request (PR) no GitHub

1.  Vá para a página do seu repositório no GitHub.
2.  O GitHub mostrará um aviso amarelo com um botão **"Compare & pull request"** para a branch que você acabou de enviar. Clique nele.
3.  Se o aviso não aparecer, vá na aba **"Pull requests"** e clique em **"New pull request"**.
4.  Selecione a `base: main` (onde você quer juntar o código) e `compare: Movimentos` (de onde vem o código).
5.  Dê um título claro para o seu PR (ex: "Adiciona Movimentação") e uma descrição, se necessário.
6.  Clique em **"Create pull request"**.

### Passo 8.5: Fazer o Merge do Pull Request

Depois que o PR for criado (e opcionalmente revisado), você pode mesclar as alterações na branch `main`.

1.  Na página do Pull Request, você verá um botão verde **"Merge pull request"**.
2.  Clique nele e, em seguida, em **"Confirm merge"**.
3.  **Opcional, mas recomendado:** Após o merge, o GitHub oferecerá um botão **"Delete branch"** para apagar a branch `Movimentos` do reposit-ório remoto. É uma boa prática fazer isso para manter o repositório limpo.

### Passo 8.6: Limpeza e Sincronização Local

Após o merge, sua branch local `Movimentos` não é mais necessária. Volte para a `main`, atualize-a e apague a branch local.

```bash
# 1. Volte para a branch principal
git checkout main

# 2. Atualize sua 'main' local com o código que acabou de ser mergeado no GitHub
git pull origin main

# 3. Apague a branch local que não será mais usada
git branch -d Movimentos
```

> **Pronto!** O processo é o mesmo para as branches `Radar` e `Ataque`. Basta repetir os passos substituindo o nome da branch.

```
```