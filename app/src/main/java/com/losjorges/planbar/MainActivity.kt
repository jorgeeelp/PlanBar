package com.losjorges.planbar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.losjorges.planbar.ui.screens.AdminScreen
import com.losjorges.planbar.ui.screens.MenuPrincipal
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
                    composable("menu") { MenuPrincipal() }
                    composable("admin_panel") { AdminScreen() }
                }
            }
        }
    }
}