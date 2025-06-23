package com.example.iptvcpruebadesdecero.adapter

// Importaciones necesarias para el funcionamiento del adaptador
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.iptvcpruebadesdecero.R
import com.example.iptvcpruebadesdecero.databinding.ItemCanalBinding
import com.example.iptvcpruebadesdecero.model.Canal

/**
 * Adaptador para mostrar una lista de canales en un RecyclerView.
 * Utiliza DiffUtil para optimizar las actualizaciones de la lista.
 * 
 * @param onCanalClick Función lambda que se ejecuta cuando se hace clic en un canal
 */
class CanalAdapter(
    private val onCanalClick: (List<Canal>, Int) -> Unit
) : ListAdapter<Canal, CanalAdapter.CanalViewHolder>(CanalDiffCallback()) {

    /**
     * ViewHolder que maneja la vista de cada canal.
     */
    inner class CanalViewHolder(private val binding: ItemCanalBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        init {
            itemView.setOnClickListener {
                onCanalClick(currentList, adapterPosition)
            }
        }

        /**
         * Vincula los datos del canal con las vistas del ViewHolder.
         * @param canal El canal a mostrar
         */
        fun bind(canal: Canal) {
            // Establece el nombre del canal
            binding.tvCanalNombre.text = canal.nombre
            // Carga el logo del canal usando Glide
            Glide.with(itemView.context)
                .load(canal.logo)
                .placeholder(R.drawable.placeholder_channel)
                .into(binding.ivCanalLogo)
        }
    }

    /**
     * Crea nuevas instancias de ViewHolder.
     * @param parent El ViewGroup padre
     * @param viewType El tipo de vista
     * @return Una nueva instancia de CanalViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CanalViewHolder {
        val binding = ItemCanalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CanalViewHolder(binding)
    }

    /**
     * Vincula los datos del canal con el ViewHolder en la posición especificada.
     * @param holder El ViewHolder a vincular
     * @param position La posición del elemento en la lista
     */
    override fun onBindViewHolder(holder: CanalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

/**
 * DiffUtil.ItemCallback para optimizar las actualizaciones del RecyclerView.
 * Compara canales para determinar si deben ser redibujados.
 */
class CanalDiffCallback : DiffUtil.ItemCallback<Canal>() {
    override fun areItemsTheSame(oldItem: Canal, newItem: Canal): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Canal, newItem: Canal): Boolean {
        return oldItem == newItem
    }
} 