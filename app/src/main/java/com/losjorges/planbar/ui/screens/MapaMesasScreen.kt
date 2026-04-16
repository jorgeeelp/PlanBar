package com.losjorges.planbar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.losjorges.planbar.models.Mesa
import com.losjorges.planbar.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.roundToInt

// ── Modelo de elemento decorativo ──────────────────────────────────────────
data class ElementoDecorativo(
    val id: Int,
    var nombre: String,
    var posX: Float,
    var posY: Float,
    var anchoDP: Float,
    var altoDP: Float,
    var colorHex: Long
)

// ── Colores predefinidos ────────────────────────────────────────────────────
val COLORES_ELEMENTO = listOf(
    Pair("Gris oscuro",   0xFF424242L),
    Pair("Marrón",        0xFF6D4C41L),
    Pair("Madera",        0xFF8D6E63L),
    Pair("Azul marino",   0xFF1565C0L),
    Pair("Verde",         0xFF2E7D32L),
    Pair("Rojo ladrillo", 0xFFC62828L),
    Pair("Negro",         0xFF212121L),
    Pair("Beige",         0xFFD7CCC8L),
    Pair("Amarillo",      0xFFF9A825L),
    Pair("Cyan",          0xFF00838FL),
)

// ── Almacén en memoria ─────────────────────────────────────────────────────
object ElementosStore {
    val lista = mutableStateListOf<ElementoDecorativo>()
    private var nextId = 1

    fun agregar(elem: ElementoDecorativo) {
        lista.add(elem.copy(id = nextId++))
    }

    fun eliminar(id: Int) {
        lista.removeAll { it.id == id }
    }
}

// ── Pantalla principal ─────────────────────────────────────────────────────
@Composable
fun MapaMesasScreen(isAdmin: Boolean, onMesaClick: (Mesa) -> Unit) {
    var listaMesas by remember { mutableStateOf(emptyList<Mesa>()) }
    var mostrarDialogoNuevoElem by remember { mutableStateOf(false) }
    var elemSeleccionadoId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        RetrofitClient.instance.getMesas().enqueue(object : Callback<List<Mesa>> {
            override fun onResponse(call: Call<List<Mesa>>, response: Response<List<Mesa>>) {
                if (response.isSuccessful) listaMesas = response.body() ?: emptyList()
            }
            override fun onFailure(call: Call<List<Mesa>>, t: Throwable) {}
        })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Lienzo del mapa
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF0F0F0))) {

            // Elementos decorativos (debajo de las mesas)
            ElementosStore.lista.forEach { elem ->
                ElementoDecorativoComposable(
                    elem = elem,
                    isAdmin = isAdmin,
                    seleccionado = elemSeleccionadoId == elem.id,
                    onTap = {
                        if (isAdmin)
                            elemSeleccionadoId = if (elemSeleccionadoId == elem.id) null else elem.id
                    },
                    onPosicionCambiada = { id, x, y ->
                        val idx = ElementosStore.lista.indexOfFirst { it.id == id }
                        if (idx >= 0) {
                            val e = ElementosStore.lista[idx]
                            ElementosStore.lista[idx] = e.copy(posX = x, posY = y)
                        }
                    }
                )
            }

            // Mesas
            listaMesas.forEach { mesa ->
                MapaMesas(
                    mesa = mesa,
                    isAdmin = isAdmin,
                    onMesaClick = { onMesaClick(mesa) },
                    onPosicionCambiada = { id, x, y ->
                        RetrofitClient.instance.updateMesaPosicion(id, x, y)
                            .enqueue(object : Callback<com.losjorges.planbar.models.LoginResponse> {
                                override fun onResponse(call: Call<com.losjorges.planbar.models.LoginResponse>, response: Response<com.losjorges.planbar.models.LoginResponse>) {}
                                override fun onFailure(call: Call<com.losjorges.planbar.models.LoginResponse>, t: Throwable) {}
                            })
                    }
                )
            }
        }

        // Botones flotantes (solo admin)
        if (isAdmin) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                if (elemSeleccionadoId != null) {
                    FloatingActionButton(
                        onClick = {
                            ElementosStore.eliminar(elemSeleccionadoId!!)
                            elemSeleccionadoId = null
                        },
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar elemento")
                    }
                }

                ExtendedFloatingActionButton(
                    onClick = { mostrarDialogoNuevoElem = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Añadir elemento") },
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }

    if (mostrarDialogoNuevoElem) {
        DialogoNuevoElemento(
            onDismiss = { mostrarDialogoNuevoElem = false },
            onConfirm = { nombre, ancho, alto, colorHex ->
                ElementosStore.agregar(
                    ElementoDecorativo(
                        id = 0,
                        nombre = nombre,
                        posX = 100f,
                        posY = 200f,
                        anchoDP = ancho,
                        altoDP = alto,
                        colorHex = colorHex
                    )
                )
                mostrarDialogoNuevoElem = false
            }
        )
    }
}

