package com.example.sudoku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.width
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.sudoku.ui.SudokuViewModel
import com.example.sudoku.ui.components.DifficultySelectDialog
import com.example.sudoku.ui.components.GridSizeSelectDialog

import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.ThemeMode

@Composable
fun HomeScreen(
    viewModel: SudokuViewModel,
    onNavigateToPlay: (String) -> Unit,
    onNavigateToCreator: (String) -> Unit,
    onNavigateToSavedGames: () -> Unit
) {
    var showDifficultyDialog by remember { mutableStateOf(false) }
    var showCustomGridDialog by remember { mutableStateOf(false) }
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    if (showDifficultyDialog) {
        DifficultySelectDialog(
            onDismiss = { showDifficultyDialog = false },
            onDifficultySelected = { difficulty, gridSize ->
                showDifficultyDialog = false
                viewModel.startNewGeneratedPuzzle(difficulty, gridSize) { puzzleId ->
                    onNavigateToPlay(puzzleId)
                }
            }
        )
    }

    if (showCustomGridDialog) {
        GridSizeSelectDialog(
            onDismiss = { showCustomGridDialog = false },
            onGridSizeSelected = { gridSize ->
                showCustomGridDialog = false
                viewModel.createCustomPuzzle(gridSize) { puzzleId ->
                    onNavigateToCreator(puzzleId)
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x2BEE6F72))
                    .padding(vertical = 12.dp)
                    .testTag("graphynovus_bottom_strip"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "G  R  A  P  H  Y  N  O  V  U  S",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    color = Color(0xFFEE6F72)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Minimalist Notebook Header
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_sudora_logo),
                            contentDescription = "SUDORA Logo",
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "SUDORA",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Digital Sudoku Notebook",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Theme Mode Switcher
                    OutlinedButton(
                        onClick = { viewModel.toggleThemeMode() },
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.testTag("home_theme_toggle_btn")
                    ) {
                        val (icon, label) = when (themeMode) {
                            ThemeMode.DARK -> Icons.Default.DarkMode to "Dark"
                            ThemeMode.LIGHT -> Icons.Default.LightMode to "Light"
                            ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto to "Auto"
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "Theme Mode: $label",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                // Minimalist Rule Line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .border(1.dp, MaterialTheme.colorScheme.onBackground)
                )
            }

            // The Three Choices
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Choice 1: Generated Sudoku
                HomeChoiceCard(
                    title = "1. Generated Sudoku",
                    subtitle = "Play auto-generated Easy, Medium or Hard (4x4, 6x6, 9x9)",
                    icon = Icons.Default.GridOn,
                    testTag = "home_choice_generated",
                    onClick = { showDifficultyDialog = true }
                )

                // Choice 2: Create Your Own Sudoku
                HomeChoiceCard(
                    title = "2. Create Your Own Sudoku",
                    subtitle = "Manually enter & lock a puzzle (4x4, 6x6, or 9x9)",
                    icon = Icons.Default.AddBox,
                    testTag = "home_choice_create_custom",
                    onClick = { showCustomGridDialog = true }
                )

                // Choice 3: Saved Games
                HomeChoiceCard(
                    title = "3. Saved Games",
                    subtitle = "Resume, manage, duplicate or organize saved puzzles",
                    icon = Icons.Default.Folder,
                    testTag = "home_choice_saved_games",
                    onClick = onNavigateToSavedGames
                )
            }

            // Footer Note
            Text(
                text = "100% Offline • Local Persistence • High Density Design",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun HomeChoiceCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(2.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp))
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .padding(start = 8.dp)
            )
        }
    }
}
