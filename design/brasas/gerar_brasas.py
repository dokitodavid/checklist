"""Gera os recursos das brasas a partir das artes em design/brasas/.

As artes originais vêm ampliadas (cada pixel da arte ocupa ~17–22 px). Este script:
1. reduz cada uma para a grade original (PNG pequeno, nítido com nearest-neighbor);
2. gera o vetor animado do widget (cada pixel vira um quadrado do vetor);
3. gera a versão parada do widget.

Uso, na raiz do repositório:  python3 design/brasas/gerar_brasas.py
Requer Pillow e NumPy.
"""
import math
from pathlib import Path

import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / "design" / "brasas"
RES = ROOT / "app" / "src" / "main" / "res"

# arte original → (nome do recurso, pixels de tela por pixel da arte)
SPRITES = [
    ("brasa-alongada.png", "ember_long", 17.2),
    ("brasa-dupla.png", "ember_double", 18.9),
    ("brasa-fragmento.png", "ember_shard", 22.1),
]


def grid_offset(profile, step):
    n = len(profile)
    best = (0.0, 0.0)
    for off in np.arange(0, step, 0.25):
        idx = np.round(np.arange(off, n, step)).astype(int)
        idx = idx[idx < n]
        best = max(best, (profile[idx].sum(), off))
    return best[1] + 1  # a borda fica entre o pixel idx e idx+1


def downscale(path, step):
    a = np.asarray(Image.open(path).convert("RGBA")).astype(float)
    ox = grid_offset(np.abs(np.diff(a, axis=1)).sum(axis=(0, 2)), step)
    oy = grid_offset(np.abs(np.diff(a, axis=0)).sum(axis=(1, 2)), step)
    rows, cols = int((a.shape[0] - oy) // step), int((a.shape[1] - ox) // step)
    cells = np.zeros((rows, cols, 4))
    for r in range(rows):
        for c in range(cols):
            # mediana do miolo de cada célula, longe das bordas suavizadas
            y0, y1 = int(oy + (r + 0.3) * step), int(oy + (r + 0.7) * step)
            x0, x1 = int(ox + (c + 0.3) * step), int(ox + (c + 0.7) * step)
            cells[r, c] = np.median(a[y0:y1, x0:x1].reshape(-1, 4), axis=0)
    cells[..., 3] = np.where(cells[..., 3] > 128, 255, 0)
    ys, xs = np.nonzero(cells[..., 3])
    cells = cells[ys.min():ys.max() + 1, xs.min():xs.max() + 1]
    return Image.fromarray(cells.astype(np.uint8), "RGBA")


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
    for src, name, step in SPRITES:
        img = downscale(SRC / src, step)
        img.save(RES / "drawable-nodpi" / f"{name}.png", optimize=True)
        sprites.append((img, color_paths(img)))
        print(f"{name}.png  {img.width}x{img.height}")

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
                    <keyframe android:fraction="0.2" android:value="{MAX_ALPHA}" />
                    <keyframe android:fraction="0.7" android:value="{MAX_ALPHA}" />
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
