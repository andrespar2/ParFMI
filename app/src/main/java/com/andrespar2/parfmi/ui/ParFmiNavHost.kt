package com.andrespar2.parfmi.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.andrespar2.parfmi.ui.about.AboutScreen
import com.andrespar2.parfmi.ui.home.HomeScreen
import com.andrespar2.parfmi.ui.lookup.LookupViewModel
import com.andrespar2.parfmi.ui.result.ResultScreen
import com.andrespar2.parfmi.ui.settings.SettingsScreen

/** Rutas de navegacion de la app. */
object Routes {
    const val HOME = "home"
    const val RESULT = "result"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
}

/**
 * Grafo de navegacion.
 *
 * El [LookupViewModel] se crea aqui (una sola instancia) y se comparte entre la
 * pantalla principal y la de resultados, de modo que esta ultima siempre
 * muestra el estado de la ultima consulta.
 */
@Composable
fun ParFmiNavHost(
    navController: NavHostController = rememberNavController(),
    lookupViewModel: LookupViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToResult = { navController.navigate(Routes.RESULT) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToAbout = { navController.navigate(Routes.ABOUT) },
                viewModel = lookupViewModel,
            )
        }
        composable(Routes.RESULT) {
            ResultScreen(
                viewModel = lookupViewModel,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}
