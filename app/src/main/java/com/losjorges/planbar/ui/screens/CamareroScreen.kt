package com.losjorges.planbar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CamareroMainScreen(nombre: String) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var pantallaActual by remember { mutableStateOf("mesas") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("CAMARERO: $nombre", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Mapa de Mesas") },
                    selected = pantallaActual == "mesas",
                    onClick = { pantallaActual = "mesas"; scope.launch { drawerState.close() } }
                )
                NavigationDrawerItem(
                    label = { Text("Ver Reservas") },
                    selected = pantallaActual == "reservas",
                    onClick = { pantallaActual = "reservas"; scope.launch { drawerState.close() } }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (pantallaActual == "mesas") "Mesas Disponibles" else "Lista de Reservas") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = null)
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (pantallaActual == "mesas") {
                    // Aquí llamaremos pronto a la lógica de mesas para camareros
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Aquí el camarero verá las mesas de colores")
                    }
                } else {
                    // Aquí llamaremos pronto a la lógica de reservas
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Aquí el camarero verá el listado de reservas")
                    }
                }
            }
        }
    }
}