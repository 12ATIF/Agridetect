package com.dicoding.capstone.dermaface.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.capstone.dermaface.R
import com.dicoding.capstone.dermaface.data.model.HistoryResponse
import com.dicoding.capstone.dermaface.databinding.ItemArticleBinding
import com.dicoding.capstone.dermaface.ui.DetailHistoryActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanHistoryAdapter(
    private val context: Context,
    private var histories: List<HistoryResponse>
) : RecyclerView.Adapter<ScanHistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val history = histories[position]

        // Set diagnosa sebagai judul
        holder.binding.tvArticleTitle.text = history.diagnosis ?: context.getString(R.string.no_title)

        // Format tanggal untuk deskripsi
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val date = Date(history.timestamp)
        holder.binding.tvArticleDescription.text = dateFormat.format(date)

        // Memuat gambar
        if (history.image_url.isNotEmpty()) {
            val imageFile = File(history.image_url)
            if (imageFile.exists()) {
                Glide.with(holder.itemView.context)
                    .load(imageFile)
                    .into(holder.binding.ivArticleImage)
            } else {
                // Coba sebagai URL jika bukan file yang valid
                Glide.with(holder.itemView.context)
                    .load(history.image_url)
                    .into(holder.binding.ivArticleImage)
            }
        } else {
            Glide.with(holder.itemView.context).clear(holder.binding.ivArticleImage)
        }

        holder.binding.root.setOnClickListener {
            val intent = Intent(context, DetailHistoryActivity::class.java).apply {
                putExtra("HISTORY_ID", history.id)
                putExtra("HISTORY_IMAGE", history.image_url)
                putExtra("HISTORY_DIAGNOSIS", history.diagnosis)
                putExtra("HISTORY_RECOMMENDATION", history.recommendation)
                putExtra("HISTORY_TIMESTAMP", history.timestamp)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = histories.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateHistories(newHistories: List<HistoryResponse>) {
        this.histories = newHistories
        notifyDataSetChanged()
    }

    class HistoryViewHolder(val binding: ItemArticleBinding) : RecyclerView.ViewHolder(binding.root)
}