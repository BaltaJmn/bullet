# Quejas repetidas del nicho y qué hace Bobbin con cada una

Regla 12 de `miniapps/ANTI-4.3.md` ("Lo que el usuario necesita"). Salen de `SPEC.md` 1 (la tabla de
benchmark, repasada entera el 30-10-2026 con reseñas de 2025 y 2026, y la lista "Fallos del mercado").
Bobbin no está publicada todavía (Apple 1.0.0 en revisión, Play en prueba cerrada), así que no hay
reseñas propias; esta tabla se rehace con las primeras. Cada queja queda en una de tres: **cubierta**
(ya está en la 1.0), **arreglada ahora** (cambio de esta rama) o **descartada** (con el porqué).

El líder por búsqueda y reseñas en iOS es Bullet (Mui Studio); le siguen COMO Bujo y BujoFlow, y de
fondo la app oficial (Companion) y May en Android.

## Del líder y de los siguientes

| Queja repetida | De quién | Estado | Dónde |
|---|---|---|---|
| Migrar es cambiar la fecha de una entrada, sin decidir | Bullet, Hejour | Cubierta | La migración es una decisión por tarea (#13, #27); `SPEC.md` 2.10 |
| Bloqueo biométrico de pago | Bullet | Cubierta | Bloqueo gratis (#39); `SPEC.md` 7 |
| Identificadores de rastreo y datos ligados a la persona | Bullet, Waffle | Cubierta | Nada sale del teléfono salvo la compra (#59, `store/formularios.md`) |
| Datos que desaparecen al actualizar | Bullet 1.78 | Cubierta | Almacén atómico con `.bak` y cuarentena (#14), migración de esquema sin pérdida (#15), copia del sistema (#46) |
| Suscripción cara frente a lo que da la app | Bullet, COMO, BujoFlow, Reflectly | Cubierta | Compra única, sin suscripción (`SPEC.md` 7, excepción a la regla 9 más abajo) |
| Solo iOS | COMO, Companion, Tiimo, NotePlan | Cubierta | Android e iOS desde la 1.0, con el mismo cuidado |
| Fallos en iPad | COMO | Cubierta | Doble página en tableta y plegable (#80) |
| Sin índice ni guía de inicio | Companion | Cubierta | Índice (#29), guía de cuatro pasos que se salta (#31) |
| El recordatorio de reflexión desaparece | Companion | Cubierta | Se reprograma al arrancar, al reiniciar y al cambiar de hora (#36 a #38) |
| Bloqueo al elegir fechas | Companion | Cubierta | Programar elige el mes de una lista, nunca un selector de calendario (#22, #25) |
| Anuncios, 14 SDK y cierres | May | Cubierta | Sin anuncios en ningún plan (#2, #59) |
| Sincronización que duplica o pierde | Obsidian, TickTick | Cubierta | Un solo fichero, elección explícita en conflicto, `.bak` previo (#69) |
| Datos atrapados, sin exportar | Daylio, Diaro | Cubierta | JSON y Markdown gratis (#44), importar fusionando (#45) |
| Sincronizar sin cifrado real | Journey | Cubierta | La sincronización va a la nube del propio usuario, sin servidor nuestro (#69) |
| Quiero apuntar sin abrir la app | Reseñas varias (`SPEC.md` 1, "Lo que se pide de verdad") | Cubierta | Widget y control para escribir (#79), captura sin abrir la app (Pro) |
| Quiero imprimir mi cuaderno | Reseñas varias, bujo en papel | Cubierta, ahora visible | Libro A5 en PDF (#68, Pro); tarjeta única en Hoy tras cerrar el primer mes |
| Nadie recuerda valorar ni sabe dónde | Todas las fichas con pocas valoraciones | Arreglada ahora | Fila "Valorar Bobbin" en Ajustes, y el aviso del sistema solo tras 2 días, sin Pro abierto y una vez |
| Estética de pegatinas y redes | COMO, Zinnia, DailyBean | Descartada | El método nació con materiales baratos y de baja fricción; la estética de redes socava su propósito (`SPEC.md` 1, "El cuaderno de papel") |
| Migración automática | Hejour | Descartada | La fricción de decidir es el mecanismo del método; las comparativas de 2026 lo confirman |
| Escritorio, Mac y web | Hejour, Jott, NotePlan | Descartada | Decisión del autor del 30-10-2026: Bobbin es Android e iOS y nada más |
| Liquid Glass e IA en un diario | Prensa y Apple, no usuarios | Descartada | Nadie lo pide en las reseñas; coste recurrente que obligaría a suscripción |
| Compartir diario con cuenta y servidor | Day One, Waffle | Descartada | Cuenta y servidor rompen la promesa; se cubre con listas enviadas como fichero (#75) |

## Excepción a la regla 9: Pro sin suscripción, a propósito

La regla 9 pide "suscripción y pago unico". Bobbin se queda solo con el pago único, y se apunta aquí y
en `SPEC.md` 7 para que nadie lo "corrija" más adelante:

- **Pro cumple la regla en contenido.** Sincronización por la nube propia, libro A5 en PDF, captura sin
  abrir la app y widgets: cuatro motivos de peso, todos pedidos por quien usa el cuaderno a diario.
- **No hay coste recurrente que sostener.** La sincronización va por iCloud Drive o la carpeta que
  elige la persona, sin servidor nuestro: un usuario cuesta 0 EUR al mes. Una suscripción no tendría
  qué financiar.
- **"Sin suscripción" es el filo contra BujoFlow, Bullet y COMO**, que cobran de 1,99 a 3,99 $ al mes
  o de 14,99 a 35,99 $ al año, y es lo que más se reprocha en el nicho (TickTick, Notion, Reflectly).
- El precio (1,99 EUR) no se toca aquí: es la decisión abierta de la issue #74, del autor.
