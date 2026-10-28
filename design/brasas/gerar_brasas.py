"""Gera os recursos das brasas a partir das spritesheets em design/brasas/.

Cada brasa tem uma spritesheet da chama (ver animacao.json): 64 quadros a 50 FPS, ampliados 2×,
sem deslocamento — a trajetória é feita por código. Este script:
1. reduz os quadros para o tamanho real da arte e recorta todos na mesma caixa;
2. salva uma tira horizontal por brasa (ember_*_frames.png), usada pelo app;
3. salva um quadro sim, outro não, separados (ember_*_NN.png), usados pelo widget a 25 FPS;
4. gera a animação quadro a quadro do widget (embers_animated.xml) e a versão parada.

O widget usa animação quadro a quadro (animation-list) dentro de um ProgressBar porque ela
não depende da escala de duração do Animator do sistema, ao contrário de vetores animados.

Uso, na raiz do repositório:  python3 design/brasas/gerar_brasas.py
Requer Pillow e NumPy.
"""
import json
import math
from pathlib import Path

import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / "design" / "brasas"
RES = ROOT / "app" / "src" / "main" / "res"
ATLAS = json.loads((SRC / "animacao.json").read_text())["local_flame_atlas"]
UPSCALE = 2  # cada pixel da arte ocupa 2×2 px na spritesheet

# spritesheet → nome do recurso (na ordem usada pelo app e pelo widget)
SPRITES = [
    ("01-gota-chama-spritesheet.png", "ember_drop"),
    ("02-fina-chama-spritesheet.png", "ember_thin"),
    ("03-curvada-chama-spritesheet.png", "ember_curved"),
    ("04-bifurcada-chama-spritesheet.png", "ember_forked"),
    ("05-fragmento-chama-spritesheet.png", "ember_shard"),
]
WIDGET_STEP = 2  # o widget usa um quadro sim, outro não (25 FPS)


def load_frames(path):
    """Devolve os quadros reduzidos, todos recortados na mesma caixa."""
    sheet = Image.open(path).convert("RGBA")
    fw, fh, cols = ATLAS["frame_width"], ATLAS["frame_height"], ATLAS["columns"]
    frames = []
    for i in range(ATLAS["frames"]):
        x, y = (i % cols) * fw, (i // cols) * fh
        f = sheet.crop((x, y, x + fw, y + fh))
        frames.append(f.resize((fw // UPSCALE, fh // UPSCALE), Image.NEAREST))
    boxes = [f.getbbox() for f in frames]
    box = (min(b[0] for b in boxes), min(b[1] for b in boxes), max(b[2] for b in boxes), max(b[3] for b in boxes))
    return [f.crop(box) for f in frames]


# Partículas do widget: (sprite, x central em dp, dp por pixel da arte, inclinação, giro,
# defasagem na subida, defasagem na cintilação). Área de desenho: W×H dp a partir do canto.
WIDGET = [
    # Cada pixel da arte com ~0,4 dp
    (0, 24, 0.40, -4, 6, 0.00, 0),
    (2, 68, 0.38, 8, 8, 0.55, 9),
    (1, 110, 0.42, 0, 6, 0.22, 17),
    (3, 152, 0.38, -6, 8, 0.78, 5),
    (4, 196, 0.36, 10, 10, 0.36, 23),
    (0, 238, 0.34, 4, 6, 0.66, 13),
]
W, H = 260, 170
FRAME_MS = 1000 // ATLAS["fps"] * WIDGET_STEP           # 40 ms
FLAME_FRAMES = ATLAS["frames"] // WIDGET_STEP           # 32 quadros de chama no widget
LOOP_FRAMES = 4 * FLAME_FRAMES                           # 128 quadros = 5,12 s por subida
MAX_ALPHA = 0.75


def alpha_at(p):
    """Acende rápido, fica estável e vai se apagando na segunda metade (igual ao app)."""
    if p < 0.1:
        return MAX_ALPHA * p / 0.1
    if p < 0.4:
        return MAX_ALPHA
    return MAX_ALPHA * (1 - (p - 0.4) / 0.6) ** 2


def layer_items(t, sizes, still=False):
    """Itens do layer-list para o instante t (0–1 do ciclo)."""
    items = []
    for i, (s, x, k, tilt, swing, off, flick) in enumerate(WIDGET):
        p = (t + off) % 1
        w, h = sizes[s]
        dw, dh = w * k, h * k
        a = MAX_ALPHA * 0.8 if still else alpha_at(p)
        if a < 0.02:
            continue
        cx = x + math.sin(p * 4 * math.pi) * (4 + i % 3)
        cy = (H * (0.25 + 0.5 * ((i * 0.37) % 1))) if still else (H + dh - p * (H + 2 * dh))
        rot = tilt + math.sin(p * 2 * math.pi) * swing
        frame = (round(t * LOOP_FRAMES) + flick) % FLAME_FRAMES
        name = f"{SPRITES[s][1]}_{frame:02d}"
        items.append(
            f'<item android:left="{cx - dw / 2:.1f}dp" android:top="{cy - dh / 2:.1f}dp"'
            f' android:width="{dw:.1f}dp" android:height="{dh:.1f}dp">'
            f'<rotate android:fromDegrees="{rot:.1f}" android:toDegrees="{rot:.1f}"'
            f' android:pivotX="50%" android:pivotY="50%">'
            f'<bitmap android:src="@drawable/{name}" android:alpha="{a:.2f}" android:filter="false" />'
            f'</rotate></item>'
        )
    return "".join(items)


def main():
    nodpi = RES / "drawable-nodpi"
    for old in nodpi.glob("ember_*.png"):
        old.unlink()
    sizes = []
    for src, name in SPRITES:
        frames = load_frames(SRC / src)
        w, h = frames[0].size
        sizes.append((w, h))
        strip = Image.new("RGBA", (w * len(frames), h))
        for i, f in enumerate(frames):
            strip.paste(f, (i * w, 0))
        for i, f in enumerate(frames[::WIDGET_STEP]):
            f.save(nodpi / f"{name}_{i:02d}.png", optimize=True)
        strip.save(nodpi / f"{name}_frames.png", optimize=True)
        print(f"{name}: {len(frames)} quadros de {w}x{h}")

    header = "<!-- Gerado por design/brasas/gerar_brasas.py. Não edite à mão. -->"
    ns = 'xmlns:android="http://schemas.android.com/apk/res/android"'
    frames_xml = "\n".join(
        f'    <item android:duration="{FRAME_MS}"><layer-list>{layer_items(f / LOOP_FRAMES, sizes)}</layer-list></item>'
        for f in range(LOOP_FRAMES)
    )
    (RES / "drawable" / "embers_animated.xml").write_text(f"""<?xml version="1.0" encoding="utf-8"?>
{header}
<!-- Brasas do widget, quadro a quadro ({LOOP_FRAMES} × {FRAME_MS} ms). Rodam dentro de um ProgressBar
     (layout/widget_embers.xml), que inicia a animação sozinho no launcher. -->
<animation-list {ns} android:oneshot="false">
{frames_xml}
</animation-list>
""")
    (RES / "drawable" / "embers_still.xml").write_text(f"""<?xml version="1.0" encoding="utf-8"?>
{header}
<layer-list {ns}>{layer_items(0, sizes, still=True)}</layer-list>
""")
    print(f"embers_animated.xml: {LOOP_FRAMES} quadros")


if __name__ == "__main__":
    main()
