package com.baltajmn.bullet.data

/**
 * Search over the loaded [com.baltajmn.bullet.model.Journal], in memory: no network, no index and no
 * second store (docs/tecnico.md 6.8). `tags` and `search` land with #35; [fold] is here from #29,
 * which is what the Index filter compares titles with.
 */

/**
 * Lowercase without diacritics, so "cafe" finds "Café" and "strasse" finds "Straße". Copied from
 * `line/.../model/Text.kt`: no `java.text.Normalizer` in common code, and a table of the letters the
 * five languages actually use is smaller than carrying an ICU.
 */
fun fold(s: String): String {
    val out = StringBuilder(s.length)
    for (c in s.lowercase()) {
        // Decomposed accents arrive from pastes and some keyboards: the letter stays, the mark goes.
        if (c.code in 0x300..0x36F) continue
        when (c) {
            'á', 'à', 'â', 'ã', 'ä', 'å' -> out.append('a')
            'é', 'è', 'ê', 'ë' -> out.append('e')
            'í', 'ì', 'î', 'ï' -> out.append('i')
            'ó', 'ò', 'ô', 'õ', 'ö' -> out.append('o')
            'ú', 'ù', 'û', 'ü' -> out.append('u')
            'ý', 'ÿ' -> out.append('y')
            'ç' -> out.append('c')
            'ñ' -> out.append('n')
            'ß' -> out.append("ss")
            'œ' -> out.append("oe")
            'æ' -> out.append("ae")
            else -> out.append(c)
        }
    }
    return out.toString()
}
