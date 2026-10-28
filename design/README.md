# Checklist · Mago assassino

Interface minimalista inspirada nos controles do ChatGPT para celular. A fantasia
sombria fica restrita à ilustração e ao ícone. Referências de arte: Alethe Agents e
Hermes; personagem original gerado para este app, sem reutilizar suas marcas.

## Interface

- Fundo neutro `#212121`, campo e filtros `#303030`.
- Texto `#ECECEC`, texto secundário `#A0A0A0`, contornos `#454545`.
- Fonte sans-serif do sistema, controles arredondados, campo em cápsula e envio circular.
- Tarefas em linhas sem cartões; ações de editar, favoritar e excluir ficam no menu ⋮.
- Tarefas importantes conservam uma pequena estrela neutra e prioridade na ordenação.
- Concluídas têm check cinza, texto riscado e separador de seção no filtro Todas.
- Sem cabeçalho medieval, texto de jornada, painel de progresso ou verde na interface.

## Arte e movimento

`companion.png` é o mago de capuz, adaga e brasa mágica, com transparência e detalhe
em pixel art de alta resolução. `checklist-icon.png` e `favicon.png` contêm o retrato
do capuz. O launcher usa retrato adaptativo e variante monocromática própria.
A figura aparece no estado vazio, com movimento discreto de 1dp para sugerir respiração.

`AmbientEmbers.kt` anima sete brasas em pixel art (`ember_*_frames.png`, de
`design/brasas/`). Cada uma cintila em 24 quadros de 80 ms e, por cima disso, sobe a tela
em 14–24 segundos, balança, gira devagar e acende/apaga, com opacidade máxima de 60%. Os pixels são desenhados sem
suavização (`FilterQuality.None`). A camada não contém controles nem interceptadores de
toque; as brasas não são anunciadas por leitores de tela. O relógio Compose respeita a
escala de animação do sistema e o ciclo de vida. O modo de economia é verificado ao
compor a camada.

No widget, `AndroidRemoteViews` incorpora um `ProgressBar` indeterminado cujo desenho é
`embers_animated.xml`, um vetor animado com as mesmas brasas (primeiro quadro de cada arte;
cada pixel vira um quadrado do vetor) que sobem, balançam, giram e acendem/apagam de forma
contínua. A cintilação fica só no app: no vetor exigiria 24 cópias de cada brasa. O launcher roda essa animação sozinho: sem serviço, atualização
periódica ou rede. Com animações desativadas ou economia de energia ativa, o widget usa
a composição estática `embers_still.xml`. O mago fica sempre no cabeçalho do widget.
Os controles de adicionar e concluir continuam em Glance.

Todas as imagens são locais. Para uma interface web, copie `favicon.png` para
seus arquivos públicos e use `<link rel="icon" href="/favicon.png" type="image/png">`.

## Brasas

As spritesheets originais (24 quadros, ampliados 4×), `animacao.json` e prévias em GIF
ficam em `design/brasas/`. Para regenerar as tiras de quadros e os vetores do widget
depois de trocar uma arte ou ajustar as partículas do widget:

```bash
python3 design/brasas/gerar_brasas.py
```

## Compilação e validação

```bash
./gradlew assembleRelease lintRelease
```

Validação em celular/emulador pendente: legibilidade da arte, teclado, fontes
ampliadas, TalkBack, desfazer, atualização entre app e widget, toque através da
camada de brasas, modo economia, animação desativada e ViewFlipper no launcher.
A prévia gerada por imagem é ilustrativa, não uma captura de execução.

Compilação release, lint (sem erros) e assinatura do APK verificados.
