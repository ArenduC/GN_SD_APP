package com.example.sudoku.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sudoku.engine.PuzzleDifficulty

@Composable
fun DifficultySelectDialog(
    onDismiss: () -> Unit,
    onDifficultySelected: (PuzzleDifficulty, Int) -> Unit
) {
    var selectedGridSize by remember { mutableIntStateOf(9) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Generate New Sudoku",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Select Grid Size:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(4, 6, 9).forEach { size ->
                        val isSelected = selectedGridSize == size
                        Button(
                            onClick = { selectedGridSize = size },
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(0.dp))
                                .testTag("grid_size_opt_$size")
                        ) {
                            Text(
                                text = "${size}x${size}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Select Difficulty:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                DifficultyOptionButton("Easy", "EASY") {
                    onDifficultySelected(PuzzleDifficulty.EASY, selectedGridSize)
                }
                Spacer(modifier = Modifier.height(8.dp))
                DifficultyOptionButton("Medium", "MEDIUM") {
                    onDifficultySelected(PuzzleDifficulty.MEDIUM, selectedGridSize)
                }
                Spacer(modifier = Modifier.height(8.dp))
                DifficultyOptionButton("Hard", "HARD") {
                    onDifficultySelected(PuzzleDifficulty.HARD, selectedGridSize)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_cancel")
            ) {
                Text("Cancel", fontFamily = FontFamily.Monospace)
            }
        },
        shape = RoundedCornerShape(0.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun GridSizeSelectDialog(
    onDismiss: () -> Unit,
    onGridSizeSelected: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create Custom Puzzle",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Select grid dimension for manual entry:",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                listOf(
                    Triple(4, "4x4 Mini (16 cells)", "custom_grid_4"),
                    Triple(6, "6x6 Midi (36 cells)", "custom_grid_6"),
                    Triple(9, "9x9 Classic (81 cells)", "custom_grid_9")
                ).forEach { (size, label, testTag) ->
                    Button(
                        onClick = { onGridSizeSelected(size) },
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(0.dp))
                            .testTag(testTag)
                    ) {
                        Text(
                            text = label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("custom_grid_cancel")
            ) {
                Text("Cancel", fontFamily = FontFamily.Monospace)
            }
        },
        shape = RoundedCornerShape(0.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun DifficultyOptionButton(
    label: String,
    tagSuffix: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(0.dp))
            .testTag("difficulty_$tagSuffix")
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun LockPuzzleConfirmDialog(
    onDismiss: () -> Unit,
    onConfirmLock: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Lock Puzzle",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            Text(
                text = "After locking, these cells become permanent.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmLock,
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("confirm_lock_btn")
            ) {
                Text("Lock Puzzle")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_lock_btn")
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(2.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun RenamePuzzleDialog(
    currentTitle: String,
    onDismiss: () -> Unit,
    onConfirmRename: (String) -> Unit
) {
    var text by remember { mutableStateOf(currentTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Rename Puzzle",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rename_input_field")
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onConfirmRename(text)
                    }
                },
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.testTag("confirm_rename_btn")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_rename_btn")
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(2.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun DeleteConfirmDialog(
    puzzleTitle: String,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Puzzle",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete '$puzzleTitle'?",
                fontSize = 14.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.testTag("confirm_delete_btn")
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_delete_btn")
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(2.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

enum class ValidationResult {
    NONE,
    CORRECT,
    INCORRECT
}

@Composable
fun ValidationDialog(
    result: ValidationResult,
    elapsedTime: Long,
    onSolveAgain: () -> Unit,
    onRevealAnswer: () -> Unit,
    onDismiss: () -> Unit,
    onShareScorecard: () -> Unit = {}
) {
    if (result == ValidationResult.NONE) return

    val isCorrect = result == ValidationResult.CORRECT

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isCorrect) "100% Correct!" else "100% Filled - Input Incorrect",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column {
                if (isCorrect) {
                    Text(
                        text = "Congratulations! All inputs are 100% correct.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Time: ${formatTime(elapsedTime)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "The grid is completely filled, but your solution contains errors or conflicting numbers.\n\nWould you like to solve again or reveal the answer?",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            if (isCorrect) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onShareScorecard,
                        shape = RoundedCornerShape(2.dp),
                        modifier = Modifier.testTag("val_dialog_share_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("Share Scorecard")
                    }
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("val_dialog_ok_btn")
                    ) {
                        Text("Awesome!")
                    }
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onSolveAgain,
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.testTag("val_dialog_solve_again_btn")
                    ) {
                        Text("Solve Again")
                    }
                    Button(
                        onClick = onRevealAnswer,
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("val_dialog_reveal_btn")
                    ) {
                        Text("Reveal Answer")
                    }
                }
            }
        },
        dismissButton = {
            if (isCorrect) null else {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("val_dialog_dismiss_btn")
                ) {
                    Text("Close")
                }
            }
        },
        shape = RoundedCornerShape(2.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun CompletionDialog(
    elapsedTime: Long,
    onDismiss: () -> Unit,
    onShareScorecard: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Puzzle Completed!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Congratulations! You have successfully solved this Sudoku puzzle.",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Time: ${formatTime(elapsedTime)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onShareScorecard,
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier.testTag("completion_share_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Share Scorecard")
                }
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier.testTag("completion_ok_btn")
                ) {
                    Text("OK")
                }
            }
        },
        shape = RoundedCornerShape(2.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

