package com.baltajmn.bullet.data

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIViewController
import platform.UIKit.popoverPresentationController

actual fun ImageBitmap.encodeToPng(): ByteArray =
    Image.makeFromBitmap(asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)?.bytes ?: ByteArray(0)

@OptIn(ExperimentalForeignApi::class)
actual object Sharing {

    actual fun sharePngs(pngs: List<ByteArray>) {
        present(pngs.mapNotNull { png -> png.toNSData()?.let { UIImage.imageWithData(it) } })
    }

    actual fun shareText(text: String) = present(listOf(text))

    private fun present(items: List<Any>) {
        if (items.isEmpty()) return
        val host = topViewController() ?: return
        val sheet = UIActivityViewController(activityItems = items, applicationActivities = null)
        // iPad presents this as a popover and needs an anchor.
        sheet.popoverPresentationController?.sourceView = host.view
        host.presentViewController(sheet, animated = true, completion = null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData? {
    if (isEmpty()) return null
    return usePinned { NSData.create(bytes = it.addressOf(0), length = size.toULong()) }
}

private fun topViewController(): UIViewController? {
    var controller = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}
