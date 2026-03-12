package com.losjorges.planbar.models

data class Reserva(
    val id_reserva: Int,
    val nombre_cliente: String,
    val numero_mesa: Int,
    val fecha_reserva: String,
    val hora_reserva: String,
    val num_personas: Int
)
