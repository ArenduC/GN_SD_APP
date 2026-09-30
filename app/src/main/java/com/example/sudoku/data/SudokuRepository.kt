package com.example.sudoku.data

import com.example.sudoku.engine.PuzzleDifficulty
import com.example.sudoku.engine.PuzzleType
import com.example.sudoku.engine.SudokuGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SudokuRepository(private val dao: SudokuDao) {

    val allPuzzles: Flow<List<SudokuPuzzle>> = dao.getAllPuzzles()

    suspend fun getPuzzle(id: String): SudokuPuzzle? = withContext(Dispatchers.IO) {
        dao.getPuzzleById(id)
    }

    suspend fun savePuzzle(puzzle: SudokuPuzzle) = withContext(Dispatchers.IO) {
        dao.insertPuzzle(puzzle.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deletePuzzle(id: String) = withContext(Dispatchers.IO) {
        dao.deletePuzzleById(id)
    }

    suspend fun renamePuzzle(id: String, newTitle: String) = withContext(Dispatchers.IO) {
        dao.renamePuzzle(id, newTitle)
    }

    suspend fun createGeneratedPuzzle(difficulty: PuzzleDifficulty, gridSize: Int = 9): SudokuPuzzle = withContext(Dispatchers.IO) {
        val totalCells = gridSize * gridSize
        val generated = SudokuGenerator.generate(difficulty, gridSize)
        val initialStr = SudokuPuzzle.intArrayToString(generated.initialGrid)
        val solutionStr = SudokuPuzzle.intArrayToString(generated.solutionGrid)

        val lockedBuilder = StringBuilder(totalCells)
        for (i in 0 until totalCells) {
            lockedBuilder.append(if (initialStr[i] != '0') '1' else '0')
        }

        val count = System.currentTimeMillis() % 10000
        val diffName = difficulty.name.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }
        val gridTag = "${gridSize}x${gridSize}"
        val title = "$gridTag $diffName Sudoku #$count"

        val puzzle = SudokuPuzzle(
            id = UUID.randomUUID().toString(),
            title = title,
            type = PuzzleType.GENERATED.name,
            difficulty = difficulty.name,
            gridSize = gridSize,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            initialGrid = initialStr,
            currentGrid = initialStr,
            lockedCells = lockedBuilder.toString(),
            solutionGrid = solutionStr,
            notesJson = "",
            elapsedTime = 0L,
            isCompleted = false,
            isLocked = true,
            mistakesCount = 0
        )
        dao.insertPuzzle(puzzle)
        puzzle
    }

    suspend fun createCustomPuzzle(gridSize: Int = 9): SudokuPuzzle = withContext(Dispatchers.IO) {
        val dateStr = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date())
        val title = "Custom ${gridSize}x${gridSize} ($dateStr)"
        val emptyGrid = SudokuPuzzle.createEmptyGrid(gridSize)

        val puzzle = SudokuPuzzle(
            id = UUID.randomUUID().toString(),
            title = title,
            type = PuzzleType.CUSTOM.name,
            difficulty = PuzzleDifficulty.NONE.name,
            gridSize = gridSize,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            initialGrid = emptyGrid,
            currentGrid = emptyGrid,
            lockedCells = SudokuPuzzle.createEmptyLocked(gridSize),
            solutionGrid = "",
            notesJson = "",
            elapsedTime = 0L,
            isCompleted = false,
            isLocked = false, // Not locked yet! User can enter numbers and then lock.
            mistakesCount = 0
        )
        dao.insertPuzzle(puzzle)
        puzzle
    }

    suspend fun duplicatePuzzle(puzzleId: String): SudokuPuzzle? = withContext(Dispatchers.IO) {
        val existing = dao.getPuzzleById(puzzleId) ?: return@withContext null
        val newTitle = "${existing.title} (Copy)"
        val duplicate = existing.copy(
            id = UUID.randomUUID().toString(),
            title = newTitle,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        dao.insertPuzzle(duplicate)
        duplicate
    }
}
