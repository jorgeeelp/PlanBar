package com.losjorges.planbar.ui.screens

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import com.losjorges.planbar.models.Producto

// Categorías unificadas para toda la app
val CATEGORIAS_MENU = listOf("entrantes", "sopas", "carnes", "pescados", "postres", "bebidas", "cafeteria")

// ── Almacén interno de productos (sin SQL) ─────────────────────────────────
object ProductosStore {
    val lista = mutableStateListOf<Producto>().also { l ->
        var id = 1
        fun p(nombre: String, precio: Double, cat: String, obs: String = "") =
            Producto(id++, nombre, precio, cat, obs, "default.jpg")

        l.addAll(listOf(
            // ENTRANTES
            p("Croquetas caseras",       8.50, "entrantes", "Jamón ibérico"),
            p("Tabla de ibéricos",       14.00,"entrantes", "Jamón, chorizo y salchichón"),
            p("Ensalada mixta",           6.50,"entrantes"),
            p("Patatas bravas",           5.50,"entrantes", "Con salsa alioli y brava"),
            // SOPAS
            p("Gazpacho andaluz",         5.00,"sopas",     "Temporada verano"),
            p("Sopa castellana",          6.00,"sopas",     "Con huevo y pan"),
            p("Crema de calabaza",        5.50,"sopas"),
            // CARNES
            p("Chuletón de ternera",     22.00,"carnes",    "400g al punto"),
            p("Secreto ibérico",         16.00,"carnes",    "Con guarnición"),
            p("Pollo al ajillo",         12.00,"carnes"),
            p("Solomillo a la pimienta", 19.00,"carnes",    "Con salsa de pimienta verde"),
            // PESCADOS
            p("Merluza a la romana",     14.00,"pescados"),
            p("Pulpo a la gallega",      16.50,"pescados",  "Con pimentón y aceite"),
            p("Lubina al horno",         18.00,"pescados",  "Con patatas panaderas"),
            p("Gambas al ajillo",        12.00,"pescados"),
            // POSTRES
            p("Tarta de la abuela",       4.50,"postres"),
            p("Flan casero",              3.50,"postres",   "Con nata"),
            p("Coulant de chocolate",     5.00,"postres",   "Con helado de vainilla"),
            p("Fruta del tiempo",         3.00,"postres"),
            // BEBIDAS
            p("Agua mineral 50cl",        1.50,"bebidas"),
            p("Refresco (lata)",          2.00,"bebidas",   "Coca-Cola, Fanta, Aquarius"),
            p("Cerveza",                  2.50,"bebidas",   "Caña o botellín"),
            p("Vino tinto (copa)",        2.80,"bebidas",   "Rioja"),
            p("Vino blanco (copa)",       2.80,"bebidas",   "Albariño"),
            p("Zumo natural",             3.00,"bebidas",   "Naranja o tomate"),
            // CAFETERÍA
            p("Café solo",               1.50,"cafeteria"),
            p("Café con leche",          1.80,"cafeteria"),
            p("Cortado",                 1.60,"cafeteria"),
            p("Infusión",                1.80,"cafeteria"),
            p("Tostada con aceite",      2.50,"cafeteria"),
            p("Croissant",               1.80,"cafeteria"),
        ))
    }
    private var nextId = 100

    fun insertar(nombre: String, precio: Double, categoria: String, obs: String, foto: String): Producto {
        val prod = Producto(nextId++, nombre, precio, categoria, obs, foto)
        lista.add(prod)
        return prod
    }

    fun actualizar(id: Int, nombre: String, precio: Double, categoria: String, obs: String, foto: String) {
        val idx = lista.indexOfFirst { it.id_producto == id }
        if (idx >= 0) lista[idx] = Producto(id, nombre, precio, categoria, obs, foto)
    }

    fun eliminar(id: Int) {
        lista.removeAll { it.id_producto == id }
    }
}

// ── Almacén de pedidos confirmados por mesa ────────────────────────────────
// Clave: idMesa  →  Valor: mapa mutable de id_producto -> LineaPedido
object PedidosStore {
    // mutableStateMapOf para que Compose recomponga al cambiar
    val pedidos = mutableStateMapOf<Int, MutableMap<Int, LineaPedido>>()

    /** Devuelve el pedido activo de una mesa (puede estar vacío) */
    fun getPedido(idMesa: Int): MutableMap<Int, LineaPedido> {
        return pedidos.getOrPut(idMesa) { mutableMapOf() }
    }

    /** Indica si una mesa tiene pedido activo con al menos un artículo */
    fun tienePedido(idMesa: Int): Boolean {
        return pedidos[idMesa]?.isNotEmpty() == true
    }

    /** Confirma el carrito actual añadiéndolo al pedido guardado de la mesa */
    fun confirmarPedido(idMesa: Int, carrito: Map<Int, LineaPedido>) {
        val pedidoActual = getPedido(idMesa)
        carrito.forEach { (idProd, linea) ->
            val existente = pedidoActual[idProd]
            if (existente != null) {
                pedidoActual[idProd] = existente.copy(cantidad = existente.cantidad + linea.cantidad)
            } else {
                pedidoActual[idProd] = linea.copy()
            }
        }
        // Forzar recomposición reemplazando el entry
        pedidos[idMesa] = pedidoActual
    }

    /** Elimina el pedido de una mesa (cuenta pagada) */
    fun liquidarMesa(idMesa: Int) {
        pedidos.remove(idMesa)
    }
}

// LineaPedido se define aquí para que sea compartido entre archivos
data class LineaPedido(val producto: Producto, var cantidad: Int)
