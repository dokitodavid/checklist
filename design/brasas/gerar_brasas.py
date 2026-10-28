"""Gera os recursos das brasas a partir das spritesheets em design/brasas/.

Cada spritesheet tem 24 quadros de cintilação (ver animacao.json), ampliados 4×.
Este script:
1. reduz os quadros para o tamanho real da arte e recorta todos na mesma caixa;
2. salva uma tira horizontal por brasa (ember_*_frames.png), usada pelo app;
3. gera o vetor animado do widget a partir do primeiro quadro (cada pixel vira um quadrado);
4. gera a versão parada do widget.

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
ANIM = json.loads((SRC / "animacao.json").read_text())
UPSCALE = 4  # cada pixel da arte ocupa 4×4 px na spritesheet

# spritesheet → nome do recurso
SPRITES = [
    ("brasa-alongada-spritesheet.png", "ember_long"),
    ("brasa-dupla-spritesheet.png", "ember_double"),
    ("brasa-fragmento-spritesheet.png", "ember_shard"),
]


def load_frames(path):
    """Devolve os quadros reduzidos, todos recortados na mesma caixa."""
    sheet = Image.open(path).convert("RGBA")
    fw, fh, cols = ANIM["frame_width"], ANIM["frame_height"], ANIM["spritesheet_columns"]
    frames = []
    for i in range(ANIM["frames"]):
        x, y = (i % cols) * fw, (i // cols) * fh
        f = sheet.crop((x, y, x + fw, y + fh))
        frames.append(f.resize((fw // UPSCALE, fh // UPSCALE), Image.NEAREST))
    boxes = [f.getbbox() for f in frames]
    box = (min(b[0] for b in boxes), min(b[1] for b in boxes), max(b[2] for b in boxes), max(b[3] for b in boxes))
    return [f.crop(box) for f in frames]


def color_paths(img, colors=12):
    """Agrupa os pixels por cor (paleta reduzida) e devolve {cor: pathData}."""
    rgb = img.convert("RGB").quantize(colors=colors, method=Image.Quantize.MEDIANCUT)
    pal = rgb.getpalette()
    idx = np.asarray(rgb)
    alpha = np.asarray(img)[..., 3]
    paths = {}
    for y in range(img.height):
        x = 0
        while x < img.width:
            if alpha[y, x] == 0:
                x += 1
                continue
            c = idx[y, x]
            x1 = x
            while x1 + 1 < img.width and alpha[y, x1 + 1] and idx[y, x1 + 1] == c:
                x1 += 1
            hexc = "#%02X%02X%02X" % tuple(pal[c * 3:c * 3 + 3])
            paths.setdefault(hexc, []).append(f"M{x},{y}h{x1 - x + 1}v1h-{x1 - x + 1}z")
            x = x1 + 1
    return {c: "".join(p) for c, p in paths.items()}


# Partículas do widget: (sprite, x central, escala, inclinação, duração ms, atraso ms)
# Viewport 320×180 ≈ dp do widget.
WIDGET = [
    (0, 30, 0.42, -6, 13000, 0),
    (2, 78, 0.40, 10, 11000, 5200),
    (1, 126, 0.45, 0, 12000, 2300),
    (0, 172, 0.36, 8, 14500, 8400),
    (2, 218, 0.34, -14, 10500, 3700),
    (1, 262, 0.40, 6, 12500, 9900),
    (0, 302, 0.38, -10, 11500, 6100),
]
VW, VH = 320, 180
MAX_ALPHA = 0.6


def particle_group(i, sprite, cx, cy, k, tilt, paths, w, h, alpha):
    x0, y0 = cx - w * k / 2, cy - h * k / 2
    inner = "\n".join(
        f'                        <path android:name="e{i}p{j}" android:fillColor="{c}"'
        f' android:fillAlpha="{alpha}" android:pathData="{d}" />'
        for j, (c, d) in enumerate(paths.items())
    )
    return f'''        <group android:name="rise{i}">
            <group android:name="sway{i}">
                <group android:name="spin{i}" android:pivotX="{cx:.1f}" android:pivotY="{cy:.1f}" android:rotation="{tilt}">
                    <group android:translateX="{x0:.2f}" android:translateY="{y0:.2f}" android:scaleX="{k}" android:scaleY="{k}">
{inner}
                    </group>
                </group>
            </group>
        </group>'''


def main():
    sprites = []
    for src, name in SPRITES:
        frames = load_frames(SRC / src)
        w, h = frames[0].size
        strip = Image.new("RGBA", (w * len(frames), h))
        for i, f in enumerate(frames):
            strip.paste(f, (i * w, 0))
        strip.save(RES / "drawable-nodpi" / f"{name}_frames.png", optimize=True)
        sprites.append((frames[0], color_paths(frames[0])))
        print(f"{name}_frames.png  {len(frames)} quadros de {w}x{h}")

    groups, targets = [], []
    rep = 'android:repeatCount="infinite" android:repeatMode="restart"'
    for i, (s, cx, k, tilt, dur, delay) in enumerate(WIDGET):
        img, paths = sprites[s]
        h = img.height * k
        groups.append(particle_group(i, s, cx, VH + h, k, tilt, paths, img.width, img.height, 0))
        targets.append(f'''    <target android:name="rise{i}">
        <aapt:attr name="android:animation">
            <objectAnimator android:propertyName="translateY" android:valueType="floatType"
                android:valueFrom="0" android:valueTo="-{VH + 2 * h + 8:.1f}" android:duration="{dur}"
                android:startOffset="{delay}" {rep} android:interpolator="@android:interpolator/linear" />
        </aapt:attr>
    </target>
    <target android:name="sway{i}">
        <aapt:attr name="android:animation">
            <objectAnimator android:propertyName="translateX" android:valueType="floatType"
                android:valueFrom="-{4 + i % 3}" android:valueTo="{4 + i % 3}" android:duration="{2600 + i * 350}"
                android:repeatCount="infinite" android:repeatMode="reverse"
                android:interpolator="@android:interpolator/accelerate_decelerate" />
        </aapt:attr>
    </target>
    <target android:name="spin{i}">
        <aapt:attr name="android:animation">
            <objectAnimator android:propertyName="rotation" android:valueType="floatType"
                android:valueFrom="{tilt - 12}" android:valueTo="{tilt + 12}" android:duration="{3400 + i * 420}"
                android:repeatCount="infinite" android:repeatMode="reverse"
                android:interpolator="@android:interpolator/accelerate_decelerate" />
        </aapt:attr>
    </target>''')
        for j in range(len(paths)):
            targets.append(f'''    <target android:name="e{i}p{j}">
        <aapt:attr name="android:animation">
            <objectAnimator android:duration="{dur}" android:startOffset="{delay}" {rep}
                android:interpolator="@android:interpolator/linear">
                <propertyValuesHolder android:propertyName="fillAlpha" android:valueType="floatType">
                    <keyframe android:fraction="0" android:value="0" />
                    <keyframe android:fraction="0.1" android:value="{MAX_ALPHA}" />
                    <keyframe android:fraction="0.4" android:value="{MAX_ALPHA}" />
                    <keyframe android:fraction="0.7" android:value="{MAX_ALPHA * 0.25:.2f}" />
                    <keyframe android:fraction="1" android:value="0" />
                </propertyValuesHolder>
            </objectAnimator>
        </aapt:attr>
    </target>''')

    header = "<!-- Gerado por design/brasas/gerar_brasas.py. Não edite à mão. -->"
    (RES / "drawable" / "embers_animated.xml").write_text(f'''<?xml version="1.0" encoding="utf-8"?>
{header}
<!-- Brasas do widget. O launcher anima este vetor dentro de um ProgressBar (layout/widget_embers.xml). -->
<animated-vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt">
    <aapt:attr name="android:drawable">
        <vector android:width="{VW}dp" android:height="{VH}dp"
            android:viewportWidth="{VW}" android:viewportHeight="{VH}">
{chr(10).join(groups)}
        </vector>
    </aapt:attr>
{chr(10).join(targets)}
</animated-vector>
''')

    # Versão parada: as mesmas brasas em posições fixas
    still = []
    for i, (s, cx, k, tilt, dur, delay) in enumerate(WIDGET[::2]):
        img, paths = sprites[s]
        cy = VH * (0.25 + 0.5 * ((i * 0.37) % 1))
        still.append(particle_group(i, s, cx, cy, k, tilt, paths, img.width, img.height, MAX_ALPHA * 0.8))
    (RES / "drawable" / "embers_still.xml").write_text(f'''<?xml version="1.0" encoding="utf-8"?>
{header}
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="{VW}dp" android:height="{VH}dp"
    android:viewportWidth="{VW}" android:viewportHeight="{VH}">
{chr(10).join(still)}
</vector>
''')


if __name__ == "__main__":
    main()
