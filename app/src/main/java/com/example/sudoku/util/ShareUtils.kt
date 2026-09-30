package com.example.sudoku.util

import android.content.Context
import android.content.Intent
import com.example.sudoku.data.SudokuPuzzle

object ShareUtils {

    fun formatScorecard(
        title: String,
        gridSize: Int,
        type: String,
        difficulty: String,
        isCompleted: Boolean,
        completionPercentage: Int,
        timerSeconds: Long
    ): String {
        val minutes = timerSeconds / 60
        val seconds = timerSeconds % 60
        val timeStr = String.format("%02d:%02d", minutes, seconds)
        val statusText = if (isCompleted) "Completed 🎉" else "$completionPercentage% Completed"

        val diffText = if (type == "GENERATED" && difficulty.isNotBlank() && difficulty != "NONE") {
            " | Difficulty: $difficulty"
        } else ""

        return """
            🧩 SUDORA Scorecard
            ─────────────────────────
            Title: $title
            Grid: ${gridSize}x${gridSize}$diffText
            Status: $statusText
            Time: $timeStr
            
            Play & solve Sudoku puzzles on SUDORA!
        """.trimIndent()
    }

    fun sharePuzzleScorecard(context: Context, puzzle: SudokuPuzzle) {
        val scorecard = formatScorecard(
            title = puzzle.title,
            gridSize = puzzle.gridSize,
            type = puzzle.type,
            difficulty = puzzle.difficulty,
            isCompleted = puzzle.isCompleted,
            completionPercentage = puzzle.getCompletionPercentage(),
            timerSeconds = puzzle.elapsedTime
        )

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, scorecard)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share SUDORA Scorecard")
        context.startActivity(shareIntent)
    }

    fun shareGenericScorecard(
        context: Context,
        title: String,
        gridSize: Int,
        difficulty: String,
        timerSeconds: Long
    ) {
        val scorecard = formatScorecard(
            title = title,
            gridSize = gridSize,
            type = "GENERATED",
            difficulty = difficulty,
            isCompleted = true,
            completionPercentage = 100,
            timerSeconds = timerSeconds
        )

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, scorecard)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share SUDORA Scorecard")
        context.startActivity(shareIntent)
    }
}
