#!/usr/bin/env python3
"""Regenerates every Bobbin icon from one geometry. Needs rsvg-convert (brew install librsvg).

The metaphor is the bobbin (docs/pantallas.md 20): two cream tabs holding seven coils of sage
thread, and the last coil unspools into the bullet dot, the method's own mark. The bobbin is the
name, the dot is the task.

Run from anywhere:  python3 tools/generate_icons.py

The author reviews tools/icon-master.svg before running this for real (SPEC 8, #18): it writes the
app's actual launcher and notification assets, on top of whatever this geometry already produced.
"""
import math
import os
import pathlib
import subprocess

S = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(S)

# The app's own dark tokens, so the icon is a slice of the product and not a separate brand.
BG_TOP, BG_BOT, BG_FLAT = "#2C2820", "#17150F", "#221E17"
CREAM = "#FBF8F3"
SAGE = "#B6D6AB"

CANVAS = 1024

# The two tabs that hold the spool, from x 212 to 612.
TAB_X, TAB_W, TAB_H, TAB_R = 212, 400, 60, 30
TAB_Y = [232, 732]

# Seven coils of thread between them, x 272 to 552, 16 apart vertically.
CAP_X, CAP_W, CAP_H, CAP_R = 272, 280, 48, 24
CAP_GAP = 16
CAP_Y = [296 + i * (CAP_H + CAP_GAP) for i in range(7)]  # 296 .. 680

# The loose thread leaves the fifth coil's right edge and curls into the dot.
THREAD_START = (CAP_X + CAP_W, CAP_Y[4] + CAP_H / 2)  # (552, 576)
THREAD_CONTROL = (660, 700)
THREAD_STROKE = 20
DOT_CENTER = (756, 640)
DOT_R = 56

# The whole block: x 212-812, y 232-792, centred on the 1024 canvas.
BLOCK_W = (DOT_CENTER[0] + DOT_R) - TAB_X  # 600
BLOCK_H = (TAB_Y[1] + TAB_H) - TAB_Y[0]  # 560


def shapes(mono=None):
    """Every piece of the bobbin: its geometry, its fill, and the loose thread as a stroke.
    `mono` recolours everything the same, for the monochrome mask and the flat plate icons."""
    for y in TAB_Y:
        yield ("rect", TAB_X, y, TAB_W, TAB_H, TAB_R, mono or CREAM)
    for y in CAP_Y:
        yield ("rect", CAP_X, y, CAP_W, CAP_H, CAP_R, mono or SAGE)
    yield ("thread", THREAD_START, THREAD_CONTROL, DOT_CENTER, THREAD_STROKE, mono or SAGE)
    yield ("circle", DOT_CENTER[0], DOT_CENTER[1], DOT_R, mono or CREAM)


def rounded_rect(x, y, w, h, r):
    return (
        f"M{x+r:.2f},{y:.2f} H{x+w-r:.2f} A{r:.2f},{r:.2f} 0 0 1 {x+w:.2f},{y+r:.2f} "
        f"V{y+h-r:.2f} A{r:.2f},{r:.2f} 0 0 1 {x+w-r:.2f},{y+h:.2f} "
        f"H{x+r:.2f} A{r:.2f},{r:.2f} 0 0 1 {x:.2f},{y+h-r:.2f} "
        f"V{y+r:.2f} A{r:.2f},{r:.2f} 0 0 1 {x+r:.2f},{y:.2f} Z"
    )


def circle_path(cx, cy, r):
    return f"M{cx-r:.2f},{cy:.2f} A{r:.2f},{r:.2f} 0 1,0 {cx+r:.2f},{cy:.2f} A{r:.2f},{r:.2f} 0 1,0 {cx-r:.2f},{cy:.2f} Z"


def svg_shape(shape):
    kind = shape[0]
    if kind == "rect":
        _, x, y, w, h, r, fill = shape
        return f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{r}" fill="{fill}"/>'
    if kind == "circle":
        _, cx, cy, r, fill = shape
        return f'<circle cx="{cx}" cy="{cy}" r="{r}" fill="{fill}"/>'
    # thread
    _, (sx, sy), (cx, cy), (ex, ey), width, colour = shape
    return (
        f'<path d="M{sx},{sy} Q{cx},{cy} {ex},{ey}" stroke="{colour}" '
        f'stroke-width="{width}" stroke-linecap="round" fill="none"/>'
    )


def svg(size, plate, scale=1.0, flat=False, mono=None):
    if plate == "circle":
        bg = f'<circle cx="{CANVAS/2}" cy="{CANVAS/2}" r="{CANVAS/2}" fill="url(#bg)"/>'
    elif plate == "rounded":
        bg = f'<rect width="{CANVAS}" height="{CANVAS}" rx="{CANVAS*0.22}" fill="url(#bg)"/>'
    elif plate == "none":
        bg = ""
    else:
        bg = f'<rect width="{CANVAS}" height="{CANVAS}" fill="url(#bg)"/>'
    if flat:
        bg = f'<rect width="{CANVAS}" height="{CANVAS}" fill="{BG_FLAT}"/>'

    inner = "".join(svg_shape(s) for s in shapes(mono=mono))
    if scale != 1.0:
        middle = CANVAS / 2
        inner = f'<g transform="translate({middle} {middle}) scale({scale}) translate({-middle} {-middle})">{inner}</g>'
    return (
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{size}" height="{size}" '
        f'viewBox="0 0 {CANVAS} {CANVAS}"><defs>'
        f'<linearGradient id="bg" x1="0" y1="0" x2="0" y2="1">'
        f'<stop offset="0" stop-color="{BG_TOP}"/><stop offset="1" stop-color="{BG_BOT}"/>'
        f"</linearGradient></defs>" + bg + inner + "</svg>"
    )


