#!/usr/bin/env kscript
@file:Include("fetchInput.kt")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.runBlocking

/**
 * Advent of Code 2019 - Day 9
 * https://adventofcode.com/2019/day/9
 */

inner class Program(val intCode: IntCode, inputs: List<Long> = emptyList()) {

    val memory: IntCode = intCode + LongArray(10000)
    var pointer = 0L
    val inputChannel = Channel<Long>(32)
    val outputChannel = Channel<Long>(32)
    val stdout
        get() = runBlocking { outputChannel.run { close(); toList() } }
    var steps = 0
    var relativeBase = 0L

    init {
        inputs.forEach { inputChannel.offer(it) }
    }

//    val modes
//     get() = (2..5).map { (memory[pointer] / 10.pow(it)) % 10 }

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
//        println(currentOp)
//        println(x)
        f(x)
        pointer += 2
        steps += 1
//        println("relativeBase: $relativeBase")
        return true
    }

    fun getInputValue() = inputChannel.poll() ?: throw java.lang.RuntimeException("No inputs available")
    fun writeOutput(value: Long) = check(outputChannel.offer(value))

    private fun getParam(idx: Long, mode: Long = 0): Long =
        when (mode) {
            0L -> memory[memory[idx]]
            1L -> memory[idx]
            2L -> memory[memory[idx] + relativeBase]
            else -> throw RuntimeException("Invalid parameter mode")
        }

    private fun write(value: Long, idx: Long, mode: Long = 0) {
//        println("write: $value to [$idx], mode: $mode")
        return when (mode) {
            0L -> memory[memory[idx]] = value
            2L -> memory[memory[idx] + relativeBase] = value
            else -> throw RuntimeException("Invalid parameter mode")
        }
    }

    private fun doOp() =
        when (currentOp.op) {
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

    fun run(): Program {
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

parseOp(1002).apply {
    check(op == 2L)
    check(p1Mode == 0L)
    check(p2Mode == 1L)
    check(p3Mode == 0L)
}

typealias IntCode = LongArray

operator fun IntCode.get(index: Long) = get(index.toInt())
operator fun IntCode.set(index: Long, value: Long) = set(index.toInt(), value)

fun String.toIntCode(): IntCode = split(',').map { it.toLong() }.toLongArray()

val testProgram = Program("109,1,204,-1,1001,100,1,100,1008,100,16,101,1006,101,0,99".toIntCode())
testProgram.run()
println("test")
//println(testProgram.stdout)
check(testProgram.stdout == testProgram.intCode.toList())

println("test2")
val testProgram2 = Program("1102,34915192,34915192,7,4,7,99,0".toIntCode())
testProgram2.run()
//println(testProgram2.stdout)
check(testProgram2.stdout.first().toString().length == 16)

val testProgram3 = Program("104,1125899906842624,99".toIntCode())
testProgram3.run()
//println("stdout: ${testProgram3.stdout}")
check(testProgram3.stdout.first() == 1125899906842624L)

println("part1")

val program = Program(getInput(9).read().toIntCode(), inputs = listOf(1))
program.run()
println(program.stdout)
println("steps: ${program.steps}")

println("part2")

val program2 = Program(getInput(9).read().toIntCode(), inputs = listOf(2))
program2.run()
println(program2.stdout)
println("steps: ${program2.steps}")

