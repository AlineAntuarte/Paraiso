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