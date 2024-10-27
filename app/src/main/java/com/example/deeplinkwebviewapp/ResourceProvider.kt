package com.example.deeplinkwebviewapp

import android.content.Context

class ResourceProvider(private val context: Context) {
    fun getString(resourceId: Int): String {
        return context.getString(resourceId)
    }
}