package com.baltajmn.bullet

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference
import com.baltajmn.bullet.data.AndroidContext
import com.baltajmn.bullet.data.Reminders
import com.baltajmn.bullet.data.Route
import com.baltajmn.bullet.data.parseLink

// FragmentActivity and not ComponentActivity: Lock.android.kt needs a fragment host for the
// biometric prompt (#39), and changing the base class after screens exist costs more than
// declaring it now.
class MainActivity : FragmentActivity() {

    private val askNotifications =
        registerForActivityResult(ActivityResultContracts.RequestPermission(), Reminders::onRequestResult)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AndroidContext.init(this)
        Reminders.host = WeakReference(this)
        Reminders.launchRequest = { askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS) }
        // A recreated Activity gets the same intent again: the link was already followed.
        if (savedInstanceState == null) parseLink(intent?.dataString)?.let { Route.pending = it }
        setContent { App() }
    }

    // launchMode is singleTask: the notification or a widget tapped while the app is up arrives here.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        parseLink(intent.dataString)?.let { Route.pending = it }
    }

    // The launcher belongs to this instance's registry: leaving it in a process wide object would
    // hold the dead Activity and then throw when something tried to launch it.
    override fun onDestroy() {
        Reminders.launchRequest = null
        Reminders.host = null
        super.onDestroy()
    }
}
