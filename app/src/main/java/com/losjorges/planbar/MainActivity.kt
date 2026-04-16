package com.losjorges.planbar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.losjorges.planbar.ui.screens.AdminScreen
import com.losjorges.planbar.ui.screens.CamareroMainScreen
import com.losjorges.planbar.ui.screens.CocineroMainScreen
import com.losjorges.planbar.ui.screens.MapaMesasScreen
import com.losjorges.planbar.ui.screens.SeleccionEmpleadoScreen
import com.losjorges.planbar.ui.theme.PlanBarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlanBarTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "seleccion_empleado") {
                    composable("seleccion_empleado") { SeleccionEmpleadoScreen(navController) }
                    composable("admin_panel") { AdminScreen(navController) }
                    composable("posicionar_mesas") { MapaMesasScreen(isAdmin = true, onMesaClick = {}) }

                    composable("camarero_home/{nombre}") { backStackEntry ->
                        val nombre = backStackEntry.arguments?.getString("nombre") ?: "Camarero"
                        CamareroMainScreen(nombre, navController)
                    }

                    composable("cocinero_home/{nombre}") { backStackEntry ->
                        val nombre = backStackEntry.arguments?.getString("nombre") ?: "Cocinero"
                        CocineroMainScreen(nombre)
                    }

                    composable("detalle_mesa/{idMesa}/{numero}") { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("idMesa") ?: "0"
                        val num = backStackEntry.arguments?.getString("numero") ?: "0"

                        Surface(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                            Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                                Text("Gestionando Mesa $num (ID: $id)")
                            }
                        }
                    }
                }
            }
        }
    }
}