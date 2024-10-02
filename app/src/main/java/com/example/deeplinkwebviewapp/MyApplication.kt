package com.example.deeplinkwebviewapp

import android.app.Activity
import android.app.Application
import android.os.Bundle

class MyApplication : Application() {

    companion object {
        var isAppInForeground = false
    }

    override fun onCreate() {
        super.onCreate()

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
