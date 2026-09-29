#!/usr/bin/env python3
"""El diario de demostracion de las capturas de tienda (#58, store/capturas.md).

Nunca un diario real: un diario de alguien en una ficha publica es justo lo que esta app promete no
hacer. Todo es fijo alrededor de --hoy, asi que dos ejecuciones el mismo dia dan el mismo diario.

    python3 tools/demo/generar.py --idioma es-ES [--hoy AAAA-MM-DD]

Deja tools/demo/salida/<idioma>/journal.json. Lo que tiene que verse, escena a escena:
- Hoy: tareas abiertas y hechas, un evento, una nota, una prioridad y una tarea que llego migrada de
  ayer. Sin la linea de dias anteriores: los dias de este mes estan todos cerrados.
- Mes: el calendario con cuatro eventos y las tareas del mes.
- Revisar: el mes anterior sin cerrar, y su primera tarea abierta migrada dos veces.
- Indice: tres meses, dos colecciones y un seguimiento.
"""
import argparse
import datetime as dt
import json
import pathlib

OUT = pathlib.Path(__file__).resolve().parent / "salida"

T = {
    "en-US": dict(
        hoy=["Buy ink", "Dinner with Ana", "Water the plants", "Send the quote", "The bookshop opens at 10", "Call the bank"],
        ayer=["Pick up the parcel", "Pottery class", "Idea: a shelf in the hallway"],
        mes_dias=["Leo's birthday", "Dentist", "Trip to Lisbon", "Car service"],
        mes_tareas=["Renew the passport", "Pay the insurance", "Sort the photos"],
        dos_veces="Book the town hall appointment",
        anterior=["Return Marta's book", "Change the bathroom bulb", "Fix the bike light"],
        cerradas=["Clean the fridge", "Reply to Pablo", "Buy stamps", "Print the tickets", "Order coffee beans"],
        futuro=["File the tax return", "Clara's wedding", "Renew the domain"],
        viaje="Trip to Lisbon", viaje_tareas=["Book the hotel", "Buy the guide", "Pack the charger"],
        ideas="Living room ideas", ideas_notas=["Lamp by the window", "Rug in dark green", "Frame the map"],
        agua="Water", filas=["Two litres", "Stretch", "Read 20 minutes"],
    ),
    "es-ES": dict(
        hoy=["Comprar tinta", "Cena con Ana", "Regar las plantas", "Enviar el presupuesto", "La librería abre a las 10", "Llamar al banco"],
        ayer=["Recoger el paquete", "Clase de cerámica", "Idea: una estantería en el pasillo"],
        mes_dias=["Cumpleaños de Leo", "Dentista", "Viaje a Lisboa", "Revisión del coche"],
        mes_tareas=["Renovar el pasaporte", "Pagar el seguro", "Ordenar las fotos"],
        dos_veces="Pedir cita en el ayuntamiento",
        anterior=["Devolver el libro a Marta", "Cambiar la bombilla del baño", "Arreglar la luz de la bici"],
        cerradas=["Limpiar la nevera", "Contestar a Pablo", "Comprar sellos", "Imprimir las entradas", "Pedir café en grano"],
        futuro=["Declaración de la renta", "Boda de Clara", "Renovar el dominio"],
        viaje="Viaje a Lisboa", viaje_tareas=["Reservar el hotel", "Comprar la guía", "Meter el cargador"],
        ideas="Ideas para el salón", ideas_notas=["Lámpara junto a la ventana", "Alfombra verde oscuro", "Enmarcar el mapa"],
        agua="Agua", filas=["Dos litros", "Estirar", "Leer 20 minutos"],
    ),
    "pt-BR": dict(
        hoy=["Comprar tinta", "Jantar com a Ana", "Regar as plantas", "Enviar o orçamento", "A livraria abre às 10", "Ligar para o banco"],
        ayer=["Buscar a encomenda", "Aula de cerâmica", "Ideia: uma estante no corredor"],
        mes_dias=["Aniversário do Leo", "Dentista", "Viagem a Lisboa", "Revisão do carro"],
        mes_tareas=["Renovar o passaporte", "Pagar o seguro", "Organizar as fotos"],
        dos_veces="Marcar horário na prefeitura",
        anterior=["Devolver o livro da Marta", "Trocar a lâmpada do banheiro", "Consertar a luz da bicicleta"],
        cerradas=["Limpar a geladeira", "Responder ao Pablo", "Comprar selos", "Imprimir os ingressos", "Pedir café em grão"],
        futuro=["Declaração do imposto", "Casamento da Clara", "Renovar o domínio"],
        viaje="Viagem a Lisboa", viaje_tareas=["Reservar o hotel", "Comprar o guia", "Levar o carregador"],
        ideas="Ideias para a sala", ideas_notas=["Luminária perto da janela", "Tapete verde escuro", "Emoldurar o mapa"],
        agua="Água", filas=["Dois litros", "Alongar", "Ler 20 minutos"],
    ),
    "de-DE": dict(
        hoy=["Tinte kaufen", "Abendessen mit Ana", "Pflanzen gießen", "Angebot schicken", "Die Buchhandlung öffnet um 10", "Bank anrufen"],
        ayer=["Paket abholen", "Töpferkurs", "Idee: ein Regal im Flur"],
        mes_dias=["Leos Geburtstag", "Zahnarzt", "Reise nach Lissabon", "Auto zur Inspektion"],
        mes_tareas=["Pass verlängern", "Versicherung zahlen", "Fotos sortieren"],
        dos_veces="Termin im Rathaus machen",
        anterior=["Martas Buch zurückgeben", "Glühbirne im Bad wechseln", "Fahrradlicht reparieren"],
        cerradas=["Kühlschrank putzen", "Pablo antworten", "Briefmarken kaufen", "Tickets drucken", "Kaffeebohnen bestellen"],
        futuro=["Steuererklärung", "Claras Hochzeit", "Domain verlängern"],
        viaje="Reise nach Lissabon", viaje_tareas=["Hotel buchen", "Reiseführer kaufen", "Ladegerät einpacken"],
        ideas="Ideen fürs Wohnzimmer", ideas_notas=["Lampe am Fenster", "Teppich in Dunkelgrün", "Karte einrahmen"],
        agua="Wasser", filas=["Zwei Liter", "Dehnen", "20 Minuten lesen"],
    ),
    "fr-FR": dict(
        hoy=["Acheter de l'encre", "Dîner avec Ana", "Arroser les plantes", "Envoyer le devis", "La librairie ouvre à 10 h", "Appeler la banque"],
        ayer=["Récupérer le colis", "Cours de poterie", "Idée : une étagère dans le couloir"],
        mes_dias=["Anniversaire de Léo", "Dentiste", "Voyage à Lisbonne", "Révision de la voiture"],
        mes_tareas=["Renouveler le passeport", "Payer l'assurance", "Trier les photos"],
        dos_veces="Prendre rendez-vous à la mairie",
        anterior=["Rendre le livre de Marta", "Changer l'ampoule de la salle de bain", "Réparer la lumière du vélo"],
        cerradas=["Nettoyer le frigo", "Répondre à Pablo", "Acheter des timbres", "Imprimer les billets", "Commander du café en grains"],
        futuro=["Déclaration d'impôts", "Mariage de Clara", "Renouveler le domaine"],
        viaje="Voyage à Lisbonne", viaje_tareas=["Réserver l'hôtel", "Acheter le guide", "Prendre le chargeur"],
        ideas="Idées pour le salon", ideas_notas=["Lampe près de la fenêtre", "Tapis vert foncé", "Encadrer la carte"],
        agua="Eau", filas=["Deux litres", "S'étirer", "Lire 20 minutes"],
    ),
}


