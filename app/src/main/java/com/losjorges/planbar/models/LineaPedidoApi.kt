package com.losjorges.planbar.models

data class LineaPedidoApi(
    val producto_id: Int,
    val nombre_producto: String,
    val precio_producto: Double,
    val categoria_producto: String,
    val observaciones_producto: String,
    val cantidad: Int
)
