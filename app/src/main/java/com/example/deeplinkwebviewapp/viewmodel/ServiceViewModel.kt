package com.example.deeplinkwebviewapp.viewmodel

// ProfileViewModel.kt
import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ServiceViewModelFactory(
    private val application: Application,
    private val sharedPreferences: SharedPreferences
) : ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ServiceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") // Warnung unterdrücken
            return ServiceViewModel(application, sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ServiceViewModel(application: Application, private val sharedPreferences: SharedPreferences) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "ServiceViewModel"
    }

    private val _serviceCenterUrl = MutableLiveData<String>()
    val serviceCenterUrl: LiveData<String> = _serviceCenterUrl

    private val _deeplinksUrl = MutableLiveData<String>()
    val deeplinksUrl: LiveData<String> = _deeplinksUrl

    // Kontaktinformationen
    private val _contactName = MutableLiveData("Ansprechpartner Name")
    val contactName: LiveData<String> = _contactName

    private val _contactEmail = MutableLiveData("email@domain.com")
    val contactEmail: LiveData<String> = _contactEmail

    private val _contactPhone = MutableLiveData("+49 123 456789")
    val contactPhone: LiveData<String> = _contactPhone

    // Schieberegler-Status
    private val _alohaEnabled = MutableLiveData(false)
    val alohaEnabled: LiveData<Boolean> = _alohaEnabled

    private val _weroEnabled = MutableLiveData(false)
    val weroEnabled: LiveData<Boolean> = _weroEnabled

    private val _cashbackEnabled = MutableLiveData(false)
    val cashbackEnabled: LiveData<Boolean> = _cashbackEnabled

    fun loadServiceCenterUrl() {
        _serviceCenterUrl.value = sharedPreferences.getString("serviceCenterUrl", "")
    }

    fun loadDeeplinksUrl() {
        _deeplinksUrl.value = sharedPreferences.getString("DeeplinkURL", "")
    }

    fun setAlohaEnabled(enabled: Boolean) {
        _alohaEnabled.value = enabled
        val enableState = if (enabled) "ON" else "OFF"
        Log.d(TAG, "aloha $enableState")
    }

    fun setWeroEnabled(enabled: Boolean) {
        _weroEnabled.value = enabled
        val enableState = if (enabled) "ON" else "OFF"
        Log.d(TAG, "wero $enableState")
    }

    fun setCashbackEnabled(enabled: Boolean) {
        _cashbackEnabled.value = enabled
        val enableState = if (enabled) "ON" else "OFF"
        Log.d(TAG, "cashback $enableState")
    }

    fun handleServiceCenterClick() {
        loadServiceCenterUrl()
    }

    fun handleDeeplinksClick() {
        loadDeeplinksUrl()
    }
}
