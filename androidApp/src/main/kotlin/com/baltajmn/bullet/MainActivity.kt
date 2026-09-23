package com.baltajmn.bullet

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity

// FragmentActivity and not ComponentActivity: Lock.android.kt needs a fragment host for the
// biometric prompt (#39), and changing the base class after screens exist costs more than
// declaring it now.
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}
