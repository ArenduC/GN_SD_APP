package com.example.sudoku.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.sudoku.data.SudokuPuzzle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.sudoku.util.ShareUtils
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sudoku.ui.SudokuViewModel
import androidx.compose.material3.TextButton
import com.example.sudoku.ui.components.CompletionDialog
import com.example.sudoku.ui.components.ScorecardDialog
import com.example.sudoku.ui.components.ValidationDialog
import com.example.sudoku.ui.components.ValidationResult
import com.example.sudoku.ui.components.NumberPad
import com.example.sudoku.ui.components.SudokuBoard
import com.example.sudoku.ui.components.TimerWidget
import com.example.sudoku.ui.components.Toolbar

import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import com.example.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SudokuPlayScreen(
    viewModel: SudokuViewModel,
    onNavigateBack: () -> Unit
) {
    val activePuzzle by viewModel.activePuzzle.collectAsStateWithLifecycle()
    val currentGrid by viewModel.currentGridState.collectAsStateWithLifecycle()
    val selectedIndex by viewModel.selectedCellIndex.collectAsStateWithLifecycle()
    val notesMap by viewModel.notesMap.collectAsStateWithLifecycle()
    val isNotesMode by viewModel.isNotesMode.collectAsStateWithLifecycle()
    val isPaused by viewModel.isPaused.collectAsStateWithLifecycle()
    val elapsedTime by viewModel.elapsedTime.collectAsStateWithLifecycle()
    val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
    val canRedo by viewModel.canRedo.collectAsStateWithLifecycle()
    val showCompletionDialog by viewModel.showCompletionDialog.collectAsStateWithLifecycle()
    val validationResult by viewModel.validationResult.collectAsStateWithLifecycle()
    val userGridBeforeReveal by viewModel.userGridBeforeReveal.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    var showScorecardModal by remember { mutableStateOf(false) }

    val puzzle = activePuzzle

    if (showScorecardModal && puzzle != null) {
        ScorecardDialog(
            gridSize = puzzle.gridSize,
            currentGrid = SudokuPuzzle.intArrayToString(currentGrid),
            lockedCells = puzzle.lockedCells,
            timerSeconds = elapsedTime,
            onDismiss = { showScorecardModal = false }
        )
    }

    if (showCompletionDialog) {
        CompletionDialog(
            elapsedTime = elapsedTime,
            onShareScorecard = {
                showScorecardModal = true
            },
            onDismiss = { viewModel.dismissCompletionDialog() }
        )
    }

    if (validationResult != ValidationResult.NONE) {
        ValidationDialog(
            result = validationResult,
            elapsedTime = elapsedTime,
            onShareScorecard = {
                showScorecardModal = true
            },
            onSolveAgain = { viewModel.dismissValidationDialog() },
            onRevealAnswer = { viewModel.revealSolution() },
            onDismiss = { viewModel.dismissValidationDialog() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = puzzle?.title ?: "SUDOKU",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                        puzzle?.let {
                            val sub = if (it.type == "GENERATED") "Difficulty: ${it.difficulty}" else "Custom Puzzle"
                            Text(
                                text = sub,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("play_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleThemeMode() },
                        modifier = Modifier.testTag("play_theme_toggle_btn")
                    ) {
                        val icon = when (themeMode) {
                            ThemeMode.DARK -> Icons.Default.DarkMode
                            ThemeMode.LIGHT -> Icons.Default.LightMode
                            ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "Toggle Theme Mode"
                        )
                    }
                    if (puzzle?.isCompleted == false) {
                        TextButton(
                            onClick = { viewModel.revealSolution() },
                            modifier = Modifier.testTag("play_reveal_answer_btn")
                        ) {
                            Text(
                                text = "Reveal Answer",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },

        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Timer & Status Bar
            TimerWidget(
                elapsedSeconds = elapsedTime,
                isPaused = isPaused,
                completionPercentage = puzzle?.getCompletionPercentage() ?: 0,
                onPauseToggle = { viewModel.togglePauseTimer() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            val gridSize = puzzle?.gridSize ?: 9

            // Sudoku Board
            SudokuBoard(
                currentGrid = currentGrid,
                lockedCells = puzzle?.lockedCells ?: "0".repeat(gridSize * gridSize),
                selectedIndex = selectedIndex,
                notesMap = notesMap,
                isLocked = puzzle?.isLocked ?: true,
                onCellSelected = { idx -> viewModel.selectCell(idx) },
                modifier = Modifier.weight(1f, fill = false),
                gridSize = gridSize,
                userGrid = userGridBeforeReveal
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Undo / Redo / Notes Toolbar
            Toolbar(
                canUndo = canUndo,
                canRedo = canRedo,
                isNotesMode = isNotesMode,
                onUndoClick = { viewModel.undo() },
                onRedoClick = { viewModel.redo() },
                onNotesToggle = { viewModel.toggleNotesMode() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Number Pad
            NumberPad(
                onNumberClick = { num -> viewModel.inputNumber(num) },
                onEraseClick = { viewModel.eraseCell() },
                gridSize = gridSize
            )
        }
    }
}
