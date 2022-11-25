package game.v2

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

class Cell(initState: CellState, coords: Pair<Int, Int>) {
    var state by mutableStateOf(initState)
    val coords = coords
    val neighboursList = mutableSetOf<Cell>() // save all cells to be able to get their state
    val neighbours = neighboursList.count { it.state == CellState.ALIVE }
    operator fun not() {
        state = !state
    }

    infix fun set(state: CellState) {
        this.state = state
    }

    fun isAlive() = state == CellState.ALIVE
    fun isDead() = !isAlive()

    constructor(initState: CellState, coords: Pair<Int, Int>, addCells: MutableSet<Cell>.() -> Unit) : this(
        initState,
        coords
    ) {
        neighboursList.addCells()
    }
}

operator fun Pair<Int, Int>.plus(o: Pair<Int, Int>): Pair<Int, Int> = Pair(first + o.first, second + o.second)

data class Game(val height: Int = 50, val width: Int = 50) {
    // list of live cells initial Empty
    private var liveCells by mutableStateOf(mutableSetOf<Cell>())
    private var timer = Timer()
    private var speed = 300f

    // calculate next board content
    private fun step() {

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

    fun contains(i: Pair<Int, Int>): Boolean {
        for (c in liveCells) {
            if (c.coords == i) return true
        }
        return false
    }

    infix fun get(i: Pair<Int, Int>): Cell {
        for (c in liveCells) {
            if (c.coords == i) return c
            for (nc in c.neighboursList) {
                if (nc.coords == i) return nc
            }
        }
        return Cell(CellState.DEAD, i)
    }

    fun set(i: Pair<Int, Int>, state: CellState) {
        for (c in liveCells) {
            if (c.coords == i) {
                c set state
                if (c.state == CellState.DEAD) liveCells.remove(c)
                return
            }
            for (nc in c.neighboursList) {
                if (nc.coords == i) {
                    nc set state
                    if (nc.state == CellState.ALIVE) {
                        liveCells.add(nc)
                        return
                    } else {
                        liveCells.remove(nc)
                        return
                    }
                }
            }
        }
        // no we know cell to set is outside of current render blocks
        // therefore we add it to live cells if live else ignore
        if (state == CellState.ALIVE) {
            val cell = Cell(state, i) {
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
                for (o in offset) {
                    add(get(i + o))
                }
            }
            liveCells.add(cell)
        }
    }

    private fun Pair<Int, Int>.clamp(min: Int, maxW: Int, maxH: Int) =
        Pair(max(min(first, maxW), min), max(min(second, maxH), min))

    fun resetGame() {
        stopGame()
        liveCells = mutableSetOf();
    }

    fun random(gameState: GameState, from: Pair<Int, Int> = Pair(0, 0), to: Pair<Int, Int> = Pair(50, 50)) {
        stopGame()
        for (x in from.first..to.first) {
            for (y in from.second..to.second) {
                set(Pair(x, y), if (Math.random() < 0.75) CellState.ALIVE else CellState.DEAD)
            }
        }
        if (gameState == GameState.RUNNING) startGame()
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