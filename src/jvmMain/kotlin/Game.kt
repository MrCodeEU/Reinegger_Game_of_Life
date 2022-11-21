package game.v1
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.math.max
import kotlin.math.min

enum class GameState {
    PAUSED, RUNNING;
}

enum class CellState {
    ALIVE, DEAD;

    operator fun not() = if (this == DEAD) ALIVE else DEAD
}

class Cell(initState: CellState) {
    var state by mutableStateOf(initState)
    var neighbors = 0
        set(n) {
            field = min(max(n, 0), 8)
        }

    operator fun not() {
        state = !state
    }

    infix fun setCellState(state: CellState) {
        this.state = state
    }

    fun isAlive() = state == CellState.ALIVE
    fun isDead() = !isAlive()

    constructor(initState: CellState, initNeighbor: Int) : this(initState) {
        neighbors = initNeighbor
    }
}

operator fun Pair<Int, Int>.plus(o: Pair<Int, Int>): Pair<Int, Int> = Pair(first + o.first, second + o.second)

data class Game(val width: Int, val height: Int) {
    // Initialize board with dead cells
    private val board = Array(width) { Array(height) { Cell(CellState.DEAD) } }
    private var timer = Timer()
    private var speed = 300f

    // calculate next board content
    private fun step() {
        // Create a new array with new cells to be able to modify the board without having artifacts
        val prevBoard = Array(width) { w -> Array(height) { h -> Cell(board[w][h].state, board[w][h].neighbors) } }
        for (h in 1..board.size) {
            for (w in 1..board[h - 1].size) {
                val i = Pair(h - 1, w - 1)
                val c = get(i)
                val p = prevBoard[i.first][i.second]
                /*
                    1. Any live cell with fewer than two live neighbours dies, as if by underpopulation.
                    2. Any live cell with two or three live neighbours lives on to the next generation. (No need to implement)
                    3. Any live cell with more than three live neighbours dies, as if by overpopulation.
                    4. Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.
                 */
                if (p.isAlive() && p.neighbors < 2) set(i, !c.state)
                else if (p.isAlive() && p.neighbors > 3) set(i, !c.state)
                else if (p.isDead() && p.neighbors == 3) set(i, !c.state)
            }
        }
    }

    fun startGame() {
        timer = Timer()
        timer.scheduleAtFixedRate(timerTask {
            step()
        }, 0, max(10, speed.toLong()))
    }

    fun stopGame() {
        timer.cancel()
        timer.purge()
    }

    fun changeSpeed(f: Float, gameState: GameState) {
        speed = f
        if (gameState == GameState.RUNNING) {
            stopGame()
            startGame()
        }
    }

    private infix fun updateNeighbors(i: Pair<Int, Int>) {
        val offset = listOf(
            Pair(1, 1),
            Pair(-1, -1),
            Pair(-1, 1),
            Pair(1, -1),
            Pair(0, 1),
            Pair(1, 0),
            Pair(0, -1),
            Pair(-1, 0)
        )
        if (get(i).isAlive()) for (o in offset) (this get (i + o)).neighbors++
        else for (o in offset) (this get (i + o)).neighbors--
    }

    // Clamp function to make sure when checking the borders
    // we do not get an Index out of Bounds Error
    infix fun get(i: Pair<Int, Int>): Cell {
        val p = i.clamp(0, width - 1, height - 1)
        if (p != i) return Cell(CellState.DEAD)
        return board[p.first][p.second]
    }

    fun set(i: Pair<Int, Int>, state: CellState) {
        val p = i.clamp(0, width - 1, height - 1)
        board[p.first][p.second].state = state
        updateNeighbors(p)
    }

    private fun Pair<Int, Int>.clamp(min: Int, maxW: Int, maxH: Int) =
        Pair(max(min(first, maxW), min), max(min(second, maxH), min))

    fun resetGame() {
        for (h in 1..board.size) {
            for (w in 1..board[h - 1].size) {
                val i = Pair(h - 1, w - 1)
                set(i, CellState.DEAD)
            }
        }
        stopGame()
    }

