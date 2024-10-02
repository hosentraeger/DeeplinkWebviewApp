package com.example.deeplinkwebviewapp.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.deeplinkwebviewapp.R

class DisrupterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disrupter)

        val base64Image = intent.getStringExtra("imageData")
        val imageView = findViewById<ImageView>(R.id.disrupterImageView)

        if (base64Image != null) {
            val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)
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
    }
}
