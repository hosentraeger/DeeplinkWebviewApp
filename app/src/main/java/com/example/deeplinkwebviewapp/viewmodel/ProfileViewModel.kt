package com.example.deeplinkwebviewapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.deeplinkwebviewapp.R
import com.example.deeplinkwebviewapp.ResourceProvider



class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val resourceProvider: ResourceProvider = ResourceProvider(application.applicationContext)

    // Liste der Menüeinträge (Ressourcen-IDs)
    private val menuItemsResourceIds = listOf(
        R.string.profile_entry_name_settings,
        R.string.profile_entry_name_notification_settings,
        R.string.profile_entry_name_log,
        R.string.profile_entry_name_system_parameter,
        R.string.profile_entry_name_logout
    )

    // LiveData für die Menüeinträge (Strings)
    private val _menuItems = MutableLiveData<List<String>>()
    val menuItems: LiveData<List<String>> = _menuItems

    // LiveData für das ausgewählte Menüelement
    private val _selectedMenuItem = MutableLiveData<String>()
    val selectedMenuItem: LiveData<String> = _selectedMenuItem

    // Funktion zum Setzen des ausgewählten Menüelements
    fun selectMenuItem(item: String) {
        _selectedMenuItem.value = item
    }

    init {
        // Lade die Menüeinträge aus den Ressourcen-IDs
        _menuItems.value = menuItemsResourceIds.map { resourceProvider.getString(it) }
    }
}