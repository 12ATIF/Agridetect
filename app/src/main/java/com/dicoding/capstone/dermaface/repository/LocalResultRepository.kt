package com.dicoding.capstone.dermaface.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocalResultRepository(private val context: Context) {

    companion object {
        private const val TAG = "LocalResultRepository"
    }

    suspend fun saveResultLocally(imageUri: Uri, diagnosis: String, recommendation: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Buat direktori penyimpanan jika belum ada
                val storageDir = File(context.filesDir, "scan_results")
                if (!storageDir.exists()) {
                    storageDir.mkdirs()
                }

                // Format tanggal untuk nama file
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

                // Salin gambar ke penyimpanan lokal
                val imageFile = File(storageDir, "${timeStamp}_image.jpg")
                context.contentResolver.openInputStream(imageUri)?.use { input ->
                    FileOutputStream(imageFile).use { output ->
                        input.copyTo(output)
                    }
                }

                // Simpan metadata ke file JSON
                val metadataFile = File(storageDir, "${timeStamp}_meta.json")
                val metadata = """
                    {
                        "image_path": "${imageFile.absolutePath}",
                        "diagnosis": "$diagnosis",
                        "recommendation": "$recommendation",
                        "timestamp": "${System.currentTimeMillis()}"
                    }
                """.trimIndent()

                FileOutputStream(metadataFile).use { it.write(metadata.toByteArray()) }

                Log.d(TAG, "Hasil scan berhasil disimpan secara lokal: ${imageFile.absolutePath}")

                Result.success(imageFile.absolutePath)
            } catch (e: Exception) {
                Log.e(TAG, "Error menyimpan hasil scan: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun loadHistory(): Result<List<Map<String, Any>>> {
        return withContext(Dispatchers.IO) {
            try {
                val storageDir = File(context.filesDir, "scan_results")
                if (!storageDir.exists() || !storageDir.isDirectory) {
                    return@withContext Result.success(emptyList())
                }

                val metadataFiles = storageDir.listFiles { file -> file.name.endsWith("_meta.json") }
                    ?: return@withContext Result.success(emptyList())

                val historyItems = metadataFiles.mapNotNull { file ->
                    try {
                        val json = file.readText()
                        // Ini adalah cara sederhana untuk mengurai JSON
                        // Dalam aplikasi nyata, Anda mungkin ingin menggunakan library JSON seperti Gson
                        val imagePath = json.substringAfter("image_path").substringAfter(":").substringAfter("\"").substringBefore("\"").trim()
                        val diagnosis = json.substringAfter("diagnosis").substringAfter(":").substringAfter("\"").substringBefore("\"").trim()
                        val recommendation = json.substringAfter("recommendation").substringAfter(":").substringAfter("\"").substringBefore("\"").trim()
                        val timestamp = json.substringAfter("timestamp").substringAfter(":").substringBefore("}").trim().toLong()

                        mapOf(
                            "id" to file.nameWithoutExtension,
                            "image_path" to imagePath,
                            "diagnosis" to diagnosis,
                            "recommendation" to recommendation,
                            "timestamp" to timestamp
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing metadata file ${file.name}: ${e.message}")
                        null
                    }
                }

                Result.success(historyItems)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading history: ${e.message}")
                Result.failure(e)
            }
        }
    }
}