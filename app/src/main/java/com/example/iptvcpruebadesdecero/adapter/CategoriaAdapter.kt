package com.example.iptvcpruebadesdecero.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iptvcpruebadesdecero.databinding.ItemCategoriaBinding
import com.example.iptvcpruebadesdecero.model.Categoria

class CategoriaAdapter(
    private val categorias: List<Categoria>,
    private val onCanalClick: (String) -> Unit
) : RecyclerView.Adapter<CategoriaAdapter.CategoriaViewHolder>() {

    inner class CategoriaViewHolder(private val binding: ItemCategoriaBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        private val canalAdapter = CanalAdapter { url ->
            onCanalClick(url)
        }

        init {
            binding.recyclerViewCanales.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = canalAdapter
            }
        }

        fun bind(categoria: Categoria) {
            binding.tvCategoriaTitulo.text = categoria.nombre
            canalAdapter.submitList(categoria.canales)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        val binding = ItemCategoriaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoriaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        holder.bind(categorias[position])
    }

    override fun getItemCount() = categorias.size
} 