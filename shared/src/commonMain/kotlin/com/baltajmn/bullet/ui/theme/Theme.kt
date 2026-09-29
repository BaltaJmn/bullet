package com.baltajmn.bullet.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.baltajmn.bullet.model.Settings

/**
 * Ink on paper, not a generic task manager (docs/pantallas.md 1.2, SPEC 5). Adapted from the
 * family's palette (line/docs/pantallas.md 1.1) with two changes in light, for AA contrast on the
 * paper: `onSurfaceVariant` (was 8B8479, 3.5:1) and `primary` (was 6FAE9B, 2.4:1). Dark already
 * passed and is unchanged. `error` is never shown; it reads as `onSurfaceVariant` in case a
 * Material component reaches for it on its own, so nothing is ever painted red.
 */
internal val Light = lightColorScheme(
    primary = Color(0xFF3F7A69),
    onPrimary = Color(0xFFFFFFFF),
    background = Color(0xFFFBF8F3),
    onBackground = Color(0xFF39352E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF39352E),
    surfaceVariant = Color(0xFFF0EBE2),
    onSurfaceVariant = Color(0xFF736D63),
    outline = Color(0xFFE3DCD1),
    outlineVariant = Color(0xFFEFE9DF),
    error = Color(0xFF736D63),
)

internal val Dark = darkColorScheme(
    primary = Color(0xFF8FC9B6),
    onPrimary = Color(0xFF12271F),
    background = Color(0xFF17150F),
    onBackground = Color(0xFFECE5D9),
    surface = Color(0xFF201D16),
    onSurface = Color(0xFFECE5D9),
    surfaceVariant = Color(0xFF2C2820),
    onSurfaceVariant = Color(0xFF9C9486),
    outline = Color(0xFF3A352B),
    outlineVariant = Color(0xFF2C2820),
    error = Color(0xFF9C9486),
)

@Composable
fun BobbinTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) Dark else Light, content = content)
}

/** docs/pantallas.md 21: the one cut between phone and tablet, the same on Android and on iOS. */
val WIDE_SCREEN_FROM = 600.dp

/** A page on a wide window: 24 columns of dots (docs/pantallas.md 1.1 and 21). */
val MAX_CONTENT_WIDTH = 576.dp

fun isWideScreen(windowWidth: Dp): Boolean = windowWidth >= WIDE_SCREEN_FROM

/** The window's width decides, so portrait and landscape are the same rule (docs/pantallas.md 21). */
@Composable
fun isWideScreen(): Boolean = with(LocalDensity.current) { isWideScreen(LocalWindowInfo.current.containerSize.width.toDp()) }

/**
 * The family's eight pastels (docs/tecnico.md 5), in its order. A cover only tints today's dot and
 * number, the widgets and, washed out by [coverSoft], the active tab and the notice cards
 * (docs/pantallas.md 1.2): never the text or the paper.
 */
enum class Cover(val id: String, val color: Color) {
    Rose("rose", Color(0xFFF0AFBE)),
    Peach("peach", Color(0xFFF5C39B)),
    Butter("butter", Color(0xFFEDDC98)),
    Sage("sage", Color(0xFFB6D6AB)),
    Mint("mint", Color(0xFF9CD3C7)),
    Sky("sky", Color(0xFFA2C3E9)),
    Periwinkle("periwinkle", Color(0xFFB4B8EC)),
    Lilac("lilac", Color(0xFFD9AFE6));

    companion object {
        /** An id this version does not know reads as the free one (docs/tecnico.md 5). */
        fun of(id: String): Cover = entries.find { it.id == id } ?: Sage
    }
}

enum class Paper(val id: String) {
    Dotted("dotted"),
    Lined("lined"),
    Grid("grid"),
    Blank("blank");

    companion object {
        fun of(id: String): Paper = entries.find { it.id == id } ?: Dotted
    }
}

/**
 * The cover's pale wash (docs/pantallas.md 1.2): behind the active tab's icon, the notice cards and the
 * picked day's number. Mixed into the paper, so it stays readable under ink in light and in dark.
 */
@Composable
fun coverSoft(cover: Cover): Color {
    val paper = MaterialTheme.colorScheme.background
    return lerp(paper, cover.color, if (paper.luminance() > 0.5f) 0.35f else 0.16f)
}

fun canUse(cover: Cover, isPro: Boolean) = isPro || cover == Cover.Sage
fun canUse(paper: Paper, isPro: Boolean) = isPro || paper == Paper.Dotted

/**
 * What is painted, not what was chosen (docs/tecnico.md 6.17): losing Pro to a refund falls back to
 * the free ones without rewriting `settings`, so the choice comes back if Pro does.
 */
fun activeCover(s: Settings, isPro: Boolean): Cover = Cover.of(s.cover).takeIf { canUse(it, isPro) } ?: Cover.Sage
fun activePaper(s: Settings, isPro: Boolean): Paper = Paper.of(s.paper).takeIf { canUse(it, isPro) } ?: Paper.Dotted
