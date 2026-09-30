package com.example.sudoku.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NumberPad(
    onNumberClick: (Int) -> Unit,
    onEraseClick: () -> Unit,
    modifier: Modifier = Modifier,
    gridSize: Int = 9
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        when (gridSize) {
            4 -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (num in 1..4) {
                        NumberButton(
                            number = num,
                            onClick = { onNumberClick(num) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    EraseButton(
                        onClick = onEraseClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            6 -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (num in 1..6) {
                        NumberButton(
                            number = num,
                            onClick = { onNumberClick(num) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    EraseButton(
                        onClick = onEraseClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            else -> {
                // 9x9 Classic Layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (num in 1..5) {
                        NumberButton(
                            number = num,
                            onClick = { onNumberClick(num) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (num in 6..9) {
                        NumberButton(
                            number = num,
                            onClick = { onNumberClick(num) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Erase button
                    EraseButton(
                        onClick = onEraseClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun NumberButton(
    number: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(0.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
            .height(50.dp)
            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(0.dp))
            .testTag("num_pad_$number")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = number.toString(),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

@Composable
private fun EraseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(0.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .height(50.dp)
            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(0.dp))
            .testTag("num_pad_erase")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Erase cell entry"
            )
        }
    }
}
