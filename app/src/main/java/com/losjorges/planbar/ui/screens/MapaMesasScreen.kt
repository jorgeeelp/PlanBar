package com.losjorges.planbar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

@Composable
fun MapaMesasScreen(isAdmin: Boolean, onMesaClick: (Mesa) -> Unit) {
    var listaMesas by remember { mutableStateOf(emptyList<Mesa>()) }

    LaunchedEffect(Unit) {
        RetrofitClient.instance.getMesas().enqueue(object : Callback<List<Mesa>> {
            override fun onResponse(call: Call<List<Mesa>>, response: Response<List<Mesa>>) {
                if (response.isSuccessful) listaMesas = response.body() ?: emptyList()
            }
            override fun onFailure(call: Call<List<Mesa>>, t: Throwable) {}
        })
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF0F0F0))) {
        listaMesas.forEach { mesa ->
            MapaMesas(
                mesa = mesa,
                isAdmin = isAdmin,
                onMesaClick = { onMesaClick(mesa) },
                onPosicionCambiada = { id, x, y ->
                    RetrofitClient.instance.updateMesaPosicion(id, x, y).enqueue(object : Callback<com.losjorges.planbar.models.LoginResponse> {
                        override fun onResponse(call: Call<com.losjorges.planbar.models.LoginResponse>, response: Response<com.losjorges.planbar.models.LoginResponse>) {}
                        override fun onFailure(call: Call<com.losjorges.planbar.models.LoginResponse>, t: Throwable) {}
                    })
                }
            )
        }
    }
}

@Composable
fun MapaMesas(
    mesa: Mesa,
    isAdmin: Boolean,
    onMesaClick: () -> Unit,
    onPosicionCambiada: (Int, Float, Float) -> Unit
) {
    var offsetX by remember { mutableStateOf(mesa.posX) }
    var offsetY by remember { mutableStateOf(mesa.posY) }

    val colorEstado = when(mesa.estado_mesa.lowercase()) {
        "libre" -> Color(0xFF2E7D32)
        "reservada" -> Color(0xFFF57C00)
        "ocupada" -> Color(0xFFD32F2F)
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
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = Color.White.copy(0.8f), modifier = Modifier.size(16.dp))
                    Text("${mesa.capacidad_mesa}", color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}