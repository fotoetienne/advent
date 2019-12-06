#!/usr/bin/env kscript

import java.io.File

/**
 * Advent of Code 2019 - Day 1
 * https://adventofcode.com/2019/day/1
 */

inner class Program(val intCode: Array<Int>, val inputValue: Int = 1) {
    constructor(intCodeString: String) : this(intCodeString.toIntCode())

    var pointer = 0
    var steps = 0
    val stdout = mutableListOf<Int>()

    private fun biFunc(f: (Int, Int) -> Int): Boolean {
        val x = getParam(pointer + 1, currentOp.p1Mode)
        val y = getParam(pointer + 2, currentOp.p2Mode)
        val outputPosition = intCode[pointer + 3]
//        println(currentOp)
//        println("bifunc $x, $y -> $outputPosition")
        intCode[outputPosition] = f(x, y)
        pointer += 4
        steps += 1
        return true
    }

    private fun pred(f: (Int) -> Boolean): Boolean {
        val x = getParam(pointer + 1, currentOp.p1Mode)
        val y = getParam(pointer + 2, currentOp.p2Mode)
        val outputPosition = intCode[pointer + 3]
//        println(currentOp)
//        println("pred $x -> $y")
        pointer = if (f(x)) y else pointer + 3
        steps += 1
        return true
    }

    private fun uFunc(f: (Int) -> Unit): Boolean {
        val x = getParam(pointer + 1, currentOp.p1Mode)
//        println("pointer: $pointer")
//        println("$currentOp")
//        println("ufunc : $x")
        f(x)
        pointer += 2
        steps += 1
        return true
    }

    private fun getParam(idx: Int, mode: Int = 0): Int =
        if (mode == 0) intCode[intCode[idx]] else intCode[idx]

    private fun doOp() =
        when (currentOp.op) {
            1 -> biFunc(Int::plus)
            2 -> biFunc(Int::times)
            3 -> uFunc { intCode[intCode[pointer + 1]] = inputValue }
            4 -> uFunc { stdout += it }
            5 -> pred { x -> x != 0 }
            6 -> pred { x -> x == 0 }
            7 -> biFunc { x, y -> if (x < y) 1 else 0 }
            8 -> biFunc { x, y -> if (x == y) 1 else 0 }
            99 -> terminate()
            else -> throw RuntimeException("Invalid Opcode ${currentOp.op} at position $pointer")
        }

    private val currentOp
        get() = parseOp(intCode[pointer])

    fun run(): List<Int> {
        try {
            while (doOp()) {
            }
        } catch (e: Exception) {
            println("pointer: $pointer")
            println("intCode: ${intCode.toList()}")
            println("Current Op: $currentOp")
            println("step: $steps")
            throw e
        }
        return stdout
    }

    private fun terminate(): Boolean {
//        println("Execution completed at position $pointer in $steps steps")
//        println(intCode.toList())
        return false
    }
}

data class Operation(val op: Int, val p1Mode: Int, val p2Mode: Int, val p3Mode: Int)

fun parseOp(code: Int) = Operation(
    op = code % 100,
    p1Mode = (code / 100) % 10,
    p2Mode = (code / 1000) % 10,
    p3Mode = (code / 10000) % 10
)

parseOp(1002).apply {
    check(op == 2)
    check(p1Mode == 0)
    check(p2Mode == 1)
    check(p3Mode == 0)
}

fun String.toIntCode() = split(',').map { it.toInt() }.toTypedArray()

fun test(program: String, expectedOutput: List<Int>) {
    val out = Program(program).run()
    try {
        check(out == expectedOutput)
    } catch (e: Exception) {
        e.printStackTrace()
        println("actual: $out, expected: $expectedOutput")
    }
}

test("1002,4,3,4,33", listOf())
test("1002,6,3,6,4,6,33", listOf(99))
test("1002,6,3,6,104,6,33", listOf(6))
test("1002,6,3,6,1004,6,33", listOf(99))
println("---")

fun diagnostic(input: Int): Int {
    val programIntCode = File("input05.txt").readText().toIntCode()
    val program = Program(programIntCode, input)
    val out = program.run()
    val diagnosticCode = out.last()
    check(out.dropLast(1).all { it == 0 })
    return diagnosticCode
}

println("part 1")
println(diagnostic(1))

/** Part 2 **/

println("part 2")
println(diagnostic(5))
println("done")
