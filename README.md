# Tarefas

App de lista de tarefas para Android, em Kotlin + Jetpack Compose (Material 3).

## O que faz
- Adicionar tarefas pelo campo de baixo (Enter ou botão de enviar)
- Marcar como feita, marcar como importante (estrela), editar tocando no texto
- Excluir com opção de desfazer
- Filtros: Todas / Pendentes / Feitas, e "Limpar concluídas" no menu
- Importantes ficam no topo; as tarefas são salvas no celular
- Tema claro/escuro com as cores do seu wallpaper (Android 12+)

## Gerar o APK pelo GitHub (sem Android Studio)
1. Crie um repositório novo no GitHub (ex.: `tarefas`).
2. Na pasta do projeto:
   ```bash
   git init
   git add .
   git commit -m "Tarefas v1"
   git branch -M main
   git remote add origin https://github.com/dokitodavid/tarefas.git
   git push -u origin main
   ```
3. Aba **Actions** → espere o "Build APK" ficar verde (~3–5 min).
4. Aba **Releases** → baixe `Tarefas.apk` pelo celular e instale
   (o Android vai pedir para permitir instalação de fontes desconhecidas).

Cada push gera uma release nova. O APK é sempre assinado com a mesma chave
(`app/tarefas.keystore`), então a atualização instala por cima sem perder as tarefas.

## Rodar pelo Android Studio
Abra a pasta do projeto, espere o Gradle sincronizar e dê Run com o celular conectado
(depuração USB ativada).

## Requisitos
Android 8.0 ou superior.
