package com.example.deeplinkwebviewapp

data class DeviceData(
    var device_id: String = "",
    var push_id: String = "<not assigned>",
    var login_id: String = "<nobody>",
    var last_login: String = "1971-01-01T00:00:00Z"
)

object DeviceDataSingleton {
    var deviceData = DeviceData()
}
