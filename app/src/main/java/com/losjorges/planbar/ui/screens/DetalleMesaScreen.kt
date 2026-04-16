package com.losjorges.planbar.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.gson.Gson
import com.losjorges.planbar.models.LineaPedidoApi
import com.losjorges.planbar.models.Producto
import com.losjorges.planbar.network.RetrofitClient
import com.losjorges.planbar.models.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleMesaScreen(idMesa: Int, numeroMesa: Int, navController: NavHostController) {
    val context = LocalContext.current

    var listaProductos by remember { mutableStateOf<List<Producto>>(ProductosStore.lista.toList()) }
    val carrito = remember { mutableStateMapOf<Int, LineaPedido>() }
    val pedidoConfirmado = remember { PedidosStore.getPedido(idMesa) }
    var isLoading by remember { mutableStateOf(true) }

    var pestañaActual by remember { mutableIntStateOf(0) }
    var tabCategoriaSeleccionado by remember { mutableIntStateOf(0) }
    var mostrarDialogoPago by remember { mutableStateOf(false) }

    val todasCategorias = listOf("todos") + CATEGORIAS_MENU

    // Cargar pedido activo desde la BD al abrir la mesa
    LaunchedEffect(Unit) {
        listaProductos = ProductosStore.lista.toList()
        RetrofitClient.instance.getPedidoMesa(idMesa).enqueue(object : Callback<List<LineaPedidoApi>> {
            override fun onResponse(call: Call<List<LineaPedidoApi>>, response: Response<List<LineaPedidoApi>>) {
                if (response.isSuccessful) {
                    val lineas = response.body() ?: emptyList()
                    PedidosStore.pedidos.remove(idMesa)
                    if (lineas.isNotEmpty()) {
                        val pedido = PedidosStore.getPedido(idMesa)
                        lineas.forEach { linea ->
                            val producto = Producto(
                                id_producto           = linea.producto_id,
                                nombre_producto       = linea.nombre_producto,
                                precio_producto       = linea.precio_producto,
                                categoria_producto    = linea.categoria_producto,
                                observaciones_producto = linea.observaciones_producto,
                                foto_producto         = "default.jpg"
                            )
                            pedido[linea.producto_id] = LineaPedido(producto, linea.cantidad)
                        }
                        PedidosStore.pedidos[idMesa] = pedido
                        PedidosStore.mesasConPedido.add(idMesa)
                        pestañaActual = 1
                    }
                }
                isLoading = false
            }
            override fun onFailure(call: Call<List<LineaPedidoApi>>, t: Throwable) {
                isLoading = false
            }
        })
    }

    val productosFiltrados = if (tabCategoriaSeleccionado == 0) listaProductos
    else listaProductos.filter { it.categoria_producto == todasCategorias[tabCategoriaSeleccionado] }

    // Totales del carrito nuevo (lo que se está añadiendo ahora)
    val totalCarritoProductos = carrito.values.sumOf { it.cantidad }
    val totalCarritoPrecio = carrito.values.sumOf { it.producto.precio_producto * it.cantidad }

    // Totales del pedido completo (confirmado + carrito actual)
    val totalPedidoProductos = pedidoConfirmado.values.sumOf { it.cantidad } + totalCarritoProductos
    val totalPedidoPrecio = pedidoConfirmado.values.sumOf { it.producto.precio_producto * it.cantidad } + totalCarritoPrecio

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("MESA $numeroMesa", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        if (totalPedidoProductos > 0) {
                            Text(
                                "$totalPedidoProductos artículos · ${"%.2f".format(totalPedidoPrecio)} €",
                                fontSize = 12.sp,
                                color = if (PedidosStore.tienePedido(idMesa)) Color(0xFFF57C00) else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Botón cuenta pagada (solo si hay pedido confirmado)
                    if (PedidosStore.tienePedido(idMesa)) {
                        TextButton(
                            onClick = { mostrarDialogoPago = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF2E7D32))
                        ) {
                            Text("💳 PAGAR", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }
                    }
                }
            )
        },
        bottomBar = {
            // Bottom bar solo si hay algo en el carrito nuevo para confirmar
            if (totalCarritoProductos > 0) {
                Surface(shadowElevation = 8.dp) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    if (PedidosStore.tienePedido(idMesa)) "AÑADIR AL PEDIDO" else "TOTAL PEDIDO",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${"%.2f".format(totalCarritoPrecio)} €",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                            Button(
                                onClick = {
                                    // Confirmar: guarda en PedidosStore y vuelve al mapa
                                    PedidosStore.confirmarPedido(idMesa, carrito.toMap())
                                    val lineasJson = Gson().toJson(
                                        PedidosStore.getPedido(idMesa).values.map { l ->
                                            mapOf(
                                                "producto_id"            to l.producto.id_producto,
                                                "nombre_producto"        to l.producto.nombre_producto,
                                                "precio_producto"        to l.producto.precio_producto,
                                                "categoria_producto"     to l.producto.categoria_producto,
                                                "observaciones_producto" to l.producto.observaciones_producto,
                                                "cantidad"               to l.cantidad
                                            )
                                        }
                                    )
                                    RetrofitClient.instance.confirmarPedido(idMesa, lineasJson)
                                        .enqueue(object : Callback<LoginResponse> {
                                            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {}
                                            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                                                Toast.makeText(context, "Error al guardar pedido", Toast.LENGTH_SHORT).show()
                                            }
                                        })
                                    carrito.clear()
                                    Toast.makeText(
                                        context,
                                        "✓ Pedido confirmado para Mesa $numeroMesa",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.popBackStack()
                                },
                                modifier = Modifier.height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                            ) {
                                Text(
                                    if (PedidosStore.tienePedido(idMesa)) "✓ AÑADIR AL PEDIDO" else "✓ CONFIRMAR PEDIDO",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // ── Pestañas: Carta | Pedido ──────────────────────────────────
            TabRow(selectedTabIndex = pestañaActual) {
                Tab(
                    selected = pestañaActual == 0,
                    onClick = { pestañaActual = 0 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🍽 CARTA", fontWeight = if (pestañaActual == 0) FontWeight.ExtraBold else FontWeight.Normal)
                            if (totalCarritoProductos > 0) {
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text("+$totalCarritoProductos", color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                )
                Tab(
                    selected = pestañaActual == 1,
                    onClick = { pestañaActual = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🧾 PEDIDO", fontWeight = if (pestañaActual == 1) FontWeight.ExtraBold else FontWeight.Normal)
                            if (PedidosStore.tienePedido(idMesa) || totalCarritoProductos > 0) {
                                Badge(containerColor = Color(0xFFF57C00)) {
                                    Text("$totalPedidoProductos", color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                )
            }

            when (pestañaActual) {
                // ── CARTA ─────────────────────────────────────────────────
                0 -> {
                    ScrollableTabRow(
                        selectedTabIndex = tabCategoriaSeleccionado,
                        edgePadding = 0.dp
                    ) {
                        for ((index, cat) in todasCategorias.withIndex()) {
                            Tab(
                                selected = tabCategoriaSeleccionado == index,
                                onClick = { tabCategoriaSeleccionado = index },
                                text = {
                                    Text(
                                        text = cat.replaceFirstChar { it.uppercase() },
                                        fontSize = 13.sp,
                                        fontWeight = if (tabCategoriaSeleccionado == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }

                    if (productosFiltrados.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No hay productos en esta categoría", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(productosFiltrados) { prod: Producto ->
                                val cantidadEnCarrito = carrito[prod.id_producto]?.cantidad ?: 0
                                ProductoCartaItem(
                                    producto = prod,
                                    cantidad = cantidadEnCarrito,
                                    onAdd = {
                                        val actual = carrito[prod.id_producto]
                                        if (actual != null) {
                                            carrito[prod.id_producto] = actual.copy(cantidad = actual.cantidad + 1)
                                        } else {
                                            carrito[prod.id_producto] = LineaPedido(prod, 1)
                                        }
                                    },
                                    onRemove = {
                                        val actual = carrito[prod.id_producto]
                                        if (actual != null) {
                                            if (actual.cantidad > 1) {
                                                carrito[prod.id_producto] = actual.copy(cantidad = actual.cantidad - 1)
                                            } else {
                                                carrito.remove(prod.id_producto)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // ── PEDIDO CONFIRMADO ─────────────────────────────────────
                1 -> {
                    val todasLasLineas: List<LineaPedido> = buildList {
                        // Pedido ya confirmado
                        addAll(pedidoConfirmado.values.toList())
                        // Más lo que hay en carrito nuevo (si lo hay)
                        carrito.values.forEach { linea ->
                            val idx = indexOfFirst { it.producto.id_producto == linea.producto.id_producto }
                            if (idx >= 0) {
                                val existente = get(idx)
                                set(idx, existente.copy(cantidad = existente.cantidad + linea.cantidad))
                            } else {
                                add(linea)
                            }
                        }
                    }

                    if (todasLasLineas.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🛒", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("El pedido está vacío", color = Color.Gray, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Añade artículos desde la Carta", color = Color.Gray, fontSize = 13.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Pedido · Mesa $numeroMesa", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                    if (PedidosStore.tienePedido(idMesa)) {
                                        Surface(
                                            color = Color(0xFFFFF3E0),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                "EN CURSO",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                color = Color(0xFFE65100),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            items(todasLasLineas) { linea ->
                                // En la vista del pedido confirmado solo mostramos, no editamos
                                PedidoLineaItem(linea = linea)
                            }

                            item {
                                Spacer(modifier = Modifier.height(4.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(8.dp))

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Artículos", color = Color.Gray, fontSize = 13.sp)
                                            Text("$totalPedidoProductos", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("TOTAL", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                                            Text(
                                                "${"%.2f".format(totalPedidoPrecio)} €",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 17.sp,
                                                color = Color(0xFF2E7D32)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Botón añadir más cosas
                                OutlinedButton(
                                    onClick = { pestañaActual = 0 },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Añadir más artículos", fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Botón cuenta pagada
                                Button(
                                    onClick = { mostrarDialogoPago = true },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                                ) {
                                    Text("💳 CUENTA PAGADA", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                                }

                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Diálogo confirmación pago ─────────────────────────────────────────
    if (mostrarDialogoPago) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPago = false },
            title = { Text("Confirmar pago", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("¿La mesa $numeroMesa ha pagado la cuenta?")
                    Text(
                        "Total: ${"%.2f".format(totalPedidoPrecio)} €",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color(0xFF2E7D32)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        RetrofitClient.instance.liquidarMesa(idMesa)
                            .enqueue(object : Callback<LoginResponse> {
                                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {}
                                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                                    Toast.makeText(context, "Error al liquidar mesa", Toast.LENGTH_SHORT).show()
                                }
                            })
                        PedidosStore.liquidarMesa(idMesa)
                        carrito.clear()
                        mostrarDialogoPago = false
                        Toast.makeText(context, "✓ Mesa $numeroMesa — cuenta saldada", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) { Text("✓ Confirmar pago", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoPago = false }) { Text("Cancelar") }
            }
        )
    }
}

// ── Línea de pedido confirmado (solo lectura) ─────────────────────────────
@Composable
fun PedidoLineaItem(linea: LineaPedido) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cantidad badge
            Surface(
                color = Color(0xFF2E7D32),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "${linea.cantidad}",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(linea.producto.nombre_producto, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    "${"%.2f".format(linea.producto.precio_producto)} € / ud",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            Text(
                "${"%.2f".format(linea.producto.precio_producto * linea.cantidad)} €",
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )
        }
    }
}

// ── Ítem de carta ─────────────────────────────────────────────────────────
@Composable
fun ProductoCartaItem(
    producto: Producto,
    cantidad: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (cantidad > 0) Color(0xFFF1F8E9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = producto.nombre_producto, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                if (producto.observaciones_producto.isNotEmpty()) {
                    Text(producto.observaciones_producto, fontSize = 12.sp, color = Color.Gray)
                }
                Text(
                    text = "${"%.2f".format(producto.precio_producto)} €",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2E7D32)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (cantidad > 0) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(36.dp).background(Color(0xFFEF9A9A), shape = RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Quitar", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        text = "$cantidad",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        modifier = Modifier.width(28.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                IconButton(
                    onClick = onAdd,
                    modifier = Modifier.size(36.dp).background(Color(0xFF2E7D32), shape = RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ── Ítem de carrito (editable) ────────────────────────────────────────────
@Composable
fun CarritoItem(
    linea: LineaPedido,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(linea.producto.nombre_producto, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    "${"%.2f".format(linea.producto.precio_producto * linea.cantidad)} €",
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = onDecrement,
                    modifier = Modifier.size(32.dp).background(Color(0xFFEF9A9A), shape = RoundedCornerShape(6.dp))
                ) { Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp)) }
                Text(
                    "${linea.cantidad}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    modifier = Modifier.width(24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                IconButton(
                    onClick = onIncrement,
                    modifier = Modifier.size(32.dp).background(Color(0xFF2E7D32), shape = RoundedCornerShape(6.dp))
                ) { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp)) }
            }
        }
    }
}
