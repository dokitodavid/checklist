# Checklist · Fogueira

Identidade original em pixel art, inspirada na atmosfera de exploração e descanso
junto à fogueira de jogos soulslike. O nome do app e os comandos continuam em português.

## Padrão visual

| Uso | Cor |
| --- | --- |
| Fundo carvão | `#101211` |
| Painéis de pedra | `#1A1E1B` |
| Superfície elevada | `#242A25` |
| Contornos | `#454B40` |
| Destaque em brasa | `#E7AD62` |
| Luz da fogueira | `#F3D49A` |
| Texto principal | `#E7E5D8` |
| Texto secundário | `#A8B09F` |
| Progresso e conclusão | `#A6BD8D` |

Use contornos de 1dp, cantos cortados de 4–10dp e espaçamento em múltiplos de 4dp.
Títulos e pequenos rótulos usam a fonte monoespaçada do sistema; tarefas e textos
longos usam a fonte padrão para facilitar a leitura. Não há fontes ou imagens remotas.
A paleta é fixa, inclusive no widget, para manter a identidade em qualquer wallpaper.

## Ícones

- `checklist-icon.svg` e `checklist-icon.png`: símbolo em fundo carvão para apresentação.
- `favicon.svg` e `favicon.ico`: versão compacta para uma futura interface web.
- O app nativo usa `ic_launcher_foreground.xml` em um ícone adaptativo Android 8+.
- Android 13+ usa `ic_launcher_monochrome.xml` quando ícones temáticos estão ativos.
- Estrela, lixeira, confirmação e adição são desenhados em uma grade de 7×7 pixels.
- A fogueira usa uma grade de 16×20 pixels e mantém uma margem segura no launcher.

Para uma versão web, copie os favicons para a raiz pública e use:

```html
<link rel="icon" href="/favicon.svg" type="image/svg+xml">
<link rel="alternate icon" href="/favicon.ico">
```

## Movimento

A chama alterna quatro poses em um ciclo de 960ms; uma única brasa sobe em passos.
O desenho Canvas lê o relógio na fase de desenho, evitando recompor a interface a
cada frame. As outras transições usam 180ms para o selo de conclusão, 240ms para a
cor do cartão e 400ms para o progresso. A lista mantém a animação de inclusão,
remoção e mudança de posição do Compose. As animações seguem a escala de duração
do Android; não há flashes, som ou vibração. O ícone do launcher e o widget são estáticos.

## Uso e verificação

As mudanças visuais mantêm o armazenamento local e as ações de adicionar, editar,
concluir, favoritar, excluir, desfazer e filtrar. Os controles de ação têm área de
toque de 48dp e descrição para leitores de tela; filtros permitem rolagem horizontal.
O estado vazio pode rolar em telas pequenas ou quando o teclado está aberto.

Para compilar e analisar:

```bash
./gradlew assembleRelease lintRelease
```

A validação em dispositivo deve cobrir o teclado, fonte ampliada, escala de animação
zero, restauração de tarefa excluída, atualização entre widget e app e recorte do
ícone nos launchers circular e quadrado. Não foi executado em emulador ou celular
neste ambiente.

`assembleRelease` e `lintRelease` passaram. O lint conserva oito avisos sobre
atualizações de dependências, atributos do widget, qualificador de recursos e uma
sugestão de KTX no armazenamento. Não há erros de lint.
