package com.example.deeplinkwebviewapp.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Html
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.deeplinkwebviewapp.R
import com.example.deeplinkwebviewapp.data.Disrupter
import kotlinx.serialization.json.Json

class DisrupterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disrupter)

        val imageView = findViewById<ImageView>(R.id.disrupterImageView)

        // Schließt die Activity beim Tippen auf das Bild
        imageView.setOnClickListener {
            finish() // Schließt die Activity
        }

        // Buttons hinzufügen
        val btnLink1 = findViewById<Button>(R.id.btn_link1)
        val btnLink2 = findViewById<Button>(R.id.btn_link2)
        val btnLink3 = findViewById<Button>(R.id.btn_link3)

        btnLink1.setOnClickListener {
            finish() // Schließt die Activity
        }

        btnLink2.setOnClickListener {
            finish() // Schließt die Activity
        }

        btnLink3.setOnClickListener {
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

        val disrupterDataJson = intent.getStringExtra("disrupterDataJson")
        val disrupterData = disrupterDataJson?.let {
            Json.decodeFromString<Disrupter>(it)
        } ?: run {
            // Handle the case when disrupterDataJson is null, maybe log an error or use default data
            null
        }

        if (disrupterData?.image != null) {
            val decodedBytes = Base64.decode(disrupterData.image, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            imageView.setImageBitmap(bitmap)
        } else {
            imageView.setImageResource(R.drawable.sample_disrupter) // Default-Bild
        }
        if (disrupterData?.firstLink?.title != null) {
            btnLink1.setText(disrupterData.firstLink.title)
        } else {
            btnLink1.setVisibility(View.INVISIBLE);
        }
        if (disrupterData?.secondLink?.title != null) {
            btnLink2.setText(disrupterData.secondLink.title)
        } else {
            btnLink2.setVisibility(View.INVISIBLE);
        }
        if (disrupterData?.thirdLink?.title != null) {
            btnLink3.setText(disrupterData.thirdLink.title)
        } else {
            btnLink3.setVisibility(View.INVISIBLE);
        }
        if (disrupterData?.forwardLink?.title != null) {
            tvRemindLater.setText(disrupterData.forwardLink.title)
        } else {
            tvRemindLater.setVisibility(View.INVISIBLE);
        }
        if (disrupterData?.noInterestLink?.title != null) {
            tvNoInterest.setText(disrupterData.noInterestLink.title)
        } else {
            tvNoInterest.setVisibility(View.INVISIBLE);
        }
        if (disrupterData?.headline != null) {
            tvTitle.setText(Html.fromHtml(disrupterData.headline, Html.FROM_HTML_MODE_LEGACY))
        } else {
            tvTitle.setVisibility(View.INVISIBLE);
        }
        if (disrupterData?.text != null) {
            tvSubtitle.setText(Html.fromHtml(disrupterData.text, Html.FROM_HTML_MODE_LEGACY))
        } else {
            tvSubtitle.setVisibility(View.INVISIBLE);
        }
    }
}
