package com.example.deeplinkwebviewapp.data

data class SilentLoginRequest(
    val apiVersion: String,
    val service: String,
    val timestamp: String,
    val params: Params,
    val applicationId: String,
    val blz: String
)

data class Params(
    val pin: String,
    val url: String,
    val legitimationsId: String
)

data class SilentLoginResponse(
    val apiVersion: String,
    val applicationId: String,
    val blz: String,
    val timestamp: String,
    val service: String,
    val params: ParamsResponse
)

data class ParamsResponse(
    val legitimationsId: String,
    val pin: String,
    val url: String,
    val productId: String,
    val kundenSystemId: String
)
