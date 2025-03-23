package com.dicoding.capstone.dermaface.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.dicoding.capstone.dermaface.R
import com.google.firebase.ml.modeldownloader.CustomModel
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import org.tensorflow.lite.Interpreter
import java.io.File

class FirebaseModelHandler(private val context: Context) {

    private var interpreter: Interpreter? = null
    private val MODEL_NAME = "ChiliDiseaseModel" // Nama model Anda di Firebase ML

    init {
        loadLocalModel()
    }

    private fun loadLocalModel() {
        val localModelFile = File(context.filesDir, "chili_model.tflite")
        if (localModelFile.exists()) {
            try {
                interpreter = Interpreter(localModelFile)
                Log.d("FirebaseModelHandler", "Model lokal dimuat dengan sukses")
            } catch (e: Exception) {
                Log.e("FirebaseModelHandler", "Error memuat model lokal: ${e.message}")
            }
        } else {
            Log.d("FirebaseModelHandler", "Model lokal tidak ditemukan, perlu diunduh dari Firebase")
        }
    }

    fun downloadModel(
        onModelDownloaded: () -> Unit,
        onError: (String) -> Unit
    ) {
        val conditions = CustomModelDownloadConditions.Builder()
            .requireWifi()  // Hapus ini jika Anda ingin mengizinkan unduhan menggunakan data seluler
            .build()

        FirebaseModelDownloader.getInstance()
            .getModel(MODEL_NAME, DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND, conditions)
            .addOnSuccessListener { model: CustomModel ->
                model.file?.let { file ->
                    try {
                        // Salin model ke lokasi permanen jika diperlukan
                        val localModelFile = File(context.filesDir, "chili_model.tflite")
                        if (!localModelFile.exists() || localModelFile.length() != file.length()) {
                            file.copyTo(localModelFile, overwrite = true)
                        }

                        // Inisialisasi interpreter dengan model yang diunduh
                        interpreter = Interpreter(localModelFile)
                        Log.d("FirebaseModelHandler", "Model berhasil diunduh dan dimuat")
                        onModelDownloaded()
                    } catch (e: Exception) {
                        Log.e("FirebaseModelHandler", "Error menyalin/memuat model: ${e.message}")
                        onError(context.getString(R.string.error_loading_model))
                    }
                } ?: run {
                    Log.e("FirebaseModelHandler", "File model tidak tersedia")
                    onError(context.getString(R.string.error_downloading_model))
                }
            }
            .addOnFailureListener { e: Exception ->
                Log.e("FirebaseModelHandler", "Error mengunduh model: ${e.message}")
                onError("${context.getString(R.string.error_downloading_model)}: ${e.message}")
            }
    }

    fun analyzeImage(bitmap: Bitmap): FloatArray? {
        val inputBuffer = ScanUtil.convertBitmapToByteBuffer(bitmap)
        val outputBuffer = Array(1) { FloatArray(7) } // Ubah dari 5 menjadi 7 kelas
        return try {
            interpreter?.run(inputBuffer, outputBuffer)
            outputBuffer[0]
        } catch (e: Exception) {
            Log.e("FirebaseModelHandler", "Error menganalisis gambar: ${e.message}")
            null
        }
    }

    // Bersihkan sumber daya
    fun close() {
        interpreter?.close()
    }
}
