#!/usr/bin/env kscript
@file:Include("fetchInput.kt")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")

import kotlinx.coroutines.channels.Channel
import java.lang.Integer.max
import kotlin.random.Random

/**
 * Advent of Code 2019 - Day 15
 * https://adventofcode.com/2019/day/15
 */

val program get() = getInput(15).read().toIntCode()

inner class Computer(intCode: IntCode, val getInput: () -> Int = { 0 }) {

    val memory: IntCode = intCode + LongArray(10000)
    var pointer = 0L
    val outputChannel = Channel<Long>(32)
    var steps = 0
    var relativeBase = 0L

    fun readOutput() = outputChannel.poll()

    private fun biFunc(f: (Long, Long) -> Long): Boolean {
        val x = getParam(pointer + 1L, currentOp.p1Mode)
        val y = getParam(pointer + 2L, currentOp.p2Mode)
        write(f(x, y), pointer + 3, currentOp.p3Mode)
        pointer += 4
        steps += 1
        return true
    }

    private fun pred(f: (Long) -> Boolean): Boolean {
        val x = getParam(pointer + 1L, currentOp.p1Mode)
        val y = getParam(pointer + 2L, currentOp.p2Mode)
        pointer = if (f(x)) y else pointer + 3
        steps += 1
        return true
    }

    private fun uFunc(f: (Long) -> Unit): Boolean {
        val x = getParam(pointer + 1L, currentOp.p1Mode)
        f(x)
        pointer += 2
        steps += 1
        return true
    }

    private fun writeOutput(value: Long) = check(outputChannel.offer(value))

    private fun getParam(idx: Long, mode: Long = 0): Long = when (mode) {
        0L -> memory[memory[idx]]
        1L -> memory[idx]
        2L -> memory[memory[idx] + relativeBase]
        else -> throw RuntimeException("Invalid parameter mode $mode")
    }

    private fun write(value: Long, idx: Long, mode: Long = 0) = when (mode) {
        0L -> memory[memory[idx]] = value
        2L -> memory[memory[idx] + relativeBase] = value
        else -> throw RuntimeException("Invalid parameter mode $mode")
    }

    private fun doOp() = when (currentOp.op) {
        1L -> biFunc(Long::plus)
        2L -> biFunc(Long::times)
        3L -> uFunc { write(getInput().toLong(), pointer + 1, currentOp.p1Mode) }
        4L -> uFunc { writeOutput(it) }
        5L -> pred { x -> x != 0L }
        6L -> pred { x -> x == 0L }
        7L -> biFunc { x, y -> if (x < y) 1 else 0 }
        8L -> biFunc { x, y -> if (x == y) 1 else 0 }
        9L -> uFunc { relativeBase += it }
        99L -> terminate()
        else -> throw RuntimeException("Invalid Opcode ${currentOp.op} at position $pointer")
    }

    private val currentOp
        get() = parseOp(memory[pointer])

    fun runOnce(): Boolean {
        try {
            while (outputChannel.isEmpty) {
                if (!doOp()) return false
            }
        } catch (e: Exception) {
            println("pointer: $pointer")
            println("memory: ${memory.toList()}")
            println("Current Op: $currentOp")
            println("step: $steps")
            throw e
        }
        return true
    }

    fun run(): Computer {
        try {
            while (doOp()) {
            }
        } catch (e: Exception) {
            println("pointer: $pointer")
            println("memory: ${memory.toList()}")
            println("Current Op: $currentOp")
            println("step: $steps")
            throw e
        }
        return this
    }

    private fun terminate(): Boolean {
//        println("Execution completed at position $pointer in $steps steps")
//        println(memory.toList())
        return false
    }
}

data class Operation(val op: Long, val p1Mode: Long, val p2Mode: Long, val p3Mode: Long)

fun parseOp(code: Long) = Operation(
    op = code % 100,
    p1Mode = (code / 100) % 10,
    p2Mode = (code / 1000) % 10,
    p3Mode = (code / 10000) % 10
)

typealias IntCode = LongArray

operator fun IntCode.get(index: Long) = get(index.toInt())
operator fun IntCode.set(index: Long, value: Long) = set(index.toInt(), value)

fun String.toIntCode(): IntCode = split(',').map { it.toLong() }.toLongArray()

