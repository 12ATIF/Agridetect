package com.dicoding.capstone.dermaface.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.capstone.dermaface.R
import com.dicoding.capstone.dermaface.repository.ScanRepository
import com.dicoding.capstone.dermaface.utils.ScanUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScanViewModel(private val scanRepository: ScanRepository) : ViewModel() {

    private val _modelReady = MutableLiveData<Boolean>()
    val modelReady: LiveData<Boolean> get() = _modelReady

    private val _analysisResult = MutableLiveData<Pair<String, String>>()
    val analysisResult: LiveData<Pair<String, String>> get() = _analysisResult

    private val _modelStatusMessage = MutableLiveData<String?>()
    val modelStatusMessage: MutableLiveData<String?> get() = _modelStatusMessage

    init {
        loadModel()
    }

    private fun loadModel() {
        _modelReady.value = false
        _modelStatusMessage.value = scanRepository.context.getString(R.string.model_downloading)
        scanRepository.downloadModel(
            onModelDownloaded = {
                _modelReady.postValue(true)
                _modelStatusMessage.postValue(scanRepository.context.getString(R.string.model_ready))
            },
            onError = { error ->
                _modelReady.postValue(false)
                _modelStatusMessage.postValue("${scanRepository.context.getString(R.string.error_downloading_model)}: $error")
            }
        )
    }

    fun analyzeImage(bitmap: Bitmap) {
        if (_modelReady.value != true) {
            _analysisResult.postValue("Error" to scanRepository.context.getString(R.string.model_not_ready))
            return
        }

        viewModelScope.launch {
            try {
                val processedBitmap = withContext(Dispatchers.IO) {
                    // Apply any preprocessing needed for your model
                    ScanUtil.preprocessImage(bitmap)
                }

                val results = withContext(Dispatchers.IO) {
                    scanRepository.analyzeImage(processedBitmap)
                }

                if (results != null) {
                    val (confidence, diseaseName) = getMaxResult(results)
                    val recommendation = withContext(Dispatchers.IO) {
                        scanRepository.getRecommendation(diseaseName)
                    }
                    _analysisResult.postValue(diseaseName to recommendation)
                } else {
                    _analysisResult.postValue("Error" to scanRepository.context.getString(R.string.error_analyzing_image))
                }
            } catch (e: Exception) {
                _analysisResult.postValue("Error" to "${scanRepository.context.getString(R.string.error_analyzing_image)}: ${e.message}")
            }
        }
    }

    private fun getMaxResult(confidences: FloatArray): Pair<Float, String> {
        // Update these labels to match your chili disease classes
        val classLabels = arrayOf(
            "Antraknosa",         // anthracnose
            "Embun Tepung",       // powderymildew
            "Kutu Kebul",         // kutukebul
            "Daun Keriting",      // leafcurl
            "Bercak Daun",        // leafspot
            "Sehat",              // sehat
            "Menguning"           // yellowish
        )

        var maxConfidence = Float.MIN_VALUE
        var maxIndex = -1

        for (i in confidences.indices) {
            if (confidences[i] > maxConfidence) {
                maxConfidence = confidences[i]
                maxIndex = i
            }
        }

        // Handle potential index issues
        val index = if (maxIndex in classLabels.indices) maxIndex else 0
        return maxConfidence to classLabels[index]
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up resources
        scanRepository.closeModel()
    }
}