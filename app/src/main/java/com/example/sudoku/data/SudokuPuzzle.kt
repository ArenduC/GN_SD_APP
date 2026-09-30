package com.example.sudoku.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "sudoku_puzzles")
data class SudokuPuzzle(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: String, // "GENERATED" or "CUSTOM"
    val difficulty: String, // "EASY", "MEDIUM", "HARD", "NONE"
    val gridSize: Int = 9, // 4, 6, or 9
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val initialGrid: String, // N*N digits ("0" for empty)
    val currentGrid: String, // N*N digits
    val lockedCells: String, // N*N chars ('1' for locked given, '0' for editable)
    val solutionGrid: String = "", // N*N digits
    val notesJson: String = "", // Delimited notes string
    val elapsedTime: Long = 0L, // in seconds
    val isCompleted: Boolean = false,
    val isLocked: Boolean = false, // Locked custom entry
    val mistakesCount: Int = 0,
    val historyJson: String = "" // Move history for undo/redo state
) {
    // Helper to calculate completion percentage
    fun getCompletionPercentage(): Int {
        if (isCompleted) return 100
        var filledCount = 0
        var editableTotal = 0
        val totalCells = gridSize * gridSize
        for (i in 0 until totalCells) {
            val isGiven = lockedCells.getOrNull(i) == '1'
            if (!isGiven) {
                editableTotal++
                val ch = currentGrid.getOrNull(i)
                if (ch != null && ch in '1'..'9') {
                    filledCount++
                }
            }
        }
        if (editableTotal == 0) return 0
        return ((filledCount.toDouble() / editableTotal.toDouble()) * 100).toInt().coerceIn(0, 100)
    }

    companion object {
        fun createEmptyGrid(gridSize: Int = 9): String = "0".repeat(gridSize * gridSize)
        fun createEmptyLocked(gridSize: Int = 9): String = "0".repeat(gridSize * gridSize)

        fun intArrayToString(array: IntArray): String = array.joinToString("") { it.toString() }
        fun stringToIntArray(str: String, gridSize: Int = 9): IntArray {
            val totalCells = gridSize * gridSize
            val arr = IntArray(totalCells)
            for (i in 0 until totalCells) {
                arr[i] = if (i < str.length) str[i].digitToIntOrNull() ?: 0 else 0
            }
            return arr
        }
    }
}
