# Diseno de las capturas de tienda: Bobbin

Lo que hace unicas las capturas de Bobbin (2-11-2026). Se pintan con la plantilla de `gk/contenido/plantilla/tienda/`:
el estilo en `estilos.js` (clave `bobbin`), los frames en `apps.js` y los demas idiomas en `textos.js`. Toda captura nueva
(otra pantalla, otro tamano, otra tienda) sigue estas reglas. Las comunes a la familia estan en el README de la plantilla.

## Idea

Un cuaderno de puntos y un hilo. El fondo es la cuadricula de puntos de un bullet journal; el hilo del icono cruza la parte
de arriba de cada captura y empalma con la siguiente, asi que en la tienda la galeria es un solo hilo. Lo resaltado del
titular va subrayado con ese mismo hilo.

## Color (del icono)

| Nombre | Hex | Uso |
|---|---|---|
| Carbon | `#26221B` | fondo de todas |
| Salvia | `#B4CCA8` | hilo, puntos (al 17 %), subrayado (al 55 %), pastillas (borde y texto), pegatina |
| Papel | `#F3EEE3` | titular |
| Papel claro | `#FBF8F3` | fondo de la pantalla |

Un solo color de acento, el salvia. Nada mas.

## Letra

Literata en su peso normal (la estatica de la app), interletra -0,01 em: mas de cuaderno que la Literata gruesa de Quilt.

## Composicion

- Hilo: una onda de 12 px de grosor a 112 px del borde de arriba, que empieza y acaba a la misma altura y con la misma
  pendiente, para que empalme con la captura de al lado.
- Titular desde 230 px, debajo del hilo; pastillas solo con borde salvia.
- Puntos cada 44 px en todo el lienzo.
- La pantalla, sin inclinar, con 70 px de margen, esquinas de 30 px como una hoja y sombra oscura.
- Pegatina: salvia con texto carbon.

## Lo que no se hace

- Varios colores o retales: son de Quilt, la otra app oscura de la familia.
- Cortar o mover el hilo en una captura: rompe la galeria.
- Inclinar la pantalla.

## Anadir una captura

1. Capturas sin procesar de iPhone (simulador "Capturas-Bobbin") en `store/screenshots/raw/iphone/<idioma>/` y de Android
   (`tools/store/crudas.py`, Pixel 8) en `store/screenshots/raw/android/<idioma>/`.
2. Frame en `apps.js` (`bobbin`): titular de 6 palabras como mucho con `[[resaltado]]`, pastillas, recorte por tienda. Los
   demas idiomas en `textos.js`, con sonnet.
3. `./todas.sh bobbin` y revisor sonnet aparte con liston 9.