def png(svg_text, out, size):
    src = f"{S}/_tmp.svg"
    pathlib.Path(src).write_text(svg_text)
    pathlib.Path(out).parent.mkdir(parents=True, exist_ok=True)
    subprocess.run(["rsvg-convert", "-w", str(size), "-h", str(size), src, "-o", out], check=True)
    pathlib.Path(src).unlink()


# --- iOS: full bleed, the system applies its own mask ---
png(svg(CANVAS, "square"), f"{ROOT}/iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png", 1024)

# --- Android legacy launcher icons (API 24 and 25 have no adaptive icons) ---
for folder, size in [("mdpi", 48), ("hdpi", 72), ("xhdpi", 96), ("xxhdpi", 144), ("xxxhdpi", 192)]:
    base = f"{ROOT}/androidApp/src/main/res/mipmap-{folder}"
    png(svg(CANVAS, "rounded"), f"{base}/ic_launcher.png", size)
    png(svg(CANVAS, "circle", scale=0.82), f"{base}/ic_launcher_round.png", size)

res = f"{ROOT}/androidApp/src/main/res"

# The 108dp canvas keeps its content inside a 66dp safe circle: the block has to fit that diagonal.
SAFE = 66 / 108
BLOCK_SCALE = SAFE * CANVAS / math.hypot(BLOCK_W, BLOCK_H)


def vector(size_dp, colour=None, scale=1.0):
    middle = CANVAS / 2
    lines = [
        '<?xml version="1.0" encoding="utf-8"?>',
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
        f'    android:width="{size_dp}dp" android:height="{size_dp}dp"',
        f'    android:viewportWidth="{CANVAS}" android:viewportHeight="{CANVAS}">',
        f'    <group android:pivotX="{middle}" android:pivotY="{middle}" '
        f'android:scaleX="{scale:.4f}" android:scaleY="{scale:.4f}">',
    ]
    for shape in shapes(mono=colour):
        if shape[0] == "rect":
            _, x, y, w, h, r, fill = shape
            lines.append(f'        <path android:fillColor="{fill}" android:pathData="{rounded_rect(x, y, w, h, r)}"/>')
        elif shape[0] == "circle":
            _, cx, cy, r, fill = shape
            lines.append(f'        <path android:fillColor="{fill}" android:pathData="{circle_path(cx, cy, r)}"/>')
        else:
            _, (sx, sy), (cx, cy), (ex, ey), width, colour_thread = shape
            lines.append(
                f'        <path android:strokeColor="{colour_thread}" android:strokeWidth="{width}" '
                f'android:strokeLineCap="round" android:pathData="M{sx},{sy} Q{cx},{cy} {ex},{ey}"/>'
            )
    lines += ["    </group>", "</vector>"]
    return "\n".join(lines) + "\n"


pathlib.Path(f"{res}/drawable").mkdir(parents=True, exist_ok=True)
pathlib.Path(f"{res}/drawable/ic_launcher_background.xml").write_text(
    '<?xml version="1.0" encoding="utf-8"?>\n'
    '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
    '    android:width="108dp" android:height="108dp"\n'
    '    android:viewportWidth="108" android:viewportHeight="108">\n'
    f'    <path android:fillColor="{BG_FLAT}" android:pathData="M0,0h108v108h-108z"/>\n'
    "</vector>\n"
)
pathlib.Path(f"{res}/drawable-v24").mkdir(parents=True, exist_ok=True)
pathlib.Path(f"{res}/drawable-v24/ic_launcher_foreground.xml").write_text(vector(108, scale=BLOCK_SCALE))
# One colour and no gradient: the system paints this layer itself, so it has to be a plain mask.
pathlib.Path(f"{res}/drawable/ic_launcher_monochrome.xml").write_text(
    vector(108, colour="#FFFFFFFF", scale=BLOCK_SCALE)
)
pathlib.Path(f"{res}/mipmap-anydpi-v26").mkdir(parents=True, exist_ok=True)
for name in ("ic_launcher.xml", "ic_launcher_round.xml"):
    pathlib.Path(f"{res}/mipmap-anydpi-v26/{name}").write_text(
        '<?xml version="1.0" encoding="utf-8"?>\n'
        '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
        '    <background android:drawable="@drawable/ic_launcher_background" />\n'
        '    <foreground android:drawable="@drawable/ic_launcher_foreground" />\n'
        '    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />\n'
        "</adaptive-icon>\n"
    )

# --- Notification icon: Android tints it white, so it is a flat silhouette, not the full bobbin ---
notification = "\n".join(
    [
        '<?xml version="1.0" encoding="utf-8"?>',
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
        '    android:width="24dp" android:height="24dp"',
        '    android:viewportWidth="24" android:viewportHeight="24">',
        f'    <path android:fillColor="#FFFFFFFF" android:pathData="{rounded_rect(4, 4, 10, 2, 1)}"/>',
        f'    <path android:fillColor="#FFFFFFFF" android:pathData="{rounded_rect(4, 18, 10, 2, 1)}"/>',
        '    <path android:fillColor="#FFFFFFFF" android:pathData="M6,6.5 H12 V17.5 H6 Z"/>',
        f'    <path android:fillColor="#FFFFFFFF" android:pathData="{circle_path(18.5, 15, 2.5)}"/>',
        "</vector>",
    ]
) + "\n"
pathlib.Path(f"{ROOT}/shared/src/androidMain/res/drawable").mkdir(parents=True, exist_ok=True)
pathlib.Path(f"{ROOT}/shared/src/androidMain/res/drawable/ic_notification.xml").write_text(notification)

pathlib.Path(f"{S}/icon-master.svg").write_text(svg(CANVAS, "square"))
print("assets written")
