package com.example.deeplinkwebviewapp.data

import android.os.Build
import java.util.Locale

object DeviceDataSingleton {
    private var _deviceData: DeviceData? = null

    // Methode, um die Instanz von DeviceData zu erhalten oder zu erstellen
    fun getDeviceData(): DeviceData {
        if (_deviceData == null) {
            _deviceData = createDeviceData()  // Instanz erstellen, falls sie noch nicht existiert
        }
        return _deviceData!!
    }

    // Optional: Methode, um den Zustand der Instanz zurückzusetzen
    fun resetDeviceData() {
        _deviceData = null
    }

    private fun createDeviceData(): DeviceData {
        return DeviceData(
            userData = createUserData(),
            deviceReport = createDeviceReport(),
            featureSettings = createFeatureSettings(),
            appUsageStatistics = createAppUsageStatistics(),
            appMetrics = createAppMetrics(),
            deviceMetaData = createDeviceMetaData()
        )
    }

    private fun createUserData(): UserData {
        return UserData(
            push_id = "<not assigned>",
            login_id = "<nobody>",
            last_login = "1971-01-04T00:00:00.000000"
        )
    }

    private fun createDeviceReport(): DeviceReport {
        return DeviceReport(
            model = Build.MODEL,
            osVersion = Build.VERSION.RELEASE,
            systemLanguage = Locale.getDefault().language,
            modified = false
        )
    }

    private fun createFeatureSettings(): FeatureSettings {
        return FeatureSettings(
            appLanguage = null,
            theme = null,
            maxSessionDuration = null,
            autoUpateBalance = false,
            accountSorting = null,
            modified = false
        )
    }

    private fun createAppUsageStatistics(): AppUsageStatistics {
        return AppUsageStatistics(
            institutionsAndAccounts = InstitutionsAndAccounts(),
            featuresRequiringAttention = listOf(),
            anonymized = false,
            modified = false
        )
    }

    private fun createAppMetrics(): AppMetrics {
        return AppMetrics(
            fullAppStartsSinceLastCommit = 0,
            fullAppStartsMsSinceLastCommit = 0,
            subsequentAppStartsSinceLastCommit = 0,
            subsequentAppStartsMsSinceLastCommit = 0,
            anonymized = false
        )
    }

    private fun createDeviceMetaData(): DeviceMetaData {
        return DeviceMetaData(
            deviceId = "deviceIdPlaceholder",
            lastConnect = "1971-01-04T00:00:00.000000",
            lastCommitIp = "192.168.0.1",
            lastCommit = "1971-01-04T00:00:00.000000",
            deviceDataCommitted = false
        )
    }
}
