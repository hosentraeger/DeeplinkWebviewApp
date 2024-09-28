package `mipmap-xhdpi`.deeplinkwebviewapp

import android.widget.TextView

object Logger {
    private var logTextView: TextView? = null
    private val logList = mutableListOf<String>() // Speichert Logs temporär

    // Initialisiere den Logger mit einer TextView
    fun init(logTextView: TextView?) {
        Logger.logTextView = logTextView
        // Zeige alle Logs in der TextView an, wenn sie verfügbar ist
        logTextView?.let {
            it.text = "" // Bestehenden Text löschen, um alles neu anzuzeigen
            logList.forEach { log -> it.append("$log\n\n") }
        }
    }

    // Funktion zum Loggen von Nachrichten
    fun log(message: String) {
        // Füge das Log der Liste hinzu
        logList.add(message)
        // Wenn die TextView verfügbar ist, zeige das Log direkt an
        logTextView?.append("$message\n\n")
    }
}
