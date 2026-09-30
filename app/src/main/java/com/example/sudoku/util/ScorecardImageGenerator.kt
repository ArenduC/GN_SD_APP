package com.example.sudoku.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import com.example.R
import java.io.File
import java.io.FileOutputStream

object ScorecardImageGenerator {

    fun shareScorecardImage(
        context: Context,
        gridSize: Int,
        currentGrid: String,
        lockedCells: String,
        timerSeconds: Long
    ) {
        val uri = generateScorecardUri(context, gridSize, currentGrid, lockedCells, timerSeconds)
        if (uri != null) {
            val minutes = timerSeconds / 60
            val seconds = timerSeconds % 60
            val timeStr = String.format("%02d:%02d mins", minutes, seconds)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "🧩 SUDORA ${gridSize}x${gridSize} Sudoku Score Card | Completed in $timeStr! #SUDORA")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Share SUDORA Score Card Image")
            context.startActivity(chooser)
        }
    }

    private fun generateScorecardUri(
        context: Context,
        gridSize: Int,
        currentGrid: String,
        lockedCells: String,
        timerSeconds: Long
    ): android.net.Uri? {
        try {
            val width = 1080
            val height = 1200
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Colors
            val bgCardColor = Color.parseColor("#1B1213")
            val circleBgColor = Color.parseColor("#281719")
            val circleBorderColor = Color.parseColor("#4A2528")
            val subtextColor = Color.parseColor("#D4BEC1")
            val gridContainerColor = Color.parseColor("#3F1F23")
            val givenBgColor = Color.parseColor("#BF5154")
            val userBgColor = Color.parseColor("#FFFFFF")
            val userTextColor = Color.parseColor("#BF5154")
            val emptyBgColor = Color.parseColor("#522B30")

            // Outer Card Background
            val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = bgCardColor
                style = Paint.Style.FILL
            }
            val cardRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
            canvas.drawRoundRect(cardRect, 70f, 70f, cardPaint)

            // Header Section
            // Circular Icon Background
            val circleCenterX = 130f
            val circleCenterY = 130f
            val circleRadius = 65f

            val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = circleBgColor
                style = Paint.Style.FILL
            }
            canvas.drawCircle(circleCenterX, circleCenterY, circleRadius, circlePaint)

            val circleStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = circleBorderColor
                style = Paint.Style.STROKE
                strokeWidth = 4f
            }
            canvas.drawCircle(circleCenterX, circleCenterY, circleRadius, circleStrokePaint)

            // Draw Logo Drawable inside Circle
            val logoDrawable = ContextCompat.getDrawable(context, R.drawable.ic_sudora_logo)
            if (logoDrawable != null) {
                val logoSize = 80
                val logoLeft = (circleCenterX - logoSize / 2).toInt()
                val logoTop = (circleCenterY - logoSize / 2).toInt()
                logoDrawable.setBounds(logoLeft, logoTop, logoLeft + logoSize, logoTop + logoSize)
                logoDrawable.draw(canvas)
            }

            // Title Text
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 54f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            canvas.drawText("SUDORA", 220f, 125f, titlePaint)

            val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = subtextColor
                textSize = 34f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }
            canvas.drawText("Score Card", 220f, 170f, subtitlePaint)

            // Right Info Text
            val rightGridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 54f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("${gridSize}x${gridSize}", width - 70f, 125f, rightGridPaint)

            val minutes = timerSeconds / 60
            val seconds = timerSeconds % 60
            val timeFormatted = String.format("%02d:%02d mins", minutes, seconds)

            val rightTimePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = subtextColor
                textSize = 34f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText(timeFormatted, width - 70f, 170f, rightTimePaint)

            // Grid Container
            val gridContainerLeft = 60f
            val gridContainerTop = 230f
            val gridContainerRight = width - 60f
            val gridContainerBottom = height - 60f

            val gridContainerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = gridContainerColor
                style = Paint.Style.FILL
            }
            val gridContainerRect = RectF(
                gridContainerLeft,
                gridContainerTop,
                gridContainerRight,
                gridContainerBottom
            )
            canvas.drawRoundRect(gridContainerRect, 50f, 50f, gridContainerPaint)

            // Draw Cells
            val padding = 36f
            val innerWidth = (gridContainerRight - gridContainerLeft) - (padding * 2)
            val innerHeight = (gridContainerBottom - gridContainerTop) - (padding * 2)

            val cellGap = 16f
            val totalGaps = cellGap * (gridSize - 1)
            val cellWidth = (innerWidth - totalGaps) / gridSize
            val cellHeight = (innerHeight - totalGaps) / gridSize

            val totalCells = gridSize * gridSize
            val currentGridStr = currentGrid.padEnd(totalCells, '0')
            val lockedCellsStr = lockedCells.padEnd(totalCells, '0')

            val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
            }

            val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textSize = when (gridSize) {
                    4 -> 56f
                    6 -> 46f
                    else -> 38f
                }
            }

            for (row in 0 until gridSize) {
                for (col in 0 until gridSize) {
                    val idx = row * gridSize + col
                    val digitChar = currentGridStr.getOrElse(idx) { '0' }
                    val isGiven = lockedCellsStr.getOrElse(idx) { '0' } == '1'
                    val isDigit = digitChar in '1'..'9'

                    val cellLeft = gridContainerLeft + padding + col * (cellWidth + cellGap)
                    val cellTop = gridContainerTop + padding + row * (cellHeight + cellGap)
                    val cellRect = RectF(cellLeft, cellTop, cellLeft + cellWidth, cellTop + cellHeight)

                    cellPaint.color = when {
                        isGiven -> givenBgColor
                        isDigit -> userBgColor
                        else -> emptyBgColor
                    }
                    canvas.drawRoundRect(cellRect, 20f, 20f, cellPaint)

                    if (isDigit) {
                        numPaint.color = if (isGiven) Color.WHITE else userTextColor
                        val fontMetrics = numPaint.fontMetrics
                        val textY = cellRect.centerY() - (fontMetrics.descent + fontMetrics.ascent) / 2
                        canvas.drawText(digitChar.toString(), cellRect.centerX(), textY, numPaint)
                    }
                }
            }

            // Save Bitmap to Cache
            val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
            val imageFile = File(imagesDir, "sudora_scorecard.png")
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
