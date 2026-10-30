package com.baltajmn.bullet.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.data.LIST_MIME
import com.baltajmn.bullet.data.ListPacket
import com.baltajmn.bullet.data.Received
import com.baltajmn.bullet.data.ReceiveOutcome
import com.baltajmn.bullet.data.Sharing
import com.baltajmn.bullet.data.encodePacket
import com.baltajmn.bullet.data.packetFileName
import com.baltajmn.bullet.data.receivePreview
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.BulletCollection
import com.baltajmn.bullet.model.CollectionKind
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.ui.theme.MAX_CONTENT_WIDTH
import com.baltajmn.bullet.ui.theme.Type
import kotlinx.coroutines.launch

/** How many entries the receive sheet shows before "and 9 more" (docs/pantallas.md 23.11). */
private const val RECEIVED_SHOWN = 5

/**
 * "Enviar la lista" (docs/pantallas.md 23.11): blank or as it is, and then the system's own sheet with
 * the `.bobbin`. Free, and only ever what the user picked: the app never suggests sending anything.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendSheet(collection: BulletCollection, onClose: () -> Unit) {
    val tracker = collection.kind == CollectionKind.TRACKER
    // Off every time it opens; a tracker only goes blank (docs/tecnico.md 12.11).
    var blank by remember(collection.id) { mutableStateOf(tracker) }

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetMaxWidth = MAX_CONTENT_WIDTH,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 0.dp,
        dragHandle = { SheetHandle() },
    ) {
        SheetHeader(S.sendList, collection.title, onClose)
        Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp).padding(bottom = 18.dp)) {
            Row(
                Modifier.fillMaxWidth().clickable(enabled = !tracker, role = Role.Switch) { blank = !blank }
                    .padding(start = 52.dp, end = 10.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(S.sendBlank, style = Type.Body)
                    Text(if (tracker) S.sendTrackerBlank else S.sendBlankSub, style = Type.Secondary, modifier = Modifier.padding(top = 2.dp))
                }
                Spacer(Modifier.width(12.dp))
                Switch(checked = blank, onCheckedChange = { blank = it }, enabled = !tracker, colors = switchColors())
            }
            ActionRow(S.sendAsFile, S.sendAsFileSub, icon = { GlyphIcon(Glyph.SHARE) }) {
                BobbinRepository.packForSending(collection.id, blank)?.let { packet ->
                    Sharing.shareFile(packetFileName(packet.title), LIST_MIME, encodePacket(packet).encodeToByteArray())
                }
                onClose()
            }
        }
    }
}

/**
 * A list someone sent, before anything is written (docs/pantallas.md 23.11): what it is, its first
 * entries, and either "Añadir al Índice" or the summary of what merging would change. [onDone] gets
 * the id of the list it landed in; a tracker over the free limit opens the paywall instead.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveSheet(packet: ListPacket, onDone: (String) -> Unit, onClose: () -> Unit) {
    val preview = remember(packet, BobbinRepository.journal) { receivePreview(BobbinRepository.journal, packet) }
    val scope = rememberCoroutineScope()
    var working by remember { mutableStateOf(false) }

    fun receive() {
        if (working) return
        working = true
        scope.launch {
            when (val outcome = BobbinRepository.receiveList(packet)) {
                is ReceiveOutcome.Landed -> {
                    Flash.show(if (preview is Received.Merge) S.listMerged else S.listAdded)
                    onDone(outcome.collectionId)
                }
                ReceiveOutcome.NeedsPro -> {
                    onClose()
                    Paywall.show()
                }
            }
            working = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!working) onClose() },
        sheetMaxWidth = MAX_CONTENT_WIDTH,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 0.dp,
        dragHandle = { SheetHandle() },
    ) {
        SheetHeader(S.receivedEyebrow, packet.title, onClose)
        Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp).padding(bottom = 12.dp)) {
            val tracker = packet.kind == CollectionKind.TRACKER
            Text(if (tracker) S.receivedRows(packet.rows.size) else S.receivedCount(packet.entries.size), style = Type.Secondary)
            Spacer(Modifier.height(8.dp))
            if (tracker) {
                packet.rows.take(RECEIVED_SHOWN).forEach { Text(it, style = Type.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            } else {
                packet.entries.take(RECEIVED_SHOWN).forEach { e ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BulletGlyph(e.bullet, e.status)
                        Spacer(Modifier.width(8.dp))
                        val closed = e.status != TaskStatus.OPEN
                        Text(
                            e.text,
                            style = Type.Ink.copy(color = if (closed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            val shown = if (tracker) packet.rows.size else packet.entries.size
            if (shown > RECEIVED_SHOWN) Text(S.receivedMore(shown - RECEIVED_SHOWN), style = Type.Secondary, modifier = Modifier.padding(top = 4.dp))
            Spacer(Modifier.height(16.dp))

            when (preview) {
                is Received.New -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    PrimaryButton(if (working) S.working else S.addToIndex, ::receive)
                    QuietButton(S.cancel, { if (!working) onClose() })
                }
                is Received.Merge -> {
                    Text(S.alreadyHave(packet.title), style = Type.Body)
                    if (preview.added + preview.updated == 0) {
                        Text(S.mergeNothing, style = Type.Secondary, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))
                        QuietButton(S.close, onClose)
                    } else {
                        Text(S.receivedSummary(preview.added, preview.updated), style = Type.Secondary, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            PrimaryButton(if (working) S.working else S.mergeList, ::receive)
                            QuietButton(S.cancel, { if (!working) onClose() })
                        }
                    }
                }
            }
        }
    }
}
