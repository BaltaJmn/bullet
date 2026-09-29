#!/usr/bin/env python3
"""Diario de prueba para #55 (docs/tecnico.md 6.19): 5.000 entradas en 36 meses, con semilla fija.

    python3 tools/perf/generar.py            # escribe tools/perf/out/journal.json y la copia .zip

La copia se importa en Ajustes > Importar copia sobre un diario vacio, y con eso se miden Hoy en frio,
Mes, Indice y Busqueda en un dispositivo real. El test 26 genera el mismo reparto dentro de la JVM.
No lleva texto de nadie: frases de relleno.
"""
import json
import random
import zipfile
from datetime import date, datetime, timedelta, timezone
from pathlib import Path

ENTRIES = 5000
MONTHS = 36
SEED = 26
WORDS = ("comprar tinta llamar a Ana revisar notas cena reunion leer un capitulo pagar la luz "
         "regar plantas escribir carta correr cinco km cafe con Luis ordenar el escritorio").split()


def generate(end: date = date(2026, 9, 30)) -> dict:
    rnd = random.Random(SEED)
    start = (end.replace(day=1) - timedelta(days=MONTHS * 30)).replace(day=1)
    days = (end - start).days + 1
    collections = [
        {"id": f"c-{i:08x}", "title": f"Coleccion {i + 1}", "createdAt": 1_600_000_000_000 + i}
        for i in range(12)
    ]
    entries = []
    for i in range(ENTRIES):
        day = start + timedelta(days=rnd.randrange(days))
        bullet = rnd.choices(["task", "event", "note"], [6, 2, 2])[0]
        status = rnd.choices(["open", "done", "migrated", "scheduled", "irrelevant"], [3, 5, 1, 1, 1])[0] if bullet == "task" else "open"
        kind = rnd.choices(["daily", "monthly", "collection"], [8, 1, 1])[0]
        if kind == "daily":
            place = {"daily": day.isoformat()}
        elif kind == "monthly":
            place = {"monthly": day.strftime("%Y-%m"), "day": day.day}
        else:
            place = {"collection": rnd.choice(collections)["id"]}
        at = int(datetime(day.year, day.month, day.day, 12, tzinfo=timezone.utc).timestamp() * 1000) + i
        entry = {
            "id": f"e-{i:08x}",
            "text": " ".join(rnd.choice(WORDS) for _ in range(rnd.randint(2, 8))),
            "place": place,
            "order": i,
            "createdAt": at,
            "updatedAt": at,
        }
        if bullet != "task":
            entry["bullet"] = bullet
        if status != "open":
            entry["status"] = status
        signifiers = [s for s, p in (("priority", 0.1), ("inspiration", 0.05), ("explore", 0.05)) if rnd.random() < p]
        if signifiers:
            entry["signifiers"] = signifiers
        entries.append(entry)
    return {"schemaVersion": 1, "entries": entries, "collections": collections}


def main() -> None:
    out = Path(__file__).parent / "out"
    out.mkdir(exist_ok=True)
    text = json.dumps(generate(), ensure_ascii=False)
    (out / "journal.json").write_text(text, encoding="utf-8")
    with zipfile.ZipFile(out / "bobbin-2026-09-30.zip", "w", zipfile.ZIP_DEFLATED) as z:
        z.writestr("journal.json", text)
    print(f"{ENTRIES} entradas en {MONTHS} meses: {out}")


if __name__ == "__main__":
    main()
