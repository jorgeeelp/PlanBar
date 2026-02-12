package com.losjorges.planbar.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.losjorges.planbar.models.Empleado
import com.losjorges.planbar.models.LoginResponse
import com.losjorges.planbar.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleccionEmpleadoScreen(navController: NavHostController) {
    val context = LocalContext.current
    var listaEmpleados by remember { mutableStateOf(emptyList<Empleado>()) }
    var showAdminDialog by remember { mutableStateOf(false) }
    var adminPassword by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        RetrofitClient.instance.getEmpleados().enqueue(object : Callback<List<Empleado>> {
            override fun onResponse(call: Call<List<Empleado>>, response: Response<List<Empleado>>) {
                if (response.isSuccessful) listaEmpleados = response.body() ?: emptyList()
            }
            override fun onFailure(call: Call<List<Empleado>>, t: Throwable) {
                Toast.makeText(context, "Sin conexión al servidor", Toast.LENGTH_SHORT).show()
            }
        })
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("PLANBAR", fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                },
                actions = {
                    IconButton(onClick = { showAdminDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Admin", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF121318), Color.White)
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Trabajadores",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(listaEmpleados) { empleado ->
                        EmpleadoCard(empleado) {
                            Toast.makeText(context, "Sesión: ${empleado.nombre_empleado}", Toast.LENGTH_SHORT).show()
                            navController.navigate("menu")
                        }
                    }
                }
            }
        }

        if (showAdminDialog) {
            AlertDialog(
                onDismissRequest = { showAdminDialog = false },
                title = { Text("Acceso Restringido", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Introduzca clave de administrador")
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = adminPassword,
                            onValueChange = { adminPassword = it },
                            label = { Text("PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            RetrofitClient.instance.loginAdmin(adminPassword).enqueue(object : Callback<LoginResponse> {
                                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                                    if (response.isSuccessful && response.body()?.success == true) {
                                        showAdminDialog = false
                                        navController.navigate("admin_panel")
                                    } else {
                                        Toast.makeText(context, "PIN Incorrecto", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {}
                            })
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Confirmar") }
                },
                dismissButton = {
                    TextButton(onClick = { showAdminDialog = false }) { Text("Cancelar") }
                }
            )
        }
    }
}

@Composable
fun EmpleadoCard(empleado: Empleado, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Gray),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = empleado.nombre_empleado,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
            Text(
                text = empleado.rol_empleado.uppercase(),
                fontSize = 12.sp,
                color = Color.White
            )
        }
    }
}