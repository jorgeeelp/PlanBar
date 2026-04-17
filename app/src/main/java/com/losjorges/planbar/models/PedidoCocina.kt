package com.losjorges.planbar.models

data class LineaCocinaApi(
    val producto_id: Int,
    val nombre_producto: String,
    val cantidad: Int
)

data class PedidoCocinaApi(
    val mesa_id: Int,
    val numero_mesa: Int,
    val preparado: Boolean,
    val lineas: List<LineaCocinaApi>
)
