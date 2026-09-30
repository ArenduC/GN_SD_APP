package com.example.sudoku.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sudoku.engine.SudokuSolver
import com.example.ui.theme.DarkCellHighlight
import com.example.ui.theme.DarkGridBorder
import com.example.ui.theme.DarkSelectedCell
import com.example.ui.theme.DarkSubtleLine
import com.example.ui.theme.LightCellHighlight
import com.example.ui.theme.LightGridBorder
import com.example.ui.theme.LightSelectedCell
import com.example.ui.theme.LightSubtleLine
import com.example.ui.theme.LocalIsDark
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoWhite

@Composable
fun SudokuBoard(
    currentGrid: IntArray,
    lockedCells: String,
    selectedIndex: Int?,
    notesMap: Map<Int, Set<Int>>,
    isLocked: Boolean,
    onCellSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    gridSize: Int = 9,
    userGrid: IntArray? = null
) {
    val isDark = LocalIsDark.current || (MaterialTheme.colorScheme.background.luminance() < 0.5f)

    val borderColor = if (isDark) DarkGridBorder else LightGridBorder
    val subtleLineColor = if (isDark) DarkSubtleLine else LightSubtleLine
    val cellHighlightColor = if (isDark) DarkCellHighlight else LightCellHighlight
    val selectedCellColor = if (isDark) DarkSelectedCell else LightSelectedCell
    val matchingCellColor = if (isDark) Color(0xFF3E2326) else Color(0xFFFFEBEE)

    val boxRows = SudokuSolver.getBoxRows(gridSize)
    val boxCols = SudokuSolver.getBoxCols(gridSize)

    val selectedRow = selectedIndex?.let { it / gridSize }
    val selectedCol = selectedIndex?.let { it % gridSize }
    val selectedValue = selectedIndex?.let { currentGrid.getOrNull(it) }?.takeIf { it != 0 }

    val numberFontSize = when (gridSize) {
        4 -> 28.sp
        6 -> 24.sp
        else -> 20.sp
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(MaterialTheme.colorScheme.background)
            .border(2.5.dp, borderColor, RoundedCornerShape(0.dp))
            .testTag("sudoku_board")
    ) {
        // Grid cells layout
        Column(modifier = Modifier.fillMaxSize()) {
            for (row in 0 until gridSize) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    for (col in 0 until gridSize) {
                        val index = row * gridSize + col
                        val number = currentGrid.getOrNull(index) ?: 0
                        val isGiven = if (isLocked) (lockedCells.getOrNull(index) == '1') else true
                        val isSelected = selectedIndex == index

                        val inSelectedRow = selectedRow == row
                        val inSelectedCol = selectedCol == col
                        val inSelectedBox = selectedRow != null && selectedCol != null &&
                                (row / boxRows == selectedRow / boxRows) && (col / boxCols == selectedCol / boxCols)
                        val isHighlightedGroup = (inSelectedRow || inSelectedCol || inSelectedBox) && !isSelected
                        val isMatchingValue = selectedValue != null && number == selectedValue && !isSelected

                        val cellBg = when {
                            isSelected -> selectedCellColor
                            isMatchingValue -> matchingCellColor
                            isHighlightedGroup -> cellHighlightColor
                            else -> Color.Transparent
                        }

                        val userNumber = userGrid?.getOrNull(index) ?: 0

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(cellBg)
                                .clickable { onCellSelected(index) }
                                .testTag("cell_$index"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (userGrid != null && !isGiven) {
                                if (userNumber != 0 && userNumber != number) {
                                    // User entered wrong input: display user's input in RED strikethrough, and correct answer below
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = userNumber.toString(),
                                            fontSize = (numberFontSize.value * 0.55).sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.SansSerif,
                                            color = if (isDark) Color(0xFFFF5252) else Color(0xFFD32F2F),
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                        Text(
                                            text = number.toString(),
                                            fontSize = (numberFontSize.value * 0.72).sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.SansSerif,
                                            color = if (isDark) Color(0xFF69F0AE) else Color(0xFF2E7D32)
                                        )
                                    }
                                } else if (userNumber != 0 && userNumber == number) {
                                    // Correct user answer
                                    Text(
                                        text = number.toString(),
                                        fontSize = numberFontSize,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.SansSerif,
                                        color = if (isDark) Color(0xFF69F0AE) else Color(0xFF2E7D32)
                                    )
                                } else {
                                    // Revealed solution for empty cell
                                    Text(
                                        text = number.toString(),
                                        fontSize = numberFontSize,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.SansSerif,
                                        color = if (isDark) Color(0xFF80D8FF) else Color(0xFF1976D2)
                                    )
                                }
                            } else if (number != 0) {
                                val textColor = if (isGiven) {
                                    if (isDark) MonoWhite else MonoBlack
                                } else {
                                    if (isDark) Color(0xFFFF8589) else Color(0xFFD32F2F)
                                }
                                Text(
                                    text = number.toString(),
                                    fontSize = numberFontSize,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.SansSerif,
                                    color = textColor
                                )
                            } else {
                                // Render pencil marks notes
                                val notes = notesMap[index]
                                if (!notes.isNullOrEmpty()) {
                                    NotesGrid(notes = notes, gridSize = gridSize)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Draw sub-box and grid lines overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellWidth = size.width / gridSize.toFloat()
            val cellHeight = size.height / gridSize.toFloat()

            // Thin cell divider lines
            for (i in 1 until gridSize) {
                if (i % boxCols != 0) {
                    // Vertical thin
                    drawLine(
                        color = subtleLineColor,
                        start = Offset(cellWidth * i, 0f),
                        end = Offset(cellWidth * i, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                if (i % boxRows != 0) {
                    // Horizontal thin
                    drawLine(
                        color = subtleLineColor,
                        start = Offset(0f, cellHeight * i),
                        end = Offset(size.width, cellHeight * i),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }

            // Bold sub-box boundary lines
            for (col in 1 until gridSize) {
                if (col % boxCols == 0) {
                    drawLine(
                        color = borderColor,
                        start = Offset(cellWidth * col, 0f),
                        end = Offset(cellWidth * col, size.height),
                        strokeWidth = 2.5.dp.toPx()
                    )
                }
            }
            for (row in 1 until gridSize) {
                if (row % boxRows == 0) {
                    drawLine(
                        color = borderColor,
                        start = Offset(0f, cellHeight * row),
                        end = Offset(size.width, cellHeight * row),
                        strokeWidth = 2.5.dp.toPx()
                    )
                }
            }
        }
    }
}

@Composable
private fun NotesGrid(notes: Set<Int>, gridSize: Int) {
    val isDark = LocalIsDark.current || (MaterialTheme.colorScheme.background.luminance() < 0.5f)
    val noteColor = if (isDark) Color(0xFFD6D6D6) else Color(0xFF555555)

    val rows = if (gridSize == 4) 2 else if (gridSize == 6) 2 else 3
    val cols = if (gridSize == 4) 2 else if (gridSize == 6) 3 else 3
    val noteFontSize = if (gridSize == 4) 12.sp else if (gridSize == 6) 10.sp else 8.sp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(1.dp)
    ) {
        for (r in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                for (c in 0 until cols) {
                    val num = r * cols + c + 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (notes.contains(num)) {
                            Text(
                                text = num.toString(),
                                fontSize = noteFontSize,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.SemiBold,
                                color = noteColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
