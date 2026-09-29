package com.baltajmn.bullet.data

import android.app.KeyguardManager
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.baltajmn.bullet.i18n.S
import java.lang.ref.WeakReference

actual object Lock {

    /** Set by MainActivity: BiometricPrompt needs a live fragment host, and holding it would leak it. */
    var host: WeakReference<FragmentActivity>? = null

    actual fun isAvailable(): Boolean {
        val context = AndroidContext.value
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BiometricManager.from(context).canAuthenticate(BIOMETRIC_WEAK or DEVICE_CREDENTIAL) ==
                BiometricManager.BIOMETRIC_SUCCESS
        } else {
            // Before API 30 the combined query lies, so the screen lock is asked about directly.
            context.getSystemService(KeyguardManager::class.java)?.isDeviceSecure == true
        }
    }

    actual fun authenticate(onResult: (Boolean) -> Unit) {
        val activity = host?.get()
        if (activity == null) {
            onResult(false)
            return
        }
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) =
                    onResult(true)

                // A wrong finger is not an answer: only giving up or an error is.
                override fun onAuthenticationError(code: Int, message: CharSequence) = onResult(false)
            },
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(S.lockPromptTitle)
            .setSubtitle(S.lockPromptSubtitle)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    setAllowedAuthenticators(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
                } else {
                    setAllowedAuthenticators(BIOMETRIC_WEAK)
                    @Suppress("DEPRECATION")
                    setDeviceCredentialAllowed(true)
                }
            }
            .build()
        prompt.authenticate(info)
    }

    actual fun setHidesPreview(on: Boolean) {
        // Not FLAG_SECURE: that would also stop the user taking their own screenshots.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            host?.get()?.setRecentsScreenshotEnabled(!on)
        }
    }
}
