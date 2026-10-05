package com.baltajmn.bullet.book

import bobbin.shared.generated.resources.Res
import com.baltajmn.bullet.model.Journal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * Draws a [Book] into a PDF (docs/tecnico.md 12.6): Android with `PdfDocument`, iOS with a UIKit PDF
 * context, both with Literata. One page at a time into a temporary file, never the whole book in memory.
 */
expect object BookRenderer {
    /** Takes Literata's bytes once; false when the platform cannot use them. */
    fun load(font: ByteArray): Boolean

    /** The width of [text] in Literata at [size] points. */
    fun measure(text: String, size: Float): Float

    fun begin(): Boolean

    /** One page: its own [Mark.Fill] if it has one, else the paper, then everything else on top. */
    fun draw(page: List<Mark>, paper: List<Mark>)

    /** Closes the file and returns its path, or null when it could not be written. */
    fun end(): String?

    fun abort()
}

/**
 * Lays out and draws the book off the main thread, saying after each page how far it is. Cancelling the
 * coroutine stops between two pages and leaves no file behind. Returns the PDF's path, or null.
 */
@OptIn(ExperimentalResourceApi::class)
suspend fun makeBook(j: Journal, cover: Long, paper: String, onPage: (Int, Int) -> Unit): String? = withContext(Dispatchers.Default) {
    if (!BookRenderer.load(Res.readBytes("font/literata_regular.ttf"))) return@withContext null
    val book = layoutBook(j, cover, paper, TimeZone.currentSystemDefault(), BookRenderer::measure)
    if (!BookRenderer.begin()) return@withContext null
    try {
        book.pages.forEachIndexed { i, page ->
            ensureActive()
            BookRenderer.draw(page, if (i == 0) emptyList() else book.paper)
            onPage(i + 1, book.size)
        }
        BookRenderer.end()
    } catch (e: CancellationException) {
        BookRenderer.abort()
        throw e
    } catch (e: Exception) {
        BookRenderer.abort()
        null
    }
}
