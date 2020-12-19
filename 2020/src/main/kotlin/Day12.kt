import Day12.Action.*
import kotlin.math.abs

// Template
object Day12 : AocPuzzle<List<Day12.Instruction>>(12) {
    override fun parseInput(input: String) = input.lines().map { line ->
        Instruction(valueOf(line[0].toString()), line.drop(1).toInt())
    }

    data class Instruction(val action: Action, val value: Int)

    enum class Action { N, S, E, W, L, R, F }

    enum class Direction(val degrees: Int) { N(0), S(180), E(90), W(270) }

    fun directionOf(degrees: Int) = Direction.values().find { it.degrees == (degrees + 360) % 360 }
        ?: error("Unable to find direction at $degrees degrees")

    data class Ship(val facing: Direction = Direction.E, val east: Int = 0, val north: Int = 0)

    tailrec fun Ship.followInstruction(instruction: Instruction): Ship =
        when (instruction.action) {
            N -> copy(north = north + instruction.value)
            S -> copy(north = north - instruction.value)
            E -> copy(east = east + instruction.value)
            W -> copy(east = east - instruction.value)
            L -> copy(facing = directionOf(facing.degrees - instruction.value))
            R -> copy(facing = directionOf(facing.degrees + instruction.value))
            F -> followInstruction(instruction.copy(action = Action.valueOf(facing.name)))
        }

    fun Ship.manhattanDist(start: Ship = Ship()) =
        abs(east - start.east) + abs(north - start.north)

    override fun part1(input: List<Instruction>): Any {
        val finalPosition = input.fold(Ship()) { ship, instruction ->
            log("$ship | $instruction")
            ship.followInstruction(instruction)
        }
        log(finalPosition)
        return finalPosition.manhattanDist()
    }

    data class Waypoint(val east: Int = 10, val north: Int = 1)

    fun Ship.followInstruction(instruction: Instruction, waypoint: Waypoint): Pair<Ship, Waypoint> {
        val newShip = if (instruction.action == F) {
            copy(east = east + (waypoint.east * instruction.value),
                north = north + (waypoint.north * instruction.value))
        } else this
        val newWaypoint = waypoint.followInstruction(instruction)
        return Pair(newShip, newWaypoint)
    }

    fun Waypoint.followInstruction(instruction: Instruction): Waypoint =
        when (instruction.action) {
            N -> copy(north = north + instruction.value)
            S -> copy(north = north - instruction.value)
            E -> copy(east = east + instruction.value)
            W -> copy(east = east - instruction.value)
            L -> turn(-instruction.value)
            R -> turn(instruction.value)
            else -> this
        }

    fun Waypoint.turn(degrees: Int) = ((degrees + 360) % 360).let {
        copy(
            east = when (it) {
                90 -> north
                180 -> -east
                270 -> -north
                0 -> east
                else -> error("invalid angle")
            },
            north = when (it) {
                90 -> -east
                180 -> -north
                270 -> east
                0 -> north
                else -> error("invalid angle")
            }
        )
    }

    override fun part2(input: List<Instruction>): Any {
        val (ship, waypoint) = input.fold(Pair(Ship(Direction.E, 0, 0), Waypoint())) { (ship, waypoint), instruction ->
            log("$ship | $waypoint | $instruction")
            ship.followInstruction(instruction, waypoint)
        }
        log("$ship | $waypoint")
        return ship.manhattanDist()
    }

    val exampleData = """
        F10
        N3
        F7
        R90
        F11
    """.trimIndent()

    override val part1Tests = listOf(TestSet(exampleData, 25))
    override val part2Tests = listOf(TestSet(exampleData, 286))
}

fun main() = Day12.testAndRun(debugLogging = false)