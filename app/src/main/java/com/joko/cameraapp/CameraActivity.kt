package com.joko.cameraapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.joko.cameraapp.adapter.EffectsAdapter
import com.joko.cameraapp.utils.CameraUtils
import com.joko.cameraapp.utils.ImageProcessor
import com.joko.cameraapp.utils.LocationUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraActivity : AppCompatActivity() {

    private lateinit var cameraPreview: PreviewView
    private lateinit var btnCapture: ImageButton
    private lateinit var tvLocation: TextView
    private lateinit var tvTimestamp: TextView
    private lateinit var effectsRecyclerView: RecyclerView

    private lateinit var cameraUtils: CameraUtils
    private lateinit var locationUtils: LocationUtils
    private lateinit var imageProcessor: ImageProcessor

    private var currentLocation: Location? = null
    private var selectedEffect = ImageProcessor.Effect.NONE
    private val handler = Handler(Looper.getMainLooper())
    private var timestampRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        initViews()
        setupCamera()
        setupLocation()
        setupEffects()
        startTimestampUpdates()
    }

    private fun initViews() {
        cameraPreview = findViewById(R.id.cameraPreview)
        btnCapture = findViewById(R.id.btnCapture)
        tvLocation = findViewById(R.id.tvLocation)
        tvTimestamp = findViewById(R.id.tvTimestamp)
        effectsRecyclerView = findViewById(R.id.effectsRecyclerView)

        btnCapture.setOnClickListener {
            captureImage()
        }
    }

    private fun setupCamera() {
        cameraUtils = CameraUtils(this)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraUtils.startCamera(cameraPreview, this)
        } else {
            Toast.makeText(this, "Izin kamera diperlukan", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupLocation() {
        locationUtils = LocationUtils(this)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationUtils.startLocationUpdates { location ->
                currentLocation = location
                runOnUiThread {
                    tvLocation.text = String.format(
                        "Lat: %.6f, Lng: %.6f",
                        location.latitude,
                        location.longitude
                    )
                }
            }
        } else {
            tvLocation.text = "Lokasi: Izin tidak diberikan"
        }
    }

    private fun setupEffects() {
        imageProcessor = ImageProcessor()

        val effects = listOf(
            ImageProcessor.Effect.NONE,
            ImageProcessor.Effect.DUOTONE_RED_BLUE,
            ImageProcessor.Effect.DUOTONE_GREEN_PURPLE,
            ImageProcessor.Effect.GRAIN_NOISE,
            ImageProcessor.Effect.DUOTONE_WITH_GRAIN,
            ImageProcessor.Effect.VINTAGE_SEPIA,
            ImageProcessor.Effect.COLD_BLUE,
            ImageProcessor.Effect.WARM_AMBER
        )

        val adapter = EffectsAdapter(effects) { effect ->
            selectedEffect = effect
            Toast.makeText(this@CameraActivity, "Effect: ${getEffectDisplayName(effect)}", Toast.LENGTH_SHORT).show()
        }

        effectsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        effectsRecyclerView.adapter = adapter
    }

    private fun getEffectDisplayName(effect: ImageProcessor.Effect): String {
        return when (effect) {
            ImageProcessor.Effect.NONE -> "Normal"
            ImageProcessor.Effect.DUOTONE_RED_BLUE -> "Duotone RB"
            ImageProcessor.Effect.DUOTONE_GREEN_PURPLE -> "Duotone GP"
            ImageProcessor.Effect.GRAIN_NOISE -> "Grain"
            ImageProcessor.Effect.DUOTONE_WITH_GRAIN -> "Duotone+Grain"
            ImageProcessor.Effect.VINTAGE_SEPIA -> "Sepia"
            ImageProcessor.Effect.COLD_BLUE -> "Cold Blue"
            ImageProcessor.Effect.WARM_AMBER -> "Warm Amber"
        }
    }

    private fun startTimestampUpdates() {
        timestampRunnable = object : Runnable {
            override fun run() {
                val currentTime = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
                tvTimestamp.text = currentTime
                handler.postDelayed(this, 1000) // Update every second
            }
        }
        timestampRunnable?.run()
    }

    private fun captureImage() {
        val imageCapture = cameraUtils.getImageCapture() ?: return

        val outputDirectory = getOutputDirectory()
        val photoFile = File(outputDirectory, "photo_${System.currentTimeMillis()}.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    openPreviewActivity(savedUri, photoFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                    Toast.makeText(
                        this@CameraActivity,
                        "Gagal mengambil foto: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    private fun openPreviewActivity(imageUri: Uri, imageFile: File) {
        val currentTimestamp = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())

        val intent = Intent(this, PreviewActivity::class.java).apply {
            putExtra("image_uri", imageUri.toString())
            putExtra("image_path", imageFile.absolutePath)
            putExtra("effect", selectedEffect.name)
            putExtra("timestamp", currentTimestamp)
            currentLocation?.let { location ->
                putExtra("latitude", location.latitude)
                putExtra("longitude", location.longitude)
            }
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        timestampRunnable?.let { handler.removeCallbacks(it) }
        locationUtils.stopLocationUpdates()
        cameraUtils.shutdown()
    }
}