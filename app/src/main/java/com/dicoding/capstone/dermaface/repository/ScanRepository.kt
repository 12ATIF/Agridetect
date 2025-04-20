package com.dicoding.capstone.dermaface.repository

import android.content.Context
import android.graphics.Bitmap
import com.dicoding.capstone.dermaface.R
import com.dicoding.capstone.dermaface.utils.FirebaseModelHandler
import com.dicoding.capstone.dermaface.utils.PlantValidator
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ScanRepository(val context: Context) {

    private val firebaseModelHandler = FirebaseModelHandler(context)
    private val firestore = FirebaseFirestore.getInstance()

    fun downloadModel(
        onModelDownloaded: () -> Unit,
        onError: (String) -> Unit
    ) {
        firebaseModelHandler.downloadModel(onModelDownloaded, onError)
    }

    fun analyzeImage(bitmap: Bitmap): FloatArray? {
        return firebaseModelHandler.analyzeImage(bitmap)
    }

    fun verifyChiliPlant(bitmap: Bitmap): Boolean {
        return PlantValidator.isLikelyChiliPlant(bitmap)
    }

    suspend fun getRecommendation(diagnosis: String): String {
        return try {
            val document = firestore.collection("chili_recommendations")
                .document(diagnosis)
                .get()
                .await()

            if (document.exists()) {
                val tips = document.get("tips") as? List<*>
                tips?.joinToString("\n") ?: getDefaultRecommendation(diagnosis)
            } else {
                getDefaultRecommendation(diagnosis)
            }
        } catch (e: Exception) {
            getDefaultRecommendation(diagnosis)
        }
    }

    private fun getDefaultRecommendation(diagnosis: String): String {
        // Menyediakan rekomendasi default jika data Firebase tidak tersedia
        return when (diagnosis) {
            "Sehat" -> "Tanaman cabai Anda terlihat sehat! Lanjutkan dengan penyiraman dan pemupukan secara teratur."

            "anthracnose" -> "Rekomendasi penanganan:\n• Buang bagian tanaman yang terinfeksi\n• Aplikasikan fungisida berbasis tembaga\n• Pastikan jarak tanam yang cukup\n• Hindari penyiraman dari atas\n• Lakukan rotasi tanaman di penanaman berikutnya"

            "powderymildew" -> "Rekomendasi penanganan:\n• Aplikasikan fungisida pada tanda pertama penyakit\n• Tingkatkan sirkulasi udara\n• Hindari penyiraman dari atas\n• Buang daun yang terinfeksi parah\n• Gunakan mulsa perak"

            "kutukebul" -> "Rekomendasi penanganan:\n• Gunakan insektisida yang sesuai\n• Pasang perangkap kuning\n• Tanam tanaman pengusir seperti kemangi\n• Gunakan semprotan air bertekanan untuk mengusir kutu\n• Aplikasikan sabun insektisida"

            "leafcurl" -> "Rekomendasi penanganan:\n• Buang dan musnahkan tanaman yang terinfeksi parah\n• Kendalikan serangga vektor dengan insektisida\n• Gunakan mulsa reflektif\n• Tanam varietas tahan virus\n• Gunakan naungan"

            "leafspot" -> "Rekomendasi penanganan:\n• Buang daun yang terinfeksi\n• Aplikasikan fungisida berbasis tembaga\n• Pastikan sirkulasi udara yang baik\n• Hindari penyiraman dari atas\n• Gunakan pupuk yang seimbang"

            "Yellowish" -> "Rekomendasi penanganan:\n• Periksa kecukupan nutrisi (terutama nitrogen)\n• Pastikan pH tanah sesuai (5.5-6.5)\n• Periksa drainase tanah\n• Cek adanya serangan hama/penyakit akar\n• Aplikasikan pupuk dengan kandungan nitrogen yang cukup"

            else -> context.getString(R.string.error_getting_recommendation)
        }
    }

    fun closeModel() {
        firebaseModelHandler.close()
    }
}