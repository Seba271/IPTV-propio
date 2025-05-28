package com.example.iptvcpruebadesdecero.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.iptvcpruebadesdecero.databinding.ItemCanalBinding
import com.example.iptvcpruebadesdecero.model.Canal

class CanalAdapter(
    private val onCanalClick: (String) -> Unit
) : ListAdapter<Canal, CanalAdapter.CanalViewHolder>(CanalDiffCallback()) {

    inner class CanalViewHolder(private val binding: ItemCanalBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(canal: Canal) {
            binding.tvCanalNombre.text = canal.nombre
            
            // Cargar logo del canal si existe
            canal.logo?.let { logoUrl ->
                Glide.with(binding.root)
                    .load(logoUrl)
                    .into(binding.ivCanalLogo)
            }

            binding.root.setOnClickListener {
                onCanalClick(canal.url)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CanalViewHolder {
        val binding = ItemCanalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CanalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CanalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class CanalDiffCallback : DiffUtil.ItemCallback<Canal>() {
    override fun areItemsTheSame(oldItem: Canal, newItem: Canal): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Canal, newItem: Canal): Boolean {
        return oldItem == newItem
    }
} 