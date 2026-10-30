#!/usr/bin/env python3
"""Saca las capturas crudas de Play del emulador, idioma a idioma (#58, store/capturas.md).

    python3 tools/demo/generar.py --idioma es-ES
    python3 tools/store/crudas.py es-ES            # con la build de depuracion ya instalada

Carga el diario de demostracion con run-as (solo funciona con la build de depuracion), pone el idioma
de la app con las preferencias por app de Android 13+, limpia la barra de estado con el modo demo del
sistema y recorre las escenas buscando cada boton por su texto en el arbol de accesibilidad, no por
coordenadas: asi sirve igual en todos los idiomas. Deja tools/demo/salida/<idioma>/crudas/*.png.
Ruso, japones y coreano no teclean en la escena 01 (escribir=None): adb input text solo escribe ASCII.
"""
import os
import pathlib
import re
import subprocess
import sys
import time

RAIZ = pathlib.Path(__file__).resolve().parents[2]
PAQUETE = "com.baltajmn.bullet"
ADB = [os.path.expanduser("~/Library/Android/sdk/platform-tools/adb")]

# What each scene looks for, in the app's own words (Strings.kt), and what gets typed on Hoy.
# "repasar" is the button of last month's notice (reviewMonth), told apart from the other review
# buttons by what follows the verb; "decidir" is the one that leaves rereading (decideTasks).
IDIOMAS = {
    "en-US": dict(escribir="Call%sLeo", mes="Month", indice="Index", ajustes="Settings", cuaderno="NOTEBOOK",
                  portada="Lilac", papel="grid",
                  repasar=r"^Review (?!those days|the day|Future)", decidir=r"^Decide the \d+ tasks$"),
    "es-ES": dict(escribir="Llamar%sa%sLeo", mes="Mes", indice="Índice", ajustes="Ajustes", cuaderno="CUADERNO",
                  portada="Lila", papel="cuadrícula",
                  repasar=r"^Repasar (?!esos días|el día|Futuro)", decidir=r"^Decidir las \d+ tareas$"),
    "pt-BR": dict(escribir="Ligar%spara%so%sLeo", mes="Mês", indice="Índice", ajustes="Ajustes", cuaderno="CADERNO",
                  portada="Lilás", papel="quadriculado",
                  repasar=r"^Revisar (?!esses dias|o dia|Futuro)", decidir=r"^Decidir as \d+ tarefas$"),
    "de-DE": dict(escribir="Leo%sanrufen", mes="Monat", indice="Index", ajustes="Einstellungen", cuaderno="NOTIZBUCH",
                  portada="Flieder", papel="kariert",
                  repasar=r"^(?!Diese Tage|Den Tag|Zukunft)\S+ durchsehen$", decidir=r"^\d+ Aufgaben entscheiden$"),
    "fr-FR": dict(escribir="Appeler%sLeo", mes="Mois", indice="Index", ajustes="Réglages", cuaderno="CARNET",
                  portada="Lilas", papel="quadrillé",
                  repasar=r"^Revoir (?!ces jours|la journée|Futur)", decidir=r"^Décider les \d+ tâches$"),
    "it-IT": dict(escribir="Chiamare%sLeo", mes="Mese", indice="Indice", ajustes="Impostazioni", cuaderno="QUADERNO", portada="Lilla", papel="a quadretti", repasar=r'^(?!(?:Rivedi\ quei\ giorni|Rivedi\ la\ giornata|Rivedi\ Futuro)$)Rivedi\ .+$', decidir=r'^Decidi\ le\ \d+\ attività$', fila=r'^\d+,\ '),
    "nl-NL": dict(escribir="Leo%sbellen", mes="Maand", indice="Index", ajustes="Instellingen", cuaderno="NOTITIEBOEK", portada="Lila", papel="geruit", repasar=r'^(?!(?:Die\ dagen\ doornemen|De\ dag\ doornemen|Toekomst\ doornemen)$)\S+\ doornemen$', decidir=r'^Beslis\ over\ \d+\ taken$', fila=r'^\d+,\ '),
    "pl-PL": dict(escribir="Napisz%sdo%sLeo", mes="Miesiąc", indice="Indeks", ajustes="Ustawienia", cuaderno="NOTATNIK", portada="Lila", papel="w kratkę", repasar=r'^(?!(?:Przejrzyj\ te\ dni|Przejrzyj\ dzień|Przejrzyj\ Przyszłość)$)Przejrzyj\ .+$', decidir=r'^Zdecyduj:\ \d+\ \S+$', fila=r'^\d+,\ '),
    "ru-RU": dict(escribir=None, mes="Месяц", indice="Индекс", ajustes="Настройки", cuaderno="БЛОКНОТ", portada="Сирень", papel="в клетку", repasar=r'^(?!(?:Просмотреть\ эти\ дни|Просмотреть\ день|Просмотреть\ Будущее)$)Просмотреть\ .+$', decidir=r'^Решить\ по\ \d+\ \S+$', fila=r'^\d+,\ '),
    "tr-TR": dict(escribir="Anneyi%sara", mes="Ay", indice="Dizin", ajustes="Ayarlar", cuaderno="DEFTER", portada="Leylak", papel="kareli", repasar=r'^(?!(?:Günleri\ gözden\ geçir|Günü\ gözden\ geçir|Gözden\ geçir:\ Gelecek)$)Gözden\ geçir:\ .+$', decidir=r'^Karar\ ver:\ \d+\ görev$', fila=r'^\d+,\ '),
    "id": dict(escribir="Telepon%sLeo", mes="Bulan", indice="Indeks", ajustes="Pengaturan", cuaderno="BUKU CATATAN", portada="Lilac", papel="kotak-kotak", repasar=r'^(?!(?:Tinjau\ hari\-hari\ itu|Tinjau\ hari|Tinjau\ Mendatang)$)Tinjau\ .+$', decidir=r'^Putuskan\ \d+\ tugas$', fila=r'^\d+,\ '),
    "ja-JP": dict(escribir=None, mes="月", indice="目次", ajustes="設定", cuaderno="ノート", portada="ライラック", papel="方眼", repasar=r'^(?!(?:それらの日を見直す|この日を見直す|未来を見直す)$).+を見直す$', decidir=r'^\d+件のタスクを決める$', fila=r'^\d+日、'),
    "ko-KR": dict(escribir=None, mes="월간", indice="목차", ajustes="설정", cuaderno="노트", portada="라일락", papel="모눈", repasar=r'^(?!(?:지난\ 날들\ 살펴보기|하루\ 살펴보기|미래\ 살펴보기)$).+\ 살펴보기$', decidir=r'^할\ 일\ \d+개\ 정하기$', fila=r'^\d+일,\ '),
}


