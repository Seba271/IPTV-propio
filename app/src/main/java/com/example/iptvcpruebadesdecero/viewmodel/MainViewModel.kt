package com.example.iptvcpruebadesdecero.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iptvcpruebadesdecero.model.Categoria
import com.example.iptvcpruebadesdecero.util.M3UParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {
    private val _categorias = MutableLiveData<List<Categoria>>()
    val categorias: LiveData<List<Categoria>> = _categorias

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun cargarPlaylist(context: Context) {
        viewModelScope.launch {
            try {
                Log.d("MainViewModel", "Iniciando carga de playlist")
                val inputStream = context.assets.open("playlist_matiasprueba_plus.m3u")
                Log.d("MainViewModel", "Archivo playlist encontrado")
                
                val parser = M3UParser()
                val categoriasParseadas = withContext(Dispatchers.IO) {
                    Log.d("MainViewModel", "Iniciando parseo de playlist")
                    val resultado = parser.parse(inputStream)
                    Log.d("MainViewModel", "Playlist parseada. Categorías encontradas: ${resultado.size}")
                    resultado
                }
                
                if (categoriasParseadas.isEmpty()) {
                    Log.w("MainViewModel", "No se encontraron categorías en la playlist")
                    _error.postValue("No se encontraron canales en la playlist")
                } else {
                    Log.d("MainViewModel", "Actualizando LiveData con ${categoriasParseadas.size} categorías")
                    _categorias.postValue(categoriasParseadas)
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error al cargar la playlist: ${e.message}", e)
                _error.postValue("Error al cargar la playlist: ${e.message}")
            }
        }
    }
} 