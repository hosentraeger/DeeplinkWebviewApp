package com.example.deeplinkwebviewapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class LogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)

        val logTextView: TextView = findViewById(R.id.textViewLog)
        Logger.init(logTextView) // Logger wird initialisiert und zeigt gespeicherte Logs an
    }
}
