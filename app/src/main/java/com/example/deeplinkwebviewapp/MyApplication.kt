package com.example.deeplinkwebviewapp

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.example.deeplinkwebviewapp.service.MyHttpClient

class MyApplication : Application() {

    companion object {
        var isAppInForeground = false
    }

    override fun onCreate() {
        super.onCreate()
        // MyHttpClient mit User-Agent initialisieren
        MyHttpClient.initialize(getString(R.string.user_agent_string))

        // Registriere ActivityLifecycleCallbacks, um den App-Status zu verfolgen
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                isAppInForeground = true
            }

            override fun onActivityPaused(activity: Activity) {
                isAppInForeground = false
            }

            // Du kannst diese Methoden leer lassen
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
}
