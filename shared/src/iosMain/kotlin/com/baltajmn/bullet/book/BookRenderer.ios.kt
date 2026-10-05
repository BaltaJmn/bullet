package com.baltajmn.bullet.book

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreFoundation.CFURLRef
import platform.CoreGraphics.CGContextAddLineToPoint
import platform.CoreGraphics.CGContextBeginPath
import platform.CoreGraphics.CGContextFillEllipseInRect
import platform.CoreGraphics.CGContextFillRect
import platform.CoreGraphics.CGContextMoveToPoint
import platform.CoreGraphics.CGContextSetFillColorWithColor
import platform.CoreGraphics.CGContextSetLineCap
import platform.CoreGraphics.CGContextSetLineJoin
import platform.CoreGraphics.CGContextSetLineWidth
import platform.CoreGraphics.CGContextSetStrokeColorWithColor
import platform.CoreGraphics.CGContextStrokeEllipseInRect
import platform.CoreGraphics.CGContextStrokePath
import platform.CoreGraphics.CGLineCap
import platform.CoreGraphics.CGLineJoin
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.CoreText.CTFontManagerRegisterFontsForURL
import platform.CoreText.kCTFontManagerScopeProcess
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.NSStrikethroughStyleAttributeName
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIGraphicsBeginPDFContextToFile
import platform.UIKit.UIGraphicsBeginPDFPage
import platform.UIKit.UIGraphicsEndPDFContext
import platform.UIKit.UIGraphicsGetCurrentContext
import platform.UIKit.drawAtPoint
import platform.UIKit.sizeWithAttributes
import com.baltajmn.bullet.data.toNSData

@OptIn(ExperimentalForeignApi::class)
actual object BookRenderer {
    private const val FONT = "Literata-Regular"
    private var loaded = false
    private var open = false
    private val path get() = NSTemporaryDirectory() + "bobbin-book.pdf"

    actual fun load(font: ByteArray): Boolean {
        if (loaded) return true
        // CTFontManager registers from a file: the font is written once next to the book.
        val file = NSTemporaryDirectory() + "literata.ttf"
        if (!font.toNSData().writeToFile(file, atomically = true)) return false
        val url = CFBridgingRetain(NSURL.fileURLWithPath(file)) as CFURLRef?
        CTFontManagerRegisterFontsForURL(url, kCTFontManagerScopeProcess, null)
        CFBridgingRelease(url)
        loaded = UIFont.fontWithName(FONT, 12.0) != null
        return loaded
    }

    private fun font(size: Float) = UIFont.fontWithName(FONT, size.toDouble()) ?: UIFont.systemFontOfSize(size.toDouble())

    actual fun measure(text: String, size: Float): Float =
        NSString.create(string = text).sizeWithAttributes(mapOf<Any?, Any?>(NSFontAttributeName to font(size))).useContents { width.toFloat() }

    actual fun begin(): Boolean {
        abort()
        UIGraphicsBeginPDFContextToFile(path, CGRectMake(0.0, 0.0, BOOK_W.toDouble(), BOOK_H.toDouble()), null)
        open = true
        return true
    }

    actual fun draw(page: List<Mark>, paper: List<Mark>) {
        if (!open) return
        UIGraphicsBeginPDFPage()
        val c = UIGraphicsGetCurrentContext() ?: return
        val fill = page.filterIsInstance<Mark.Fill>().firstOrNull()
        CGContextSetFillColorWithColor(c, color(fill?.color ?: BOOK_PAPER).CGColor)
        CGContextFillRect(c, CGRectMake(0.0, 0.0, BOOK_W.toDouble(), BOOK_H.toDouble()))
        CGContextSetLineCap(c, CGLineCap.kCGLineCapRound)
        CGContextSetLineJoin(c, CGLineJoin.kCGLineJoinRound)
        for (mark in (if (fill == null) paper else emptyList()) + page) {
            when (mark) {
                is Mark.Fill -> Unit
                is Mark.Text -> {
                    val f = font(mark.size)
                    val attributes = mutableMapOf<Any?, Any?>(NSFontAttributeName to f, NSForegroundColorAttributeName to color(mark.color))
                    if (mark.strike) attributes[NSStrikethroughStyleAttributeName] = 1
                    // UIKit places a line by its top: the baseline is an ascender lower.
                    NSString.create(string = mark.text).drawAtPoint(CGPointMake(mark.x.toDouble(), mark.baseline - f.ascender), attributes)
                }
                is Mark.Line -> {
                    CGContextSetStrokeColorWithColor(c, color(mark.color).CGColor)
                    CGContextSetLineWidth(c, mark.width.toDouble())
                    CGContextBeginPath(c)
                    mark.points.forEachIndexed { i, (x, y) ->
                        if (i == 0) CGContextMoveToPoint(c, x.toDouble(), y.toDouble()) else CGContextAddLineToPoint(c, x.toDouble(), y.toDouble())
                    }
                    CGContextStrokePath(c)
                }
                is Mark.Dot -> {
                    val r = mark.radius.toDouble()
                    val rect = CGRectMake(mark.x - r, mark.y - r, 2 * r, 2 * r)
                    if (mark.stroke > 0f) {
                        CGContextSetStrokeColorWithColor(c, color(mark.color).CGColor)
                        CGContextSetLineWidth(c, mark.stroke.toDouble())
                        CGContextStrokeEllipseInRect(c, rect)
                    } else {
                        CGContextSetFillColorWithColor(c, color(mark.color).CGColor)
                        CGContextFillEllipseInRect(c, rect)
                    }
                }
            }
        }
    }

    actual fun end(): String? {
        if (!open) return null
        UIGraphicsEndPDFContext()
        open = false
        return path.takeIf { NSFileManager.defaultManager.fileExistsAtPath(it) }
    }

    actual fun abort() {
        if (open) UIGraphicsEndPDFContext()
        open = false
        NSFileManager.defaultManager.removeItemAtPath(path, null)
    }

    private fun color(argb: Long) = UIColor(
        red = ((argb shr 16) and 0xFF) / 255.0,
        green = ((argb shr 8) and 0xFF) / 255.0,
        blue = (argb and 0xFF) / 255.0,
        alpha = ((argb shr 24) and 0xFF) / 255.0,
    )
}
