#!/usr/bin/env kscript
@file:Include("fetchInput.kt")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")

import kotlinx.coroutines.channels.Channel
import java.lang.Math.floorMod

/**
 * Advent of Code 2019 - Day 11
 * https://adventofcode.com/2019/day/11
 */

val input = getInput(11).read()

inner class Computer(val intCode: IntCode, inputs: List<Long> = emptyList()) {

    val memory: IntCode = intCode + LongArray(10000)
    var pointer = 0L
    val inputChannel = Channel<Long>(32)
    val outputChannel = Channel<Long>(32)
    var steps = 0
    var relativeBase = 0L

    init {
        inputs.forEach { inputChannel.offer(it) }
    }

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

    fun getInputValue() = inputChannel.poll() ?: throw java.lang.RuntimeException("No inputs available")
    fun writeOutput(value: Long) = check(outputChannel.offer(value))

    private fun getParam(idx: Long, mode: Long = 0): Long = when (mode) {
        0L -> memory[memory[idx]]
        1L -> memory[idx]
        2L -> memory[memory[idx] + relativeBase]
        else -> throw RuntimeException("Invalid parameter mode")
    }

    private fun write(value: Long, idx: Long, mode: Long = 0) = when (mode) {
        0L -> memory[memory[idx]] = value
        2L -> memory[memory[idx] + relativeBase] = value
        else -> throw RuntimeException("Invalid parameter mode")
    }

    private fun doOp() = when (currentOp.op) {
        1L -> biFunc(Long::plus)
        2L -> biFunc(Long::times)
        3L -> uFunc { write(getInputValue(), pointer + 1, currentOp.p1Mode) }
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

class HullPaintingRobot(size: Int = 2000) {
    var orientation = 0
    var x = size / 2
    var y = size / 2
    val canvas: Array<IntArray> = Array(size) { IntArray(size) { 0 } }
    val path: MutableSet<Panel> = mutableSetOf()

    fun getColor() = canvas[x][y]
    fun paint(color: Int): HullPaintingRobot {
        canvas[x][y] = color
        path.add(Panel(x, y))
        return this
    }

    fun turn(direction: Int): HullPaintingRobot {
        when (direction) {
            0 -> orientation = floorMod(orientation - 1, 4)
            1 -> orientation = floorMod(orientation + 1, 4)
            else -> throw java.lang.RuntimeException("Invalid direction $direction")
        }
//        println("orientation: $orientation")
        return this
    }

    fun move(): HullPaintingRobot {
        when (orientation) {
            0 -> y += 1
            1 -> x += 1
            2 -> y -= 1
            3 -> x -= 1
            else -> throw java.lang.RuntimeException("Invalid orientation $orientation")
        }
//        println("position: $x, $y")
        return this
    }
}

data class Panel(val x: Int, val y: Int)

fun paintHull(canvasSize: Int = 200, startColor: Int = 0): HullPaintingRobot {
    val computer = Computer(input.toIntCode())
    val robot = HullPaintingRobot(canvasSize).paint(startColor)

    var run = true
    while (run) {
        computer.inputChannel.offer(robot.getColor().toLong())
        run = computer.runOnce()
        if (!run) break
        val color = computer.outputChannel.poll() ?: throw RuntimeException("No color")
        run = computer.runOnce()
        val direction = computer.outputChannel.poll() ?: throw RuntimeException("No direction")
//        println("color: $color, direction: $direction")
        robot.paint(color.toInt())
        robot.turn(direction.toInt())
        robot.move()
    }
    return robot
}

println("part 1: ${paintHull().path.size}")

val robot = paintHull(100, 1)

println("part 2:")

for (row in robot.canvas) {
    for (panel in row) {
        if (panel == 0) print(".") else print("#")
    }
    println("")
}
