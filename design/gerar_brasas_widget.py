"""Gera a animação das brasas do widget (pixels âmbar subindo, como no app).

O widget não roda código de animação; por isso a animação é quadro a quadro (animation-list)
dentro de um ProgressBar (layout/widget_embers.xml), que o launcher inicia sozinho. Esse tipo
de animação não depende da escala de duração do Animator do sistema.

Uso, na raiz do repositório:  python3 design/gerar_brasas_widget.py
"""
import math
from pathlib import Path

RES = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res" / "drawable"

COLOR = "F2A541"      # AshenColors.Ember
MAX_ALPHA = 0.5
COUNT = 7
W, H = 260, 170       # área de desenho em dp, a partir do canto do widget
FRAME_MS = 40         # 25 quadros por segundo
LOOP_FRAMES = 300     # 12 s para cada pixel atravessar o widget


def items(t, still=False):
    out = []
    for i in range(COUNT):
        p = (t + i / COUNT) % 1
        # Mesma fórmula do app (AmbientEmbers.kt)
        x = W * ((0.12 + i * 0.137 + math.sin(p * 2 * math.pi) * 0.025) % 1)
        y = H * (0.25 + 0.5 * ((i * 0.37) % 1)) if still else H * (1.04 - p * 1.12)
        a = MAX_ALPHA * 0.8 if still else max(0.0, math.sin(p * math.pi)) * MAX_ALPHA
        if a < 0.02:
            continue
        w = 3 if i % 3 == 0 else 2
        out.append(
            f'<item android:left="{x:.1f}dp" android:top="{y:.1f}dp" android:width="{w}dp" android:height="3dp">'
            f'<shape><solid android:color="#{round(a * 255):02X}{COLOR}" /></shape></item>'
        )
    return "".join(out)


def main():
    header = "<!-- Gerado por design/gerar_brasas_widget.py. Não edite à mão. -->"
    ns = 'xmlns:android="http://schemas.android.com/apk/res/android"'
    frames = "\n".join(
        f'    <item android:duration="{FRAME_MS}"><layer-list>{items(f / LOOP_FRAMES)}</layer-list></item>'
        for f in range(LOOP_FRAMES)
    )
    (RES / "embers_animated.xml").write_text(f"""<?xml version="1.0" encoding="utf-8"?>
{header}
<!-- Brasas do widget, quadro a quadro ({LOOP_FRAMES} × {FRAME_MS} ms). -->
<animation-list {ns} android:oneshot="false">
{frames}
</animation-list>
""")
    (RES / "embers_still.xml").write_text(f"""<?xml version="1.0" encoding="utf-8"?>
{header}
<layer-list {ns}>{items(0, still=True)}</layer-list>
""")
    print(f"embers_animated.xml: {LOOP_FRAMES} quadros")


if __name__ == "__main__":
    main()
