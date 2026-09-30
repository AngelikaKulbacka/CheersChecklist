package com.example.cheerschecklist

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

actual val supportsCameraCapture: Boolean = true

@Composable
actual fun rememberPhotoPicker(onPhotoPicked: (String) -> Unit): (PhotoSource) -> Unit {
    val context = LocalContext.current
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val file = pendingCameraFile
        pendingCameraFile = null
        if (success && file != null) {
            normalizeOrientation(file)
            onPhotoPicked(file.absolutePath)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            copyUriToPhotosDir(context, uri)?.let(onPhotoPicked)
        }
    }

    return { source ->
        when (source) {
            PhotoSource.CAMERA -> {
                val file = createPhotoFile(context)
                pendingCameraFile = file
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                cameraLauncher.launch(uri)
            }
            PhotoSource.GALLERY -> {
                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }
    }
}

actual fun decodeImageBitmap(path: String): ImageBitmap? =
    runCatching { BitmapFactory.decodeFile(path)?.asImageBitmap() }.getOrNull()

actual fun deletePhotoFile(path: String) {
    runCatching { File(path).delete() }
}

private fun photosDir(context: Context): File =
    File(context.filesDir, "photos").apply { mkdirs() }

private fun createPhotoFile(context: Context): File =
    File(photosDir(context), "${UUID.randomUUID()}.jpg")

private fun copyUriToPhotosDir(context: Context, uri: Uri): String? {
    val destFile = createPhotoFile(context)
    return runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        normalizeOrientation(destFile)
        destFile.absolutePath
    }.getOrNull()
}

private fun normalizeOrientation(file: File) {
    val rotationDegrees = runCatching {
        when (ExifInterface(file.path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }.getOrDefault(0)
    if (rotationDegrees == 0) return

    runCatching {
        val bitmap = BitmapFactory.decodeFile(file.path) ?: return
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        file.outputStream().use { out -> rotated.compress(Bitmap.CompressFormat.JPEG, 90, out) }
        bitmap.recycle()
        rotated.recycle()
    }
}
