package com.dicoding.capstone.dermaface.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.capstone.dermaface.data.model.HistoryResponse
import com.dicoding.capstone.dermaface.databinding.ItemHistoryBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private val histories: MutableList<HistoryResponse> = mutableListOf(),
    private val onItemClick: (HistoryResponse) -> Unit,
    private val onDeleteClick: (HistoryResponse) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val history = histories[position]

        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val date = Date(history.timestamp)

        holder.binding.tvDate.text = dateFormat.format(date)
        holder.binding.tvDiagnosis.text = history.diagnosis

        // Load image
        if (history.image_url.isNotEmpty()) {
            val imageFile = File(history.image_url)
            if (imageFile.exists()) {
                Glide.with(holder.itemView.context)
                    .load(imageFile)
                    .into(holder.binding.ivHistoryImage)
            } else {
                // Try as URL if not a valid file
                Glide.with(holder.itemView.context)
                    .load(history.image_url)
                    .into(holder.binding.ivHistoryImage)
            }
        } else {
            Glide.with(holder.itemView.context).clear(holder.binding.ivHistoryImage)
        }

        holder.binding.root.setOnClickListener { onItemClick(history) }
        holder.binding.btnDelete.setOnClickListener { onDeleteClick(history) }
    }

    override fun getItemCount() = histories.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateHistories(newHistories: List<HistoryResponse>) {
        histories.clear()
        histories.addAll(newHistories)
        notifyDataSetChanged()
    }
}