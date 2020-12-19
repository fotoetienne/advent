import Day11.SeatState.*
import Day11.visibleSeats

typealias SeatLayout = List<List<Day11.SeatState>>

object Day11 : AocPuzzle<SeatLayout>(11, debug = false) {
    override fun parseInput(input: String) = input.lines()
        .map { line ->
            line.map { char ->
                values().find { it.symbol == char } ?: error("Bad char: $char in line $line")
            }
        }

    data class Seat(val position: Coord, val state: SeatState)

    val directions = listOf(
        Coord(-1, -1),
        Coord(-1, 0),
        Coord(-1, 1),
        Coord(0, -1),
        Coord(0, 1),
        Coord(1, -1),
        Coord(1, 0),
        Coord(1, 1),
    )

    data class Coord(val x: Int, val y: Int) {
        infix operator fun plus(other: Coord) = Coord(x + other.x, y + other.y)
        val neighbors get() = directions.map(this::plus)
    }

    enum class SeatState(val symbol: Char) { FLOOR('.'), EMPTY('L'), OCCUPIED('#') }

    fun SeatLayout.seatState(seat: Coord) = getOrNull(seat.x)?.getOrNull(seat.y)

    fun SeatLayout.occupiedNeighbors(seat: Coord) =
        seat.neighbors.map { seatState(it) }.count { it == OCCUPIED }

    fun SeatLayout.nextState(seat: Coord): SeatState {
        val currentState = seatState(seat)!!
        val neighbors = occupiedNeighbors(seat)
        return when {
            currentState == EMPTY && neighbors == 0 -> OCCUPIED
            currentState == OCCUPIED && neighbors >= 4 -> EMPTY
            else -> currentState
        }
    }

    fun SeatLayout.seats() = indices.flatMap { x ->
        get(x).indices.map { y ->
            val pos = Coord(x, y)
            Seat(pos, seatState(pos)!!)
        }
    }

    fun SeatLayout.nextState() = seats().map { it.copy(state = nextState(it.position)) }.toSeatLayout()

    fun List<Seat>.toSeatLayout(): SeatLayout {
        val xMax = maxOf { it.position.x }
        val yMax = maxOf { it.position.y }
        val seats = Array(xMax + 1) {
            arrayOfNulls<SeatState>(yMax + 1)
        }
        forEach { seats[it.position.x][it.position.y] = it.state }
        return seats.map { it.map { it!! } }
    }

    fun SeatLayout.occupiedSeats() = seats().count { it.state == OCCUPIED }

    fun SeatLayout.print() {
        forEach { row ->
            row.forEach { seat ->
                log(seat.symbol, newline = false)
            }
            log()
        }
        log()
    }

    override fun part1(input: SeatLayout): Any {
        var currentLayout = input
        var previousLayout: SeatLayout? = null

        log()
        currentLayout.print()
        while (currentLayout != previousLayout) {
            previousLayout = currentLayout
            currentLayout = currentLayout.nextState()
            currentLayout.print()
        }

        return currentLayout.occupiedSeats()
    }

    fun SeatState?.isSeat() = this != null && this != FLOOR
    fun SeatState?.isNotSeat() = !isSeat()

    fun SeatLayout.visibleSeats(pos: Coord) = directions.mapNotNull { direction ->
        var seat = pos + direction
        while (seatState(seat).isNotSeat()) {
            if (seatState(seat) == null) return@mapNotNull null
            seat += direction
        }
        Seat(seat, seatState(seat)!!)
    }

    fun SeatLayout.occupiedVisible(seat: Coord) =
        visibleSeats(seat).count { it.state == OCCUPIED }

    fun SeatLayout.nextStateV2(seat: Coord): SeatState {
        val currentState = seatState(seat)!!
        val occupied = occupiedVisible(seat)
        return when {
            currentState == EMPTY && occupied == 0 -> OCCUPIED
            currentState == OCCUPIED && occupied >= 5 -> EMPTY
            else -> currentState
        }
    }

    fun SeatLayout.nextStateV2() = seats().map { it.copy(state = nextStateV2(it.position)) }.toSeatLayout()

    override fun part2(input: SeatLayout): Any {
        var currentLayout = input
        var previousLayout: SeatLayout? = null

        log()
        currentLayout.print()
        while (currentLayout != previousLayout) {
            previousLayout = currentLayout
            currentLayout = currentLayout.nextStateV2()
            currentLayout.print()
        }

        return currentLayout.occupiedSeats()
    }

    val exampleData = """
        L.LL.LL.LL
        LLLLLLL.LL
        L.L.L..L..
        LLLL.LL.LL
        L.LL.LL.LL
        L.LLLLL.LL
        ..L.L.....
        LLLLLLLLLL
        L.LLLLLL.L
        L.LLLLL.LL
    """.trimIndent()

    override val part1Tests = listOf(TestSet(exampleData, 37))
    override val part2Tests = listOf(TestSet(exampleData, 26))
}

fun main() = Day11.testAndRun()