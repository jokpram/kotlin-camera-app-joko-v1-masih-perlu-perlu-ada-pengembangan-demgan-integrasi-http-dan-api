package com.joko.cameraapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var btnOpenCamera: Button

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            startCameraActivity()
        } else {
            Toast.makeText(this, "Izin diperlukan untuk menggunakan aplikasi", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnOpenCamera = findViewById(R.id.btnOpenCamera)
        btnOpenCamera.setOnClickListener {
            checkPermissions()
        }
    }

    private fun checkPermissions() {
        val requiredPermissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            requiredPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        val ungrantedPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (ungrantedPermissions.isEmpty()) {
            startCameraActivity()
        } else {
            permissionLauncher.launch(ungrantedPermissions.toTypedArray())
        }
    }

    private fun startCameraActivity() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
    }
}