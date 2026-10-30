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
    "it-IT": dict(
        hoy=["Comprare inchiostro", "Cena con Ana", "Annaffiare le piante", "Inviare il preventivo", "La libreria apre alle 10", "Chiamare la banca"],
        ayer=["Ritirare il pacco", "Corso di ceramica", "Idea: una mensola nel corridoio"],
        mes_dias=["Compleanno di Leo", "Dentista", "Viaggio a Lisbona", "Revisione dell'auto"],
        mes_tareas=["Rinnovare il passaporto", "Pagare l'assicurazione", "Sistemare le foto"],
        dos_veces="Prenotare l'appuntamento in comune",
        anterior=["Restituire il libro a Marta", "Cambiare la lampadina del bagno", "Aggiustare il fanalino della bici"],
        cerradas=["Pulire il frigo", "Rispondere a Pablo", "Comprare i francobolli", "Stampare i biglietti", "Ordinare il caffè in grani"],
        futuro=["Dichiarazione dei redditi", "Matrimonio di Clara", "Rinnovare il dominio"],
        viaje="Viaggio a Lisbona",
        viaje_tareas=["Prenotare l'hotel", "Comprare la guida", "Portare il caricatore"],
        ideas="Idee per il soggiorno",
        ideas_notas=["Lampada vicino alla finestra", "Tappeto verde scuro", "Incorniciare la mappa"],
        agua="Acqua",
        filas=["Due litri", "Stretching", "Leggere 20 minuti"],
    ),
    "nl-NL": dict(
        hoy=["Inkt kopen", "Diner met Ana", "De planten water geven", "Offerte sturen", "De boekhandel opent om 10 uur", "Bank bellen"],
        ayer=["Pakketje ophalen", "Pottenbakles", "Idee: een plank in de gang"],
        mes_dias=["Verjaardag van Leo", "Tandarts", "Reis naar Lissabon", "Auto naar de garage"],
        mes_tareas=["Paspoort verlengen", "Verzekering betalen", "Foto's sorteren"],
        dos_veces="Afspraak maken bij de gemeente",
        anterior=["Boek aan Marta teruggeven", "Lamp in de badkamer vervangen", "Fietslicht repareren"],
        cerradas=["Koelkast schoonmaken", "Pablo antwoorden", "Postzegels kopen", "Tickets printen", "Koffiebonen bestellen"],
        futuro=["Belastingaangifte doen", "Bruiloft van Clara", "Domeinnaam verlengen"],
        viaje="Reis naar Lissabon",
        viaje_tareas=["Hotel boeken", "Reisgids kopen", "Oplader inpakken"],
        ideas="Ideeën voor de woonkamer",
        ideas_notas=["Lamp bij het raam", "Tapijt in donkergroen", "De kaart inlijsten"],
        agua="Water",
        filas=["Twee liter", "Rekken", "20 minuten lezen"],
    ),
    "pl-PL": dict(
        hoy=["Kupić atrament", "Kolacja z Aną", "Podlać rośliny", "Wysłać wycenę", "Księgarnia otwiera się o 10", "Zadzwonić do banku"],
        ayer=["Odebrać paczkę", "Zajęcia z ceramiki", "Pomysł: półka w przedpokoju"],
        mes_dias=["Leo ma urodziny", "Dentysta", "Podróż do Lizbony", "Przegląd samochodu"],
        mes_tareas=["Odnowić paszport", "Opłacić ubezpieczenie", "Uporządkować zdjęcia"],
        dos_veces="Umówić wizytę w urzędzie",
        anterior=["Oddać książkę Marcie", "Wymienić żarówkę w łazience", "Naprawić lampkę w rowerze"],
        cerradas=["Umyć lodówkę", "Odpisać Pablowi", "Kupić znaczki", "Wydrukować bilety", "Zamówić kawę w ziarnach"],
        futuro=["Rozliczenie PIT", "Ślub Clary", "Odnowić domenę"],
        viaje="Podróż do Lizbony",
        viaje_tareas=["Zarezerwować hotel", "Kupić przewodnik", "Spakować ładowarkę"],
        ideas="Pomysły na salon",
        ideas_notas=["Lampa przy oknie", "Ciemnozielony dywan", "Oprawić mapę w ramkę"],
        agua="Woda",
        filas=["Dwa litry", "Rozciągać się", "Czytać 20 minut"],
    ),
    "ru-RU": dict(
        hoy=["Купить чернила", "Ужин с Аной", "Полить цветы", "Отправить смету", "Книжный открывается в 10", "Позвонить в банк"],
        ayer=["Забрать посылку", "Занятие по керамике", "Идея: полка в коридоре"],
        mes_dias=["День рождения Лео", "Стоматолог", "Поездка в Казань", "ТО машины"],
        mes_tareas=["Заменить загранпаспорт", "Оплатить страховку", "Разобрать фото"],
        dos_veces="Записаться в МФЦ",
        anterior=["Вернуть книгу Марте", "Поменять лампочку в ванной", "Починить фару на велосипеде"],
        cerradas=["Помыть холодильник", "Ответить Пабло", "Купить марки", "Распечатать билеты", "Заказать кофе в зёрнах"],
        futuro=["Налоговая декларация", "Свадьба Клары", "Продлить домен"],
        viaje="Поездка в Казань",
        viaje_tareas=["Забронировать отель", "Купить путеводитель", "Взять зарядку"],
        ideas="Идеи для гостиной",
        ideas_notas=["Лампа у окна", "Тёмно-зелёный ковёр", "Карту в рамку"],
        agua="Вода",
        filas=["Два литра", "Растяжка", "Читать 20 минут"],
    ),
    "tr-TR": dict(
        hoy=["Mürekkep al", "Ana ile akşam yemeği", "Çiçekleri sula", "Teklifi gönder", "Kitapçı saat 10'da açılıyor", "Bankayı ara"],
        ayer=["Kargoyu al", "Seramik dersi", "Fikir: koridora bir raf"],
        mes_dias=["Leo'nun doğum günü", "Diş doktoru", "Lizbon gezisi", "Araba bakımı"],
        mes_tareas=["Pasaportu yenile", "Sigortayı öde", "Fotoğrafları düzenle"],
        dos_veces="Belediyeden randevu al",
        anterior=["Marta'nın kitabını geri ver", "Banyodaki ampulü değiştir", "Bisikletin ışığını tamir et"],
        cerradas=["Buzdolabını temizle", "Pablo'ya cevap ver", "Pul al", "Biletleri yazdır", "Çekirdek kahve sipariş et"],
        futuro=["Vergi beyannamesi", "Clara'nın düğünü", "Alan adını yenile"],
        viaje="Lizbon gezisi",
        viaje_tareas=["Otel rezervasyonu yap", "Rehber kitabı al", "Şarj aletini çantaya koy"],
        ideas="Salon fikirleri",
        ideas_notas=["Pencerenin yanına lamba", "Koyu yeşil halı", "Haritayı çerçevelet"],
        agua="Su",
        filas=["İki litre", "Esneme", "20 dakika oku"],
    ),
    "id": dict(
        hoy=["Beli tinta", "Makan malam dengan Ana", "Siram tanaman", "Kirim penawaran", "Toko buku buka jam 10", "Telepon bank"],
        ayer=["Ambil paket", "Kelas keramik", "Ide: rak di lorong"],
        mes_dias=["Ulang tahun Leo", "Dokter gigi", "Liburan ke Bali", "Servis mobil"],
        mes_tareas=["Perpanjang SIM", "Bayar asuransi", "Rapikan foto"],
        dos_veces="Buat janji di kelurahan",
        anterior=["Kembalikan buku Marta", "Ganti lampu kamar mandi", "Perbaiki lampu sepeda"],
        cerradas=["Bersihkan kulkas", "Balas pesan Pablo", "Beli perangko", "Cetak tiket", "Pesan biji kopi"],
        futuro=["Lapor SPT pajak", "Pernikahan Clara", "Perpanjang domain"],
        viaje="Liburan ke Bali",
        viaje_tareas=["Pesan hotel", "Beli buku panduan", "Masukkan charger ke tas"],
        ideas="Ide untuk ruang tamu",
        ideas_notas=["Lampu dekat jendela", "Karpet hijau tua", "Pigura peta"],
        agua="Air",
        filas=["Dua liter", "Peregangan", "Baca 20 menit"],
    ),
    "ja-JP": dict(
        hoy=["インクを買う", "アナと夕食", "植物に水をやる", "見積もりを送る", "本屋は10時に開店", "銀行に電話する"],
        ayer=["荷物を受け取る", "陶芸教室", "アイデア: 廊下に棚を置く"],
        mes_dias=["レオの誕生日", "歯医者", "リスボン旅行", "車の点検"],
        mes_tareas=["パスポートを更新する", "保険料を払う", "写真を整理する"],
        dos_veces="市役所の予約を取る",
        anterior=["マルタに本を返す", "浴室の電球を替える", "自転車のライトを直す"],
        cerradas=["冷蔵庫を掃除する", "パブロに返信する", "切手を買う", "チケットを印刷する", "コーヒー豆を注文する"],
        futuro=["確定申告", "クララの結婚式", "ドメインを更新する"],
        viaje="リスボン旅行",
        viaje_tareas=["ホテルを予約する", "ガイドブックを買う", "充電器を入れる"],
        ideas="リビングのアイデア",
        ideas_notas=["窓のそばにランプ", "濃い緑のラグ", "地図を額に入れる"],
        agua="水分",
        filas=["2リットル", "ストレッチ", "20分読書"],
    ),
    "ko-KR": dict(
        hoy=["잉크 사기", "아나와 저녁 식사", "화분에 물 주기", "견적서 보내기", "서점은 10시에 열어요", "은행에 전화하기"],
        ayer=["택배 찾기", "도예 수업", "아이디어: 복도에 선반"],
        mes_dias=["레오 생일", "치과", "리스본 여행", "자동차 점검"],
        mes_tareas=["여권 갱신", "보험료 내기", "사진 정리하기"],
        dos_veces="구청 방문 예약하기",
        anterior=["마르타에게 책 돌려주기", "욕실 전구 갈기", "자전거 라이트 고치기"],
        cerradas=["냉장고 청소", "파블로에게 답장", "우표 사기", "티켓 인쇄", "원두 주문"],
        futuro=["종합소득세 신고", "클라라의 결혼식", "도메인 갱신"],
        viaje="리스본 여행",
        viaje_tareas=["호텔 예약하기", "가이드북 사기", "충전기 챙기기"],
        ideas="거실 아이디어",
        ideas_notas=["창가에 조명", "짙은 초록색 러그", "지도 액자에 넣기"],
        agua="물",
        filas=["2리터", "스트레칭", "20분 읽기"],
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
