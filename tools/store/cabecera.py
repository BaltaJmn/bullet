#!/usr/bin/env python3
"""Grafico de cabecera de Play (1024x500) en cinco idiomas y el icono de 512 de la ficha (#58).

    python3 tools/store/cabecera.py

El grafico se lee a tamano de sello en la lista de la tienda: el icono, el nombre, una linea y un
trozo de pagina punteada con los simbolos del metodo, que no se traducen. Las entradas de la pagina
son las de la vista previa de Ajustes (`previewTask`, `previewEvent`) y dos mas del mismo tono: nada
que no exista en la app. Las formas las pinta rsvg-convert; el texto, PIL con la Literata de la app,
porque rsvg en macOS va por CoreText y no ve una fuente que no este instalada.
"""
import os
import pathlib
import subprocess
import tempfile

from PIL import Image, ImageDraw, ImageFont

RAIZ = pathlib.Path(__file__).resolve().parents[2]
PAPEL = "#FBF8F3"
TINTA = "#39352E"
GRIS = "#736D63"
PUNTO = "#E3DCD1"
SALVIA = "#B6D6AB"

TEXTOS = {
    "en-US": ("Rapid logging. Migration by hand.", "Wednesday 23",
              [("task", "Buy ink"), ("event", "Dinner with Ana"), ("done", "Water the plants"), ("migrated", "Call the bank")]),
    "es-ES": ("Rapid logging. Migración a mano.", "Miércoles 23",
              [("task", "Comprar tinta"), ("event", "Cena con Ana"), ("done", "Regar las plantas"), ("migrated", "Llamar al banco")]),
    "pt-BR": ("Rapid logging. Migração à mão.", "Quarta-feira, 23",
              [("task", "Comprar tinta"), ("event", "Jantar com a Ana"), ("done", "Regar as plantas"), ("migrated", "Ligar para o banco")]),
    "de-DE": ("Rapid logging. Migration von Hand.", "Mittwoch, 23.",
              [("task", "Tinte kaufen"), ("event", "Abendessen mit Ana"), ("done", "Pflanzen gießen"), ("migrated", "Bank anrufen")]),
    "fr-FR": ("Rapid logging. Migration à la main.", "Mercredi 23",
              [("task", "Acheter de l'encre"), ("event", "Dîner avec Ana"), ("done", "Arroser les plantes"), ("migrated", "Appeler la banque")]),
}


def glifo(kind, x, y, s):
    """The method's glyphs in a 24 unit box (docs/pantallas.md 1.5), top left at x, y, scale s."""
    def p(a, b):
        return "%.1f,%.1f" % (x + a * s, y + b * s)
    trazo = 'stroke="%s" stroke-width="%.1f" stroke-linecap="round" stroke-linejoin="round" fill="none"' % (TINTA, 1.5 * s)
    punto = '<circle cx="%.1f" cy="%.1f" r="%.1f" fill="%s"/>' % (x + 12 * s, y + 12 * s, 2.5 * s, TINTA)
    if kind == "task":
        return punto
    if kind == "done":
        return punto + '<path d="M%s L%s M%s L%s" %s/>' % (p(8, 8), p(16, 16), p(16, 8), p(8, 16), trazo)
    if kind == "event":
        return '<circle cx="%.1f" cy="%.1f" r="%.1f" %s/>' % (x + 12 * s, y + 12 * s, 4 * s, trazo)
    if kind == "migrated":
        return '<path d="M%s L%s L%s" %s/>' % (p(10, 8), p(14.5, 12), p(10, 16), trazo)
    raise ValueError(kind)


def cabecera(linea, dia, entradas):
    """The shapes as SVG, and the text apart as (x, baseline, size, colour, font, text)."""
    u = 44  # the grid unit of the page, as the app's 24dp at a larger scale
    x0, y0 = 560, 60
    col = x0 + 22
    puntos = "".join(
        '<circle cx="%d" cy="%d" r="1.6" fill="%s"/>' % (col + c * u, y0 + 30 + f * u, PUNTO)
        for f in range(9) for c in range(10)
    )
    formas = ['<circle cx="%d" cy="%d" r="7" fill="%s"/>' % (col + u // 2, y0 + 30 + u - 12, SALVIA)]
    textos = [
        (60, 318, 84, TINTA, "ink", "Bobbin"),
        (64, 370, 27, GRIS, "sans", linea),
        (col + u, y0 + 30 + u, 38, TINTA, "ink", dia),
    ]
    base = y0 + 30 + 2 * u
    for i, (kind, texto) in enumerate(entradas):
        y = base + i * u
        formas.append(glifo(kind, col + u - 38, y - 27, 1.5))
        textos.append((col + u, y, 29, GRIS if kind in ("done", "migrated") else TINTA, "ink", texto))
    icono = (RAIZ / "tools" / "icon-master.svg").read_text()
    icono = icono[icono.index(">") + 1:icono.rindex("</svg>")]
    svg = """<svg xmlns="http://www.w3.org/2000/svg" width="1024" height="500" viewBox="0 0 1024 500">
  <defs><clipPath id="c"><rect width="1024" height="1024" rx="224"/></clipPath></defs>
  <rect width="1024" height="500" fill="{papel}"/>
  <g transform="translate(64 96) scale(0.125)" clip-path="url(#c)">{icono}</g>
  <rect x="{px}" y="{py}" width="440" height="{ph}" rx="18" fill="{papel}" stroke="{punto}" stroke-width="2"/>
  {puntos}
  {formas}
</svg>""".format(papel=PAPEL, punto=PUNTO, icono=icono, px=x0, py=y0, ph=9 * u - 16, puntos=puntos,
                 formas="".join(formas))
    return svg, textos


FUENTES = {
    "ink": str(RAIZ / "shared/src/commonMain/composeResources/font/literata_regular.ttf"),
    "sans": "/System/Library/Fonts/HelveticaNeue.ttc",
}


def escribir(png, textos):
    imagen = Image.open(png).convert("RGBA")
    dibujo = ImageDraw.Draw(imagen)
    for x, y, tam, color, fuente, texto in textos:
        ruta = FUENTES[fuente] if os.path.exists(FUENTES[fuente]) else FUENTES["ink"]
        dibujo.text((x, y), texto, font=ImageFont.truetype(ruta, tam), fill=color, anchor="ls")
    imagen.convert("RGB").save(png)


def render(svg, salida, w, h):
    with tempfile.NamedTemporaryFile("w", suffix=".svg", delete=False, encoding="utf-8") as f:
        f.write(svg)
    subprocess.run(["rsvg-convert", "-w", str(w), "-h", str(h), f.name, "-o", str(salida)], check=True)
    os.unlink(f.name)
    print(salida.relative_to(RAIZ))


def main():
    play = RAIZ / "store" / "play"
    otros = RAIZ / "store" / "feature"
    play.mkdir(parents=True, exist_ok=True)
    otros.mkdir(parents=True, exist_ok=True)
    for idioma, (linea, dia, entradas) in TEXTOS.items():
        destino = play / "feature-1024x500.png" if idioma == "en-US" else otros / (idioma + ".png")
        svg, textos = cabecera(linea, dia, entradas)
        render(svg, destino, 1024, 500)
        escribir(destino, textos)
    # Play masks the icon itself: it goes square, full bleed, like the launcher's master.
    render((RAIZ / "tools" / "icon-master.svg").read_text(), play / "icon-512.png", 512, 512)


if __name__ == "__main__":
    main()
