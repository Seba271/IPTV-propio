package com.example.iptvcpruebadesdecero.util

import android.util.Log
import com.example.iptvcpruebadesdecero.model.Canal
import com.example.iptvcpruebadesdecero.model.Categoria
import java.io.BufferedReader
import java.io.InputStreamReader

class M3UParser {
    fun parse(inputStream: java.io.InputStream): List<Categoria> {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val categorias = mutableMapOf<String, Categoria>()
        var currentCanal: Canal? = null
        var lineCount = 0

        try {
            reader.useLines { lines ->
                lines.forEach { line ->
                    lineCount++
                    try {
                        when {
                            line.startsWith("#EXTINF:") -> {
                                // Parsear información del canal
                                val nombre = line.substringAfter(",").trim()
                                val categoria = line.substringAfter("group-title=\"").substringBefore("\"").trim()
                                val id = line.substringAfter("tvg-id=\"").substringBefore("\"").trim()
                                val logo = line.substringAfter("tvg-logo=\"").substringBefore("\"").trim()

                                Log.d("M3UParser", "Parseando canal: $nombre, categoría: $categoria")

                                currentCanal = Canal(
                                    id = id,
                                    nombre = nombre,
                                    url = "", // Se llenará en la siguiente línea
                                    logo = if (logo.isNotEmpty()) logo else null,
                                    categoria = categoria
                                )
                            }
                            line.startsWith("http") -> {
                                // URL del canal
                                currentCanal?.let { canal ->
                                    val canalCompleto = canal.copy(url = line.trim())
                                    val categoria = categorias.getOrPut(canal.categoria) { Categoria(canal.categoria) }
                                    categoria.canales.add(canalCompleto)
                                    Log.d("M3UParser", "Canal agregado: ${canal.nombre} a categoría: ${canal.categoria}")
                                }
                                currentCanal = null
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("M3UParser", "Error al parsear línea $lineCount: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("M3UParser", "Error al leer el archivo: ${e.message}", e)
            throw e
        }

        Log.d("M3UParser", "Parseo completado. Categorías encontradas: ${categorias.size}")
        return categorias.values.toList()
    }
} 