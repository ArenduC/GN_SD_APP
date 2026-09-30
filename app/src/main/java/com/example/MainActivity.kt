package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sudoku.ui.SudokuViewModel
import com.example.sudoku.ui.screens.CustomCreatorScreen
import com.example.sudoku.ui.screens.HomeScreen
import com.example.sudoku.ui.screens.SavedGamesScreen
import com.example.sudoku.ui.screens.SudokuPlayScreen
import com.example.ui.theme.SudokuOrganizerTheme
import com.example.ui.theme.ThemeMode

import com.example.sudoku.ui.screens.SplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: SudokuViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            }

            DisposableEffect(isDark) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    },
                    navigationBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    }
                )
                onDispose {}
            }

            SudokuOrganizerTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SudokuAppNavHost(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun SudokuAppNavHost(
    viewModel: SudokuViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        // 0. Splash Screen
        composable("splash") {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // 1. Home Screen
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToPlay = { puzzleId ->
                    navController.navigate("play/$puzzleId")
                },
                onNavigateToCreator = { puzzleId ->
                    navController.navigate("creator/$puzzleId")
                },
                onNavigateToSavedGames = {
                    navController.navigate("saved_games")
                }
            )
        }

        // 2. Saved Games
        composable("saved_games") {
            SavedGamesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlay = { puzzleId ->
                    navController.navigate("play/$puzzleId")
                }
            )
        }

        // 3. Custom Creator (Manual Entry)
        composable(
            route = "creator/{puzzleId}",
            arguments = listOf(navArgument("puzzleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val puzzleId = backStackEntry.arguments?.getString("puzzleId") ?: return@composable
            viewModel.loadPuzzle(puzzleId)
            CustomCreatorScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onPuzzleLocked = { lockedPuzzleId ->
                    navController.navigate("play/$lockedPuzzleId") {
                        popUpTo("home")
                    }
                }
            )
        }

        // 4. Play Screen
        composable(
            route = "play/{puzzleId}",
            arguments = listOf(navArgument("puzzleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val puzzleId = backStackEntry.arguments?.getString("puzzleId") ?: return@composable
            viewModel.loadPuzzle(puzzleId)
            SudokuPlayScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
