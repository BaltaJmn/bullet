package com.baltajmn.bullet.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.baltajmn.bullet.data.BobbinRepository
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
    }

    internal fun close(bought: Boolean) {
        open = false
        val then = onPro
        onPro = null
        if (bought) then?.invoke()
    }
}

@Composable
fun ProDialog() {
    val scope = rememberCoroutineScope()
    var pack by remember { mutableStateOf<Package?>(null) }
    var loading by remember { mutableStateOf(true) }
    var busy by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        pack = Billing.proPackage()
        loading = false
    }
    val price = pack?.storeProduct?.price?.formatted

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
                listOfNotNull(S.proCovers, S.proPapers, S.proTrackers, S.proMonthWidget, S.proLockWidget.takeIf { onIos })
                    .forEach { line ->
                        Row(Modifier.heightIn(min = 32.dp), verticalAlignment = Alignment.CenterVertically) {
                            BulletGlyph(Bullet.NOTE, TaskStatus.OPEN)
                            Box(Modifier.width(8.dp))
                            Text(line, style = Type.Body)
                        }
                    }
                Text(S.proFree, style = Type.Secondary, modifier = Modifier.padding(top = 8.dp))
                // With no store there is no price, and a button that cannot say what it costs is not an offer.
                if (!loading && price == null) Text(S.storeUnavailable, style = Type.Secondary, modifier = Modifier.padding(top = 8.dp))
                note?.let { Text(it, style = Type.Secondary, modifier = Modifier.padding(top = 8.dp)) }
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
                                    busy = false
                                    note = S.buyFailed
                                }
                            }
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
