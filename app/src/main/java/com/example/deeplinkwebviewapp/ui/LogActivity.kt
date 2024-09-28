package `mipmap-xhdpi`.deeplinkwebviewapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import `mipmap-xhdpi`.deeplinkwebviewapp.Logger
import com.example.deeplinkwebviewapp.R

class LogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)

        val logTextView: TextView = findViewById(R.id.textViewLog)
        `mipmap-xhdpi`.deeplinkwebviewapp.Logger.init(logTextView) // Logger wird initialisiert und zeigt gespeicherte Logs an
    }
}
