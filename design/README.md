# Checklist · Mago assassino

Interface minimalista inspirada nos controles do ChatGPT para celular. A fantasia
sombria fica restrita à ilustração e ao ícone. Referências de arte: Alethe Agents e
Hermes; personagem original gerado para este app, sem reutilizar suas marcas.

## Interface

- Fundo neutro `#212121`, campo e filtros `#303030`.
- Texto `#ECECEC`, texto secundário `#A0A0A0`, contornos `#454545`.
- Fonte sans-serif do sistema, controles arredondados, campo em cápsula e envio circular.
- Tarefas em linhas sem cartões; arrastar para os lados conclui/exclui, segurar e arrastar
  reordena, e editar/favoritar/excluir também ficam no menu ⋮.
- Tarefas importantes ganham uma pequena estrela neutra; a ordem é manual.
- Concluídas têm check cinza, texto riscado e separador de seção no filtro Todas.
- Sem cabeçalho medieval, texto de jornada, painel de progresso ou verde na interface.

## Arte e movimento

`companion.png` (no app, `ashen_companion.png`) é o mago de capuz, adaga e brasa mágica,
com transparência e detalhe em pixel art de alta resolução. `checklist-icon.png` e
`favicon.png` contêm o retrato do capuz. O launcher usa retrato adaptativo e variante
monocromática própria. O mago respira com um movimento discreto de 1 dp.

**Cenários** (`app/src/main/res/drawable-nodpi/`, em WebP, desenhados sem suavização):

- `ruins_hero.webp` — tela principal (Atividade): cenário estendido de ruínas atrás do mago,
  com 65% de opacidade, ocupando a largura da tela (FillWidth, sem esticar). As bordas somem
  em degradê na cor de fundo, e o cenário termina antes dos atalhos Todas/Pendentes/Feitas.
  A capa do mago é assimétrica, então só o mago é deslocado 15% da própria largura para a
  esquerda, deixando capuz e tronco no eixo da tela e do portal da catedral.
- `ruins_tabs.webp` — listas: cenário vertical preso à tela (Crop central), com 30% de
  opacidade; a lista rola na frente. Componente `RuinsBackground` em `Scenery.kt`.
- `ruins_widget_bg.webp` — widget: o mesmo cenário da tela principal, menor e já esmaecido
  (25% sobre o cinza `#212121`), para não aplicar transparência ao widget inteiro. O widget
  não tem personagem nem brasas; mostra o quadro de atividade e o botão de adicionar.

**Onda de conclusão** (`TaskCompletionWave.kt`): ao concluir uma tarefa pendente, uma onda
monocromática de quadradinhos cinza se espalha pela tela em 960 ms. Só dispara na ação de
concluir (checkbox ou arrastar para a direita): não ao abrir o app, reabrir, desfazer ou
navegar. Conclusões seguidas reiniciam a onda. Não recebe toques, não aparece para leitores
de tela, fica fora das barras do sistema e do teclado e respeita a escala de animação do
Android (com animações desligadas, não aparece). Sem som, vibração ou flash.

Todas as imagens são locais. Para uma interface web, copie `favicon.png` para
seus arquivos públicos e use `<link rel="icon" href="/favicon.png" type="image/png">`.

## Compilação e validação

```bash
./gradlew assembleRelease lintRelease
```

Validação em celular/emulador pendente: centralização do mago e recorte do cenário em telas
de larguras diferentes, legibilidade das listas sobre o cenário, teclado, fontes ampliadas,
TalkBack, desfazer, atualização entre app e widget, widget estreito e largo, onda de
conclusão (seguidas e com animações desativadas).
A prévia gerada por imagem é ilustrativa, não uma captura de execução.

Compilação release, lint (sem erros) e assinatura do APK verificados.
