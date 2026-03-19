package com.losjorges.planbar.models

data class Mesa(
    val id_mesa: Int,
    val numero_mesa: Int,
    val capacidad_mesa: Int,
    val estado_mesa: String,
    val posX: Float,
    val posY: Float
)
