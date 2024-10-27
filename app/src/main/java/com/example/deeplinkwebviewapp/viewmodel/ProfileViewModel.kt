package com.example.deeplinkwebviewapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    // Liste der Menüeinträge
    private val _menuItems = MutableLiveData<List<String>>(
        listOf(
            "Einstellungen",
            "Benachrichtigungen",
            "Kommunikationsprotokoll",
            "Logout"
        )
    )
    val menuItems: LiveData<List<String>> = _menuItems

    // LiveData für das ausgewählte Menüelement
    private val _selectedMenuItem = MutableLiveData<String>()
    val selectedMenuItem: LiveData<String> = _selectedMenuItem

    // Funktion zum Setzen des ausgewählten Menüelements
    fun selectMenuItem(item: String) {
        _selectedMenuItem.value = item
    }
}
