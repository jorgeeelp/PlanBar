package com.losjorges.planbar.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.losjorges.planbar.models.Empleado
import com.losjorges.planbar.models.LoginResponse
import com.losjorges.planbar.models.Mesa
import com.losjorges.planbar.models.Producto
import com.losjorges.planbar.network.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var pantallaActual by remember { mutableStateOf("empleados") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Menu", modifier = Modifier.padding(16.dp), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                HorizontalDivider()

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = {
                        Text(
                            text = "Gestión Empleados",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Left
                        )
                    },
                    selected = pantallaActual == "empleados",
                    onClick = {
                        pantallaActual = "empleados"
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Build, contentDescription = null) },
                    label = { Text("Gestión Mesas") },
                    selected = pantallaActual == "mesas",
                    onClick = {
                        pantallaActual = "mesas"
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                    label = { Text("Gestión Productos") },
                    selected = pantallaActual == "productos",
                    onClick = {
                        pantallaActual = "productos"
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            when(pantallaActual) {
                                "empleados" -> "Gestión Empleados Admin"
                                "mesas" -> "Gestión Mesas Admin"
                                "productos" -> "Gestión Productos Admin"
                                else -> "Administrador"
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (pantallaActual) {
                    "empleados" -> GestionEmpleadosContent()
                    "mesas" -> GestionMesasContent()
                    "productos" -> GestionProductosContent()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionMesasContent() {
    val context = LocalContext.current

    var numero by remember { mutableStateOf("") }
    var capacidad by remember { mutableStateOf("") }

    var listaMesas by remember { mutableStateOf(emptyList<Mesa>()) }
    var idMesaSeleccionada by remember { mutableStateOf<Int?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var mesaToDelete by remember { mutableStateOf<Mesa?>(null) }

    fun limpiarFormulario() {
        numero = ""; capacidad = ""; idMesaSeleccionada = null
    }

    fun cargarMesas() {
        RetrofitClient.instance.getMesas().enqueue(object : Callback<List<Mesa>> {
            override fun onResponse(call: Call<List<Mesa>>, response: Response<List<Mesa>>) {
                if (response.isSuccessful) listaMesas = response.body() ?: emptyList()
            }
            override fun onFailure(call: Call<List<Mesa>>, t: Throwable) {}
        })
    }

    LaunchedEffect(Unit) { cargarMesas() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (idMesaSeleccionada == null) "Añadir Nueva Mesa" else "Modificar Mesa",
                    fontWeight = FontWeight.Bold,
                    color = if (idMesaSeleccionada == null) MaterialTheme.colorScheme.primary else Color(0xFFE65100)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = numero,
                    onValueChange = { if (it.all { char -> char.isDigit() }) numero = it },
                    label = { Text("Número de Mesa") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = capacidad,
                    onValueChange = { if (it.all { char -> char.isDigit() }) capacidad = it },
                    label = { Text("Capacidad") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val numMesa = numero.toIntOrNull()
                            val capMesa = capacidad.toIntOrNull()

                            if (numMesa != null && capMesa != null) {
                                RetrofitClient.instance.insertMesa(numMesa, capMesa).enqueue(object : Callback<LoginResponse> {
                                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                                        if (response.isSuccessful) {
                                            limpiarFormulario()
                                            cargarMesas()
                                            Toast.makeText(context, "Mesa añadida", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {}
                                })
                            } else {
                                Toast.makeText(context, "Por favor, introduce números válidos", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = idMesaSeleccionada == null
                    ) { Text("Añadir") }

                    Button(
                        onClick = {
                            idMesaSeleccionada?.let { id ->
                                RetrofitClient.instance.updateMesa(id, numero.toInt(), capacidad.toInt()).enqueue(object : Callback<LoginResponse> {
                                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                                        if (response.isSuccessful) {
                                            limpiarFormulario()
                                            cargarMesas()
                                            Toast.makeText(context, "Mesa actualizada", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {}
                                })
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = idMesaSeleccionada != null,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                    ) { Text("Editar") }
                }

                if (idMesaSeleccionada != null) {
                    TextButton(onClick = { limpiarFormulario() }, modifier = Modifier.fillMaxWidth()) { Text("Cancelar Edición", color = Color.Gray) }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Mesas", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(listaMesas) { mesa ->
                Surface(
                    onClick = {
                        idMesaSeleccionada = mesa.id_mesa
                        numero = mesa.numero_mesa.toString()
                        capacidad = mesa.capacidad_mesa.toString()
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = if (idMesaSeleccionada == mesa.id_mesa) Color(0xFFFFF3E0) else Color.White,
                    tonalElevation = 2.dp
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("MESA Nº ${mesa.numero_mesa}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                            Text("Capacidad: ${mesa.capacidad_mesa} personas", fontSize = 14.sp, color = Color.Gray)

                            Text(
                                text = "Estado: ${mesa.estado_mesa.uppercase()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = when(mesa.estado_mesa.lowercase()) {
                                    "libre", "disponible" -> Color(0xFF2E7D32)
                                    "reservada" -> Color(0xFFF57C00)
                                    "ocupada" -> Color.Red
                                    else -> Color.Gray
                                }
                            )
                        }
                        IconButton(
                            onClick = { mesaToDelete = mesa; showDeleteDialog = true },
                            modifier = Modifier.background(Color.Red, shape = RoundedCornerShape(4.dp)).size(35.dp)
                        ) { Text("-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                    }
                }
            }
        }
    }

    if (showDeleteDialog && mesaToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Mesa") },
            text = { Text("¿Deseas eliminar la mesa nº ${mesaToDelete?.numero_mesa} de forma permanente?") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    onClick = {
                        RetrofitClient.instance.deleteMesa(mesaToDelete!!.id_mesa).enqueue(object : Callback<LoginResponse> {
                            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                                if (response.isSuccessful) { cargarMesas(); showDeleteDialog = false }
                            }
                            override fun onFailure(call: Call<LoginResponse>, t: Throwable) { showDeleteDialog = false }
                        })
                    }
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionEmpleadosContent() {
    val context = LocalContext.current
    var dni by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val opcionesRol = listOf("camarero", "cocinero")
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = rol,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Seleccionar Rol") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        opcionesRol.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion) },
                                onClick = { rol = opcion; expanded = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (dni.isNotEmpty() && nombre.isNotEmpty() && rol.isNotEmpty()) {
                                RetrofitClient.instance.insertEmpleado(dni, nombre, rol).enqueue(object : Callback<LoginResponse> {
                                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                                        if (response.isSuccessful) { limpiarFormulario(); cargarEmpleados(); Toast.makeText(context, "Insertado", Toast.LENGTH_SHORT).show() }
                                    }
                                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {}
                                })
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
                                        if (response.isSuccessful) { limpiarFormulario(); cargarEmpleados(); Toast.makeText(context, "Actualizado", Toast.LENGTH_SHORT).show() }
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
        Text("Empleados", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                            Text("ID: ${emp.id_empleado} | ${emp.rol_empleado.uppercase()}", fontSize = 11.sp, color = Color.Black)
                            Text(emp.nombre_empleado, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                            Text("DNI: ${emp.dni_empleado}", fontSize = 13.sp, color = Color.Black)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionProductosContent() {
    val context = LocalContext.current

    // Estados de los campos
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }

    // ESTADOS PARA CATEGORÍA
    var categoria by remember { mutableStateOf("") }
    var expandedCat by remember { mutableStateOf(false) }
    val opcionesCategoria = listOf("entrantes", "carnes", "pescados", "bebidas", "postres")

    var foto by remember { mutableStateOf("default.jpg") }
    var listaProductos by remember { mutableStateOf(emptyList<Producto>()) }
    var idProductoSeleccionado by remember { mutableStateOf<Int?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var productoToDelete by remember { mutableStateOf<Producto?>(null) }

    fun limpiarFormulario() {
        nombre = ""; precio = ""; observaciones = ""; categoria = ""; idProductoSeleccionado = null
    }

    fun cargarProductos() {
        RetrofitClient.instance.getProductos().enqueue(object : Callback<List<Producto>> {
            override fun onResponse(call: Call<List<Producto>>, response: Response<List<Producto>>) {
                if (response.isSuccessful) {
                    listaProductos = response.body() ?: emptyList()
                } else {
                    Toast.makeText(context, "Error al cargar: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Producto>>, t: Throwable) {
                Toast.makeText(context, "Fallo al procesar datos: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    LaunchedEffect(Unit) { cargarProductos() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (idProductoSeleccionado == null) "Nuevo Producto" else "Modificar Producto",
                    fontWeight = FontWeight.Bold,
                    color = if (idProductoSeleccionado == null) MaterialTheme.colorScheme.primary else Color(0xFFE65100)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Nombre
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre del Producto") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(8.dp))

                // Fila para Precio y Categoría
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = precio,
                        onValueChange = { if (it.isEmpty() || it.replace(",", ".").toDoubleOrNull() != null) precio = it },
                        label = { Text("Precio (€)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    ExposedDropdownMenuBox(
                        expanded = expandedCat,
                        onExpandedChange = { expandedCat = !expandedCat },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = categoria,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoría") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCat) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = expandedCat, onDismissRequest = { expandedCat = false }) {
                            opcionesCategoria.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = { categoria = cat; expandedCat = false }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = observaciones,
                    onValueChange = { observaciones = it },
                    label = { Text("Observaciones") },
                    modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val precioDouble = precio.replace(",", ".").toDoubleOrNull()
                            if (nombre.isNotEmpty() && precioDouble != null && categoria.isNotEmpty()) {
                                RetrofitClient.instance.insertProducto(nombre, precioDouble, categoria, observaciones, foto).enqueue(object : Callback<LoginResponse> {
                                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                                        if (response.isSuccessful) {
                                            limpiarFormulario()
                                            cargarProductos()
                                            Toast.makeText(context, "Producto añadido", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {}
                                })
                            } else {
                                Toast.makeText(context, "Rellena Nombre, Precio y Categoría", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = idProductoSeleccionado == null
                    ) { Text("Añadir") }

                    Button(
                        onClick = {
                            val precioDouble = precio.replace(",", ".").toDoubleOrNull()
                            idProductoSeleccionado?.let { id ->
                                if (precioDouble != null) {
                                    RetrofitClient.instance.updateProducto(id, nombre, precioDouble, categoria, observaciones, foto).enqueue(object : Callback<LoginResponse> {
                                        override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                                            if (response.isSuccessful) {
                                                limpiarFormulario()
                                                cargarProductos()
                                                Toast.makeText(context, "Producto actualizado", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {}
                                    })
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = idProductoSeleccionado != null,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                    ) { Text("Editar") }
                }

                if (idProductoSeleccionado != null) {
                    TextButton(onClick = { limpiarFormulario() }, modifier = Modifier.fillMaxWidth()) { Text("Cancelar Edición", color = Color.Gray) }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Productos", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))

        // LISTA DE PRODUCTOS
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(listaProductos) { prod ->
                Surface(
                    onClick = {
                        idProductoSeleccionado = prod.id_producto
                        nombre = prod.nombre_producto
                        precio = prod.precio_producto.toString()
                        categoria = prod.categoria_producto
                        observaciones = prod.observaciones_producto
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = if (idProductoSeleccionado == prod.id_producto) Color(0xFFFFF3E0) else Color.White,
                    tonalElevation = 2.dp
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            // MOSTRAR: NOMBRE | CATEGORÍA
                            Text(
                                text = "${prod.nombre_producto.uppercase()} | ${prod.categoria_producto.uppercase()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.Black
                            )
                            Text("${prod.precio_producto} €", fontSize = 14.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            if (prod.observaciones_producto.isNotEmpty()) {
                                Text(prod.observaciones_producto, fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                        IconButton(
                            onClick = { productoToDelete = prod; showDeleteDialog = true },
                            modifier = Modifier.background(Color.Red, shape = RoundedCornerShape(4.dp)).size(35.dp)
                        ) { Text("-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                    }
                }
            }
        }
    }

    if (showDeleteDialog && productoToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Producto") },
            text = { Text("¿Deseas eliminar ${productoToDelete?.nombre_producto}?") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    onClick = {
                        RetrofitClient.instance.deleteProducto(productoToDelete!!.id_producto).enqueue(object : Callback<LoginResponse> {
                            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                                if (response.isSuccessful) {
                                    cargarProductos()
                                    showDeleteDialog = false
                                }
                            }
                            override fun onFailure(call: Call<LoginResponse>, t: Throwable) { showDeleteDialog = false }
                        })
                    }
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } }
        )
    }
}