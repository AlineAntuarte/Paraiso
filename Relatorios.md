# Relatórios

> **Aline - 13-10-2025 | 01:30**
>

## Configurar ambiente em casa (Vs Code)

Por natureza o ambiente do IFSC é configurado ara rodar perfeitamente o robocode, em casa a exata mesma pasta de jogo dava erro de importação.
Corrigi indo na pasta "robocode-1.10" e criando uma pasta de configuração ".vscode" com o arquivo "settings.json" com o seguinte conteúdo:

```json
{
    "java.project.sourcePaths": ["robots"],
    "java.project.referencedLibraries": [
        "libs/**/*.jar"
    ]
}
```

Após isso você pode fechar a janela e abrir novamente na pasta "robocode-1.10" que o Vs Code vai reconhecer o projeto Java e permitir compilar e rodar os robôs. Caso não ocorra, tente Ctrl+Shift+P e digite "Java: Clean the Java language server workspace" e dê enter.

> **Aline - 13-10-2025 | 01:51**
>
## Erro de pacote (Paraiso)

O pacote Paraiso não era reconhecido ao tentar iniciar uma batalha, mesmo com o Vs Code configurado. Muito simples, mas como fiquei perdida quis relatar. Apenas precisava "renovar" o arquivo compilado (.class) e ao iniciar batalhas ele reconheceu o pacote :D

> **Aline - 14-10-2025 | 15:50**
>
## Confusão entre repositórios locais e remotos

Supondo que o time tente codar o robocode tanto no PC pessoal quanto no público (IFSC), vou dar uma dica/orientação.
Me faltou atenção ao detalhe na diferença entre ter o pacote robocode completo para ser um ambiente do nosso robô e ter a pasta Paraiso como repositório (onde reside o .git).
**NÃO É A MESMA COISA.** Seguimos os passos:
1. Baixar e instalar robocode
2. modificar o arquivo robocode.sh
3. ir até a pasta /robots
4. DENTRO DELA faremos o git clone, automáticamente o git deve fazer dela um repositório local também criando a pasta .git, caso já não houvesse.
EXEMPLO:
```
aluno: robots$ git clone https://github.com/AlineAntuarte/Paraiso.git
```

5. TODO push e pull deve ser com a linha de comando dentro da pasta PARAISO. Agora já temos o repositório clonado graças ao passo 4, devemos trabalhar DENTRO DELE.
Exemplo:
```
aluno: Paraiso$ git push/pull origin main
aluno: Paraiso$ git push/pull (em casos de branch)
```