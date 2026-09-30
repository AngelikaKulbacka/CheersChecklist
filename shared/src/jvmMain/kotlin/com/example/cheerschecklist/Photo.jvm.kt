package com.example.cheerschecklist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image as SkiaImage

actual val supportsCameraCapture: Boolean = false

@Composable
actual fun rememberPhotoPicker(onPhotoPicked: (String) -> Unit): (PhotoSource) -> Unit {
    val scope = rememberCoroutineScope()
    return { _ ->
        scope.launch {
            val path = withContext(Dispatchers.IO) { pickAndCopyFile() }
            if (path != null) onPhotoPicked(path)
        }
    }
}

private fun pickAndCopyFile(): String? {
    val dialog = FileDialog(null as Frame?, "Choose a photo", FileDialog.LOAD)
    dialog.isVisible = true
    val fileName = dialog.file ?: return null
    val directory = dialog.directory ?: return null
    val selected = File(directory, fileName)
    if (!selected.exists()) return null

    val destDir = File(System.getProperty("user.home"), ".cheerschecklist/photos").apply { mkdirs() }
    val destFile = File(destDir, "${UUID.randomUUID()}.jpg")
    selected.copyTo(destFile, overwrite = true)
    return destFile.absolutePath
}

actual fun decodeImageBitmap(path: String): ImageBitmap? =
    runCatching { SkiaImage.makeFromEncoded(File(path).readBytes()).toComposeImageBitmap() }.getOrNull()

actual fun deletePhotoFile(path: String) {
    runCatching { File(path).delete() }
}