inner class RepairDroid(
    program: IntCode,
    val size: Int = 50,
    var startX: Int = size / 2,
    var startY: Int = size / 2,
    var findOxygen: Boolean = true,
    var printFrequency: Int = 1000,
    val auto: Boolean = true
) {
    private var computer: Computer
    val map: Array<IntArray> = Array(size) { IntArray(size) { -1 } }
    var x = startX
    var y = startY
    var oxygenX = -1
    var oxygenY = -1
    var oxygenDistance = -1
    var currentInstruction = 1
    var maxDist = 0

    init {
        computer = Computer(program, ::controller)
        map[y][x] = 0
    }

    fun controller(): Int {
        currentInstruction = if (auto) autopilot() else joystick()
//        println(currentInstruction)
        return currentInstruction
    }

    private fun randWalk(): Int {
        return Random.nextInt(1, 5)
    }

    fun inFront(): Int {
        return when (currentInstruction) {
            0 -> 0
            1 -> map[y - 1][x]
            2 -> map[y + 1][x]
            3 -> map[y][x - 1]
            4 -> map[y][x + 1]
            else -> throw java.lang.RuntimeException("Invalid instruction $currentInstruction")
        }
    }

    fun turnRight() {
        currentInstruction = when (currentInstruction) {
            1 -> 4
            4 -> 2
            2 -> 3
            3 -> 1
            else -> throw java.lang.RuntimeException("Invalid instruction $currentInstruction")
        }
    }

    fun reverse() {
        currentInstruction = when (currentInstruction) {
            1 -> 2
            2 -> 1
            3 -> 4
            4 -> 3
            else -> throw java.lang.RuntimeException("Invalid instruction $currentInstruction")
        }
    }

    private fun autopilot(): Int {
        if (Random.nextInt(0, 3) == 0)
            currentInstruction = randWalk()
//        val f = currentInstruction
        if (inFront() == -3) {
            turnRight()
        }
        if (inFront() == -3) {
            reverse()
        }
        if (inFront() == -3) {
            turnRight()
            reverse()
        }
        return currentInstruction
    }

    private fun joystick(): Int {
        return when (readLine()) {
            "k" -> 1 // North
            "j" -> 2 // South
            "h" -> 3 // West
            "l" -> 4 // East
            else -> currentInstruction
        }
    }

    fun printScreen(steps: Long? = null, force: Boolean = false) {
        if (Random.nextInt(0, printFrequency) != 0 && !force) return
        print("---")
        if (steps != null) print(steps)
        println("")
        for ((i, row) in map.withIndex()) {
            for ((j, tileId) in row.withIndex()) {
                if (i == 0 && j == 0) print("[")
                else if (i == size - 1 && j == size - 1) print("]")
                else if (i == startX && j == startY) print("*")
                else if (j == x && i == y) print("D")
                else if (j == oxygenX && i == oxygenY) print("X")
                else print(
                    when (tileId) {
                        -1 -> " "
                        -2 -> "D"
                        -3 -> "#"
                        -4 -> "O"
                        else -> tileId % 10
                    }
                )
            }
            println("")
        }
        println("---")
        println("maxDist: $maxDist, droidDist: ${thisDist()}")
        if (oxygenDistance >= 0) println("oxygen: x=$oxygenX, y=$oxygenY, dist=$oxygenDistance")
    }

    fun move(velocity: Int = 1) {
        when (currentInstruction) {
            1 -> y -= velocity
            2 -> y += velocity
            3 -> x -= velocity
            4 -> x += velocity
        }
    }

    // value of neighboring spots
    fun neighbors() = listOf(map[y - 1][x], map[y + 1][x], map[y][x - 1], map[y][x + 1])

    fun thisDist(): Int {
        val dist = if (x == startX && y == startY) {
            0
        } else
            (neighbors().filter { it >= 0 }.min() ?: 0) + 1
        maxDist = max(maxDist, dist)
        return dist
    }

    fun setCurrentPosition(value: Int) {
        map[y][x] = value
    }

    fun run(maxIterations: Long = 10000000L): Int {
        for (i in (0..maxIterations)) {
            printScreen(steps = i)
            computer.runOnce()
            val status = computer.readOutput()
            when (status) {
                // Wall
                0L -> {
                    move()
                    setCurrentPosition(-3)
                    move(-1)
                }
                // Move
                1L -> {
                    setCurrentPosition(thisDist())
                    move()
                    setCurrentPosition(thisDist())
//                    setCurrentPosition(-2)
                }
                // Oxygen
                2L -> {
                    setCurrentPosition(thisDist())
                    move()
//                    setCurrentPosition(-4)
                    setCurrentPosition(thisDist())
                    oxygenX = x
                    oxygenY = y
                    if (oxygenDistance < 0) {
                        oxygenDistance = thisDist()
                        if (findOxygen) {
                            printScreen(steps = i, force = true)
                            println("Found the Oxygen! Stopping.")
                            return thisDist()
                        }
                    }
                }
            }
        }
        printScreen(maxIterations, true)
        return -1
    }
}

val droid = RepairDroid(program)
println("part 1: ${droid.run()}")
// Not 418

/** Part 2 **/

fun RepairDroid.resetMap() {
    maxDist = 0
    for (row in map) {
        for (i in row.indices) {
            if (row[i] >= 0) row[i] = -1
        }
    }
    setCurrentPosition(0)
    startX = x
    startY = y
}

println("part 2:")
droid.findOxygen = false
droid.resetMap()
droid.printFrequency = 100000

droid.run(1_000_000_000)

// < 405