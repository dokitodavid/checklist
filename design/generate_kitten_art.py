"""Pixel art original em retângulos inteiros; gera SVGs e VectorDrawables idênticos.

Execute da raiz: python design/generate_kitten_art.py
Sem dependências externas, imagens remotas ou suavização de pixels.
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DRAWABLE = ROOT / 'app/src/main/res/drawable'
DESIGN = ROOT / 'design'


def rect(shapes, color, x, y, w, h):
    shapes.append((color, x, y, w, h))


def export(name, size, shapes):
    paths = '\n'.join(
        f'    <path android:fillColor="{c}" android:pathData="M{x},{y}h{w}v{h}h-{w}z" />'
        for c, x, y, w, h in shapes
    )
    (DRAWABLE / f'{name}.xml').write_text(
        f'<vector xmlns:android="http://schemas.android.com/apk/res/android" '
        f'android:width="{size}dp" android:height="{size}dp" '
        f'android:viewportWidth="{size}" android:viewportHeight="{size}">\n{paths}\n</vector>\n', encoding='utf-8')
    rects = ''.join(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="{c}"/>'
                    for c, x, y, w, h in shapes)
    (DESIGN / f'{name}.svg').write_text(
        f'<svg xmlns="http://www.w3.org/2000/svg" width="480" height="480" '
        f'viewBox="0 0 {size} {size}" shape-rendering="crispEdges" role="img" '
        f'aria-label="Pixel art original do tema Gatinho">{rects}</svg>\n', encoding='utf-8')


cat = []
r = lambda c, x, y, w, h: rect(cat, c, x, y, w, h)
outline, cream, shade, pink, dark = '#795666', '#FFEBD0', '#EAC3A8', '#D98DA7', '#503846'
# Sombra e cauda enrolada.
r('#E6CED8', 9, 42, 31, 2)
r(outline, 32, 27, 7, 14); r(outline, 37, 25, 5, 10)
r(shade, 34, 28, 4, 11); r(cream, 38, 27, 2, 6)
# Corpo sentado, patinhas e peito claro.
r(outline, 14, 24, 19, 18); r(outline, 12, 34, 23, 8)
r(shade, 15, 25, 17, 15); r(cream, 14, 35, 19, 5)
r('#FFF6E7', 19, 28, 9, 12)
r(outline, 20, 39, 1, 3); r(outline, 27, 39, 1, 3)
r(shade, 15, 39, 3, 1); r(shade, 29, 39, 3, 1)
# Cabeça arredondada em degraus e orelhas.
r(outline, 11, 5, 5, 12); r(outline, 16, 8, 3, 8)
r(outline, 30, 5, 5, 12); r(outline, 27, 8, 3, 8)
r(outline, 12, 12, 23, 14); r(outline, 9, 15, 29, 8)
r(outline, 15, 24, 17, 4)
r(cream, 13, 7, 2, 9); r(cream, 31, 7, 2, 9)
r(pink, 13, 10, 3, 6); r(pink, 30, 10, 3, 6)
r(cream, 13, 14, 21, 11); r(cream, 11, 17, 25, 5)
r('#FFF6E7', 16, 14, 15, 10)
r(shade, 20, 13, 2, 3); r(shade, 25, 13, 2, 3)
# Olhos com brilho, bochechas e focinho.
r(dark, 16, 18, 2, 3); r(dark, 29, 18, 2, 3)
r('#FFFFFF', 16, 18, 1, 1); r('#FFFFFF', 29, 18, 1, 1)
r('#F2A5AD', 12, 21, 4, 2); r('#F2A5AD', 31, 21, 4, 2)
r(pink, 22, 21, 3, 1); r(dark, 23, 22, 1, 2)
r(dark, 21, 24, 2, 1); r(dark, 24, 24, 2, 1)
# Cachecol, ponta e pequenos pontos de luz.
r('#B65C80', 15, 26, 18, 3); r(pink, 15, 26, 17, 2)
r('#B65C80', 29, 28, 4, 7); r(pink, 29, 28, 3, 6)
r('#FFE4ED', 30, 30, 1, 2)
# Coração flutuando junto da orelha.
r(pink, 38, 10, 3, 2); r(pink, 42, 10, 3, 2)
r(pink, 37, 12, 9, 2); r(pink, 38, 14, 7, 2)
r(pink, 40, 16, 3, 2); r(pink, 41, 18, 1, 1)
r('#FFD9E5', 39, 11, 1, 2)
r('#CCA965', 5, 23, 1, 5); r('#CCA965', 3, 25, 5, 1)
r('#CCA965', 39, 39, 1, 3); r('#CCA965', 38, 40, 3, 1)
export('kitten_companion', 48, cat)

garden = []
g = lambda c, x, y, w, h: rect(garden, c, x, y, w, h)
g('#FFF1EB', 0, 0, 96, 96)
# Janela de céu azul-claro, moldura lavanda e nuvens em degraus.
g('#E5CAD8', 17, 8, 62, 70); g('#FAE3DE', 19, 10, 58, 66)
g('#DDEBE6', 21, 12, 54, 60)
g('#FFF8F1', 28, 24, 19, 4); g('#FFF8F1', 31, 21, 11, 10)
g('#FFF8F1', 55, 36, 17, 4); g('#FFF8F1', 59, 33, 9, 10)
g('#F5DDAA', 60, 18, 7, 7); g('#F5DDAA', 58, 20, 11, 3)
g('#C3D7C7', 21, 62, 54, 10); g('#C3D7C7', 24, 58, 11, 6)
g('#C3D7C7', 59, 56, 13, 9)
g('#FAE3DE', 46, 12, 3, 60); g('#FAE3DE', 21, 44, 54, 3)
g('#D6B6C7', 14, 75, 68, 3); g('#E5CAD8', 12, 78, 72, 4)
g('#F3DED7', 0, 86, 96, 10); g('#E6CCC9', 0, 86, 96, 2)
# Vasos e florzinhas nos cantos, sem competir com o personagem.
for x, y in [(7, 68), (79, 69)]:
    g('#B18E9D', x, y+7, 10, 2); g('#D9A9B9', x+1, y+9, 8, 7)
    g('#7F9D86', x+4, y-8, 2, 15); g('#98B49B', x, y-3, 5, 3)
    g('#98B49B', x+6, y-6, 4, 3)
    g('#DF9AB0', x+2, y-12, 6, 7); g('#DF9AB0', x, y-10, 10, 3)
    g('#FFE2A6', x+4, y-10, 2, 3)
g('#E2C7D3', 5, 16, 2, 8); g('#E2C7D3', 2, 19, 8, 2)
g('#E2C7D3', 86, 48, 2, 6); g('#E2C7D3', 84, 50, 6, 2)
export('kitten_garden', 96, garden)

# Fundo sólido e discreto para RemoteViews/Glance, com a mesma paleta do app.
widget = [('#FFF8F4', 0, 0, 96, 96)]
for c, x, y, w, h in garden[1:]:
    base = (255, 248, 244)
    rgb = tuple(int(c[i:i+2], 16) for i in (1, 3, 5))
    faded = '#' + ''.join(f'{round(a * .12 + b * .88):02X}' for a, b in zip(rgb, base))
    widget.append((faded, x, y, w, h))
export('kitten_widget_bg', 96, widget)

print('Pixel art: gatinho, jardim e fundo do widget gerados em SVG e VectorDrawable.')
