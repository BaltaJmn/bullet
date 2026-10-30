package com.baltajmn.bullet

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.baltajmn.bullet.data.AndroidContext
import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.ui.CaptureSheet
import com.baltajmn.bullet.widget.CAPTURE_FROM
import com.baltajmn.bullet.widget.CAPTURE_FROM_WIDGET
import kotlinx.coroutines.launch

/**
 * The Quick Settings tile "Anotar en Bobbin" (docs/tecnico.md 12.5). With Pro it opens the capture over
 * whatever is on screen; without, the app with the paywall. On the lock screen it asks to unlock first:
 * nothing is written into the diary from a locked phone.
 */
class CaptureTileService : TileService() {
    override fun onStartListening() {
        qsTile?.apply {
            label = S.tileLabel
            state = Tile.STATE_INACTIVE
            updateTile()
        }
    }

    override fun onClick() {
        if (isLocked) unlockAndRun(::open) else open()
    }

    private fun open() {
        AndroidContext.init(this)
        BobbinRepository.ensureLoaded()
        val intent = if (BobbinRepository.isPro) {
            Intent(this, CaptureActivity::class.java)
        } else {
            Intent(this, MainActivity::class.java).setData(Uri.parse("bobbin://pro"))
        }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}

/**
 * Only the composer, writing in today; Intro saves, says so with the system's toast, and closes. It opens
 * from the tile, from the "Escribir" widget and from text another app shares (docs/tecnico.md 12.14).
 * Writing without opening the app is Pro: without it, the widget still opens Hoy to write, and the
 * tile and sharing open the paywall.
 */
class CaptureActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AndroidContext.init(this)
        BobbinRepository.ensureLoaded()
        // The tile checked, but Pro can have been refunded since it was placed.
        if (!BobbinRepository.isPro) {
            val link = if (intent?.getStringExtra(CAPTURE_FROM) == CAPTURE_FROM_WIDGET) "bobbin://today?focus" else "bobbin://pro"
            startActivity(Intent(this, MainActivity::class.java).setData(Uri.parse(link)))
            finish()
            return
        }
        val shared = if (intent?.action == Intent.ACTION_SEND) {
            intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()?.takeIf { it.isNotBlank() } ?: intent.getStringExtra(Intent.EXTRA_SUBJECT).orEmpty()
        } else {
            ""
        }
        setContent {
            CaptureSheet(
                initial = shared,
                onSaved = {
                    lifecycleScope.launch {
                        BobbinRepository.flush()
                        Toast.makeText(this@CaptureActivity, S.captureSaved, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                },
                onDismiss = ::finish,
            )
        }
    }
}
