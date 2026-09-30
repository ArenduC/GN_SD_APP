package com.example.sudoku.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sudoku.ui.SudokuViewModel
import com.example.sudoku.ui.components.LockPuzzleConfirmDialog
import com.example.sudoku.ui.components.NumberPad
import com.example.sudoku.ui.components.SudokuBoard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomCreatorScreen(
    viewModel: SudokuViewModel,
    onNavigateBack: () -> Unit,
    onPuzzleLocked: (String) -> Unit
) {
    val activePuzzle by viewModel.activePuzzle.collectAsStateWithLifecycle()
    val currentGrid by viewModel.currentGridState.collectAsStateWithLifecycle()
    val selectedIndex by viewModel.selectedCellIndex.collectAsStateWithLifecycle()
    val notesMap by viewModel.notesMap.collectAsStateWithLifecycle()

    var showLockDialog by remember { mutableStateOf(false) }

    if (showLockDialog) {
        LockPuzzleConfirmDialog(
            onDismiss = { showLockDialog = false },
            onConfirmLock = {
                showLockDialog = false
                viewModel.lockCustomPuzzle {
                    activePuzzle?.id?.let { puzzleId ->
                        onPuzzleLocked(puzzleId)
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "MANUAL ENTRY",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("creator_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate Back"
                        )
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
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Instruction header
            val gridSize = activePuzzle?.gridSize ?: 9
            Text(
                text = "Enter ${gridSize}x${gridSize} numbers from newspaper or book, then tap LOCK PUZZLE.",
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Board (Unlocked mode: all cells editable for manual entry)
            SudokuBoard(
                currentGrid = currentGrid,
                lockedCells = activePuzzle?.lockedCells ?: "0".repeat(gridSize * gridSize),
                selectedIndex = selectedIndex,
                notesMap = notesMap,
                isLocked = false, // Not locked yet
                onCellSelected = { idx -> viewModel.selectCell(idx) },
                modifier = Modifier.weight(1f, fill = false),
                gridSize = gridSize
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Number input pad
            NumberPad(
                onNumberClick = { num -> viewModel.inputNumber(num) },
                onEraseClick = { viewModel.eraseCell() },
                gridSize = gridSize
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lock Puzzle Button
            Button(
                onClick = { showLockDialog = true },
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("lock_puzzle_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "LOCK PUZZLE",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
