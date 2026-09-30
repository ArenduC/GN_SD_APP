package com.example.sudoku.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.sudoku.data.SudokuPuzzle
import com.example.sudoku.ui.SudokuViewModel
import com.example.sudoku.ui.components.DeleteConfirmDialog
import com.example.sudoku.ui.components.RenamePuzzleDialog
import com.example.sudoku.ui.components.ScorecardDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedGamesScreen(
    viewModel: SudokuViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPlay: (String) -> Unit
) {
    val puzzles by viewModel.allPuzzles.collectAsStateWithLifecycle()

    var puzzleToRename by remember { mutableStateOf<SudokuPuzzle?>(null) }
    var puzzleToDelete by remember { mutableStateOf<SudokuPuzzle?>(null) }
    var puzzleForScorecard by remember { mutableStateOf<SudokuPuzzle?>(null) }

    if (puzzleForScorecard != null) {
        ScorecardDialog(
            gridSize = puzzleForScorecard!!.gridSize,
            currentGrid = puzzleForScorecard!!.currentGrid,
            lockedCells = puzzleForScorecard!!.lockedCells,
            timerSeconds = puzzleForScorecard!!.elapsedTime,
            onDismiss = { puzzleForScorecard = null }
        )
    }

    if (puzzleToRename != null) {
        RenamePuzzleDialog(
            currentTitle = puzzleToRename!!.title,
            onDismiss = { puzzleToRename = null },
            onConfirmRename = { newTitle ->
                viewModel.renamePuzzle(puzzleToRename!!.id, newTitle)
                puzzleToRename = null
            }
        )
    }

    if (puzzleToDelete != null) {
        DeleteConfirmDialog(
            puzzleTitle = puzzleToDelete!!.title,
            onDismiss = { puzzleToDelete = null },
            onConfirmDelete = {
                viewModel.deletePuzzle(puzzleToDelete!!.id)
                puzzleToDelete = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SAVED GAMES",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("saved_games_back_btn")
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
                .padding(horizontal = 16.dp)
        ) {
            if (puzzles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No saved games found.\nStart a generated puzzle or create your own!",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        lineHeight = 20.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = puzzles,
                        key = { it.id }
                    ) { puzzle ->
                        SavedPuzzleCard(
                            puzzle = puzzle,
                            onResumeClick = {
                                viewModel.loadPuzzle(puzzle.id)
                                onNavigateToPlay(puzzle.id)
                            },
                            onShareClick = { puzzleForScorecard = puzzle },
                            onRenameClick = { puzzleToRename = puzzle },
                            onDuplicateClick = {
                                viewModel.duplicatePuzzle(puzzle.id) { newId ->
                                    viewModel.loadPuzzle(newId)
                                    onNavigateToPlay(newId)
                                }
                            },
                            onDeleteClick = { puzzleToDelete = puzzle }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedPuzzleCard(
    puzzle: SudokuPuzzle,
    onResumeClick: () -> Unit,
    onShareClick: () -> Unit,
    onRenameClick: () -> Unit,
    onDuplicateClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val createdDateStr = dateFormat.format(Date(puzzle.createdAt))
    val lastPlayedStr = timeFormat.format(Date(puzzle.updatedAt))

    val compPercentage = puzzle.getCompletionPercentage()

    Surface(
        onClick = onResumeClick,
        shape = RoundedCornerShape(2.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp))
            .testTag("saved_puzzle_item_${puzzle.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title & Type/Difficulty Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = puzzle.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Grid Size Badge
                    BadgeChip(text = "${puzzle.gridSize}x${puzzle.gridSize}")
                    // Type Badge
                    BadgeChip(text = puzzle.type)
                    if (puzzle.type == "GENERATED" && puzzle.difficulty != "NONE") {
                        BadgeChip(text = puzzle.difficulty)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metadata: Dates & Completion %
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Created: $createdDateStr",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Last Played: $lastPlayedStr",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = if (puzzle.isCompleted) "Completed" else "$compPercentage%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier.testTag("share_btn_${puzzle.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Scorecard"
                    )
                }
                IconButton(
                    onClick = onRenameClick,
                    modifier = Modifier.testTag("rename_btn_${puzzle.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Rename Puzzle"
                    )
                }
                IconButton(
                    onClick = onDuplicateClick,
                    modifier = Modifier.testTag("duplicate_btn_${puzzle.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplicate Puzzle"
                    )
                }
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.testTag("delete_btn_${puzzle.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Puzzle"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = onResumeClick,
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier.testTag("resume_btn_${puzzle.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Resume")
                }
            }
        }
    }
}

@Composable
private fun BadgeChip(text: String) {
    Surface(
        shape = RoundedCornerShape(2.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp))
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
