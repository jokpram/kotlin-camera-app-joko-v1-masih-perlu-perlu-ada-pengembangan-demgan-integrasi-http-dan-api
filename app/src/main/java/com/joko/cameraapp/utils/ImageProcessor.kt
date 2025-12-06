package com.joko.cameraapp.utils

import android.graphics.*
import android.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random
import java.text.SimpleDateFormat
import java.util.*

class ImageProcessor {

    enum class Effect {
        NONE, DUOTONE_RED_BLUE, DUOTONE_GREEN_PURPLE, GRAIN_NOISE, DUOTONE_WITH_GRAIN,
        VINTAGE_SEPIA, COLD_BLUE, WARM_AMBER
    }

    data class EffectSettings(
        val colorIntensity: Int = 50,
        val noiseIntensity: Int = 30
    )

    fun applyEffect(bitmap: Bitmap, effect: Effect, settings: EffectSettings = EffectSettings()): Bitmap {
        return when (effect) {
            Effect.NONE -> bitmap
            Effect.DUOTONE_RED_BLUE -> applyDuotoneEffect(bitmap, Color.RED, Color.BLUE, settings.colorIntensity)
            Effect.DUOTONE_GREEN_PURPLE -> applyDuotoneEffect(bitmap, Color.GREEN, Color.MAGENTA, settings.colorIntensity)
            Effect.GRAIN_NOISE -> applyGrainEffect(bitmap, settings.noiseIntensity)
            Effect.DUOTONE_WITH_GRAIN -> applyDuotoneWithGrain(bitmap, Color.YELLOW, Color.CYAN, settings)
            Effect.VINTAGE_SEPIA -> applySepiaEffect(bitmap, settings.colorIntensity)
            Effect.COLD_BLUE -> applyColdBlueEffect(bitmap, settings.colorIntensity)
            Effect.WARM_AMBER -> applyWarmAmberEffect(bitmap, settings.colorIntensity)
        }
    }

    private fun applyDuotoneEffect(bitmap: Bitmap, color1: Int, color2: Int, intensity: Int): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val paint = Paint()
        val matrix = ColorMatrix().apply {
            setSaturation(0f)
        }

        paint.colorFilter = ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        val intensityFactor = intensity / 100f
        val adjustedColor1 = adjustColorIntensity(color1, intensityFactor)
        val adjustedColor2 = adjustColorIntensity(color2, intensityFactor)

        val gradient = LinearGradient(
            0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(),
            adjustedColor1, adjustedColor2, Shader.TileMode.CLAMP
        )

