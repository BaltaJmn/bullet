package com.baltajmn.bullet.data

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File

actual fun ImageBitmap.encodeToPng(): ByteArray {
    val out = ByteArrayOutputStream()
    asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, out)
    return out.toByteArray()
}

actual object Sharing {

    /** cache/share/ through the FileProvider: the share sheet can read these and nothing else. */
    actual fun sharePngs(pngs: List<ByteArray>) {
        val context = AndroidContext.value
        val dir = File(context.cacheDir, "share").apply {
            // The pages of a moment ago have nothing left to say once they are shared.
            deleteRecursively()
            mkdirs()
        }
        val uris = ArrayList<Uri>(pngs.mapIndexed { i, png ->
            val file = File(dir, "bobbin-${i + 1}.png").apply { writeBytes(png) }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        })
        val send = if (uris.size == 1) {
            Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_STREAM, uris.first())
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        }
        send.type = "image/png"
        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    actual fun shareText(text: String) {
        val context = AndroidContext.value
        val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
        context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
