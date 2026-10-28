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

`AmbientEmbers.kt` desenha sete brasas pixeladas esparsas em um ciclo de 24 segundos.
São quadrados de 1–2dp em âmbar discreto (`#D4A17B`), com opacidade máxima de 30%.
A camada não contém controles nem interceptadores de toque; as brasas não são
anunciadas por leitores de tela. O relógio Compose respeita a escala de animação
do sistema e o ciclo de vida. O modo de economia é verificado ao compor a camada.

No widget, `AndroidRemoteViews` incorpora um `ViewFlipper` com 16 quadros vetoriais
transparentes de cinco brasas, trocados a cada 1,5 segundo. Não usa serviço,
atualização periódica do widget ou rede para animar. Ao atualizar o widget com
animações desativadas ou economia de energia ativa, escolhe uma composição estática.
A animação depende do suporte e das políticas do launcher; sua execução em um
launcher real ainda precisa ser validada. Os controles de adicionar e concluir
continuam em Glance. O personagem aparece apenas quando não há pendências.

Todas as imagens são locais. Para uma interface web, copie `favicon.png` para
seus arquivos públicos e use `<link rel="icon" href="/favicon.png" type="image/png">`.

## Compilação e validação

```bash
./gradlew assembleRelease lintRelease
```

Validação em celular/emulador pendente: legibilidade da arte, teclado, fontes
ampliadas, TalkBack, desfazer, atualização entre app e widget, toque através da
camada de brasas, modo economia, animação desativada e ViewFlipper no launcher.
A prévia gerada por imagem é ilustrativa, não uma captura de execução.

Compilação release, lint (sem erros) e assinatura do APK verificados.