// ── Diálogo de creación ────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoNuevoElemento(
    onDismiss: () -> Unit,
    onConfirm: (nombre: String, ancho: Float, alto: Float, colorHex: Long) -> Unit
) {
    var nombre by remember { mutableStateOf("Pared") }
    var anchoStr by remember { mutableStateOf("120") }
    var altoStr by remember { mutableStateOf("20") }
    var colorSeleccionado by remember { mutableStateOf(COLORES_ELEMENTO[0]) }

    val tiposRapidos = listOf(
        Triple("Pared H",  200f, 18f),
        Triple("Pared V",   18f, 150f),
        Triple("Barra",    220f, 40f),
        Triple("Columna",   30f, 30f),
        Triple("Puerta",    50f, 10f),
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir Elemento al Plano", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Presets rápidos
                Text("Preset rápido:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tiposRapidos.forEach { (tip, w, h) ->
                        FilterChip(
                            selected = nombre == tip,
                            onClick = {
                                nombre = tip
                                anchoStr = w.toInt().toString()
                                altoStr = h.toInt().toString()
                            },
                            label = { Text(tip, fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider()

                // Nombre
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Etiqueta") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Tamaño
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = anchoStr,
                        onValueChange = { if (it.all { c -> c.isDigit() }) anchoStr = it },
                        label = { Text("Ancho (dp)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = altoStr,
                        onValueChange = { if (it.all { c -> c.isDigit() }) altoStr = it },
                        label = { Text("Alto (dp)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                val w = anchoStr.toIntOrNull() ?: 0
                val h = altoStr.toIntOrNull() ?: 0
                Text("Tamaño: ${w} × ${h} dp", fontSize = 12.sp, color = Color.Gray)

                // Color
                Text("Color:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    COLORES_ELEMENTO.chunked(5).forEach { fila ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            fila.forEach { (nombreColor, hex) ->
                                val selec = colorSeleccionado.second == hex
                                Box(
                                    modifier = Modifier
                                        .size(if (selec) 38.dp else 32.dp)
                                        .background(Color(hex), RoundedCornerShape(6.dp))
                                        .pointerInput(Unit) {
                                            detectTapGestures {
                                                colorSeleccionado = Pair(nombreColor, hex)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selec) {
                                        Text("✓", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(16.dp).background(Color(colorSeleccionado.second), RoundedCornerShape(3.dp)))
                    Text(colorSeleccionado.first, fontSize = 12.sp, color = Color.Gray)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = anchoStr.toFloatOrNull() ?: 80f
                    val h = altoStr.toFloatOrNull() ?: 20f
                    onConfirm(nombre, w, h, colorSeleccionado.second)
                }
            ) { Text("Añadir al plano") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// ── Elemento decorativo ────────────────────────────────────────────────────
@Composable
fun ElementoDecorativoComposable(
    elem: ElementoDecorativo,
    isAdmin: Boolean,
    seleccionado: Boolean,
    onTap: () -> Unit,
    onPosicionCambiada: (Int, Float, Float) -> Unit
) {
    var offsetX by remember(elem.id) { mutableStateOf(elem.posX) }
    var offsetY by remember(elem.id) { mutableStateOf(elem.posY) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .width(elem.anchoDP.dp)
            .height(elem.altoDP.dp)
            .background(Color(elem.colorHex), RoundedCornerShape(4.dp))
            .pointerInput(isAdmin) {
                if (isAdmin) {
                    detectDragGestures(
                        onDragEnd = { onPosicionCambiada(elem.id, offsetX, offsetY) },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    )
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { onTap() }
            },
        contentAlignment = Alignment.Center
    ) {
        // Etiqueta si hay espacio suficiente
        if (elem.anchoDP >= 40f && elem.altoDP >= 14f) {
            Text(
                text = elem.nombre,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
        // Borde amarillo cuando está seleccionado
        if (seleccionado) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Yellow.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
            )
            // Indicador en esquina
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(14.dp)
                    .background(Color(0xFFFFD600), RoundedCornerShape(bottomStart = 4.dp))
            ) {
                Text("✕", fontSize = 8.sp, color = Color.Black, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

// ── Mesa arrastrable ──────────────────────────────────────────────────────
@Composable
fun MapaMesas(
    mesa: Mesa,
    isAdmin: Boolean,
    onMesaClick: () -> Unit,
    onPosicionCambiada: (Int, Float, Float) -> Unit
) {
    var offsetX by remember { mutableStateOf(mesa.posX) }
    var offsetY by remember { mutableStateOf(mesa.posY) }

    // Si hay pedido activo en memoria, la mesa se muestra amarilla
    val tienePedido = PedidosStore.tienePedido(mesa.id_mesa)
    val colorEstado = when {
        tienePedido -> Color(0xFFF9A825)   // amarillo = pedido activo
        mesa.estado_mesa.lowercase() == "libre"     -> Color(0xFF2E7D32)
        mesa.estado_mesa.lowercase() == "reservada" -> Color(0xFFF57C00)
        mesa.estado_mesa.lowercase() == "ocupada"   -> Color(0xFFD32F2F)
        else -> Color.Red
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .size(85.dp)
            .background(colorEstado, shape = RoundedCornerShape(5.dp))
            .pointerInput(Unit) {
                if (isAdmin) {
                    detectDragGestures(
                        onDragEnd = { onPosicionCambiada(mesa.id_mesa, offsetX, offsetY) },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    )
                }
            }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onMesaClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(0.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("MESA ${mesa.numero_mesa}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = Color.White.copy(0.8f), modifier = Modifier.size(16.dp))
                    Text("${mesa.capacidad_mesa}", color = Color.White, fontSize = 14.sp)
                }
                if (tienePedido) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("🧾 pedido", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
