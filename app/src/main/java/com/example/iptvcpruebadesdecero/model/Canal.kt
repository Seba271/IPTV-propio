package com.example.iptvcpruebadesdecero.model

/**
 * Clase de datos que representa un canal de IPTV.
 * Esta clase se utiliza para almacenar y transportar la información de cada canal.
 * 
 * @property id Identificador único del canal
 * @property nombre Nombre o título del canal
 * @property url URL del stream del canal
 * @property logo URL opcional del logo del canal (puede ser null)
 * @property categoria Categoría a la que pertenece el canal
 */
data class Canal(
    val id: String,
    val nombre: String,
    val url: String,
    val logo: String?,
    val categoria: String
) 