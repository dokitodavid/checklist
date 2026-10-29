# Checklist · Mago e Gatinho

As duas aparências compartilham tarefas, navegação, lembretes e histórico no mesmo
APK. A seleção fica em **Configurações → Aparência**, disponível pela engrenagem
da Atividade ou pelo menu ⋮ da lista. A preferência é salva em `AppearanceStore`,
separada dos dados de tarefas; instalações existentes começam no Mago.

## Interface e interação

- Fonte sans-serif do sistema, controles arredondados e campo em cápsula.
- Tarefas em linhas, com checkbox à esquerda e menu ⋮ à direita.
- Concluir/reabrir pelo checkbox; excluir pelo menu, com Desfazer.
- Sem gestos horizontais para concluir ou excluir em nenhum tema.
- Segurar e arrastar reordena as pendentes. Concluídas ficam no fim.
- Configurações usa um grupo de seleção com papel de radio button, rótulos e
  indicação textual “Em uso”; a seleção não depende apenas de cor.
- O app, as barras do sistema e o widget acompanham a aparência salva.

## Tema Mago

Interface neutra e escura, com fantasia na ilustração e no cenário.

| Uso | Cor |
|---|---|
| Fundo | `#212121` |
| Superfícies | `#303030` |
| Texto | `#ECECEC` |
| Texto secundário | `#A0A0A0` |
| Contorno | `#454545` |
| Atividade | `#F2A541` |

`companion.png` (`ashen_companion.png` no app) é o mago de capuz, adaga e brasa,
com transparência e detalhe em pixel art. `checklist-icon.png` e `favicon.png`
contêm o retrato do capuz; o launcher mantém seu ícone adaptativo e monocromático.
Referências originais: Alethe Agents e Hermes, sem reutilizar suas marcas.

Os cenários em `drawable-nodpi/` continuam os originais:

- `ruins_hero.webp`: Atividade, Crop/TopCenter, 65% de opacidade, fades nas bordas.
- `ruins_tabs.webp`: papel de parede fixo nas listas, com 30% de opacidade.
- `ruins_widget_bg.webp`: fundo já esmaecido para o widget, sem personagem, com 85% de
  opacidade (a tela inicial aparece de leve por trás; textos e botões ficam opacos).

O mago tem largura de 196 dp e deslocamento de 15% à esquerda para compensar a capa
assimétrica. A composição da tela inicial mantém o cenário de 300 dp e agora inclui
um cabeçalho com acesso às configurações.

## Tema Gatinho

Um gatinho creme com cachecol rosa, bochechas, cauda enrolada e coração, diante de
uma janela com nuvens e vasinhos de flores.

| Uso | Cor |
|---|---|
| Fundo | `#FFF8F4` |
| Superfícies | `#F6E7ED` |
| Texto | `#503846` |
| Texto secundário | `#7E606F` |
| Contorno | `#E6C9D4` |
| Controles principais | `#9C4566` |
| Atividade / onda | `#B4557B` |

O gatinho é centralizado em uma área de 240 × 240 dp, sem a compensação da capa do
mago. O cenário da Atividade tem 100% de opacidade com fades, e o das listas fica
em 12%, preservando a leitura. O widget recebe uma versão já esmaecida do jardim.

### Arte reproduzível

`generate_kitten_art.py` descreve os desenhos com retângulos de coordenadas inteiras:

- `kitten_companion`: grade de 48 × 48.
- `kitten_garden`: grade de 96 × 96.
- `kitten_widget_bg`: cenário com as cores misturadas a 12% sobre o fundo creme, com 85% de
  opacidade (`android:alpha`), como o fundo do widget do Mago.

O mesmo comando gera os SVGs desta pasta e os VectorDrawables de `res/drawable/`:

```bash
python design/generate_kitten_art.py
```

Sem dependências extras. A arte é local, original e vetorial; não há downloads no
app. O ícone do launcher continua o original nos dois temas.

## Movimento

Os dois companheiros respiram com movimento de 1 dp usando o relógio Compose.
A onda de conclusão (`TaskCompletionWave.kt`) nasce no centro do checkbox e dura
960 ms, em cinza no Mago e rosa no Gatinho.

A origem é capturada em coordenadas da janela antes de a linha mudar de posição e
convertida para a Canvas considerando os insets. Só dispara ao concluir pelo
checkbox, não ao reabrir, trocar tema, restaurar o app ou navegar. Conclusões seguidas
reiniciam a onda. Ela não recebe toques nem aparece para leitores de tela, fica fora
das barras e do teclado e respeita a escala de animação do Android.

## Validação

```bash
./gradlew testDebugUnitTest assembleRelease lintRelease
```

Os testes verificam o tema inicial, persistência ao reabrir, retorno ao Mago,
fallback para preferência desconhecida, leitura do tema pelo Flow e preservação
das preferências de tarefas.

A galeria no README mostra os assets do projeto, não capturas de execução.
Validação em aparelho/emulador continua necessária para os dois temas: recorte e
centralização, teclado, fontes ampliadas, TalkBack, reordenar, Desfazer, lembretes,
widget estreito/largo, troca de tema com widgets existentes e animações desligadas.
