package com.losjorges.planbar.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
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
import com.losjorges.planbar.models.Reserva
import com.losjorges.planbar.network.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CamareroMainScreen(nombre: String, navController: NavHostController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var pantallaActual by remember { mutableStateOf("mesas") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(15.dp))
                Text("CAMARERO: $nombre",
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))
                NavigationDrawerItem(
                    label = { Text("Mapa de Mesas") },
                    selected = pantallaActual == "mesas",
                    onClick = {
                        pantallaActual = "mesas"
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(modifier = Modifier.height(10.dp))
                NavigationDrawerItem(
                    label = { Text("Ver Reservas") },
                    selected = pantallaActual == "reservas",
                    onClick = {
                        pantallaActual = "reservas"
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(modifier = Modifier.weight(1f))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ExitToApp, null) },
                    label = { Text("Cerrar Sesión") },
                    selected = false,
                    onClick = { navController.navigate("seleccion_empleado") }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                if (pantallaActual == "mesas") "GESTIÓN DE MESAS" else "RESERVAS",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text("Atendiendo como: $nombre", fontSize = 11.sp, color = Color.Gray)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, null)
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (pantallaActual == "mesas") {
                    GestionMesasCamareroContent(navController)
                } else {
                    ListaReservasContent()
                }
            }
        }
    }
}

@Composable
fun GestionMesasCamareroContent(navController: NavHostController) {
    MapaMesasScreen(isAdmin = false) { mesa ->
        navController.navigate("detalle_mesa/${mesa.id_mesa}/${mesa.numero_mesa}")
        Log.d("Navigation", "Navigating to detalle_mesa/${mesa.id_mesa}/${mesa.numero_mesa}")
    }
}

@Composable
fun MesaItem(mesa: Mesa, onClick: () -> Unit) {
    val colorEstado = when (mesa.estado_mesa.lowercase()) {
        "libre" -> Color(0xFF2E7D32)
        "reservada" -> Color(0xFFF57C00)
        "ocupada" -> Color(0xFFD32F2F)
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
            Text("MESA", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
            Text(mesa.numero_mesa.toString(), fontSize = 42.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
            Text("${mesa.capacidad_mesa} PERSONAS", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ListaReservasContent() {
    val context = LocalContext.current
    var listaReservas by remember { mutableStateOf(emptyList<Reserva>()) }
    var cargando by remember { mutableStateOf(true) }

    fun cargarReservas() {
        cargando = true
        RetrofitClient.instance.getReservas().enqueue(object : Callback<List<Reserva>> {
            override fun onResponse(call: Call<List<Reserva>>, response: Response<List<Reserva>>) {
                cargando = false
                if (response.isSuccessful) {
                    listaReservas = response.body() ?: emptyList()
                }
            }
            override fun onFailure(call: Call<List<Reserva>>, t: Throwable) {
                cargando = false
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    LaunchedEffect(Unit) {
        cargarReservas()
    }

    if (cargando) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { cargarReservas() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (listaReservas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay reservas todavía", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(listaReservas) { reserva ->
                        ReservaItem(reserva)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservaItem(reserva: Reserva) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(reserva.nombre_cliente, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Text("MESA ${reserva.numero_mesa}", modifier = Modifier.padding(4.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("CUÁNDO", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(reserva.fecha_reserva, fontSize = 14.sp, color = Color.Black)
                    Text(reserva.hora_reserva, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("PERSONAS", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("${reserva.num_personas} Comensales", fontSize = 14.sp, color = Color.Black)
                }
            }
        }
    }
}