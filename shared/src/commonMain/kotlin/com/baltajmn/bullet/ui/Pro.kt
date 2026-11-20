package com.baltajmn.bullet.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.baltajmn.bullet.data.BobbinRepository
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.baltajmn.bullet.billing.Billing
import com.baltajmn.bullet.billing.PurchaseOutcome
import com.baltajmn.bullet.data.AppInfo
import com.baltajmn.bullet.data.Offers
import com.baltajmn.bullet.data.PRIVACY_URL
import com.baltajmn.bullet.data.TERMS_URL
import com.baltajmn.bullet.data.onIos
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.ui.theme.Type
import com.revenuecat.purchases.kmp.models.Package
import kotlinx.coroutines.launch

/**
 * The only paywall (docs/pantallas.md 15.1, docs/tecnico.md 6.16): opened by a real limit, the Settings
 * row or `bobbin://pro`, never at start-up nor after some number of uses. It is a dialog over whatever
 * opened it, not a screen of the stack, so closing it leaves that screen exactly as it was.
 */
object Paywall {
    var open by mutableStateOf(false)
        private set
    private var onPro: (() -> Unit)? = null

    /** [onPro] is the limit that opened it, carried out once the purchase goes through. */
    fun show(onPro: () -> Unit = {}) {
        this.onPro = onPro
        open = true
        Offers.proTouched()
    }

    internal fun close(bought: Boolean) {
        open = false
        val then = onPro
        onPro = null
        if (bought) then?.invoke()
    }
}

/** Runs [action] with Pro, or opens the paywall and runs it once bought (docs/tecnico.md 6.16). */
fun proOr(action: () -> Unit) {
    if (BobbinRepository.isPro) action() else Paywall.show(action)
}

@Composable
fun ProDialog() {
    val scope = rememberCoroutineScope()
    var pack by remember { mutableStateOf<Package?>(null) }
    var loading by remember { mutableStateOf(true) }
    // The retry in flight: the sentence and the button stay where they are, so nothing under them moves.
    var retrying by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        pack = Billing.proPackage()
        loading = false
    }
    val price = pack?.storeProduct?.price?.formatted
    var more by remember { mutableStateOf(false) }
    if (!loading && price == null) LaunchedEffect(Unit) { Offers.noteError() }

    AlertDialog(
        onDismissRequest = { if (!busy) Paywall.close(bought = false) },
        modifier = Modifier.widthIn(max = DIALOG_MAX_WIDTH_DP.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 0.dp,
        // Title and "one purchase, no subscription" go before the list, so both are on screen
        // without scrolling even with the font at 200%.
        title = {
            Column {
                Text(S.proTitle, style = Type.PageTitle)
                Text(S.proOnce, style = Type.Body)
            }
        },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                // Six reasons at most; the rest wait behind one row so the buy button below stays in view.
                val reasons = listOfNotNull(S.proBook, S.proTrackers, S.proMonthWidget, S.proSync, S.proCapture(onIos), S.proCovers) to
                    listOfNotNull(S.proPapers, S.proLockWidget.takeIf { onIos }, S.proWriteOutside.takeIf { !onIos }, S.proYearSummary)
                (if (more) reasons.first + reasons.second else reasons.first).forEach { line ->
                    Row(Modifier.heightIn(min = 32.dp), verticalAlignment = Alignment.CenterVertically) {
                        BulletGlyph(Bullet.NOTE, TaskStatus.OPEN)
                        Box(Modifier.width(8.dp))
                        Text(line, style = Type.Body)
                    }
                }
                if (!more) QuietButton(S.proEverything, { more = true })
                Text(S.proFree, style = Type.Secondary, modifier = Modifier.padding(top = 8.dp))
                // With no store there is no price, and a button that cannot say what it costs is not an offer.
                if (!loading && price == null) Text(S.storeUnavailable, style = Type.Secondary, modifier = Modifier.padding(top = 8.dp))
                note?.let { Text(it, style = Type.Secondary, modifier = Modifier.padding(top = 8.dp)) }
                // Both stores ask for the two documents next to a purchase. Bobbin has no terms page of its own, so the
                // store's apply (TERMS_URL); a flow row because in some languages the two names do not fit one line.
                FlowRow(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    QuietButton(S.termsRow, { AppInfo.open(TERMS_URL) })
                    QuietButton(S.privacyRow, { AppInfo.open(PRIVACY_URL) })
                }
            }
        },
        confirmButton = {
            // Stacked on the right, one per row (docs/pantallas.md 15.1).
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(0.dp)) {
                val target = pack
                if (target != null && price != null) {
                    DialogAction(if (busy) S.working else S.buy(price), enabled = !busy) {
                        busy = true
                        note = null
                        scope.launch {
                            when (Billing.purchase(target)) {
                                PurchaseOutcome.Success -> Paywall.close(bought = true)
                                // Changing your mind says nothing and shows nothing.
                                PurchaseOutcome.Cancelled -> busy = false
                                PurchaseOutcome.Failed -> {
                                    Offers.noteError()
                                    busy = false
                                    note = S.buyFailed
                                }
                            }
                        }
                    }
                } else if (!loading) {
                    // No price: the sentence above says why, and this asks the store again instead of leaving no button at all.
                    DialogAction(if (retrying) S.working else S.retry, enabled = !busy && !retrying) {
                        retrying = true
                        note = null
                        scope.launch {
                            pack = Billing.proPackage()
                            retrying = false
                        }
                    }
                }
                // Always, with Pro too: both stores ask for it.
                DialogAction(S.restore, enabled = !busy) {
                    busy = true
                    scope.launch {
                        val found = Billing.restore()
                        busy = false
                        if (found) Paywall.close(bought = true) else note = S.restoreNothing
                    }
                }
                DialogAction(S.notNow, enabled = !busy) { Paywall.close(bought = false) }
            }
        },
    )
}
