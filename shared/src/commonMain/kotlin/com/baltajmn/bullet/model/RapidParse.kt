package com.baltajmn.bullet.model

/** What [rapidParse] read out of the typed line: the bullet, its signifiers and the text left after every prefix. */
data class Parsed(val bullet: Bullet, val signifiers: Set<Signifier>, val text: String)

/**
 * Reads the bullet and the signifiers straight out of what was typed, like the paper method does
 * (docs/tecnico.md 6.2): no picker decides on its own what an entry is.
 *
 * The line is scanned from the start for a two-character prefix not used yet: `"- "` and `"o "` each
 * give a bullet (only one is ever consumed), `"* "`, `"! "` and `"? "` each add one signifier, in any
 * order and mixed freely with the bullet prefix. Consumption stops at the first character that is
 * not an unused prefix, so a dash in the middle of a sentence, or a second `"- "` once NOTE is
 * already set, is left as text. [picked] (the row's optional Task/Event/Note selector) only fills in
 * when nothing typed set a bullet; a written prefix always wins over it. Returns null when nothing is
 * left to save once every prefix is stripped and the remainder is trimmed.
 */
fun rapidParse(input: String, picked: Bullet? = null): Parsed? {
    var s = input.oneLine().trimStart()
    var bullet: Bullet? = null
    val signifiers = mutableSetOf<Signifier>()

    consuming@ while (true) {
        when {
            bullet == null && s.startsWith("- ") -> {
                bullet = Bullet.NOTE
                s = s.substring(2)
            }
            bullet == null && s.startsWith("o ") -> {
                bullet = Bullet.EVENT
                s = s.substring(2)
            }
            Signifier.PRIORITY !in signifiers && s.startsWith("* ") -> {
                signifiers += Signifier.PRIORITY
                s = s.substring(2)
            }
            Signifier.INSPIRATION !in signifiers && s.startsWith("! ") -> {
                signifiers += Signifier.INSPIRATION
                s = s.substring(2)
            }
            Signifier.EXPLORE !in signifiers && s.startsWith("? ") -> {
                signifiers += Signifier.EXPLORE
                s = s.substring(2)
            }
            else -> break@consuming
        }
    }

    val text = s.trim()
    if (text.isEmpty()) return null
    return Parsed(bullet ?: picked ?: Bullet.TASK, signifiers, text)
}
