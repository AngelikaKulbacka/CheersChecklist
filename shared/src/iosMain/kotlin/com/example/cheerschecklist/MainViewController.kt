package com.example.cheerschecklist

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = run {
    initKoin()
    ComposeUIViewController { App() }.also { IosRootViewController.current = it }
}