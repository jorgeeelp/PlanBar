package com.losjorges.planbar.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
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
import com.losjorges.planbar.models.Mesa
import com.losjorges.planbar.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MesasScreen(navController: NavHostController) {
    val context = LocalContext.current
    var listaMesas by remember { mutableStateOf(emptyList<Mesa>()) }
    var cargando by remember { mutableStateOf(true) }

    fun cargarMesas() {
        cargando = true
        RetrofitClient.instance.getMesas().enqueue(object : Callback<List<Mesa>> {
            override fun onResponse(call: Call<List<Mesa>>, response: Response<List<Mesa>>) {
                cargando = false
                if (response.isSuccessful) {
                    listaMesas = response.body() ?: emptyList()
                }
            }
            override fun onFailure(call: Call<List<Mesa>>, t: Throwable) {
                cargando = false
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    LaunchedEffect(Unit) {
        cargarMesas()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GESTIÓN DE MESAS", fontWeight = FontWeight.Black, fontSize = 20.sp) },
                actions = {
                    IconButton(onClick = { cargarMesas() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                    IconButton(onClick = { navController.navigate("seleccion_empleado") }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Salir")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Pulse una mesa para gestionar el pedido",
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2), // Dos mesas por fila
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(listaMesas) { mesa ->
                            MesaItem(mesa) {
                                // Aquí navegaremos a la pantalla de pedido en el futuro
                                Toast.makeText(context, "Mesa ${mesa.numero_mesa} seleccionada", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MesaItem(mesa: Mesa, onClick: () -> Unit) {
    // Lógica de colores según tus instrucciones
    val colorEstado = when (mesa.estado_mesa.lowercase()) {
        "libre", "disponible" -> Color(0xFF2E7D32) // Verde
        "reservada" -> Color(0xFFF57C00)           // Naranja
        "ocupada" -> Color(0xFFD32F2F)             // Rojo
        else -> Color.DarkGray
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorEstado),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "MESA",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = mesa.numero_mesa.toString(),
                fontSize = 42.sp,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "${mesa.capacidad_mesa} PERSONAS",
                fontSize = 11.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}