package com.example.iptvcpruebadesdecero

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iptvcpruebadesdecero.adapter.CategoriaAdapter
import com.example.iptvcpruebadesdecero.databinding.ActivityMainBinding
import com.example.iptvcpruebadesdecero.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var categoriaAdapter: CategoriaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setupViewModel()
            setupRecyclerView()
            observeViewModel()
            cargarPlaylist()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error en onCreate: ${e.message}", e)
            Toast.makeText(this, "Error al iniciar la aplicación: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupViewModel() {
        try {
            viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        } catch (e: Exception) {
            Log.e("MainActivity", "Error en setupViewModel: ${e.message}", e)
            throw e
        }
    }

    private fun setupRecyclerView() {
        try {
            categoriaAdapter = CategoriaAdapter(emptyList()) { url ->
                abrirReproductor(url)
            }

            binding.recyclerViewCategorias.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = categoriaAdapter
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error en setupRecyclerView: ${e.message}", e)
            throw e
        }
    }

    private fun observeViewModel() {
        try {
            viewModel.categorias.observe(this) { categorias ->
                try {
                    if (categorias.isEmpty()) {
                        binding.textViewMensaje.visibility = View.VISIBLE
                        binding.textViewMensaje.text = "No se encontraron canales"
                    } else {
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