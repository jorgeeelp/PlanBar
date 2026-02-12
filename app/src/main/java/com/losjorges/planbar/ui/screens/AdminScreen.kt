package com.losjorges.planbar.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.losjorges.planbar.models.Empleado
import com.losjorges.planbar.models.LoginResponse
import com.losjorges.planbar.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen() {
    val context = LocalContext.current

    var dni by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }

    val opcionesRol = listOf("camarero", "cocinero", "admin")

    var listaEmpleados by remember { mutableStateOf(emptyList<Empleado>()) }
    var idEmpleadoSeleccionado by remember { mutableStateOf<Int?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var employeeToDelete by remember { mutableStateOf<Empleado?>(null) }

    fun limpiarFormulario() {
        dni = ""; nombre = ""; rol = ""; idEmpleadoSeleccionado = null
    }

    fun cargarEmpleados() {
        RetrofitClient.instance.getEmpleadosAdmin().enqueue(object : Callback<List<Empleado>> {
            override fun onResponse(call: Call<List<Empleado>>, response: Response<List<Empleado>>) {
                if (response.isSuccessful) listaEmpleados = response.body() ?: emptyList()
            }
            override fun onFailure(call: Call<List<Empleado>>, t: Throwable) {}
        })
    }

    LaunchedEffect(Unit) { cargarEmpleados() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Administrador - Gestión") }) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {

            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (idEmpleadoSeleccionado == null) "Añadir Nuevo Empleado" else "Editando Empleado",
                        fontWeight = FontWeight.Bold,
                        color = if (idEmpleadoSeleccionado == null) MaterialTheme.colorScheme.primary else Color(0xFFE65100)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(value = dni, onValueChange = { dni = it }, label = { Text("DNI") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = rol,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Seleccionar Rol") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            opcionesRol.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion) },
                                    onClick = {
                                        rol = opcion
                                        expanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }
                    // --- FIN DEL DESPLEGABLE ---

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (dni.isNotEmpty() && nombre.isNotEmpty() && rol.isNotEmpty()) {
                                    RetrofitClient.instance.insertEmpleado(dni, nombre, rol).enqueue(object : Callback<LoginResponse> {
                                        override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                                            if (response.isSuccessful) {
                                                limpiarFormulario()
                                                cargarEmpleados()
                                                Toast.makeText(context, "Insertado", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {}
                                    })
                                } else {
                                    Toast.makeText(context, "Faltan datos", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = idEmpleadoSeleccionado == null
                        ) { Text("Añadir") }

                        Button(
                            onClick = {
                                idEmpleadoSeleccionado?.let { id ->
                                    RetrofitClient.instance.updateEmpleado(id, dni, nombre, rol).enqueue(object : Callback<LoginResponse> {
                                        override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                                            if (response.isSuccessful) {
                                                limpiarFormulario()
                                                cargarEmpleados()
                                                Toast.makeText(context, "Actualizado", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {}
                                    })
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = idEmpleadoSeleccionado != null,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                        ) { Text("Editar") }
                    }

                    if (idEmpleadoSeleccionado != null) {
                        TextButton(onClick = { limpiarFormulario() }, modifier = Modifier.fillMaxWidth()) {
                            Text("Cancelar Edición", color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Lista de Empleados (Toca para editar)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                val listaFiltrada = listaEmpleados.filter { it.rol_empleado != "admin" }
                items(listaFiltrada) { emp ->
                    Surface(
                        onClick = {
                            idEmpleadoSeleccionado = emp.id_empleado
                            dni = emp.dni_empleado
                            nombre = emp.nombre_empleado
                            rol = emp.rol_empleado
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = if (idEmpleadoSeleccionado == emp.id_empleado) Color(0xFFFFF3E0) else Color.White,
                        tonalElevation = 2.dp
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("ID: ${emp.id_empleado} | ${emp.rol_empleado.uppercase()}", fontSize = 11.sp, color = Color.Gray)
                                Text(emp.nombre_empleado, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("DNI: ${emp.dni_empleado}", fontSize = 13.sp)
                            }
                            IconButton(
                                onClick = { employeeToDelete = emp; showDeleteDialog = true },
                                modifier = Modifier.background(Color.Red, shape = RoundedCornerShape(4.dp)).size(35.dp)
                            ) { Text("-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                        }
                    }
                }
            }
        }

        // Diálogo de eliminación
        if (showDeleteDialog && employeeToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirmar eliminación") },
                text = { Text("¿Deseas eliminar a ${employeeToDelete?.nombre_empleado}?") },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        onClick = {
                            RetrofitClient.instance.deleteEmpleado(employeeToDelete!!.id_empleado).enqueue(object : Callback<LoginResponse> {
                                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                                    if (response.isSuccessful) {
                                        cargarEmpleados()
                                        showDeleteDialog = false
                                        if (idEmpleadoSeleccionado == employeeToDelete?.id_empleado) limpiarFormulario()
                                    }
                                }
                                override fun onFailure(call: Call<LoginResponse>, t: Throwable) { showDeleteDialog = false }
                            })
                        }
                    ) { Text("Sí, eliminar", color = Color.White) }
                },
                dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } }
            )
        }
    }
}