package com.example.cheerschecklist

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.compose.koinInject

private val AppBackground = Color(0xFFF8F2FA)

private val AppColorScheme = lightColorScheme(
    background = AppBackground,
    primaryContainer = AppBackground,
)

@Composable
@Preview
fun App() {
    MaterialTheme(colorScheme = AppColorScheme) {
        val repository: TastedDrinkRepository = koinInject()
        val categoryRepository: CategoryRepository = koinInject()
        val viewModel: TastingViewModel = viewModel { TastingViewModel(repository, categoryRepository) }
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "list") {
            composable("list") {
                ListScreen(
                    viewModel = viewModel,
                    onAddNew = {
                        viewModel.cancelEditing()
                        navController.navigate("edit")
                    },
                    onEditEntry = { entry ->
                        viewModel.startEditing(entry)
                        navController.navigate("edit")
                    },
                    onManageCategories = { navController.navigate("categories") },
                )
            }
            composable("edit") {
                EditScreen(
                    viewModel = viewModel,
                    onDone = { navController.popBackStack() },
                )
            }
            composable("categories") {
                ManageCategoriesScreen(
                    viewModel = viewModel,
                    onDone = { navController.popBackStack() },
                )
            }
        }
    }
}
