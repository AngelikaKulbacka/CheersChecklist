package com.example.cheerschecklist

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "CheersChecklist",
        ) {
            App()
        }
    }
}