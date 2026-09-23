package com.baltajmn.bullet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import bobbin.shared.generated.resources.Res
import bobbin.shared.generated.resources.literata_regular
import org.jetbrains.compose.resources.Font

/**
 * The two voices of docs/pantallas.md 1.3: Literata for what the user wrote (`Ink`, `PageTitle`),
 * the system font for everything around it. Each style already carries its color, so a call site
 * never mixes one up. Line heights are `gridUnit` multiples so text follows the page grid at any
 * font scale.
 */
object Type {
    val literata: FontFamily @Composable get() = FontFamily(Font(Res.font.literata_regular))

    val Ink: TextStyle @Composable get() = ink(17.sp, 24.sp)
    val PageTitle: TextStyle @Composable get() = ink(24.sp, 48.sp)

    val Body: TextStyle @Composable get() = system(15.sp, 24.sp, FontWeight.Normal, colors.onBackground)
    val Secondary: TextStyle @Composable get() = system(13.sp, 24.sp, FontWeight.Normal, colors.onSurfaceVariant)

    /** Block and section labels. The caller passes the text through uppercase(). */
    val Eyebrow: TextStyle @Composable get() =
        system(11.sp, 24.sp, FontWeight.Medium, colors.onSurfaceVariant).copy(letterSpacing = 1.4.sp)

    private val colors @Composable get() = MaterialTheme.colorScheme

    @Composable
    private fun ink(size: TextUnit, line: TextUnit) =
        TextStyle(fontFamily = literata, fontSize = size, lineHeight = line, color = colors.onBackground)

    private fun system(size: TextUnit, line: TextUnit, weight: FontWeight, color: Color) =
        TextStyle(fontSize = size, lineHeight = line, fontWeight = weight, color = color)
}
