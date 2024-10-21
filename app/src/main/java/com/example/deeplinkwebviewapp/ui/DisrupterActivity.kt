package com.example.deeplinkwebviewapp.ui

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Html
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.deeplinkwebviewapp.R
import com.example.deeplinkwebviewapp.data.SfcIfResponse
import com.example.deeplinkwebviewapp.service.MyHttpClient
import kotlinx.serialization.json.Json

class DisrupterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disrupter)

        val sfcIfResponse = loadSfcIfResponseFromPrefs(this)
        val disrupterData = sfcIfResponse?.services?.firstOrNull()?.IF?.disrupter

        val imageView = findViewById<ImageView>(R.id.disrupterImageView)

        if (disrupterData?.image != null) {
            val decodedBytes = Base64.decode(disrupterData.image, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            imageView.setImageBitmap(bitmap)
        } else {
            imageView.setImageResource(R.drawable.sample_disrupter) // Default-Bild
        }

        // Schließt die Activity beim Tippen auf das Bild
        imageView.setOnClickListener {
            finish() // Schließt die Activity
        }

        // Buttons hinzufügen
        val btnLink1 = findViewById<Button>(R.id.btn_link1)
        val btnLink2 = findViewById<Button>(R.id.btn_link2)
        val btnLink3 = findViewById<Button>(R.id.btn_link3)

        btnLink1.setOnClickListener {
            val uri = disrupterData?.firstLink?.url
            if (uri != null) {
                openWebView(uri)
            }
            finish() // Schließt die Activity
        }

        btnLink2.setOnClickListener {
            val uri = disrupterData?.secondLink?.url
            if (uri != null) {
                openWebView(uri)
            }
            finish() // Schließt die Activity
        }

        btnLink3.setOnClickListener {
            val uri = disrupterData?.thirdLink?.url
            if (uri != null) {
                openWebView(uri)
            }
            finish() // Schließt die Activity
        }

        // TextViews für "Später erinnern" und "Kein Interesse"
        val tvRemindLater = findViewById<TextView>(R.id.tv_remind_later)
        val tvNoInterest = findViewById<TextView>(R.id.tv_no_interest)

        tvRemindLater.setOnClickListener {
            finish() // Schließt die Activity
        }

        tvNoInterest.setOnClickListener {
            // Hier kannst du die Funktionalität für "Kein Interesse" hinzufügen
            finish() // Schließt die Activity
        }

        // TextViews für "Später erinnern" und "Kein Interesse"
        val tvTitle = findViewById<TextView>(R.id.header_text)
        val tvSubtitle = findViewById<TextView>(R.id.subtitle_text)

        if (disrupterData?.firstLink?.title != null) {
            val title = disrupterData?.firstLink?.title?.trimEnd() ?: ""
            btnLink1.setText(title)
        } else {
            btnLink1.setVisibility(View.GONE);
        }
        if (disrupterData?.secondLink?.title != null) {
            val title = disrupterData?.secondLink?.title?.trimEnd() ?: ""
            btnLink2.setText(title)
        } else {
            btnLink2.setVisibility(View.GONE);
        }
        if (disrupterData?.thirdLink?.title != null) {
            val title = disrupterData?.thirdLink?.title?.trimEnd() ?: ""
            btnLink3.setText(title)
        } else {
            btnLink3.setVisibility(View.GONE);
        }
        if (disrupterData?.forwardLink?.title != null) {
            val title = disrupterData?.forwardLink?.title?.trimEnd() ?: ""
            tvRemindLater.setText(title)
        } else {
            tvRemindLater.setVisibility(View.GONE);
        }
        if (disrupterData?.noInterestLink?.title != null) {
            val title = disrupterData?.noInterestLink?.title?.trimEnd() ?: ""
            tvNoInterest.setText(title)
        } else {
            tvNoInterest.setVisibility(View.GONE);
        }
        if (disrupterData?.headline != null) {
            val headline = disrupterData?.headline?.trimEnd() ?: ""
            tvTitle.setText(Html.fromHtml(headline, Html.FROM_HTML_MODE_LEGACY))
        } else {
            tvTitle.setVisibility(View.GONE);
        }
        if (disrupterData?.text != null) {
            val text = disrupterData?.text?.trimEnd() ?: ""
            tvSubtitle.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY))
        } else {
            tvSubtitle.setVisibility(View.GONE);
        }
    }
    fun loadSfcIfResponseFromPrefs(context: Context): SfcIfResponse? {
        val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val jsonResponse = sharedPreferences.getString("vkaData", null)
        return if (jsonResponse != null) {
            Json.decodeFromString(jsonResponse)
        } else {
            null  // Falls nichts gespeichert wurde
        }
    }
    private fun openWebView(uriString: String) {
        if (uriString.isNotBlank()) { // Überprüfe, ob die URL nicht leer ist
            MyHttpClient.getInstance().getRedirectLocation(uriString) { newLocation ->
                if (newLocation != null) {
                    println("Neue Location: $newLocation")
                } else {
                    println("Kein Redirect oder Fehler aufgetreten.")
                }
                val isSilentLogin = false // uri.getQueryParameter("IF_SILENT_LOGIN")?.lowercase() == "true"
                val intent = Intent(this, WebViewActivity::class.java).apply {
                    putExtra("EXTRA_URL", newLocation)
                    putExtra("IF_SILENT_LOGIN", isSilentLogin)
                }
                startActivity(intent)
            }
        } else {
            Toast.makeText(this, "Die URL ist leer.", Toast.LENGTH_SHORT).show()
        }
    }
}
