package com.example.sudoku.engine

import kotlin.random.Random

enum class PuzzleType {
    GENERATED, CUSTOM
}

enum class PuzzleDifficulty {
    EASY, MEDIUM, HARD, NONE
}

data class GeneratedPuzzle(
    val initialGrid: IntArray,
    val solutionGrid: IntArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as GeneratedPuzzle
        return initialGrid.contentEquals(other.initialGrid) && solutionGrid.contentEquals(other.solutionGrid)
    }

    override fun hashCode(): Int {
        var result = initialGrid.contentHashCode()
        result = 31 * result + solutionGrid.contentHashCode()
        return result
    }
}

object SudokuSolver {

    fun getBoxRows(gridSize: Int): Int = when (gridSize) {
        4 -> 2
        6 -> 2
        else -> 3
    }

    fun getBoxCols(gridSize: Int): Int = when (gridSize) {
        4 -> 2
        6 -> 3
        else -> 3
    }

    fun solve(grid: IntArray, gridSize: Int = 9): IntArray? {
        val board = grid.copyOf()
        return if (solveBacktrack(board, gridSize)) board else null
    }

    private fun solveBacktrack(board: IntArray, gridSize: Int): Boolean {
        val emptyIndex = board.indexOf(0)
        if (emptyIndex == -1) return true

        val row = emptyIndex / gridSize
        val col = emptyIndex % gridSize
        for (num in 1..gridSize) {
            if (isValidMove(board, row, col, num, gridSize)) {
                board[emptyIndex] = num
                if (solveBacktrack(board, gridSize)) return true
                board[emptyIndex] = 0
            }
        }
        return false
    }

    fun countSolutions(grid: IntArray, gridSize: Int = 9, limit: Int = 2): Int {
        val board = grid.copyOf()
        return countBacktrack(board, 0, limit, gridSize)
    }

    private fun countBacktrack(board: IntArray, count: Int, limit: Int, gridSize: Int): Int {
        var currentCount = count
        if (currentCount >= limit) return currentCount

        val emptyIndex = board.indexOf(0)
        if (emptyIndex == -1) {
            return currentCount + 1
        }

        val row = emptyIndex / gridSize
        val col = emptyIndex % gridSize

        for (num in 1..gridSize) {
            if (isValidMove(board, row, col, num, gridSize)) {
                board[emptyIndex] = num
                currentCount = countBacktrack(board, currentCount, limit, gridSize)
                board[emptyIndex] = 0
                if (currentCount >= limit) break
            }
        }
        return currentCount
    }

    fun isValidMove(board: IntArray, row: Int, col: Int, num: Int, gridSize: Int): Boolean {
        val boxRows = getBoxRows(gridSize)
        val boxCols = getBoxCols(gridSize)

        for (i in 0 until gridSize) {
            if (i != col && board[row * gridSize + i] == num) return false
            if (i != row && board[i * gridSize + col] == num) return false
        }
        val startRow = (row / boxRows) * boxRows
        val startCol = (col / boxCols) * boxCols
        for (r in startRow until startRow + boxRows) {
            for (c in startCol until startCol + boxCols) {
                if ((r != row || c != col) && board[r * gridSize + c] == num) {
                    return false
                }
            }
        }
        return true
    }

    fun isValidBoard(board: IntArray, gridSize: Int): Boolean {
        val totalCells = gridSize * gridSize
        if (board.size != totalCells) return false
        for (i in 0 until totalCells) {
            val num = board[i]
            if (num in 1..gridSize) {
                val row = i / gridSize
                val col = i % gridSize

                val boxRows = getBoxRows(gridSize)
                val boxCols = getBoxCols(gridSize)

                for (c in 0 until gridSize) {
                    if (c != col && board[row * gridSize + c] == num) return false
                }
                for (r in 0 until gridSize) {
                    if (r != row && board[r * gridSize + col] == num) return false
                }
                val startRow = (row / boxRows) * boxRows
                val startCol = (col / boxCols) * boxCols
                for (r in startRow until startRow + boxRows) {
                    for (c in startCol until startCol + boxCols) {
                        if ((r != row || c != col) && board[r * gridSize + c] == num) return false
                    }
                }
            } else {
                return false
            }
        }
        return true
    }
}

object SudokuGenerator {

    fun generate(difficulty: PuzzleDifficulty, gridSize: Int = 9): GeneratedPuzzle {
        val totalCells = gridSize * gridSize
        val fullGrid = IntArray(totalCells)
        fillRandomized(fullGrid, gridSize)
        val solutionGrid = fullGrid.copyOf()

        val puzzleGrid = fullGrid.copyOf()
        val targetEmptyCells = when (gridSize) {
            4 -> when (difficulty) {
                PuzzleDifficulty.EASY -> Random.nextInt(4, 7)
                PuzzleDifficulty.MEDIUM -> Random.nextInt(7, 9)
                PuzzleDifficulty.HARD -> Random.nextInt(9, 11)
                PuzzleDifficulty.NONE -> 0
            }
            6 -> when (difficulty) {
                PuzzleDifficulty.EASY -> Random.nextInt(12, 16)
                PuzzleDifficulty.MEDIUM -> Random.nextInt(16, 20)
                PuzzleDifficulty.HARD -> Random.nextInt(20, 24)
                PuzzleDifficulty.NONE -> 0
            }
            else -> when (difficulty) {
                PuzzleDifficulty.EASY -> Random.nextInt(36, 41)
                PuzzleDifficulty.MEDIUM -> Random.nextInt(44, 49)
                PuzzleDifficulty.HARD -> Random.nextInt(50, 55)
                PuzzleDifficulty.NONE -> 0
            }
        }

        val indices = (0 until totalCells).shuffled(Random).toMutableList()
        var emptyCount = 0

        for (idx in indices) {
            if (emptyCount >= targetEmptyCells) break
            val temp = puzzleGrid[idx]
            puzzleGrid[idx] = 0

            if (SudokuSolver.countSolutions(puzzleGrid, gridSize, limit = 2) == 1) {
                emptyCount++
            } else {
                puzzleGrid[idx] = temp
            }
        }

        return GeneratedPuzzle(
            initialGrid = puzzleGrid,
            solutionGrid = solutionGrid
        )
    }

    private fun fillRandomized(board: IntArray, gridSize: Int): Boolean {
        val emptyIndex = board.indexOf(0)
        if (emptyIndex == -1) return true

        val row = emptyIndex / gridSize
        val col = emptyIndex % gridSize
        val numbers = (1..gridSize).shuffled(Random)

        for (num in numbers) {
            if (SudokuSolver.isValidMove(board, row, col, num, gridSize)) {
                board[emptyIndex] = num
                if (fillRandomized(board, gridSize)) return true
                board[emptyIndex] = 0
            }
        }
        return false
    }
}
