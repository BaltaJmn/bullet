package com.baltajmn.bullet.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.data.ShareContent
import com.baltajmn.bullet.data.Sharing
import com.baltajmn.bullet.data.encodeToPng
import com.baltajmn.bullet.data.shareText
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.ui.theme.MAX_CONTENT_WIDTH
import com.baltajmn.bullet.ui.theme.Type
import com.baltajmn.bullet.ui.theme.activePaper
import com.baltajmn.bullet.ui.theme.gridUnit

/**
 * The share sheet (docs/pantallas.md 17.1): a bottom sheet and not a destination, with the first page to
 * scale and two ways out to the system's own sheet. Free always, and only what the user chose.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareSheet(content: ShareContent, onClose: () -> Unit) {
    val journal = BobbinRepository.journal
    val literata = Type.literata
    val resolver = LocalFontFamilyResolver.current
    // Density 1, not the screen's: every size on the page is a pixel of the image (17.2).
    val measurer = remember(resolver) { TextMeasurer(resolver, Density(1f), LayoutDirection.Ltr) }
    val paper = activePaper(journal.settings, BobbinRepository.isPro)
    val pages = remember(content, paper, literata) { renderSharePages(content, journal, paper, literata, measurer) }
    val shape = RoundedCornerShape(12.dp)

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetMaxWidth = MAX_CONTENT_WIDTH,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 0.dp,
    ) {
        Column(Modifier.fillMaxWidth()) {
            Image(
                pages.first(),
                contentDescription = content.title,
                modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth().aspectRatio(SHARE_W / SHARE_H.toFloat())
                    .clip(shape).border(1.dp, MaterialTheme.colorScheme.outline, shape),
            )
            if (pages.size > 1) {
                Text(S.sharePages(pages.size), style = Type.Secondary, modifier = Modifier.padding(start = 24.dp, top = 8.dp))
            }
            Spacer(Modifier.height(gridUnit))
            SheetRow(glyph = null, label = S.shareImage) {
                Sharing.sharePngs(pages.map { it.encodeToPng() })
                onClose()
            }
            SheetRow(glyph = null, label = S.shareText) {
                Sharing.shareText(shareText(content))
                onClose()
            }
            Spacer(Modifier.height(gridUnit))
        }
    }
}
