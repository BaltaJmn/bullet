package com.baltajmn.bullet.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.data.ShareContent
import com.baltajmn.bullet.data.ShareFormat
import com.baltajmn.bullet.data.Sharing
import com.baltajmn.bullet.data.TrackerShare
import com.baltajmn.bullet.data.encodeToPng
import com.baltajmn.bullet.data.shareText
import com.baltajmn.bullet.data.trackerShareText
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.ui.theme.MAX_CONTENT_WIDTH
import com.baltajmn.bullet.ui.theme.Type
import com.baltajmn.bullet.ui.theme.activeCover
import com.baltajmn.bullet.ui.theme.activePaper
import com.baltajmn.bullet.ui.theme.gridUnit

/** A day, a month or a list (docs/pantallas.md 17.1). */
@Composable
fun ShareSheet(content: ShareContent, onClose: () -> Unit) {
    val journal = BobbinRepository.journal
    val literata = Type.literata
    val measurer = pixelMeasurer()
    val paper = activePaper(journal.settings, BobbinRepository.isPro)
    PagesSheet(content.title, shareText(content), onClose) { format ->
        remember(content, paper, literata, format) { renderSharePages(content, journal, paper, literata, measurer, format) }
    }
}

/** A tracker's month (docs/pantallas.md 23.12): its grid, with the marks in the active cover. */
@Composable
fun TrackerShareSheet(tracker: TrackerShare, onClose: () -> Unit) {
    val settings = BobbinRepository.journal.settings
    val literata = Type.literata
    val measurer = pixelMeasurer()
    val paper = activePaper(settings, BobbinRepository.isPro)
    val mark = activeCover(settings, BobbinRepository.isPro).color
    PagesSheet(tracker.title, trackerShareText(tracker), onClose) { format ->
        remember(tracker, paper, mark, literata, format) { renderTrackerPages(tracker, paper, mark, literata, measurer, format) }
    }
}

/** Density 1, not the screen's: every size on the page is a pixel of the image (17.2). */
@Composable
private fun pixelMeasurer(): TextMeasurer {
    val resolver = LocalFontFamilyResolver.current
    return remember(resolver) { TextMeasurer(resolver, Density(1f), LayoutDirection.Ltr) }
}

/**
 * The share sheet (docs/pantallas.md 17.1, 23.12): a bottom sheet and not a destination, with the
 * shape to pick (a post or a story, a post every time it opens), the first page to scale and two ways
 * out to the system's own sheet. Free always, and only what the user chose.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PagesSheet(title: String, text: String, onClose: () -> Unit, pagesOf: @Composable (ShareFormat) -> List<ImageBitmap>) {
    var format by remember { mutableStateOf(ShareFormat.POST) }
    val pages = pagesOf(format)
    val shape = RoundedCornerShape(12.dp)

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetMaxWidth = MAX_CONTENT_WIDTH,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 0.dp,
        dragHandle = { SheetHandle() },
    ) {
        // A story is tall: on a small phone the sheet scrolls rather than cutting the buttons off.
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            TwoChoice(
                S.formatPost,
                S.formatStory,
                second = format == ShareFormat.STORY,
                onChange = { format = if (it) ShareFormat.STORY else ShareFormat.POST },
                modifier = Modifier.padding(start = 24.dp, bottom = 12.dp),
            )
            Image(
                pages.first(),
                contentDescription = title,
                modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth().aspectRatio(SHARE_W / format.height.toFloat())
                    .clip(shape).border(1.dp, MaterialTheme.colorScheme.outline, shape),
            )
            if (pages.size > 1) {
                Text(S.sharePages(pages.size), style = Type.Secondary, modifier = Modifier.padding(start = 24.dp, top = 8.dp))
            }
            Spacer(Modifier.height(gridUnit))
            ActionRow(S.shareImage) {
                Sharing.sharePngs(pages.map { it.encodeToPng() })
                onClose()
            }
            ActionRow(S.shareText) {
                Sharing.shareText(text)
                onClose()
            }
            Spacer(Modifier.height(gridUnit))
        }
    }
}
