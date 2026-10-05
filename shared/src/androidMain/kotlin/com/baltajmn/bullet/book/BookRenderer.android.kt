package com.baltajmn.bullet.book

import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.baltajmn.bullet.data.AndroidContext
import java.io.File

actual object BookRenderer {
    private var typeface: Typeface? = null
    private var doc: PdfDocument? = null
    private var number = 0
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val file get() = File(AndroidContext.value.cacheDir, "book/book.pdf")

    actual fun load(font: ByteArray): Boolean = typeface != null || runCatching {
        // Typeface reads a font from a file, not from bytes.
        val f = File(AndroidContext.value.cacheDir, "book/literata.ttf").apply { parentFile?.mkdirs(); writeBytes(font) }
        typeface = Typeface.createFromFile(f)
    }.isSuccess

    actual fun measure(text: String, size: Float): Float {
        paint.typeface = typeface
        paint.textSize = size
        return paint.measureText(text)
    }

    actual fun begin(): Boolean {
        abort()
        doc = PdfDocument()
        number = 0
        return true
    }

    actual fun draw(page: List<Mark>, paper: List<Mark>) {
        val d = doc ?: return
        val p = d.startPage(PdfDocument.PageInfo.Builder(BOOK_W.toInt(), BOOK_H.toInt(), ++number).create())
        val c = p.canvas
        val fill = page.filterIsInstance<Mark.Fill>().firstOrNull()
        c.drawColor((fill?.color ?: BOOK_PAPER).toInt())
        for (mark in (if (fill == null) paper else emptyList()) + page) {
            paint.reset()
            paint.isAntiAlias = true
            when (mark) {
                is Mark.Fill -> Unit
                is Mark.Text -> {
                    paint.typeface = typeface
                    paint.textSize = mark.size
                    paint.color = mark.color.toInt()
                    paint.isStrikeThruText = mark.strike
                    c.drawText(mark.text, mark.x, mark.baseline, paint)
                }
                is Mark.Line -> {
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = mark.width
                    paint.strokeCap = Paint.Cap.ROUND
                    paint.strokeJoin = Paint.Join.ROUND
                    paint.color = mark.color.toInt()
                    val path = Path()
                    mark.points.forEachIndexed { i, (x, y) -> if (i == 0) path.moveTo(x, y) else path.lineTo(x, y) }
                    c.drawPath(path, paint)
                }
                is Mark.Dot -> {
                    paint.color = mark.color.toInt()
                    if (mark.stroke > 0f) {
                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = mark.stroke
                    }
                    c.drawCircle(mark.x, mark.y, mark.radius, paint)
                }
            }
        }
        d.finishPage(p)
    }

    actual fun end(): String? {
        val d = doc ?: return null
        doc = null
        return try {
            file.parentFile?.mkdirs()
            file.outputStream().use { d.writeTo(it) }
            file.path
        } catch (e: Exception) {
            file.delete()
            null
        } finally {
            d.close()
        }
    }

    actual fun abort() {
        doc?.close()
        doc = null
        file.delete()
    }
}