def month_add(d, n):
    y, m = divmod(d.year * 12 + d.month - 1 + n, 12)
    return dt.date(y, m + 1, 1)


def ym(d):
    return "%04d-%02d" % (d.year, d.month)


def build(t, hoy):
    entries = []
    n = [0]

    def at(day, hour=9):
        return int(dt.datetime(day.year, day.month, day.day, hour, tzinfo=dt.timezone.utc).timestamp() * 1000) + n[0]

    def add(text, place, day, bullet="task", status="open", signifiers=(), frm=None):
        n[0] += 1
        e = {"id": "e-demo%04d" % n[0], "text": text, "place": place, "order": n[0], "createdAt": at(day), "updatedAt": at(day)}
        if bullet != "task":
            e["bullet"] = bullet
        if status != "open":
            e["status"] = status
        if signifiers:
            e["signifiers"] = list(signifiers)
        if frm:
            e["from"] = frm
        entries.append(e)
        return e["id"]

    daily = lambda d: {"daily": d.isoformat()}
    este = hoy.replace(day=1)
    ant = month_add(este, -1)
    ant2 = month_add(este, -2)
    ayer = hoy - dt.timedelta(days=1)

    # Two months ago, all closed but the first two steps of the chain migrated twice.
    a = add(t["dos_veces"], daily(ant2.replace(day=10)), ant2.replace(day=10), status="migrated")
    for i, text in enumerate(t["cerradas"][:3]):
        add(text, daily(ant2.replace(day=4 + i * 6)), ant2.replace(day=4 + i * 6), status="done")
    b = add(t["dos_veces"], daily(ant2.replace(day=24)), ant2.replace(day=24), status="migrated", frm=a)

    # Last month, not closed: the twice migrated task first, on its second day.
    add(t["dos_veces"], daily(ant.replace(day=2)), ant.replace(day=2), frm=b)
    add(t["anterior"][0], daily(ant.replace(day=14)), ant.replace(day=14))
    add(t["anterior"][2], daily(ant.replace(day=21)), ant.replace(day=21))
    add(t["anterior"][1], {"monthly": ym(ant)}, ant.replace(day=1))
    for i, text in enumerate(t["cerradas"][3:]):
        add(text, daily(ant.replace(day=8 + i * 9)), ant.replace(day=8 + i * 9), status="done")

    # This month: the calendar and the tasks of the month.
    for day, text in zip((3, 6, 9, 12), t["mes_dias"]):
        if day <= 28:
            add(text, {"monthly": ym(este), "day": day}, este, bullet="event")
    add(t["mes_tareas"][0], {"monthly": ym(este)}, este, signifiers=["priority"])
    add(t["mes_tareas"][1], {"monthly": ym(este)}, este, status="done")
    add(t["mes_tareas"][2], {"monthly": ym(este)}, este)

    # The days of this month before yesterday, written most days and all closed: the month widget has
    # something to show, and Today still has no line of earlier days.
    for day in range(1, ayer.day):
        if (day * 7919) % 10 < 7 and day not in (3, 6, 9, 12):
            d = este.replace(day=day)
            add(t["cerradas"][day % 5], daily(d), d, status="done")

    # Yesterday: one task went on to today, the rest closed.
    llamar = add(t["hoy"][5], daily(ayer), ayer, status="migrated")
    add(t["ayer"][0], daily(ayer), ayer, status="done")
    add(t["ayer"][1], daily(ayer), ayer, bullet="event")
    add(t["ayer"][2], daily(ayer), ayer, bullet="note", signifiers=["inspiration"])

    # Today.
    # hoy[0] is left out: with the typed line and the keyboard up, one more row scrolls the top bar away.
    add(t["hoy"][1], daily(hoy), hoy, bullet="event")
    add(t["hoy"][2], daily(hoy), hoy, status="done")
    add(t["hoy"][3], daily(hoy), hoy, signifiers=["priority"])
    add(t["hoy"][5], daily(hoy), hoy, frm=llamar)
    add(t["hoy"][4], daily(hoy), hoy, bullet="note")

    # The Future Log.
    add(t["futuro"][0], {"future": ym(month_add(este, 1))}, hoy)
    add(t["futuro"][1], {"future": ym(month_add(este, 2)), "day": 5}, hoy, bullet="event")
    add(t["futuro"][2], {"future": ym(month_add(este, 3))}, hoy)

    colecciones = [
        {"id": "c-demo-viaje", "title": t["viaje"], "createdAt": at(ant.replace(day=5), 20)},
        {"id": "c-demo-ideas", "title": t["ideas"], "createdAt": at(ant.replace(day=12), 20)},
        {"id": "c-demo-agua", "title": t["agua"], "createdAt": at(este, 8), "kind": "tracker", "month": ym(este),
         "rows": [
             {"id": "r-1", "title": t["filas"][0], "days": [d for d in range(1, hoy.day + 1) if d % 3 != 0]},
             {"id": "r-2", "title": t["filas"][1], "days": [d for d in range(1, hoy.day + 1) if d % 2 == 1]},
             {"id": "r-3", "title": t["filas"][2], "days": [d for d in range(1, hoy.day) if d % 4 != 1]},
         ]},
    ]
    for i, text in enumerate(t["viaje_tareas"]):
        add(text, {"collection": "c-demo-viaje"}, ant.replace(day=5), status="done" if i == 0 else "open")
    for text in t["ideas_notas"]:
        add(text, {"collection": "c-demo-ideas"}, ant.replace(day=12), bullet="note")

    return {
        "schemaVersion": 1,
        "entries": entries,
        "collections": colecciones,
        # The reminder offer and the Future Log line would cover what the scenes are about.
        "settings": {"reminderOffered": True, "futureSeen": ym(este)},
    }


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--idioma", required=True, choices=sorted(T))
    p.add_argument("--hoy", default=dt.date.today().isoformat())
    a = p.parse_args()
    out = OUT / a.idioma
    out.mkdir(parents=True, exist_ok=True)
    (out / "journal.json").write_text(json.dumps(build(T[a.idioma], dt.date.fromisoformat(a.hoy)), ensure_ascii=False), encoding="utf-8")
    print(out / "journal.json")


if __name__ == "__main__":
    main()
