package com.example.cheerschecklist

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
@Preview
fun App() {
    MaterialTheme {
        val viewModel: TastingViewModel = viewModel { TastingViewModel() }
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
                )
            }
            composable("edit") {
                EditScreen(
                    viewModel = viewModel,
                    onDone = { navController.popBackStack() },
                )
            }
        }
    }
}
