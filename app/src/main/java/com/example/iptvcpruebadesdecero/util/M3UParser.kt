package com.example.iptvcpruebadesdecero.util

import android.util.Log
import com.example.iptvcpruebadesdecero.model.Canal
import com.example.iptvcpruebadesdecero.model.Categoria
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Clase encargada de parsear archivos M3U (formato de lista de reproducción IPTV).
 * Convierte el contenido del archivo M3U en una lista de categorías con sus respectivos canales.
 */
class M3UParser {
    /**
     * Parsea un archivo M3U desde un InputStream y lo convierte en una lista de categorías.
     * 
     * @param inputStream El stream de entrada que contiene el archivo M3U
     * @return Lista de categorías con sus canales correspondientes
     * @throws Exception Si ocurre un error al leer o parsear el archivo
     */
    fun parse(inputStream: java.io.InputStream): List<Categoria> {
        // Inicialización de variables para el proceso de parseo
        val reader = BufferedReader(InputStreamReader(inputStream))
        val categorias = mutableMapOf<String, Categoria>() // Mapa para almacenar categorías únicas
        var currentCanal: Canal? = null // Variable temporal para almacenar el canal que se está procesando
        var lineCount = 0 // Contador de líneas para el manejo de errores

        try {
            reader.useLines { lines ->
                lines.forEach { line ->
                    lineCount++
                    try {
                        when {
                            // Línea que contiene la información del canal (EXTINF)
                            line.startsWith("#EXTINF:") -> {
                                // Extracción de información del canal desde la línea EXTINF
                                val nombre = line.substringAfter(",").trim()
                                val categoria = line.substringAfter("group-title=\"").substringBefore("\"").trim()
                                val id = line.substringAfter("tvg-id=\"").substringBefore("\"").trim()
                                val logo = line.substringAfter("tvg-logo=\"").substringBefore("\"").trim()

                                Log.d("M3UParser", "Parseando canal: $nombre, categoría: $categoria")

                                // Creación del objeto Canal con la información extraída
                                currentCanal = Canal(
                                    id = id,
                                    nombre = nombre,
                                    url = "", // La URL se agregará en la siguiente línea
                                    logo = if (logo.isNotEmpty()) logo else null,
                                    categoria = categoria
                                )
                            }
                            // Línea que contiene la URL del stream del canal
                            line.startsWith("http") -> {
                                currentCanal?.let { canal ->
                                    // Creación del canal completo con su URL
                                    val canalCompleto = canal.copy(url = line.trim())
                                    // Obtención o creación de la categoría correspondiente
                                    val categoria = categorias.getOrPut(canal.categoria) { Categoria(canal.categoria) }
                                    // Agregar el canal a su categoría
                                    categoria.canales.add(canalCompleto)
                                    Log.d("M3UParser", "Canal agregado: ${canal.nombre} a categoría: ${canal.categoria}")
                                }
                                currentCanal = null // Resetear el canal actual
                            }
                        }
                    } catch (e: Exception) {
                        // Manejo de errores para cada línea individual
                        Log.e("M3UParser", "Error al parsear línea $lineCount: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            // Manejo de errores generales del proceso de lectura
            Log.e("M3UParser", "Error al leer el archivo: ${e.message}", e)
            throw e
        }

        // Log final con el resultado del proceso
        Log.d("M3UParser", "Parseo completado. Categorías encontradas: ${categorias.size}")
        return categorias.values.toList()
    }
} 