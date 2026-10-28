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
| **Adicionar** | Digite no campo de baixo e toque em enviar (ou Enter). |
| **Concluir** | Toque no checkbox. A tarefa fica riscada e desce para o fim da lista. |
| **Importante** | Toque na estrela ⭐ — tarefas importantes sobem para o topo. |
| **Editar** | Toque no texto da tarefa para abrir o diálogo de edição. |
| **Excluir** | Ícone da lixeira, com **Desfazer** na barra de baixo por alguns segundos. |
| **Filtrar** | Chips no topo: *Todas* · *Pendentes* · *Feitas*. |
| **Limpar concluídas** | Menu ⋮ → remove todas as feitas de uma vez (também dá pra desfazer). |
| **Contador** | Abaixo do título: “3 pendentes”, “1 pendente” ou “Tudo em dia”. |

**Ordem da lista:** pendentes primeiro → importantes no topo → mais recentes antes.

### Widget da tela inicial

- Mostra as tarefas **pendentes**, com as importantes marcadas pela estrela.
- Toque no checkbox para concluir direto do widget, sem abrir o app.
- Botão **+** abre o app já com o teclado pronto para digitar.
- Atualiza sozinho quando algo muda no app (e vice-versa).
- Redimensionável (padrão 3×2).

### Visual

- Tema claro/escuro seguindo o sistema.
- No Android 12+ usa as cores do wallpaper (Material You).

### Dados

- Tudo fica salvo **só no celular** (SharedPreferences, em JSON). Sem conta, sem internet.
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
├── TarefasWidget.kt       # Widget (Glance): lista, checkbox e botão +
├── data/
│   ├── Task.kt            # Modelo da tarefa
│   └── TaskStore.kt       # Salvar/carregar + Flow de mudanças
└── ui/
    ├── TodoScreen.kt      # Tela principal: lista, filtros, campo, menu
    ├── TaskRow.kt         # Linha da tarefa
    ├── EditTaskDialog.kt  # Diálogo de edição
    └── theme/Theme.kt     # Tema claro/escuro/dinâmico

app/src/main/res/
├── drawable/              # Ícones (app, estrela, +)
├── mipmap-anydpi-v26/     # Ícone adaptativo
└── xml/                   # Configuração do widget

.github/workflows/build.yml  # Compila o APK e publica em Releases
```

## Requisitos

- Android **8.0** (API 26) ou superior
- Para compilar: JDK 17 (o Gradle wrapper baixa o resto)