def adb(*args, salida=None):
    return subprocess.run(ADB + list(args), check=True, stdout=salida or subprocess.PIPE, text=salida is None).stdout


def nodos():
    # A dump can fail ("null root node") while the screen animates: the old file is removed first, so a
    # failed dump reads as an empty screen and the caller tries again, never as the previous screen.
    xml = subprocess.run(ADB + ["shell", "rm -f /data/local/tmp/ui.xml; uiautomator dump /data/local/tmp/ui.xml >/dev/null; "
                                "cat /data/local/tmp/ui.xml 2>/dev/null"], stdout=subprocess.PIPE, text=True).stdout
    out = []
    for m in re.finditer(r'<node [^>]*>', xml):
        n = m.group(0)
        texto = re.search(r' text="([^"]*)"', n).group(1) or re.search(r'content-desc="([^"]*)"', n).group(1)
        x1, y1, x2, y2 = map(int, re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n).groups())
        if texto:
            out.append((texto.replace("&apos;", "'").replace("&amp;", "&"), (x1, y1, x2, y2)))
    return out


def buscar(que, exacto=True, espera=15):
    fin = time.time() + espera
    while time.time() < fin:
        for texto, caja in nodos():
            if que.search(texto) if isinstance(que, re.Pattern) else (texto == que) if exacto else (que in texto):
                return caja
        time.sleep(0.7)
    sys.exit("No aparece en pantalla: %r" % que)


def teclado():
    """Whether the keyboard takes up screen: the IME can say it is shown with a frame of zero height."""
    m = re.search(r"type=ime frame=\S+ visibleFrame=\[\d+,(\d+)\]\[\d+,(\d+)\] visible=true", adb("shell", "dumpsys", "window"))
    return bool(m) and int(m.group(2)) > int(m.group(1))


def cerrar_teclado():
    """BACK only while the keyboard is up: with it down, BACK would close the app."""
    if teclado():
        adb("shell", "input", "keyevent", "KEYCODE_BACK")
        time.sleep(0.8)


