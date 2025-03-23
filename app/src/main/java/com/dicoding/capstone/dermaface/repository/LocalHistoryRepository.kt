package com.dicoding.capstone.dermaface.repository

import android.content.Context
import android.util.Log
import com.dicoding.capstone.dermaface.data.model.HistoryResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocalHistoryRepository(private val context: Context) {

    private val TAG = "LocalHistoryRepository"

    suspend fun fetchHistories(): Result<List<HistoryResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val storageDir = File(context.filesDir, "scan_results")
                Log.d(TAG, "Mencari riwayat di direktori: ${storageDir.absolutePath}")

                if (!storageDir.exists()) {
                    Log.d(TAG, "Direktori scan_results tidak ditemukan")
                    return@withContext Result.success(emptyList())
                }

                // Cari semua file gambar, metadata akan diekstrak dari nama file
                val imageFiles = storageDir.listFiles { file ->
                    file.name.endsWith("_image.jpg")
                }

                if (imageFiles == null || imageFiles.isEmpty()) {
                    Log.d(TAG, "Tidak ada file gambar ditemukan")
                    return@withContext Result.success(emptyList())
                }

                Log.d(TAG, "Ditemukan ${imageFiles.size} file gambar")

                val histories = imageFiles.mapNotNull { imageFile ->
                    try {
                        // Dapatkan timestamp dari nama file (format: yyyyMMdd_HHmmss_image.jpg)
                        val timestampPart = imageFile.name.substringBefore("_image.jpg")
                        Log.d(TAG, "Mengekstrak timestamp: $timestampPart")

                        // Parse timestamp jika dalam format yang benar
                        val timestamp = try {
                            val date = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                                .parse(timestampPart)
                            date?.time ?: System.currentTimeMillis()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error mengurai timestamp: ${e.message}")
                            System.currentTimeMillis()
                        }

                        // Cek file metadata yang terkait
                        val metadataFile = File(storageDir, "${timestampPart}_meta.json")
                        var diagnosis = "Hasil Pemindaian"
                        var recommendation = ""

                        if (metadataFile.exists()) {
                            Log.d(TAG, "Metadata ditemukan: ${metadataFile.name}")
                            val metadataContent = metadataFile.readText()

                            // Parse metadata secara manual
                            diagnosis = extractFromJson(metadataContent, "diagnosis") ?: diagnosis
                            recommendation = extractFromJson(metadataContent, "recommendation") ?: ""
                        } else {
                            Log.d(TAG, "Metadata tidak ditemukan")
                        }

                        // Buat objek HistoryResponse
                        val history = HistoryResponse(
                            id = timestampPart,
                            image_url = imageFile.absolutePath,
                            diagnosis = diagnosis,
                            recommendation = recommendation,
                            timestamp = timestamp
                        )

                        Log.d(TAG, "Riwayat dibuat: ${history.id} - ${history.diagnosis}")
                        history
                    } catch (e: Exception) {
                        Log.e(TAG, "Error memproses file ${imageFile.name}: ${e.message}")
                        null
                    }
                }.sortedByDescending { it.timestamp }

                Log.d(TAG, "Total riwayat yang berhasil dimuat: ${histories.size}")
                Result.success(histories)
            } catch (e: Exception) {
                Log.e(TAG, "Error mengambil riwayat: ${e.message}")
                Result.failure(e)
            }
        }
    }

    private fun extractFromJson(json: String, key: String): String? {
        val pattern = "\"$key\"\\s*:\\s*\"([^\"]*)\"".toRegex()
        val matchResult = pattern.find(json)
        return matchResult?.groupValues?.getOrNull(1)
    }

    suspend fun deleteHistory(historyId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val storageDir = File(context.filesDir, "scan_results")
                val imageFile = File(storageDir, "${historyId}_image.jpg")
                val metadataFile = File(storageDir, "${historyId}_meta.json")

                var success = true

                if (imageFile.exists()) {
                    success = imageFile.delete() && success
                    Log.d(TAG, "Menghapus file gambar: $success")
                }

                if (metadataFile.exists()) {
                    success = metadataFile.delete() && success
                    Log.d(TAG, "Menghapus file metadata: $success")
                }

                if (success) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Gagal menghapus beberapa file riwayat"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error menghapus riwayat: ${e.message}")
                Result.failure(e)
            }
        }
    }
}