package com.example.cheerschecklist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject
import platform.posix.SEEK_END
import platform.posix.SEEK_SET
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fseek
import platform.posix.ftell
import platform.posix.fwrite
import platform.posix.memcpy
import org.jetbrains.skia.Image as SkiaImage

actual val supportsCameraCapture: Boolean = true

// Kept alive at file scope so ARC doesn't collect the delegate before the callback fires.
private var activePickerDelegate: NSObject? = null

@Composable
actual fun rememberPhotoPicker(onPhotoPicked: (String) -> Unit): (PhotoSource) -> Unit {
    return remember {
        { source ->
            val sourceType = when (source) {
                PhotoSource.CAMERA -> UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
                PhotoSource.GALLERY -> UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
            }
            if (UIImagePickerController.isSourceTypeAvailable(sourceType)) {
                val picker = UIImagePickerController()
                picker.sourceType = sourceType
                val delegate = PhotoPickerDelegate { image ->
                    activePickerDelegate = null
                    val path = image?.let(::saveImageToDocuments)
                    if (path != null) onPhotoPicked(path)
                }
                activePickerDelegate = delegate
                picker.delegate = delegate
                IosRootViewController.current?.presentViewController(picker, animated = true, completion = null)
            }
        }
    }
}

private class PhotoPickerDelegate(
    private val onFinished: (UIImage?) -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    override fun imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo: Map<Any?, *>) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        picker.dismissViewControllerAnimated(true, completion = null)
        onFinished(image)
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
        onFinished(null)
    }
}

private fun saveImageToDocuments(image: UIImage): String? {
    val data = UIImageJPEGRepresentation(image, 0.8) ?: return null
    val path = "${photosDirectory()}/${NSUUID().UUIDString()}.jpg"
    return if (writeBytesToFile(path, data.toByteArray())) path else null
}

@OptIn(ExperimentalForeignApi::class)
private fun photosDirectory(): String {
    val documentsDir = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    val photosPath = requireNotNull(documentsDir?.path) + "/photos"
    NSFileManager.defaultManager.createDirectoryAtPath(
        path = photosPath,
        withIntermediateDirectories = true,
        attributes = null,
        error = null,
    )
    return photosPath
}

actual fun decodeImageBitmap(path: String): ImageBitmap? {
    val bytes = readBytesFromFile(path) ?: return null
    return runCatching { SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap() }.getOrNull()
}

@OptIn(ExperimentalForeignApi::class)
actual fun deletePhotoFile(path: String) {
    NSFileManager.defaultManager.removeItemAtPath(path, error = null)
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = this.length.toInt()
    val bytes = ByteArray(size)
    if (size > 0) {
        bytes.usePinned { pinned ->
            memcpy(pinned.addressOf(0), this.bytes, this.length)
        }
    }
    return bytes
}

@OptIn(ExperimentalForeignApi::class)
private fun writeBytesToFile(path: String, bytes: ByteArray): Boolean {
    val file = fopen(path, "wb") ?: return false
    return try {
        if (bytes.isNotEmpty()) {
            bytes.usePinned { pinned ->
                fwrite(pinned.addressOf(0), 1u, bytes.size.convert(), file)
            }
        }
        true
    } finally {
        fclose(file)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun readBytesFromFile(path: String): ByteArray? {
    val file = fopen(path, "rb") ?: return null
    return try {
        fseek(file, 0, SEEK_END)
        val size = ftell(file)
        fseek(file, 0, SEEK_SET)
        if (size <= 0) return null
        val bytes = ByteArray(size.toInt())
        bytes.usePinned { pinned ->
            fread(pinned.addressOf(0), 1u, size.convert(), file)
        }
        bytes
    } finally {
        fclose(file)
    }
}
