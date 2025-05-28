package com.example.iptvcpruebadesdecero.model

data class Categoria(
    val nombre: String,
    val canales: MutableList<Canal> = mutableListOf()
) 