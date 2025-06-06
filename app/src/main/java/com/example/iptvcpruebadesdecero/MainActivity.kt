package com.example.iptvcpruebadesdecero

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iptvcpruebadesdecero.adapter.CategoriaAdapter
import com.example.iptvcpruebadesdecero.databinding.ActivityMainBinding
import com.example.iptvcpruebadesdecero.viewmodel.MainViewModel
import com.google.android.material.textfield.TextInputEditText

/**
 * Actividad principal de la aplicación que muestra la lista de categorías y canales IPTV.
 * Implementa la interfaz de usuario principal y maneja la interacción con el ViewModel.
 */
class MainActivity : AppCompatActivity() {
    // Binding para acceder a las vistas de manera segura
    private lateinit var binding: ActivityMainBinding
    // ViewModel para manejar la lógica de negocio
    private lateinit var viewModel: MainViewModel
    // Adaptador para mostrar las categorías y sus canales
    private lateinit var categoriaAdapter: CategoriaAdapter

    /**
     * Método de inicialización de la actividad.
     * Configura la interfaz de usuario y carga los datos iniciales.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            // Inicialización del ViewBinding
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Configuración inicial de la actividad
            setupViewModel()
            setupRecyclerView()
            setupSearch()
            observeViewModel()
            cargarPlaylist()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error en onCreate: ${e.message}", e)
            Toast.makeText(this, "Error al iniciar la aplicación: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Configura el ViewModel de la actividad.
     * Inicializa el ViewModel usando ViewModelProvider.
     */
    private fun setupViewModel() {
        try {
            viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        } catch (e: Exception) {
            Log.e("MainActivity", "Error en setupViewModel: ${e.message}", e)
            throw e
        }
    }

    /**
     * Configura el RecyclerView y su adaptador.
     * Inicializa el adaptador con una lista vacía y configura el layout manager.
     */
    private fun setupRecyclerView() {
        try {
            // Creación del adaptador con callback para manejar clics en canales
            categoriaAdapter = CategoriaAdapter(emptyList()) { url ->
                abrirReproductor(url)
            }

            // Configuración del RecyclerView
            binding.recyclerViewCategorias.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = categoriaAdapter
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error en setupRecyclerView: ${e.message}", e)
            throw e
        }
    }

    /**
     * Configura la búsqueda en la actividad.
     */
    private fun setupSearch() {
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.searchEditText.text.toString()
                viewModel.buscarCanales(query)
                true
            } else {
                false
            }
        }

        // Búsqueda en tiempo real mientras el usuario escribe
        binding.searchEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.buscarCanales(s?.toString() ?: "")
            }
        })
    }

    /**
     * Configura los observadores del ViewModel.
     * Observa cambios en las categorías y errores.
     */
    private fun observeViewModel() {
        try {
            // Observador para las categorías
            viewModel.categorias.observe(this) { categorias ->
                try {
                    if (categorias.isEmpty()) {
                        // Mostrar mensaje si no hay categorías
                        binding.textViewMensaje.visibility = View.VISIBLE
                        binding.textViewMensaje.text = "No se encontraron canales"
                    } else {
                        // Actualizar el adaptador con las nuevas categorías
                        binding.textViewMensaje.visibility = View.GONE
                        categoriaAdapter = CategoriaAdapter(categorias) { url ->
                            abrirReproductor(url)
                        }
                        binding.recyclerViewCategorias.adapter = categoriaAdapter
                    }
                    binding.progressBar.visibility = View.GONE
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error al actualizar categorías: ${e.message}", e)
                    binding.textViewMensaje.visibility = View.VISIBLE
                    binding.textViewMensaje.text = "Error al mostrar categorías: ${e.message}"
                }
            }

            // Observador para los errores
            viewModel.error.observe(this) { error ->
                binding.progressBar.visibility = View.GONE
                binding.textViewMensaje.visibility = View.VISIBLE
                binding.textViewMensaje.text = "Error: $error"
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error en observeViewModel: ${e.message}", e)
            throw e
        }
    }

    /**
     * Abre la actividad del reproductor con la URL del canal seleccionado.
     * @param url URL del stream del canal a reproducir
     */
    private fun abrirReproductor(url: String) {
        try {
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra("url", url)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al abrir reproductor: ${e.message}", e)
            Toast.makeText(this, "Error al abrir el reproductor: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Inicia el proceso de carga de la playlist.
     * Muestra el indicador de progreso y oculta mensajes anteriores.
     */
    private fun cargarPlaylist() {
        try {
            binding.progressBar.visibility = View.VISIBLE
            binding.textViewMensaje.visibility = View.GONE
            viewModel.cargarPlaylist(this)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al cargar playlist: ${e.message}", e)
            binding.textViewMensaje.visibility = View.VISIBLE
            binding.textViewMensaje.text = "Error al cargar la playlist: ${e.message}"
            binding.progressBar.visibility = View.GONE
        }
    }
}