        paint.colorFilter = null
        paint.shader = gradient
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
        canvas.drawRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), paint)

        return result
    }

    private fun applyGrainEffect(bitmap: Bitmap, intensity: Int): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint()

        val intensityFactor = intensity / 100f
        val alpha = (intensityFactor * 50).toInt()

        val noiseBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val random = Random(System.currentTimeMillis())

        for (x in 0 until noiseBitmap.width) {
            for (y in 0 until noiseBitmap.height) {
                val noiseValue = random.nextInt(50) - 25
                val color = Color.argb(alpha, noiseValue, noiseValue, noiseValue)
                noiseBitmap.setPixel(x, y, color)
            }
        }

        canvas.drawBitmap(noiseBitmap, 0f, 0f, paint)
        return result
    }

    private fun applyDuotoneWithGrain(bitmap: Bitmap, color1: Int, color2: Int, settings: EffectSettings): Bitmap {
        val duotone = applyDuotoneEffect(bitmap, color1, color2, settings.colorIntensity)
        return applyGrainEffect(duotone, settings.noiseIntensity)
    }

    private fun applySepiaEffect(bitmap: Bitmap, intensity: Int): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()

        val intensityFactor = intensity / 100f

        val colorMatrix = ColorMatrix().apply {
            set(floatArrayOf(
                0.393f + 0.607f * intensityFactor, 0.769f - 0.769f * intensityFactor, 0.189f - 0.189f * intensityFactor, 0f, 0f,
                0.349f - 0.349f * intensityFactor, 0.686f + 0.314f * intensityFactor, 0.168f - 0.168f * intensityFactor, 0f, 0f,
                0.272f - 0.272f * intensityFactor, 0.534f - 0.534f * intensityFactor, 0.131f + 0.869f * intensityFactor, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
        }

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return result
    }

    private fun applyColdBlueEffect(bitmap: Bitmap, intensity: Int): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()

        val intensityFactor = intensity / 100f

        val colorMatrix = ColorMatrix().apply {
            set(floatArrayOf(
                1f - 0.3f * intensityFactor, 0f, 0f, 0f, 0f,
                0f, 1f - 0.1f * intensityFactor, 0f, 0f, 0f,
                0f, 0f, 1f + 0.3f * intensityFactor, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
        }

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return result
    }

    private fun applyWarmAmberEffect(bitmap: Bitmap, intensity: Int): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()

        val intensityFactor = intensity / 100f

        val colorMatrix = ColorMatrix().apply {
            set(floatArrayOf(
                1f + 0.2f * intensityFactor, 0f, 0f, 0f, 0f,
                0f, 1f + 0.1f * intensityFactor, 0f, 0f, 0f,
                0f, 0f, 1f - 0.2f * intensityFactor, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
        }

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return result
    }

    private fun adjustColorIntensity(color: Int, intensity: Float): Int {
        val alpha = Color.alpha(color)
        val red = (Color.red(color) * intensity).toInt()
        val green = (Color.green(color) * intensity).toInt()
        val blue = (Color.blue(color) * intensity).toInt()

        return Color.argb(alpha, red, green, blue)
    }

    fun saveImageWithMetadata(
        bitmap: Bitmap,
        file: File,
        latitude: Double?,
        longitude: Double?,
        timestamp: String
    ) {
        try {
            // Create bitmap with timestamp and location overlay
            val finalBitmap = addTimestampAndLocationOverlay(bitmap, timestamp, latitude, longitude)

            FileOutputStream(file).use { out ->
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            // Save location to EXIF if available
            if (latitude != null && longitude != null) {
                val exif = ExifInterface(file.absolutePath)
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, convertToDegreeMinuteSeconds(latitude))
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, if (latitude >= 0) "N" else "S")
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, convertToDegreeMinuteSeconds(longitude))
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, if (longitude >= 0) "E" else "W")
                exif.saveAttributes()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addTimestampAndLocationOverlay(
        bitmap: Bitmap,
        timestamp: String,
        latitude: Double?,
        longitude: Double?
    ): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint()

        // Setup text paint
        paint.color = Color.WHITE
        paint.textSize = 36f
        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        paint.setShadowLayer(4f, 2f, 2f, Color.BLACK)

        // Create info text
        val locationText = if (latitude != null && longitude != null) {
            String.format("Lat: %.4f, Lng: %.4f", latitude, longitude)
        } else {
            "Location: Not available"
        }

        val infoText = "$timestamp\n$locationText"

        // Draw background for text
        val textBounds = Rect()
        paint.getTextBounds(infoText, 0, infoText.length, textBounds)

        val backgroundPaint = Paint().apply {
            color = Color.argb(128, 0, 0, 0)
            style = Paint.Style.FILL
        }

        val padding = 20
        val backgroundRect = Rect(
            padding,
            bitmap.height - textBounds.height() - padding * 3,
            bitmap.width - padding,
            bitmap.height - padding
        )

        canvas.drawRect(backgroundRect, backgroundPaint)

        // Draw text
        val xPos = padding + 10f
        val yPos = bitmap.height - padding - 10f

        // Draw timestamp
        canvas.drawText(timestamp, xPos, yPos - 40, paint)

        // Draw location
        canvas.drawText(locationText, xPos, yPos, paint)

        return result
    }

    private fun convertToDegreeMinuteSeconds(coordinate: Double): String {
        val absolute = Math.abs(coordinate)
        val degrees = Math.floor(absolute)
        val minutes = Math.floor((absolute - degrees) * 60)
        val seconds = ((absolute - degrees - minutes / 60) * 3600)
        return "$degrees/1,$minutes/1,$seconds/1000"
    }

    fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}