def tocar(caja):
    x1, y1, x2, y2 = caja
    adb("shell", "input", "tap", str((x1 + x2) // 2), str((y1 + y2) // 2))
    time.sleep(1.2)


def captura(destino):
    with open(destino, "wb") as f:
        adb("exec-out", "screencap", "-p", salida=f)
    print(destino.relative_to(RAIZ))


def barra_limpia():
    adb("shell", "settings", "put", "global", "sysui_demo_allowed", "1")
    for orden in (["enter"], ["clock", "-e", "hhmm", "0941"], ["battery", "-e", "level", "100", "-e", "plugged", "false"],
                  ["network", "-e", "wifi", "show", "-e", "level", "4"], ["notifications", "-e", "visible", "false"]):
        adb("shell", "am", "broadcast", "-a", "com.android.systemui.demo", "-e", "command", *orden)


def main():
    idioma = sys.argv[1]
    t = IDIOMAS[idioma]
    diario = RAIZ / "tools" / "demo" / "salida" / idioma / "journal.json"
    salida = diario.parent / "crudas"
    salida.mkdir(exist_ok=True)

    adb("shell", "am", "force-stop", PAQUETE)
    # en-GB for en-US: the emulator's Gboard never raises its keyboard on the en-US subtype, and the app
    # reads only the language, so every text is the same.
    adb("shell", "cmd", "locale", "set-app-locales", PAQUETE, "--locales", "en-GB" if idioma == "en-US" else idioma)
    adb("push", str(diario), "/data/local/tmp/journal.json")
    adb("shell", "run-as %s sh -c 'mkdir -p files && cp /data/local/tmp/journal.json files/journal.json && rm -f files/journal.bak.json'" % PAQUETE)
    # The guide and Today's hint are for a first start, not for a diary that already has months in it.
    pref("guideSeen", "true")
    pref("hintSeen", "true")
    barra_limpia()

    # 01 Today, typing: the app opens with the capture field focused and the keyboard up. On the
    # emulator the keyboard misses a cold start now and then, and a new start is the way to get it:
    # a tap on the field leaves the text handle up, and the handle stays over the next screen.
    for _ in range(4):
        adb("shell", "am", "force-stop", PAQUETE)
        adb("shell", "am", "start", "-n", PAQUETE + "/.MainActivity")
        # The settings icon, not a tab: with the keyboard up the tab bar steps aside.
        buscar(t["ajustes"])
        fin = time.time() + 6
        while not teclado() and time.time() < fin:
            time.sleep(0.5)
        if teclado():
            break
    else:
        sys.exit("El teclado no sube")
    time.sleep(1)
    if t["escribir"]:
        adb("shell", "input", "text", t["escribir"])
        time.sleep(1)
    captura(salida / "01_hoy.png")

    # 03 Reviewing last month, from Today's notice: past rereading, the first task is the one moved
    # twice, with each way out saying what it leaves on the page.
    cerrar_teclado()
    tocar(buscar(re.compile(t["repasar"])))
    tocar(buscar(re.compile(t["decidir"])))
    time.sleep(0.8)
    captura(salida / "03_revisar.png")

    # 02 Month, from the top: title, notice and the first half with its events. It waits for the day
    # rows ("29, martes, ..."), which Today does not have.
    adb("shell", "input", "keyevent", "KEYCODE_BACK")
    time.sleep(0.8)
    tocar(buscar(t["mes"]))
    buscar(re.compile(t.get("fila", r"^\d+\.?, ")))
    for _ in range(3):
        adb("shell", "input", "swipe", "540", "700", "540", "2000", "250")
    time.sleep(1.2)
    captura(salida / "02_mes.png")

    # 04 Index.
    tocar(buscar(t["indice"]))
    time.sleep(1)
    captura(salida / "04_indice.png")

    # 06 Notebook: a Pro cover and paper looked at without buying, with the preview.
    # Slow swipes, so there is no fling: the section header ends up near the top, with the preview whole.
    tocar(buscar(t["ajustes"]))
    for _ in range(8):
        cabecera = [c for texto, c in nodos() if texto == t["cuaderno"]]
        if cabecera:
            if cabecera[0][1] > 300:
                adb("shell", "input", "swipe", "540", str(cabecera[0][1] + 200), "540", "500", "1500")
                time.sleep(0.8)
            break
        adb("shell", "input", "swipe", "540", "1700", "540", "1100", "1500")
        time.sleep(0.6)
    tocar(buscar(t["portada"], exacto=False))
    tocar(buscar(t["papel"], exacto=False))
    captura(salida / "06_cuaderno.png")

    # 05 Widgets, with Pro so the month one is not locked. The two Bobbin widgets have to be alone on
    # the launcher's second page, placed there by hand once (store/capturas.md). Pro is set in the
    # debug build's own preferences and taken away again, so scene 06 is always seen without it.
    adb("shell", "am", "force-stop", PAQUETE)
    pref("pro", "true")
    adb("shell", "am", "start", "-n", PAQUETE + "/.MainActivity")
    buscar(t["ajustes"])
    time.sleep(2)
    adb("shell", "input", "keyevent", "KEYCODE_HOME")
    time.sleep(2)
    adb("shell", "input", "swipe", "900", "1000", "150", "1000", "300")
    time.sleep(2.5)
    captura(salida / "05_widgets.png")
    adb("shell", "am", "force-stop", PAQUETE)
    pref("pro", None)


def pref(nombre, valor):
    """Writes a flag of Prefs straight into the debug build's SharedPreferences, or removes it with None."""
    orden = ("cd shared_prefs 2>/dev/null || { mkdir shared_prefs; cd shared_prefs; }; "
             "[ -f bobbin.xml ] || printf '<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\" ?>\\n<map>\\n</map>\\n' > bobbin.xml; "
             "sed -i '/name=\"%s\"/d' bobbin.xml" % nombre)
    if valor:
        orden += "; sed -i 's#</map>#    <boolean name=\"%s\" value=\"%s\" />\\n</map>#' bobbin.xml" % (nombre, valor)
    adb("shell", "run-as %s sh -c '%s'" % (PAQUETE, orden.replace("'", "'\\''")))


if __name__ == "__main__":
    main()
