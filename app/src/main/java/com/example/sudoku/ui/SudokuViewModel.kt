package com.example.sudoku.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudoku.data.AppDatabase
import com.example.sudoku.data.SudokuPuzzle
import com.example.sudoku.data.SudokuRepository
import com.example.sudoku.engine.PuzzleDifficulty
import com.example.sudoku.engine.PuzzleType
import com.example.sudoku.engine.SudokuSolver
import com.example.sudoku.ui.components.ValidationResult
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MoveAction(
    val cellIndex: Int,
    val prevNum: Int,
    val newNum: Int,
    val prevNotes: Set<Int>,
    val newNotes: Set<Int>
)

class SudokuViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SudokuRepository
    val allPuzzles: StateFlow<List<SudokuPuzzle>>

    private val _activePuzzle = MutableStateFlow<SudokuPuzzle?>(null)
    val activePuzzle: StateFlow<SudokuPuzzle?> = _activePuzzle.asStateFlow()

    private val _selectedCellIndex = MutableStateFlow<Int?>(null)
    val selectedCellIndex: StateFlow<Int?> = _selectedCellIndex.asStateFlow()

    private val _currentGridState = MutableStateFlow<IntArray>(IntArray(81))
    val currentGridState: StateFlow<IntArray> = _currentGridState.asStateFlow()

    private val _notesMap = MutableStateFlow<Map<Int, Set<Int>>>(emptyMap())
    val notesMap: StateFlow<Map<Int, Set<Int>>> = _notesMap.asStateFlow()

    private val _isNotesMode = MutableStateFlow(false)
    val isNotesMode: StateFlow<Boolean> = _isNotesMode.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private val _showCompletionDialog = MutableStateFlow(false)
    val showCompletionDialog: StateFlow<Boolean> = _showCompletionDialog.asStateFlow()

    private val _validationResult = MutableStateFlow<ValidationResult>(ValidationResult.NONE)
    val validationResult: StateFlow<ValidationResult> = _validationResult.asStateFlow()

    private val _userGridBeforeReveal = MutableStateFlow<IntArray?>(null)
    val userGridBeforeReveal: StateFlow<IntArray?> = _userGridBeforeReveal.asStateFlow()

    private val _themeMode = MutableStateFlow<ThemeMode>(ThemeMode.DARK)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun toggleThemeMode() {
        _themeMode.value = when (_themeMode.value) {
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.SYSTEM
            ThemeMode.SYSTEM -> ThemeMode.DARK
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    private val undoStack = mutableListOf<MoveAction>()
    private val redoStack = mutableListOf<MoveAction>()

    private var timerJob: Job? = null

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SudokuRepository(database.sudokuDao())
        allPuzzles = repository.allPuzzles.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun startNewGeneratedPuzzle(difficulty: PuzzleDifficulty, gridSize: Int = 9, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val puzzle = repository.createGeneratedPuzzle(difficulty, gridSize)
            loadPuzzle(puzzle.id)
            onCreated(puzzle.id)
        }
    }

    fun createCustomPuzzle(gridSize: Int = 9, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val puzzle = repository.createCustomPuzzle(gridSize)
            loadPuzzle(puzzle.id)
            onCreated(puzzle.id)
        }
    }

    fun loadPuzzle(puzzleId: String) {
        viewModelScope.launch {
            stopTimer()
            val puzzle = repository.getPuzzle(puzzleId) ?: return@launch
            _activePuzzle.value = puzzle
            _currentGridState.value = SudokuPuzzle.stringToIntArray(puzzle.currentGrid, puzzle.gridSize)
            _notesMap.value = deserializeNotes(puzzle.notesJson, puzzle.gridSize)
            _elapsedTime.value = puzzle.elapsedTime
            _isPaused.value = false
            _selectedCellIndex.value = null
            _showCompletionDialog.value = false
            _validationResult.value = ValidationResult.NONE
            _userGridBeforeReveal.value = null
            undoStack.clear()
            redoStack.clear()
            updateUndoRedoStates()

            if (puzzle.isLocked && !puzzle.isCompleted) {
                startTimer()
            }
        }
    }

    fun selectCell(index: Int) {
        if (_isPaused.value) return
        _selectedCellIndex.value = index
    }

    fun toggleNotesMode() {
        _isNotesMode.value = !_isNotesMode.value
    }

    fun togglePauseTimer() {
        if (_isPaused.value) {
            _isPaused.value = false
            startTimer()
        } else {
            _isPaused.value = true
            stopTimer()
        }
    }

    fun inputNumber(number: Int) {
        val cellIdx = _selectedCellIndex.value ?: return
        val puzzle = _activePuzzle.value ?: return
        if (_isPaused.value) return

        // Check if cell is fixed/locked given
        if (puzzle.isLocked && puzzle.lockedCells.getOrNull(cellIdx) == '1') {
            return
        }

        val grid = _currentGridState.value.copyOf()
        val currentNotes = _notesMap.value.toMutableMap()
        val prevNum = grid[cellIdx]
        val prevCellNotes = currentNotes[cellIdx] ?: emptySet()

        if (!_isNotesMode.value) {
            // Normal number input
            val newNum = if (prevNum == number) 0 else number
            if (prevNum == newNum) return

            grid[cellIdx] = newNum
            // Clear notes for this cell when entering a number
            val newCellNotes = emptySet<Int>()
            if (newCellNotes.isEmpty()) {
                currentNotes.remove(cellIdx)
            } else {
                currentNotes[cellIdx] = newCellNotes
            }

            // Record undo action
            val action = MoveAction(cellIdx, prevNum, newNum, prevCellNotes, newCellNotes)
            undoStack.add(action)
            redoStack.clear()
            updateUndoRedoStates()

            _currentGridState.value = grid
            _notesMap.value = currentNotes

            checkCompletion(grid, puzzle)
            autoSave()
        } else {
            // Notes mode input
            if (grid[cellIdx] != 0) return // Cannot add notes to filled cell

            val existingNotes = (currentNotes[cellIdx] ?: emptySet()).toMutableSet()
            if (existingNotes.contains(number)) {
                existingNotes.remove(number)
            } else {
                existingNotes.add(number)
            }

            if (existingNotes.isEmpty()) {
                currentNotes.remove(cellIdx)
            } else {
                currentNotes[cellIdx] = existingNotes
            }

            val action = MoveAction(cellIdx, prevNum, prevNum, prevCellNotes, existingNotes)
            undoStack.add(action)
            redoStack.clear()
            updateUndoRedoStates()

            _notesMap.value = currentNotes
            autoSave()
        }
    }

    fun eraseCell() {
        val cellIdx = _selectedCellIndex.value ?: return
        val puzzle = _activePuzzle.value ?: return
        if (_isPaused.value) return

        if (puzzle.isLocked && puzzle.lockedCells.getOrNull(cellIdx) == '1') {
            return
        }

        val grid = _currentGridState.value.copyOf()
        val currentNotes = _notesMap.value.toMutableMap()
        val prevNum = grid[cellIdx]
        val prevCellNotes = currentNotes[cellIdx] ?: emptySet()

        if (prevNum == 0 && prevCellNotes.isEmpty()) return

        grid[cellIdx] = 0
        currentNotes.remove(cellIdx)

        val action = MoveAction(cellIdx, prevNum, 0, prevCellNotes, emptySet())
        undoStack.add(action)
        redoStack.clear()
        updateUndoRedoStates()

        _currentGridState.value = grid
        _notesMap.value = currentNotes
        autoSave()
    }

    fun undo() {
        if (undoStack.isEmpty() || _isPaused.value) return
        val action = undoStack.removeAt(undoStack.size - 1)
        redoStack.add(action)
        updateUndoRedoStates()

        val grid = _currentGridState.value.copyOf()
        grid[action.cellIndex] = action.prevNum
        _currentGridState.value = grid

        val notes = _notesMap.value.toMutableMap()
        if (action.prevNotes.isEmpty()) {
            notes.remove(action.cellIndex)
        } else {
            notes[action.cellIndex] = action.prevNotes
        }
        _notesMap.value = notes

        autoSave()
    }

    fun redo() {
        if (redoStack.isEmpty() || _isPaused.value) return
        val action = redoStack.removeAt(redoStack.size - 1)
        undoStack.add(action)
        updateUndoRedoStates()

        val grid = _currentGridState.value.copyOf()
        grid[action.cellIndex] = action.newNum
        _currentGridState.value = grid

        val notes = _notesMap.value.toMutableMap()
        if (action.newNotes.isEmpty()) {
            notes.remove(action.cellIndex)
        } else {
            notes[action.cellIndex] = action.newNotes
        }
        _notesMap.value = notes

        val puzzle = _activePuzzle.value
        if (puzzle != null) {
            checkCompletion(grid, puzzle)
        }
        autoSave()
    }

    fun lockCustomPuzzle(onLocked: () -> Unit) {
        val puzzle = _activePuzzle.value ?: return
        if (puzzle.isLocked) return

        val gridSize = puzzle.gridSize
        val totalCells = gridSize * gridSize
        val grid = _currentGridState.value
        val lockedBuilder = StringBuilder(totalCells)
        for (i in 0 until totalCells) {
            lockedBuilder.append(if (grid[i] != 0) '1' else '0')
        }

        // Solve in background to verify solution
        val solved = SudokuSolver.solve(grid, gridSize)
        val solutionStr = if (solved != null) SudokuPuzzle.intArrayToString(solved) else ""

        val updatedPuzzle = puzzle.copy(
            isLocked = true,
            lockedCells = lockedBuilder.toString(),
            initialGrid = SudokuPuzzle.intArrayToString(grid),
            solutionGrid = solutionStr,
            updatedAt = System.currentTimeMillis()
        )

        _activePuzzle.value = updatedPuzzle
        viewModelScope.launch {
            repository.savePuzzle(updatedPuzzle)
            startTimer()
            onLocked()
        }
    }

    fun renamePuzzle(puzzleId: String, newTitle: String) {
        viewModelScope.launch {
            repository.renamePuzzle(puzzleId, newTitle.trim())
            if (_activePuzzle.value?.id == puzzleId) {
                _activePuzzle.value = _activePuzzle.value?.copy(title = newTitle.trim())
            }
        }
    }

    fun duplicatePuzzle(puzzleId: String, onDuplicated: (String) -> Unit) {
        viewModelScope.launch {
            val dup = repository.duplicatePuzzle(puzzleId)
            if (dup != null) {
                onDuplicated(dup.id)
            }
        }
    }

    fun deletePuzzle(puzzleId: String) {
        viewModelScope.launch {
            repository.deletePuzzle(puzzleId)
            if (_activePuzzle.value?.id == puzzleId) {
                stopTimer()
                _activePuzzle.value = null
            }
        }
    }

    fun dismissCompletionDialog() {
        _showCompletionDialog.value = false
    }

    fun dismissValidationDialog() {
        _validationResult.value = ValidationResult.NONE
    }

    fun revealSolution() {
        val puzzle = _activePuzzle.value ?: return
        val gridSize = puzzle.gridSize

        if (_userGridBeforeReveal.value == null) {
            _userGridBeforeReveal.value = _currentGridState.value.copyOf()
        }

        val solutionArray = if (puzzle.solutionGrid.isNotBlank()) {
            SudokuPuzzle.stringToIntArray(puzzle.solutionGrid, gridSize)
        } else {
            val initArray = SudokuPuzzle.stringToIntArray(puzzle.initialGrid, gridSize)
            SudokuSolver.solve(initArray, gridSize)
        }

        if (solutionArray != null) {
            _currentGridState.value = solutionArray
            _notesMap.value = emptyMap()
            stopTimer()
            val completedPuzzle = puzzle.copy(
                isCompleted = true,
                currentGrid = SudokuPuzzle.intArrayToString(solutionArray),
                solutionGrid = SudokuPuzzle.intArrayToString(solutionArray),
                updatedAt = System.currentTimeMillis()
            )
            _activePuzzle.value = completedPuzzle
            viewModelScope.launch {
                repository.savePuzzle(completedPuzzle)
            }
        }
        _validationResult.value = ValidationResult.NONE
    }

    private fun checkCompletion(grid: IntArray, puzzle: SudokuPuzzle) {
        val gridSize = puzzle.gridSize
        // When progress reaches 100% (no 0s in grid)
        if (!grid.contains(0)) {
            val totalCells = gridSize * gridSize
            val initArray = SudokuPuzzle.stringToIntArray(puzzle.initialGrid, gridSize)
            var matchesInitialGivens = true
            for (i in 0 until minOf(totalCells, puzzle.lockedCells.length)) {
                if (puzzle.lockedCells[i] == '1' && grid[i] != initArray[i]) {
                    matchesInitialGivens = false
                    break
                }
            }

            val isBoardValid = SudokuSolver.isValidBoard(grid, gridSize)

            if (isBoardValid && matchesInitialGivens) {
                stopTimer()
                val completedPuzzle = puzzle.copy(
                    isCompleted = true,
                    currentGrid = SudokuPuzzle.intArrayToString(grid),
                    elapsedTime = _elapsedTime.value,
                    updatedAt = System.currentTimeMillis()
                )
                _activePuzzle.value = completedPuzzle
                _validationResult.value = ValidationResult.CORRECT
                viewModelScope.launch {
                    repository.savePuzzle(completedPuzzle)
                }
            } else {
                _validationResult.value = ValidationResult.INCORRECT
            }
        }
    }

    private fun autoSave() {
        val puzzle = _activePuzzle.value ?: return
        val currentGridStr = SudokuPuzzle.intArrayToString(_currentGridState.value)
        val notesStr = serializeNotes(_notesMap.value, puzzle.gridSize)

        val updated = puzzle.copy(
            currentGrid = currentGridStr,
            notesJson = notesStr,
            elapsedTime = _elapsedTime.value,
            updatedAt = System.currentTimeMillis()
        )
        _activePuzzle.value = updated
        viewModelScope.launch {
            repository.savePuzzle(updated)
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                if (!_isPaused.value && _activePuzzle.value?.isCompleted == false) {
                    _elapsedTime.value += 1
                    if (_elapsedTime.value % 5 == 0L) {
                        autoSave()
                    }
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun updateUndoRedoStates() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

    private fun serializeNotes(notes: Map<Int, Set<Int>>, gridSize: Int = 9): String {
        val totalCells = gridSize * gridSize
        return (0 until totalCells).joinToString(";") { i ->
            notes[i]?.sorted()?.joinToString(",") ?: ""
        }
    }

    private fun deserializeNotes(notesStr: String, gridSize: Int = 9): Map<Int, Set<Int>> {
        val totalCells = gridSize * gridSize
        val map = mutableMapOf<Int, Set<Int>>()
        if (notesStr.isBlank()) return map
        val parts = notesStr.split(";")
        for (i in 0 until minOf(totalCells, parts.size)) {
            val str = parts[i]
            if (str.isNotBlank()) {
                val set = str.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
                if (set.isNotEmpty()) {
                    map[i] = set
                }
            }
        }
        return map
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
