#!/usr/bin/env kscript
@file:Include("fetchInput.kt")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")

import kotlinx.coroutines.channels.Channel

/**
 * Advent of Code 2019 - Day 13
 * https://adventofcode.com/2019/day/13
 */

val gameCode get() = getInput(13).read().toIntCode()

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

data class Tile(val x: Int, val y: Int, val id: Int)

fun part1(): Int {
    val computer = Computer(gameCode)
    val blocks = mutableMapOf<String, Tile>()
    for (i in (0..10000)) {
        computer.runOnce()
        val x = computer.readOutput()?.toInt()
        computer.runOnce()
        val y = computer.readOutput()?.toInt()
        computer.runOnce()
        val tileId = computer.readOutput()?.toInt()
        if (x != null && y != null && tileId != null) {
            blocks["$x,$y"] = Tile(x, y, tileId)
        } else break
    }
    return blocks.values.filter { it.id == 2 }.size
}

println("part 1: ${part1()}")

/** part 2 **/

fun Computer.oneTile(): Tile? {
    runOnce()
    val x = readOutput()?.toInt()
    runOnce()
    val y = readOutput()?.toInt()
    runOnce()
    val tileId = readOutput()?.toInt()
    return if (x != null && y != null && tileId != null)
        Tile(x, y, tileId)
    else null
}

inner class Pong(gameCode: IntCode, val print: Boolean = true, val autoPlay: Boolean = false) {
    private var computer: Computer
    private val screen: Array<IntArray> = Array(45) { IntArray(45) }
    var segmentDisplay = 0
    var ballPosition = 0
    var paddlePosition = 0

    init {
        gameCode[0] = 2
        computer = Computer(gameCode, if (autoPlay) ::movePaddle else ::joystick)
    }

    // Automatically move the paddle towards the ball
    private fun movePaddle(): Int = ballPosition.compareTo(paddlePosition)

    private fun joystick(): Int {
        printScreen()
        return when (readLine()) {
            "j" -> 1
            "k" -> -1
            else -> 0
        }
    }

    fun printScreen() {
        if (!print) return
        println("---")
        for (row in screen) {
            for (tileId in row) {
                print(
                    when (tileId) {
                        1 -> "W"
                        2 -> "B"
                        3 -> "|"
                        4 -> "o"
                        else -> " "
                    }
                )
            }
            println("")
        }
        println("---")
        println("score: $segmentDisplay")
    }

    fun play(): Int {
        for (i in (0..50000)) {
            val tile = computer.oneTile()
            if (tile != null) {
                if (tile.x == -1 && tile.y == 0) {
                    segmentDisplay = tile.id
                    printScreen()
                } else {
                    try {
                        screen[tile.x][tile.y] = tile.id
                        when (tile.id) {
                            4 -> ballPosition = tile.x
                            3 -> paddlePosition = tile.x
                        }
                        if (i % 10 == 0) printScreen()
                    } catch (e: Exception) {
                        println("invalid: $tile")
                    }
                }
            } else {
                printScreen()
                if (print) println("GAME OVER")
                break
            }
        }
        return segmentDisplay
    }
}

val finalScore = Pong(gameCode, false, autoPlay = true).play()
println("part 2: $finalScore")

// Uncomment this to watch the game:
//Pong(gameCode, true, autoPlay = true).play()

// Uncomment this to actually play the game:
//Pong(gameCode).play()
