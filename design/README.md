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

`AmbientEmbers.kt` anima sete brasas em pixel art, de cinco tipos (gota, fina, curvada,
bifurcada e fragmento; `ember_*_frames.png`, de `design/brasas/`). A chama de cada uma
roda em 64 quadros a 50 FPS e, por cima disso, a brasa sobe a tela em 14–24 segundos,
balança e gira devagar. Acende rápido ao entrar (máximo de 75%) e vai perdendo
opacidade aos poucos na segunda metade da subida, até sumir. Os pixels são desenhados sem
suavização (`FilterQuality.None`). A camada não contém controles nem interceptadores de
toque; as brasas não são anunciadas por leitores de tela. O movimento usa o relógio de
quadros da tela, e não as animações do Compose, para continuar mesmo com a escala de
duração do Animator zerada (alguns modos de bateria e desempenho fazem isso). Só param
com a economia de energia, verificada ao compor a camada.

No widget, `AndroidRemoteViews` incorpora um `ProgressBar` indeterminado cujo desenho é
`embers_animated.xml`: uma animação quadro a quadro (128 quadros de 40 ms, ciclo de 5,12 s)
em que cada quadro posiciona seis brasas, com rotação, opacidade e o quadro da chama de
cada uma (um sim, outro não: 25 FPS, para não pesar no launcher). O launcher roda essa animação sozinho, sem serviço, atualização periódica ou
rede, e ela não depende da escala de animação do sistema. Com economia de energia ativa,
o widget usa a composição estática `embers_still.xml`. O mago fica sempre no cabeçalho do
widget. Os controles de adicionar e concluir continuam em Glance.

Todas as imagens são locais. Para uma interface web, copie `favicon.png` para
seus arquivos públicos e use `<link rel="icon" href="/favicon.png" type="image/png">`.

## Brasas

As spritesheets das chamas (64 quadros cada, ampliados 2×), `animacao.json` e o LEIA-ME
ficam em `design/brasas/`. Para regenerar os quadros e a animação do widget depois de
trocar uma arte ou ajustar as partículas do widget:

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
