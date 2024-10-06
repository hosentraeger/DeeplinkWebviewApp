package com.example.deeplinkwebviewapp.data

data class BankEntry(val blz: String, val username: String) {
    constructor(entry: String) : this(
        blz = entry.substringBefore('.'),
        username = entry.substringAfter('.')
    )
}