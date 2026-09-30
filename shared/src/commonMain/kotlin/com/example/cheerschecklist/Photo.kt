package com.example.cheerschecklist

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

enum class PhotoSource { CAMERA, GALLERY }

expect val supportsCameraCapture: Boolean

@Composable
expect fun rememberPhotoPicker(onPhotoPicked: (String) -> Unit): (PhotoSource) -> Unit

expect fun decodeImageBitmap(path: String): ImageBitmap?

expect fun deletePhotoFile(path: String)
