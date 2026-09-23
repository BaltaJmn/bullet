package com.baltajmn.bullet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler

/**
 * Placeholder until the real destinations land issue by issue (docs/tecnico.md 3): Today, Month,
 * Future, Index, a collection, Review, Search, the Key, Settings and Pro. Today is the only one
 * that exists here, so it is also the only one the app can show.
 */
enum class Screen { Today }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App() {
    var screen by remember { mutableStateOf(Screen.Today) }

    // Nothing to pop yet: the stack grows as the real screens replace this placeholder.
    BackHandler(false) {}

    MaterialTheme {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            when (screen) {
                Screen.Today -> Text("Bobbin", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
