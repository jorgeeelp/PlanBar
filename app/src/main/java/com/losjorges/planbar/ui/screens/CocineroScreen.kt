package com.losjorges.planbar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CocineroMainScreen(nombre: String) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("COCINA") })
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Hola Chef $nombre", fontSize = 20.sp)
                Text("No hay pedidos pendientes.", color = Color.Gray)
            }
        }
    }
}