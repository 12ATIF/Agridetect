package com.dicoding.capstone.dermaface.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.capstone.dermaface.R
import com.dicoding.capstone.dermaface.adapter.HistoryAdapter
import com.dicoding.capstone.dermaface.repository.LocalHistoryRepository
import com.dicoding.capstone.dermaface.data.model.HistoryResponse
import com.dicoding.capstone.dermaface.viewmodel.LocalHistoryViewModel
import com.dicoding.capstone.dermaface.viewmodel.ViewModelFactory
import com.dicoding.capstone.dermaface.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private val historyViewModel: LocalHistoryViewModel by viewModels {
        ViewModelFactory(
            context = this,
            localHistoryRepository = LocalHistoryRepository(this)
        )
    }
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Log.d("HistoryActivity", "Activity started")
        setupRecyclerView()
        observeViewModel()
        historyViewModel.fetchHistories()

        binding.btnBack.setOnClickListener {
            navigateToMainActivity()
        }
    }

    override fun onBackPressed() {
        if (isTaskRoot) {
            navigateToMainActivity()
        } else {
            super.onBackPressed()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(
            mutableListOf(),
            onItemClick = { history ->
                val intent = Intent(this, DetailHistoryActivity::class.java).apply {
                    putExtra("HISTORY_ID", history.id)
                    putExtra("HISTORY_IMAGE", history.image_url)
                    putExtra("HISTORY_DIAGNOSIS", history.diagnosis)
                    putExtra("HISTORY_RECOMMENDATION", history.recommendation)
                    putExtra("HISTORY_TIMESTAMP", history.timestamp)
                }
                startActivity(intent)
            },
            onDeleteClick = { history ->
                showDeleteConfirmationDialog(history)
            }
        )

        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = historyAdapter

        // Tambahkan dekorasi untuk spasi antar item
        binding.rvHistory.addItemDecoration(
            DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        )
    }

    private fun showDeleteConfirmationDialog(history: HistoryResponse) {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_delete_history)
            .setPositiveButton(R.string.yes) { _, _ ->
                historyViewModel.deleteHistory(history)
            }
            .setNegativeButton(R.string.no, null)
            .create()
            .show()
    }

    private fun observeViewModel() {
        historyViewModel.histories.observe(this) { fetchedHistories ->
            binding.progressBar.visibility = View.GONE
            Log.d("HistoryActivity", "Menerima ${fetchedHistories.size} riwayat")

            if (fetchedHistories.isEmpty()) {
                // Tampilkan pesan "Tidak ada riwayat"
                if (binding.tvNoHistory != null) {
                    binding.tvNoHistory.visibility = View.VISIBLE
                    binding.rvHistory.visibility = View.GONE
                } else {
                    Log.e("HistoryActivity", "tvNoHistory is null")
                }
            } else {
                if (binding.tvNoHistory != null) {
                    binding.tvNoHistory.visibility = View.GONE
                }
                binding.rvHistory.visibility = View.VISIBLE
                historyAdapter.updateHistories(fetchedHistories)
            }
        }

        historyViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        historyViewModel.deletionStatus.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, R.string.history_deleted, Toast.LENGTH_SHORT).show()
            }
            result.onFailure {
                Toast.makeText(this, R.string.history_delete_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }
}