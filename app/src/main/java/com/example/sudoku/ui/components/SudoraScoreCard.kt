package com.example.sudoku.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.UbuntuFontFamily

val CardBackground = Color(0xFF1B1213)
val HeaderCircleBg = Color(0xFF281719)
val HeaderCircleBorder = Color(0xFF4A2528)
val SubtextColor = Color(0xFFD4BEC1)
val GridContainerBg = Color(0xFF3F1F23)
val GivenCellBg = Color(0xFFBF5154)
val UserCellBg = Color(0xFFFFFFFF)
val UserCellText = Color(0xFFBF5154)
val EmptyCellBg = Color(0xFF522B30)

@Composable
fun SudoraScoreCard(
    gridSize: Int,
    currentGrid: String,
    lockedCells: String,
    timerSeconds: Long,
    modifier: Modifier = Modifier
) {
    val minutes = timerSeconds / 60
    val seconds = timerSeconds % 60
    val timeFormatted = String.format("%02d:%02d mins", minutes, seconds)

    val totalCells = gridSize * gridSize
    val currentGridStr = currentGrid.padEnd(totalCells, '0')
    val lockedCellsStr = lockedCells.padEnd(totalCells, '0')

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(CardBackground)
            .padding(20.dp)
            .testTag("sudora_score_card")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Circle Icon + App Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(HeaderCircleBg)
                            .border(1.5.dp, HeaderCircleBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_sudora_logo),
                            contentDescription = "SUDORA Logo",
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "SUDORA",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = UbuntuFontFamily,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Score Card",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = UbuntuFontFamily,
                            color = SubtextColor
                        )
                    }
                }

                // Right: Grid size + Time
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${gridSize}x${gridSize}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = UbuntuFontFamily,
                        color = Color.White
                    )
                    Text(
                        text = timeFormatted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = UbuntuFontFamily,
                        color = SubtextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Grid Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(GridContainerBg)
                    .padding(12.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (row in 0 until gridSize) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (col in 0 until gridSize) {
                                val idx = row * gridSize + col
                                val digitChar = currentGridStr.getOrElse(idx) { '0' }
                                val isGiven = lockedCellsStr.getOrElse(idx) { '0' } == '1'
                                val isDigit = digitChar in '1'..'9'

                                val cellBg = when {
                                    isGiven -> GivenCellBg
                                    isDigit -> UserCellBg
                                    else -> EmptyCellBg
                                }

                                val textColor = when {
                                    isGiven -> Color.White
                                    isDigit -> UserCellText
                                    else -> Color.Transparent
                                }

                                val fontSize = when (gridSize) {
                                    4 -> 20.sp
                                    6 -> 17.sp
                                    else -> 14.sp
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(cellBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isDigit) {
                                        Text(
                                            text = digitChar.toString(),
                                            fontSize = fontSize,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = UbuntuFontFamily,
                                            color = textColor,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