    fun random(gameState: GameState) {
        stopGame()
        for (h in 1..board.size) {
            for (w in 1..board[h - 1].size) {
                set(Pair(h - 1, w - 1), if (Math.random() < 0.75) CellState.ALIVE else CellState.DEAD)
            }
        }
        if(gameState == GameState.RUNNING ) startGame()
    }

    fun addGliderGun(offset: Pair<Int, Int> = Pair(5, 5), gameState: GameState) {
        stopGame()
        set(offset + Pair(2, 8), CellState.ALIVE)
        set(offset + Pair(2, 9), CellState.ALIVE)
        set(offset + Pair(3, 9), CellState.ALIVE)
        set(offset + Pair(3, 8), CellState.ALIVE)
        set(offset + Pair(12, 8), CellState.ALIVE)
        set(offset + Pair(12, 9), CellState.ALIVE)
        set(offset + Pair(12, 10), CellState.ALIVE)
        set(offset + Pair(13, 11), CellState.ALIVE)
        set(offset + Pair(14, 12), CellState.ALIVE)
        set(offset + Pair(15, 12), CellState.ALIVE)
        set(offset + Pair(17, 11), CellState.ALIVE)
        set(offset + Pair(18, 10), CellState.ALIVE)
        set(offset + Pair(18, 9), CellState.ALIVE)
        set(offset + Pair(18, 8), CellState.ALIVE)
        set(offset + Pair(19, 9), CellState.ALIVE)
        set(offset + Pair(16, 9), CellState.ALIVE)
        set(offset + Pair(17, 7), CellState.ALIVE)
        set(offset + Pair(15, 6), CellState.ALIVE)
        set(offset + Pair(14, 6), CellState.ALIVE)
        set(offset + Pair(13, 7), CellState.ALIVE)
        set(offset + Pair(22, 8), CellState.ALIVE)
        set(offset + Pair(22, 7), CellState.ALIVE)
        set(offset + Pair(22, 6), CellState.ALIVE)
        set(offset + Pair(23, 6), CellState.ALIVE)
        set(offset + Pair(23, 7), CellState.ALIVE)
        set(offset + Pair(23, 8), CellState.ALIVE)
        set(offset + Pair(24, 9), CellState.ALIVE)
        set(offset + Pair(24, 5), CellState.ALIVE)
        set(offset + Pair(26, 5), CellState.ALIVE)
        set(offset + Pair(26, 4), CellState.ALIVE)
        set(offset + Pair(26, 9), CellState.ALIVE)
        set(offset + Pair(26, 10), CellState.ALIVE)
        set(offset + Pair(36, 6), CellState.ALIVE)
        set(offset + Pair(36, 7), CellState.ALIVE)
        set(offset + Pair(37, 7), CellState.ALIVE)
        set(offset + Pair(37, 6), CellState.ALIVE)
        if (gameState == GameState.RUNNING) startGame()
    }

    fun addGlider(offset: Pair<Int, Int> = Pair(5, 5), gameState: GameState) {
        stopGame()
        set(offset + Pair(3, 2), CellState.ALIVE)
        set(offset + Pair(4, 3), CellState.ALIVE)
        set(offset + Pair(4, 4), CellState.ALIVE)
        set(offset + Pair(3, 4), CellState.ALIVE)
        set(offset + Pair(2, 4), CellState.ALIVE)
        if (gameState == GameState.RUNNING) startGame()
    }

    fun addPentomino(offset: Pair<Int, Int> = Pair(20, 20), gameState: GameState) {
        stopGame()
        set(offset + Pair(38, 24), CellState.ALIVE)
        set(offset + Pair(39, 25), CellState.ALIVE)
        set(offset + Pair(39, 24), CellState.ALIVE)
        set(offset + Pair(39, 23), CellState.ALIVE)
        set(offset + Pair(40, 23), CellState.ALIVE)
        if (gameState == GameState.RUNNING) startGame()
    }
}