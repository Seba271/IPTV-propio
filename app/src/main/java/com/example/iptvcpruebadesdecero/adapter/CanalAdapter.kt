package com.example.iptvcpruebadesdecero.adapter

// Importaciones necesarias para el funcionamiento del adaptador
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.iptvcpruebadesdecero.databinding.ItemCanalBinding
import com.example.iptvcpruebadesdecero.model.Canal

/**
 * Adaptador para mostrar una lista de canales en un RecyclerView.
 * Utiliza ListAdapter para manejar automáticamente las actualizaciones de la lista.
 * @param onCanalClick Función lambda que se ejecuta cuando se hace clic en un canal
 */
class CanalAdapter(
    private val onCanalClick: (String) -> Unit
) : ListAdapter<Canal, CanalAdapter.CanalViewHolder>(CanalDiffCallback()) {

    /**
     * ViewHolder que maneja la vista de cada elemento del RecyclerView.
     * Utiliza ViewBinding para acceder a las vistas de manera segura.
     */
    inner class CanalViewHolder(private val binding: ItemCanalBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        /**
         * Vincula los datos del canal con las vistas del ViewHolder.
         * @param canal El canal a mostrar
         */
        fun bind(canal: Canal) {
            // Establece el nombre del canal
            binding.tvCanalNombre.text = canal.nombre
            
            // Carga el logo del canal si existe usando Glide
            canal.logo?.let { logoUrl ->
                Glide.with(binding.root)
                    .load(logoUrl)
                    .into(binding.ivCanalLogo)
            }

            // Configura el listener de clic que llama a onCanalClick con la URL del canal
            binding.root.setOnClickListener {
                onCanalClick(canal.url)
            }
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
 * Clase que implementa DiffUtil.ItemCallback para manejar las comparaciones entre elementos.
 * Se utiliza para optimizar las actualizaciones de la lista.
 */
class CanalDiffCallback : DiffUtil.ItemCallback<Canal>() {
    /**
     * Compara si dos elementos son el mismo basándose en su ID.
     * @param oldItem El elemento antiguo
     * @param newItem El elemento nuevo
     * @return true si los elementos son el mismo, false en caso contrario
     */
    override fun areItemsTheSame(oldItem: Canal, newItem: Canal): Boolean {
        return oldItem.id == newItem.id
    }

    /**
     * Compara si el contenido de dos elementos es el mismo.
     * @param oldItem El elemento antiguo
     * @param newItem El elemento nuevo
     * @return true si el contenido es el mismo, false en caso contrario
     */
    override fun areContentsTheSame(oldItem: Canal, newItem: Canal): Boolean {
        return oldItem == newItem
    }
} 