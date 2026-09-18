package com.example.cheerschecklist

import android.content.Context

object AppContext {
    lateinit var instance: Context
    fun init(context: Context) { instance = context.applicationContext }
}
