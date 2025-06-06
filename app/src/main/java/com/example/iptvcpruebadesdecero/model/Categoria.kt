package com.example.iptvcpruebadesdecero.model

/**
 * Clase de datos que representa una categoría de canales IPTV.
 * Esta clase se utiliza para agrupar canales relacionados en una misma categoría.
 * 
 * @property nombre Nombre de la categoría (ej: "Deportes", "Películas", "Series", etc.)
 * @property canales Lista mutable de canales que pertenecen a esta categoría.
 *                  Se inicializa como una lista vacía por defecto.
 */
data class Categoria(
    val nombre: String,
    val canales: MutableList<Canal> = mutableListOf()
) 