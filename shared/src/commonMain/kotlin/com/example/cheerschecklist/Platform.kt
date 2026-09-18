package com.example.cheerschecklist

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform