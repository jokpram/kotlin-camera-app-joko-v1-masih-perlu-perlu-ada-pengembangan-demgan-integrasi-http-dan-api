package com.joko.cameraapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.joko.cameraapp.utils.ImageProcessor
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PreviewActivity : AppCompatActivity() {

    private lateinit var ivPreview: ImageView
    private lateinit var btnRetake: Button
    private lateinit var btnDownload: Button
    private lateinit var tvLocationInfo: TextView
    private lateinit var seekBarColor: SeekBar
    private lateinit var seekBarNoise: SeekBar

    private lateinit var imageProcessor: ImageProcessor
    private var originalBitmap: Bitmap? = null
    private var processedBitmap: Bitmap? = null
    private var imagePath: String? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var selectedEffect: ImageProcessor.Effect = ImageProcessor.Effect.NONE
    private var captureTimestamp: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        initViews()
        processIntent()
        setupImageProcessor()
        setupSeekBars()
        displayImage()
        setupClickListeners()
    }

    private fun initViews() {
        ivPreview = findViewById(R.id.ivPreview)
        btnRetake = findViewById(R.id.btnRetake)
        btnDownload = findViewById(R.id.btnDownload)
        tvLocationInfo = findViewById(R.id.tvLocationInfo)
        seekBarColor = findViewById(R.id.seekBarColor)
        seekBarNoise = findViewById(R.id.seekBarNoise)
    }

    private fun processIntent() {
        imagePath = intent.getStringExtra("image_path")
        latitude = intent.getDoubleExtra("latitude", 0.0).takeIf { it != 0.0 }
        longitude = intent.getDoubleExtra("longitude", 0.0).takeIf { it != 0.0 }
        captureTimestamp = intent.getStringExtra("timestamp") ?: imageProcessor.getCurrentTimestamp()

        val effectName = intent.getStringExtra("effect") ?: "NONE"
        selectedEffect = try {
            ImageProcessor.Effect.valueOf(effectName)
        } catch (e: Exception) {
            ImageProcessor.Effect.NONE
        }

        updateLocationInfo()
    }

    private fun setupImageProcessor() {
        imageProcessor = ImageProcessor()
    }

    private fun setupSeekBars() {
        seekBarColor.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    applyEffects()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarNoise.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    applyEffects()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun displayImage() {
        imagePath?.let { path ->
            try {
                val options = BitmapFactory.Options().apply {
                    inSampleSize = 2
                }
                val bitmap = BitmapFactory.decodeFile(path, options)
                originalBitmap = bitmap
                applyEffects()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this, "Path gambar tidak valid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyEffects() {
        originalBitmap?.let { bitmap ->
            val settings = ImageProcessor.EffectSettings(
                colorIntensity = seekBarColor.progress,
                noiseIntensity = seekBarNoise.progress
            )

            processedBitmap = imageProcessor.applyEffect(bitmap, selectedEffect, settings)
            ivPreview.setImageBitmap(processedBitmap)
        }
    }

    private fun updateLocationInfo() {
        val locationText = if (latitude != null && longitude != null) {
            String.format("Lokasi: %.6f, %.6f", latitude, longitude)
        } else {
            "Lokasi: Tidak tersedia"
        }

        val timestampText = "Waktu: $captureTimestamp"
        tvLocationInfo.text = "$timestampText\n$locationText"
    }

    private fun setupClickListeners() {
        btnRetake.setOnClickListener {
            finish()
        }

        btnDownload.setOnClickListener {
            downloadImage()
        }
    }

    private fun downloadImage() {
        processedBitmap?.let { bitmap ->
            try {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "photo_${timeStamp}.jpg"

                val downloadsDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    ?: filesDir
                val outputFile = File(downloadsDir, fileName)

                imageProcessor.saveImageWithMetadata(
                    bitmap,
                    outputFile,
                    latitude,
                    longitude,
                    captureTimestamp
                )

                // Scan file to make it visible in gallery
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                mediaScanIntent.data = Uri.fromFile(outputFile)
                sendBroadcast(mediaScanIntent)

                Toast.makeText(this, "Foto berhasil disimpan: ${outputFile.absolutePath}", Toast.LENGTH_LONG).show()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Gagal menyimpan foto: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this, "Tidak ada gambar untuk disimpan", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        originalBitmap?.recycle()
        processedBitmap?.recycle()
    }
}