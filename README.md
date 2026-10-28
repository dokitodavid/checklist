<div align="center">

# ✓ Checklist

Lista de tarefas simples e direta para Android.<br>
Kotlin · Jetpack Compose · Material 3

</div>

---

## Funcionalidades

### No app

| | |
|---|---|
| **Adicionar** | Digite no campo de baixo e toque em enviar (ou Enter). A tarefa nova entra no topo. |
| **Concluir** | Toque no checkbox ou **arraste a tarefa para a direita**. Ela fica riscada e vai para *Concluídas*. Arrastar para a direita uma feita reabre. |
| **Excluir** | **Arraste para a esquerda** (ou menu ⋮ → Excluir), com **Desfazer** na barra de baixo por alguns segundos. |
| **Reordenar** | **Segure e arraste** uma pendente para mudar a ordem. A ordem é sua: nada muda de lugar sozinho. |
| **Data e lembrete** | No diálogo de edição: escolha o dia e, se quiser, o horário do lembrete (notificação com botão **Concluir**). A data aparece embaixo do título, em vermelho se estiver atrasada. |
| **Organizar por data** | Ícone de calendário no topo da lista (aparece quando há tarefas com data): põe as pendentes em ordem de data, com Desfazer. |
| **Importante** | Menu ⋮ da tarefa → Marcar importante (aparece uma estrela). |
| **Editar** | Toque no texto da tarefa para abrir o diálogo de edição. |
| **Filtrar** | Chips no topo: *Todas* · *Pendentes* · *Feitas*. |
| **Limpar concluídas** | Menu ⋮ → remove todas as feitas de uma vez (também dá pra desfazer). |
| **Contador** | Abaixo do título: “3 pendentes”, “1 pendente” ou “Tudo em dia”. |
| **Atividade** | Tela inicial: o mago em destaque, atalhos *Todas* · *Pendentes* · *Feitas* (com contagem) que abrem a lista filtrada, botão **Nova tarefa** e quadro no estilo do GitHub com um quadrado por dia (*Uso*, *Criadas* ou *Feitas*), totais do ano e sequência. |

**Ordem da lista:** pendentes primeiro, na ordem que você definir; depois as concluídas.

**Lembretes:** na primeira vez o Android pede permissão para notificações. Os lembretes são
reagendados sozinhos depois de reiniciar o celular ou atualizar o app.

### Widget da tela inicial

- Quadro de atividade no estilo do GitHub (últimas 16 semanas, tarefas criadas + feitas),
  com o dia de hoje contornado e o cenário de ruínas ao fundo.
- Mostra quantas estão pendentes, quantas foram feitas hoje e a sequência de dias.
- Botão **+** abre o app já com o teclado pronto para digitar; tocar no resto abre o app.
- Atualiza sozinho quando algo muda no app (e vice-versa).
- Redimensionável (padrão 3×2).

### Visual

- Interface **clean**, neutra, com controles arredondados e campo em cápsula.
- Lista sem cartões; gestos para concluir, excluir e reordenar, e o resto no menu ⋮.
- Mago assassino original em pixel art, em destaque na Atividade diante de um cenário de ruínas, e no ícone.
- Listas com cenário vertical de ruínas discreto ao fundo; widget com o cenário, sem personagem.
- Ao concluir uma tarefa, uma onda de quadradinhos cinza se espalha pela tela.
- Tema escuro fixo, sem verde, com widget na mesma paleta.
- Artes locais em alta resolução; ícone adaptativo e monocromático para Android 13+.
- [Guia visual e limites de validação](design/README.md); [favicon PNG](design/favicon.png).

### Dados

- Tudo fica salvo **só no celular** (SharedPreferences, em JSON). Sem conta, sem internet.
- O histórico da Atividade (criadas/feitas por dia) é guardado à parte e continua mesmo
  se as tarefas forem excluídas.
- O backup automático do Android (`allowBackup`) pode levar as tarefas para um celular novo.

---

## Instalar no celular

O APK é gerado pelo GitHub Actions — não precisa do Android Studio.

1. Faça push (ou merge) na branch `main`.
2. Aba **Actions** → espere o **Build APK** ficar verde (~3–5 min).
3. Aba **Releases** → baixe o `Checklist.apk` pelo celular e instale
   (o Android pede para permitir “fontes desconhecidas”).

Cada build com sucesso na `main` gera uma release nova (`1.0`, `1.1`, `1.2`, …);
builds que falham não pulam número. Para começar a `2.0`, mude `VERSAO_PRINCIPAL`
em `.github/workflows/build.yml`. Também dá pra rodar manualmente em
**Actions → Build APK → Run workflow**.

Para atualizar o app, baixe o APK da release nova e instale por cima.

**O que mudou:** cada release lista os títulos dos commits desde a anterior.
Commits com `[skip ci]` na mensagem (mudanças só no README, no workflow etc.)
não geram release e ficam fora da lista.

> O APK é sempre assinado com a mesma chave (`app/tarefas.keystore`), então a
> versão nova instala **por cima** da antiga sem perder as tarefas.
> A chave e a senha estão no repositório de propósito — é um app de uso pessoal.
> Se um dia for publicar, troque por uma chave privada guardada em *secrets*.

### Pelo Android Studio

Abra a pasta do projeto, espere o Gradle sincronizar e clique em **Run** com o
celular conectado (depuração USB ativada).

```bash
./gradlew assembleRelease   # APK em app/build/outputs/apk/release/
```

---

## Estrutura

```
app/src/main/java/com/dkt/todo/
├── MainActivity.kt        # Activity; trata o atalho "+" do widget
├── TarefasWidget.kt       # Widget (Glance): quadro de atividade, cenário e botão +
├── Reminders.kt           # Lembretes: alarmes, notificação e reagendar ao reiniciar
├── data/
│   ├── Task.kt            # Modelo da tarefa
│   ├── DayActivity.kt     # Criadas/feitas em um dia
│   └── TaskStore.kt       # Salvar/carregar, histórico diário + Flow de mudanças
└── ui/
    ├── ChecklistApp.kt    # Navegação entre lista e atividade
    ├── SwipeableTask.kt   # Arrastar para os lados: concluir / excluir
    ├── ScreenHeader.kt    # Cabeçalho (título, subtítulo, ações)
    ├── TodoScreen.kt      # Tela principal: lista, filtros, campo, menu
    ├── ActivityScreen.kt  # Atividade: quadro de dias e totais
    ├── TaskRow.kt         # Linha da tarefa
    ├── EditTaskDialog.kt  # Diálogo de edição
    ├── PixelArt.kt        # Personagem e ícones de ação
    ├── Scenery.kt         # Cenários de ruínas (fundo)
    ├── TaskCompletionWave.kt # Onda ao concluir uma tarefa
    └── theme/Theme.kt     # Paleta, tipografia e formas

app/src/main/res/
├── drawable/              # Ícones (app, estrela, +)
├── mipmap-anydpi-v26/     # Ícone adaptativo
└── xml/                   # Configuração do widget

.github/workflows/build.yml  # Compila o APK e publica em Releases
```

## Requisitos

- Android **8.0** (API 26) ou superior
- Para compilar: JDK 17 (o Gradle wrapper baixa o resto)
