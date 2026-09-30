package com.example.sudoku.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SudokuDao {
    @Query("SELECT * FROM sudoku_puzzles ORDER BY updatedAt DESC")
    fun getAllPuzzles(): Flow<List<SudokuPuzzle>>

    @Query("SELECT * FROM sudoku_puzzles WHERE id = :id")
    suspend fun getPuzzleById(id: String): SudokuPuzzle?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPuzzle(puzzle: SudokuPuzzle)

    @Query("DELETE FROM sudoku_puzzles WHERE id = :id")
    suspend fun deletePuzzleById(id: String)

    @Query("UPDATE sudoku_puzzles SET title = :newTitle, updatedAt = :updatedAt WHERE id = :id")
    suspend fun renamePuzzle(id: String, newTitle: String, updatedAt: Long = System.currentTimeMillis())
}
