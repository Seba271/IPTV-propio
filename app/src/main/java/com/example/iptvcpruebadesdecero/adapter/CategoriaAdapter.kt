package com.example.iptvcpruebadesdecero.adapter

// Importaciones necesarias para el funcionamiento del adaptador
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iptvcpruebadesdecero.databinding.ItemCategoriaBinding
import com.example.iptvcpruebadesdecero.model.Categoria

/**
 * Adaptador para mostrar una lista de categorías en un RecyclerView.
 * Cada categoría contiene su propia lista horizontal de canales.
 * 
 * @param categorias Lista de categorías a mostrar
 * @param onCanalClick Función lambda que se ejecuta cuando se hace clic en un canal
 */
class CategoriaAdapter(
    private val categorias: List<Categoria>,
    private val onCanalClick: (String) -> Unit
) : RecyclerView.Adapter<CategoriaAdapter.CategoriaViewHolder>() {

    /**
     * ViewHolder que maneja la vista de cada categoría.
     * Contiene un RecyclerView horizontal para mostrar los canales de la categoría.
     */
    inner class CategoriaViewHolder(private val binding: ItemCategoriaBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        // Adaptador para la lista horizontal de canales
        private val canalAdapter = CanalAdapter { url ->
            onCanalClick(url)
        }

        // Inicialización del RecyclerView de canales
        init {
            binding.recyclerViewCanales.apply {
                // Configuración del layout horizontal
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = canalAdapter
            }
        }

        /**
         * Vincula los datos de la categoría con las vistas del ViewHolder.
         * @param categoria La categoría a mostrar
         */
        fun bind(categoria: Categoria) {
            // Establece el título de la categoría
            binding.tvCategoriaTitulo.text = categoria.nombre
            // Actualiza la lista de canales en el adaptador
            canalAdapter.submitList(categoria.canales)
        }
    }

    /**
     * Crea nuevas instancias de ViewHolder.
     * @param parent El ViewGroup padre
     * @param viewType El tipo de vista
     * @return Una nueva instancia de CategoriaViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        val binding = ItemCategoriaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoriaViewHolder(binding)
    }

    /**
     * Vincula los datos de la categoría con el ViewHolder en la posición especificada.
     * @param holder El ViewHolder a vincular
     * @param position La posición del elemento en la lista
     */
    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        holder.bind(categorias[position])
    }

    /**
     * Retorna el número total de categorías en la lista.
     * @return El tamaño de la lista de categorías
     */
    override fun getItemCount() = categorias.size
} 