package com.losjorges.planbar.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.losjorges.planbar.models.LoginResponse
import com.losjorges.planbar.models.PedidoCocinaApi
import com.losjorges.planbar.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CocineroMainScreen(nombre: String) {
    val context = LocalContext.current
    var pedidos by remember { mutableStateOf(emptyList<PedidoCocinaApi>()) }
    var cargando by remember { mutableStateOf(true) }
    val itemsMarcados = remember { mutableStateMapOf<Int, MutableSet<Int>>() }

    fun cargar() {
        cargando = true
        RetrofitClient.instance.getPedidosCocina().enqueue(object : Callback<List<PedidoCocinaApi>> {
            override fun onResponse(call: Call<List<PedidoCocinaApi>>, response: Response<List<PedidoCocinaApi>>) {
                cargando = false
                if (response.isSuccessful) {
                    pedidos = response.body() ?: emptyList()
                    pedidos.filter { it.preparado }.forEach { itemsMarcados.remove(it.mesa_id) }
                } else {
                    android.util.Log.e("PLANBAR_COCINA", "HTTP ${response.code()}: ${response.errorBody()?.string()}")
                    Toast.makeText(context, "Error servidor: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<List<PedidoCocinaApi>>, t: Throwable) {
                cargando = false
                val url = call.request().url().toString()
                val msg = "${t::class.java.simpleName}: ${t.message} | causa: ${t.cause?.message}"
                android.util.Log.e("PLANBAR_COCINA", "URL: $url")
                android.util.Log.e("PLANBAR_COCINA", msg)
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        })
    }

    LaunchedEffect(Unit) { cargar() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("COCINA", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text("Chef: $nombre", fontSize = 11.sp, color = Color.Gray)
                    }
                },
                actions = {
                    IconButton(onClick = { cargar() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B1B1B),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->

        if (cargando) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        } else if (pedidos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✅", fontSize = 52.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No hay pedidos pendientes", color = Color.Gray, fontSize = 16.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pedidos, key = { it.mesa_id }) { pedido ->
                    val marcados = itemsMarcados.getOrPut(pedido.mesa_id) { mutableSetOf() }
                    val todosListos = pedido.lineas.isNotEmpty() && pedido.lineas.all { marcados.contains(it.producto_id) }

                    PedidoCocinaCard(
                        pedido = pedido,
                        marcados = marcados,
                        todosListos = todosListos,
                        onToggle = { productoId ->
                            val old = itemsMarcados.getOrPut(pedido.mesa_id) { mutableSetOf() }
                            val new = old.toMutableSet()
                            if (new.contains(productoId)) new.remove(productoId) else new.add(productoId)
                            itemsMarcados[pedido.mesa_id] = new
                        },
                        onMarcarListo = {
                            RetrofitClient.instance.marcarPedidoListo(pedido.mesa_id)
                                .enqueue(object : Callback<LoginResponse> {
                                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                                        if (response.isSuccessful && response.body()?.success == true) {
                                            PedidosStore.mesasListas.add(pedido.mesa_id)
                                            itemsMarcados.remove(pedido.mesa_id)
                                            Toast.makeText(context, "✅ Mesa ${pedido.numero_mesa} lista", Toast.LENGTH_SHORT).show()
                                            cargar()
                                        }
                                    }
                                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                                        Toast.makeText(context, "Error al marcar", Toast.LENGTH_SHORT).show()
                                    }
                                })
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PedidoCocinaCard(
    pedido: PedidoCocinaApi,
    marcados: Set<Int>,
    todosListos: Boolean,
    onToggle: (Int) -> Unit,
    onMarcarListo: () -> Unit
) {
    val colorFondo = if (pedido.preparado) Color(0xFF1B2E1B) else Color(0xFF1E1E1E)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (pedido.preparado) Color(0xFF2E7D32) else Color(0xFFE65100),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${pedido.numero_mesa}", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    }
                    Column {
                        Text("MESA ${pedido.numero_mesa}", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Text(
                            text = if (pedido.preparado) "✅ Listo para servir" else "${pedido.lineas.size} platos pendientes",
                            color = if (pedido.preparado) Color(0xFF81C784) else Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
                if (pedido.preparado) {
                    Surface(color = Color(0xFF2E7D32).copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            "LISTO",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = Color(0xFF81C784),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(8.dp))

            pedido.lineas.forEach { linea ->
                val marcado = marcados.contains(linea.producto_id)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = { if (!pedido.preparado) onToggle(linea.producto_id) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (marcado) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                            contentDescription = null,
                            tint = if (marcado) Color(0xFF81C784) else Color.Gray,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Text(
                        text = "×${linea.cantidad}",
                        color = Color(0xFFFFA726),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        modifier = Modifier.width(36.dp)
                    )
                    Text(
                        text = linea.nombre_producto,
                        color = if (marcado) Color.Gray else Color.White,
                        fontSize = 15.sp,
                        fontWeight = if (marcado) FontWeight.Normal else FontWeight.Medium,
                        textDecoration = if (marcado) TextDecoration.LineThrough else TextDecoration.None,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (!pedido.preparado) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onMarcarListo,
                    enabled = todosListos,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32),
                        disabledContainerColor = Color(0xFF2E2E2E)
                    )
                ) {
                    Text(
                        text = if (todosListos) "✅ Marcar pedido como listo" else "Marca todos los platos primero",
                        fontWeight = FontWeight.ExtraBold,
                        color = if (todosListos) Color.White else Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
