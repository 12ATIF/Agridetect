package com.dicoding.capstone.dermaface.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.dicoding.capstone.dermaface.R
import com.dicoding.capstone.dermaface.databinding.ActivityDetailHistoryBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailHistoryBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get data from intent
        val imagePath = intent.getStringExtra("HISTORY_IMAGE")
        val diagnosis = intent.getStringExtra("HISTORY_DIAGNOSIS")
        val recommendation = intent.getStringExtra("HISTORY_RECOMMENDATION")
        val timestamp = intent.getLongExtra("HISTORY_TIMESTAMP", 0L)

        if (imagePath != null && diagnosis != null) {
            displayHistoryDetails(imagePath, diagnosis, recommendation ?: "", timestamp)
        } else {
            Toast.makeText(this, R.string.no_history_data, Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun displayHistoryDetails(imagePath: String, diagnosis: String, recommendation: String, timestamp: Long) {
        binding.progressBar.visibility = View.GONE
        binding.tvDiagnosis.text = diagnosis
        binding.tvRecommendation.text = recommendation

        // Show image from local file
        val imageFile = File(imagePath)
        if (imageFile.exists()) {
            Glide.with(this).load(imageFile).into(binding.ivHistory)
        } else {
            // Try as URL if not a valid file
            Glide.with(this).load(imagePath).into(binding.ivHistory)
        }
    }
}