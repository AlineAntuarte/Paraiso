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
