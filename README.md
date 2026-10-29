<a name="inicio"></a>
<div align="center">

# ✓ Checklist

### Seu dia, no seu ritmo. Agora com duas companhias.

Um app Android de tarefas com pixel art, histórico de atividade e widget.<br>
Escolha o **Mago** ou o **Gatinho** nas configurações — suas tarefas continuam com você.

[![Build APK](https://github.com/dokitodavid/checklist/actions/workflows/build.yml/badge.svg)](https://github.com/dokitodavid/checklist/actions/workflows/build.yml)
![Android 8.0+](https://img.shields.io/badge/Android-8.0%2B-9C4566?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack_Compose-795666?logo=kotlin&logoColor=white)
![Offline](https://img.shields.io/badge/100%25-offline-566B57)

[**Baixar APK ↓**](https://github.com/dokitodavid/checklist/releases/latest) · [Escolher um tema](#temas) · [Como usar](#como-usar) · [Compilar](#desenvolver)

</div>

---

<a name="temas"></a>

## ✨ Duas aparências, um Checklist

<table>
<tr>
<th align="center" width="50%">🌙 Mago</th>
<th align="center" width="50%">🌸 Gatinho</th>
</tr>
<tr>
<td align="center"><img src="design/companion.png" height="280" alt="Mago de capuz com adaga e brasa em pixel art"></td>
<td align="center"><img src="design/kitten_companion.svg" height="280" alt="Gatinho creme com cachecol rosa e coração em pixel art"></td>
</tr>
<tr>
<td align="center">Ruínas, cinza escuro e atividade em âmbar.<br>A companhia misteriosa da versão original.</td>
<td align="center">Jardim, fundo creme e detalhes rosa.<br>Um gatinho para acompanhar cada pequena conquista.</td>
</tr>
</table>

<p align="center"><sub>Artes reais incluídas no projeto; as imagens acima não são capturas de tela.</sub></p>

**Para trocar:** na tela inicial, toque em **⚙ Configurações → Aparência → Mago ou Gatinho**.
Na lista, o mesmo caminho fica em **⋮ → Configurações**.

A mudança aparece na hora e fica salva ao fechar o app. Cenário, personagem, cores,
efeito de conclusão e widget acompanham a escolha. É **um único APK**: tarefas,
lembretes e histórico são os mesmos nos dois temas. O Mago continua sendo o padrão
nas instalações existentes; o ícone do launcher continua o original.

<details>
<summary><strong>🌙 Explorar a aparência Mago</strong></summary>

- Tema escuro com superfícies neutras e controles arredondados.
- Mago original de capuz, adaga e brasa, com respiração discreta.
- Cenário de ruínas na Atividade e papel de parede discreto nas listas.
- Quadro de atividade em âmbar e onda de pixels cinza ao concluir.

<img src="app/src/main/res/drawable-nodpi/ruins_hero.webp" width="360" alt="Cenário de ruínas do tema Mago">

</details>

<details>
<summary><strong>🌸 Explorar a aparência Gatinho</strong></summary>

- Tema claro em creme, rosa e lavanda, com texto escuro.
- Gatinho de olhos brilhantes, bochechas rosadas, cachecol e coração.
- Jardim com janela, nuvens e flores em pixels inteiros.
- Quadro de atividade e onda de conclusão em rosa; widget na mesma paleta.
- Arte vetorial local, sem downloads e sem suavização entre os pixels.

<img src="design/kitten_garden.svg" width="360" alt="Janela com céu pastel, nuvens e vasinhos de flores em pixel art">

</details>

<a name="como-usar"></a>

## ☑ Do primeiro toque à tarefa feita

| Você quer… | Faça assim |
|---|---|
| **Adicionar** | Toque em **Nova tarefa**, digite e envie. Na lista, use o campo de baixo ou Enter. A tarefa entra no topo. |
| **Concluir ou reabrir** | Toque no **checkbox**. As feitas ficam riscadas, no fim da lista. |
| **Excluir** | Abra o **⋮ da tarefa → Excluir**. Use **Desfazer** na barra inferior se precisar recuperar. |
| **Reordenar** | **Segure e arraste** uma tarefa pendente para definir sua posição. |
| **Editar** | Toque no texto da tarefa ou em **⋮ → Editar**. |
| **Marcar importante** | Use **⋮ → Marcar importante**; uma estrela aparece na linha. |
| **Escolher data e lembrete** | Abra a edição, escolha o dia e, se quiser, o horário. Datas atrasadas ficam em vermelho. |
| **Organizar por data** | Toque no calendário do topo da lista. A ação pode ser desfeita. |
| **Filtrar** | Use **Todas · Pendentes · Feitas** na Atividade ou na lista. |
| **Limpar feitas** | No menu do topo, toque em **Limpar concluídas**; também dá para desfazer. |
| **Trocar o visual** | Abra **Configurações → Aparência** e selecione um dos dois temas. |

**Sem ações ao arrastar para os lados:** concluir e excluir usam os controles acima
nos dois temas. O gesto de segurar e arrastar continua servindo para reordenar.

<details>
<summary><strong>🗓 Ver como funciona a Atividade</strong></summary>

A tela inicial reúne o companheiro escolhido, atalhos com contagem de tarefas,
**Nova tarefa** e um quadro com um quadradinho por dia.

- Alterne entre **Uso**, **Criadas** e **Feitas**.
- Toque em um dia para consultar os detalhes.
- Veja os totais do último ano, as feitas de hoje e a sequência de dias concluindo.
- O histórico continua salvo mesmo quando as tarefas são excluídas.

</details>

<details>
<summary><strong>🏠 Conhecer o widget</strong></summary>

- Quadro das últimas **16 semanas**, com o dia atual contornado.
- Contagem de pendentes, feitas hoje e sequência de dias.
- **+** abre a lista com o teclado pronto para uma nova tarefa.
- Tocar no quadro abre o app.
- Redimensionável, com tamanho inicial **3 × 2**.
- Também pode ficar na **tela de bloqueio**, nos aparelhos em que o Android permite
  widgets ali (ao editar a tela de bloqueio, procure *Checklist* na lista de widgets).
- Atualiza com as tarefas e acompanha o tema escolhido em Configurações.

</details>

<details>
<summary><strong>🔔 Lembretes, dados e privacidade</strong></summary>

Na primeira vez, o Android pede permissão para notificações. Um lembrete permite
concluir a tarefa direto pela notificação, inclusive na tela de bloqueio, onde o
título da tarefa aparece e o botão **Concluir** funciona sem desbloquear. Alarmes são reagendados após reiniciar
o celular ou atualizar o aplicativo; o horário pode variar conforme as permissões
e a economia de bateria do Android.

Tarefas, histórico e preferência de aparência ficam **só no celular**, em
SharedPreferences. Sem conta, servidor ou conexão de internet. O backup automático
do Android pode restaurar esses dados em outro aparelho.

</details>

## 📲 Instalar no celular

1. Abra a [release mais recente](https://github.com/dokitodavid/checklist/releases/latest).
2. Baixe **Checklist.apk** no celular.
3. Abra o arquivo e permita a instalação por essa fonte, se o Android solicitar.
4. Explore a Atividade e escolha sua companhia em **⚙ Configurações**.

Para atualizar, instale o novo APK por cima do anterior. O identificador do app e
a chave de assinatura continuam os mesmos, preservando as tarefas.

<details>
<summary><strong>⚙ Como os APKs são publicados</strong></summary>

Cada push ou merge na `main` executa o workflow [Build APK](https://github.com/dokitodavid/checklist/actions/workflows/build.yml):
testes de preferência, compilação release e lint. Quando passa, publica `Checklist.apk`
em uma nova release (`1.0`, `1.1`, `1.2`…). Também é possível executar em **Actions →
Build APK → Run workflow**.

Builds que falham não pulam números. Para iniciar a série `2.0`, altere
`VERSAO_PRINCIPAL` em `.github/workflows/build.yml`. As notas listam os commits desde
a release anterior; commits com `[skip ci]` não geram release.

A chave `app/tarefas.keystore` é a chave de uso pessoal já utilizada pelo projeto.
Para distribuição pública, use uma chave privada e secrets próprios.

</details>

<a name="desenvolver"></a>

## 🛠 Para quem desenvolve

**Android 8.0+ (API 26)** · **JDK 17** · **Android SDK 35** · **Kotlin + Jetpack Compose + Material 3**

Abra o projeto no Android Studio e execute no aparelho ou emulador. Pelo terminal:

```bash
./gradlew testDebugUnitTest assembleRelease lintRelease
# Windows: .\gradlew.bat testDebugUnitTest assembleRelease lintRelease
# APK: app/build/outputs/apk/release/app-release.apk
```

<details>
<summary><strong>🧩 Navegar pela estrutura do projeto</strong></summary>

```text
app/src/main/java/com/dkt/todo/
├── MainActivity.kt           # Preferência, barras do sistema e entrada do app
├── TarefasWidget.kt          # Atividade na tela inicial, com o tema salvo
├── Reminders.kt              # Alarmes, notificações e botão Concluir
├── data/
│   ├── AppearanceStore.kt    # Mago/Gatinho: preferência persistida e Flow
│   ├── Task.kt               # Modelo da tarefa
│   ├── DayActivity.kt        # Criadas/feitas em um dia
│   └── TaskStore.kt          # Tarefas e histórico local
└── ui/
    ├── ChecklistApp.kt       # Navegação: Atividade, lista e configurações
    ├── SettingsScreen.kt     # Seletor acessível de aparência
    ├── ActivityScreen.kt     # Companheiro, atalhos e quadro de atividade
    ├── TodoScreen.kt         # Lista, filtros, adicionar e reordenar
    ├── TaskRow.kt            # Checkbox, edição e menu da tarefa
    ├── EditTaskDialog.kt     # Texto, data e horário do lembrete
    ├── PixelArt.kt           # Companheiro de cada tema e ícones
    ├── Scenery.kt            # Cenários de cada tema
    ├── TaskCompletionWave.kt # Onda de pixels ao concluir pelo checkbox
    └── theme/Theme.kt        # Paletas compartilhadas pelo app e widget

app/src/test/                # Testes de persistência da aparência
design/                      # Artes, gerador de pixel art e guia visual
.github/workflows/build.yml  # Testa, compila, verifica e publica o APK
```

As artes do gatinho são reproduzíveis sem bibliotecas extras:

```bash
python design/generate_kitten_art.py
```

O comando gera os mesmos retângulos em **SVG**, para documentação, e
**VectorDrawable**, para o app. Veja o [guia visual](design/README.md).

</details>

<div align="center">

[↑ Voltar ao topo](#inicio) · [Baixar o app](https://github.com/dokitodavid/checklist/releases/latest) · [Ver o código](app/src/main/java/com/dkt/todo)

</div>
