package com.example.deeplinkwebviewapp.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.deeplinkwebviewapp.R

class DisrupterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disrupter)

        // Empfange die Base64-Bilddaten
        val base64Image = intent.getStringExtra("imageData")
        base64Image?.let {
            val decodedBytes = Base64.decode(it, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

            val imageView = findViewById<ImageView>(R.id.disrupterImageView)
            imageView.setImageBitmap(bitmap)

            // Schließt die Activity beim Tippen auf das Bild
            imageView.setOnClickListener {
                finish() // Schließt die Activity
            }
        }
    }
}
