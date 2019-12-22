#!/usr/bin/env kscript
@file:Include("fetchInput.kt")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")

import kotlinx.coroutines.channels.Channel

/**
 * IntCode Computer
 *
 * Advent of Code 2019
 * https://adventofcode.com/2019/
 */

class Computer(intCode: IntCode, val getInput: () -> Int = { 0 }) {

    val memory: IntCode = intCode + LongArray(10000)
    var pointer = 0L
    val outputChannel = Channel<Long>(10000)
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

    fun parseOp(code: Long) = Operation(
        op = code % 100,
        p1Mode = (code / 100) % 10,
        p2Mode = (code / 1000) % 10,
        p3Mode = (code / 10000) % 10
    )
}

data class Operation(val op: Long, val p1Mode: Long, val p2Mode: Long, val p3Mode: Long)


typealias IntCode = LongArray

operator fun IntCode.get(index: Long) = get(index.toInt())
operator fun IntCode.set(index: Long, value: Long) = set(index.toInt(), value)

fun String.toIntCode(): IntCode = split(',').map { it.toLong() }.toLongArray()

