package com.example.deeplinkwebviewapp.viewmodel

// ProfileViewModel.kt
import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ServiceViewModel : ViewModel() {
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

    fun setAlohaEnabled(enabled: Boolean) {
        _alohaEnabled.value = enabled
    }

    fun setWeroEnabled(enabled: Boolean) {
        _weroEnabled.value = enabled
    }

    fun setCashbackEnabled(enabled: Boolean) {
        _cashbackEnabled.value = enabled
    }
}
