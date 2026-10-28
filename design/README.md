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

`AmbientEmbers.kt` desenha sete pixels âmbar (`#F2A541`, 2–3 dp) que sobem a tela em
24 segundos, balançando de leve e acendendo/apagando, com opacidade máxima de 50%. A
posição é contínua, sem prender os pixels à grade, para o movimento ficar fluido. A camada
não contém controles nem interceptadores de toque; as brasas não são anunciadas por
leitores de tela. O movimento usa o relógio de quadros da tela, e não as animações do
Compose, para continuar mesmo com a escala de duração do Animator zerada (alguns modos de
bateria e desempenho fazem isso). Só param com a economia de energia, verificada ao compor
a camada.

No widget, `AndroidRemoteViews` incorpora um `ProgressBar` indeterminado cujo desenho é
`embers_animated.xml`: uma animação quadro a quadro (300 quadros de 40 ms, ciclo de 12 s)
com os mesmos pixels âmbar do app, gerada por `design/gerar_brasas_widget.py`. O launcher
roda essa animação sozinho, sem serviço, atualização periódica ou rede, e ela não depende
da escala de animação do sistema. Com economia de energia ativa, o widget usa a composição
estática `embers_still.xml`. O widget mostra o quadro de atividade
(desenhado como imagem) com o mago translúcido ao fundo (`companion_widget_bg.png`, com a
transparência já aplicada) e o botão de adicionar em Glance.

Todas as imagens são locais. Para uma interface web, copie `favicon.png` para
seus arquivos públicos e use `<link rel="icon" href="/favicon.png" type="image/png">`.

## Brasas do widget

Para ajustar quantidade, cor, opacidade ou velocidade das brasas do widget, edite e rode:

```bash
python3 design/gerar_brasas_widget.py
```

## Compilação e validação

```bash
./gradlew assembleRelease lintRelease
```

Validação em celular/emulador pendente: legibilidade da arte, teclado, fontes
ampliadas, TalkBack, desfazer, atualização entre app e widget, toque através da
camada de brasas, modo economia, animação desativada e animação do widget no launcher.
A prévia gerada por imagem é ilustrativa, não uma captura de execução.

Compilação release, lint (sem erros) e assinatura do APK verificados.
