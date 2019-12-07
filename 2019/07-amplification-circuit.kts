#!/usr/bin/env kscript
import _07.IntCode
import java.io.File
import java.lang.Integer.max

/**
 * Advent of Code 2019 - Day 7
 * https://adventofcode.com/2019/day/7
 */

fun amplifierControlSoftware() = File("input07.txt").readText().trim().toIntCode()

inner class Program(val intCode: Array<Int>, val phase: Int, var input: Int = 0) {
    constructor(intCodeString: String, phase: Int) : this(intCodeString.toIntCode(), phase)

    var pointer = 0
    var steps = 0
    val stdout = mutableListOf<Int>()
    var inputCount = 0

    private fun biFunc(f: (Int, Int) -> Int): Boolean {
        val x = getParam(pointer + 1, currentOp.p1Mode)
        val y = getParam(pointer + 2, currentOp.p2Mode)
        val outputPosition = intCode[pointer + 3]
        intCode[outputPosition] = f(x, y)
        pointer += 4
        steps += 1
        return true
    }

    private fun pred(f: (Int) -> Boolean): Boolean {
        val x = getParam(pointer + 1, currentOp.p1Mode)
        val y = getParam(pointer + 2, currentOp.p2Mode)
        val outputPosition = intCode[pointer + 3]
        pointer = if (f(x)) y else pointer + 3
        steps += 1
        return true
    }

    private fun uFunc(f: (Int) -> Unit): Boolean {
        val x = getParam(pointer + 1, currentOp.p1Mode)
        f(x)
        pointer += 2
        steps += 1
        return true
    }

    fun getInputValue() = if (inputCount == 0) {
        inputCount += 1
        phase
    } else {
        inputCount += 1
        input
    }

    private fun getParam(idx: Int, mode: Int = 0): Int =
        if (mode == 0) intCode[intCode[idx]] else intCode[idx]

    private fun doOp() =
        when (currentOp.op) {
            1 -> biFunc(Int::plus)
            2 -> biFunc(Int::times)
            3 -> uFunc { intCode[intCode[pointer + 1]] = getInputValue() }
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

    fun runOnce(): Boolean {
        try {
            while (stdout.isEmpty()) {
                if (!doOp()) return false
            }
        } catch (e: Exception) {
            println("pointer: $pointer")
            println("intCode: ${intCode.toList()}")
            println("Current Op: $currentOp")
            println("step: $steps")
            throw e
        }
        return true
    }

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

typealias IntCode = Array<Int>

fun String.toIntCode() = split(',').map { it.toInt() }.toTypedArray()

fun IntCode.amplifier(phase: Int, input: Int = 0): Int {
    val program = Program(this, phase, input)
    val out = program.run()
    val diagnosticCode = out.last()
    check(out.dropLast(1).all { it == 0 })
    return diagnosticCode
}

fun IntCode.trySequence(phaseSequence: List<Int>): Int {
    var input = 0
    for (phase in phaseSequence) {
        val codeCopy = this.copyOf()
        val output = codeCopy.amplifier(phase, input)
        input = output
    }
    return input
}

fun <T> permute(input: List<T>): List<List<T>> {
    if (input.size == 1) return listOf(input)
    val perms = mutableListOf<List<T>>()
    val toInsert = input[0]
    for (perm in permute(input.drop(1))) {
        for (i in 0..perm.size) {
            val newPerm = perm.toMutableList()
            newPerm.add(i, toInsert)
            perms.add(newPerm)
        }
    }
    return perms
}

val phasePermutations = permute((0..4).toList())

fun IntCode.maxThrust(): Int {
    var maxThrust = 0
    for (phaseSequence in phasePermutations) {
        val thrust = trySequence(phaseSequence)
        maxThrust = max(thrust, maxThrust)
    }
    return maxThrust
}

val testSoftware1: IntCode = "3,15,3,16,1002,16,10,16,1,16,15,15,4,15,99,0,0".toIntCode()
check(testSoftware1.trySequence(listOf(4, 3, 2, 1, 0)) == 43210)
check(testSoftware1.maxThrust() == 43210)

val testSoftware2: IntCode =
    "3,23,3,24,1002,24,10,24,1002,23,-1,23,101,5,23,23,1,24,23,23,4,23,99,0,0".toIntCode()
check(testSoftware2.trySequence(listOf(0,1,2,3,4)) == 54321)
check(testSoftware2.maxThrust() == 54321)

println("part 1: ${amplifierControlSoftware().maxThrust()}")

/** Part 2 **/

fun IntCode.feedback(phaseSequence: List<Int>): Int {

    val amplifiers = phaseSequence.map { phase ->
        Program(this.copyOf(), phase)
    }

    var buffer = 0
    var halt = false

    while (!halt) {
        for (amplifier in amplifiers) {
            amplifier.input = buffer
            val notHalted = amplifier.runOnce()
            if (notHalted && amplifier.stdout.isNotEmpty()) {
                buffer = amplifier.stdout.first()
                amplifier.stdout.clear()
            } else {
                halt = true
            }
//            println("${amplifier.phase} $buffer ${amplifier.stdout}")
        }
    }
    return buffer
}

fun IntCode.maxThrustWFeedback(): Int {
    var maxThrust = 0
    for (phaseSequence in phasePermutations59) {
        val thrust = feedback(phaseSequence)
        maxThrust = max(thrust, maxThrust)
    }
    return maxThrust
}

val phasePermutations59 = permute((5..9).toList())
val testSoftware21: IntCode =
    "3,26,1001,26,-4,26,3,27,1002,27,2,27,1,27,26,27,4,27,1001,28,-1,28,1005,28,6,99,0,0,5".toIntCode()

check(testSoftware21.feedback(listOf(9,8,7,6,5)) == 139629729)
check(testSoftware21.maxThrustWFeedback() == 139629729)

println("part 2: ${amplifierControlSoftware().maxThrustWFeedback()}")
