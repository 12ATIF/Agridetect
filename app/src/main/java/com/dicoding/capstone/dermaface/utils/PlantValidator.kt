package com.dicoding.capstone.dermaface.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log

class PlantValidator {
    companion object {
        private const val TAG = "PlantValidator"

        /**
         * Memeriksa apakah gambar kemungkinan adalah tanaman cabai
         * berdasarkan dominasi warna hijau dan karakteristik warna.
         */
        fun isLikelyChiliPlant(bitmap: Bitmap): Boolean {
            // Resize gambar untuk optimasi performa
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

            var greenPixels = 0
            var redPixels = 0
            var brownPixels = 0
            val totalPixels = resizedBitmap.width * resizedBitmap.height

            // Hitung distribusi warna
            for (x in 0 until resizedBitmap.width) {
                for (y in 0 until resizedBitmap.height) {
                    val pixel = resizedBitmap.getPixel(x, y)
                    val red = Color.red(pixel)
                    val green = Color.green(pixel)
                    val blue = Color.blue(pixel)

                    // Identifikasi warna hijau (karakteristik daun tanaman)
                    if (green > red && green > blue && green > 70) {
                        greenPixels++
                    }

                    // Identifikasi warna merah (kemungkinan buah cabai)
                    if (red > green && red > blue && red > 100) {
                        redPixels++
                    }

                    // Identifikasi warna coklat (kemungkinan batang tanaman)
                    if (red > 60 && red < 150 && green > 40 && green < 100 && blue < 60) {
                        brownPixels++
                    }
                }
            }

            // Hitung rasio warna
            val greenRatio = greenPixels.toFloat() / totalPixels
            val redRatio = redPixels.toFloat() / totalPixels
            val brownRatio = brownPixels.toFloat() / totalPixels

            Log.d(TAG, "Rasio hijau: ${greenRatio * 100}%")
            Log.d(TAG, "Rasio merah: ${redRatio * 100}%")
            Log.d(TAG, "Rasio coklat: ${brownRatio * 100}%")

            // Kriteria validasi:
            // 1. Minimal 25% piksel hijau (daun tanaman)
            // 2. Minimal 5% piksel merah atau coklat (buah cabai atau batang)
            val isPlant = greenRatio > 0.25 && (redRatio > 0.05 || brownRatio > 0.05)

            Log.d(TAG, "Hasil validasi tanaman cabai: $isPlant")
            return isPlant
        }
    }
}