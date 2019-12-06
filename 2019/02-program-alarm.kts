#!/usr/bin/env kscript

import java.io.File

/**
 * Advent of Code 2019 - Day 1
 * https://adventofcode.com/2019/day/1
 */

class Program(val intCode: Array<Int>) {
    constructor(intCodeString: String) : this(intCodeString.toIntCode())

    var pointer = 0
    var steps = 0

    private fun biFunc(f: (Int, Int) -> Int): Boolean {
        val x = intCode[pointer + 1]
        val y = intCode[pointer + 2]
        val outputPosition = intCode[pointer + 3]
        intCode[outputPosition] = f(intCode[x], intCode[y])
        pointer += 4
        steps += 1
        return true
    }

    private fun doOp() =
        when (val currentOp = intCode[pointer]) {
            1 -> biFunc(Int::plus)
            2 -> biFunc(Int::times)
            99 -> terminate()
            else -> throw RuntimeException("Invalid Opcode $currentOp at position $pointer")
        }

    fun run(): Array<Int> {
        while(doOp()) {}
        return intCode
    }

    private fun terminate(): Boolean {
//        println("Execution completed at position $pointer in $steps steps")
//        println(intCode.toList())
        return false
    }
}

fun String.toIntCode() = split(',').map { it.toInt() }.toTypedArray()

fun restoreAlarmState(intCode: Array<Int>) {
    intCode[1] = 12
    intCode[2] = 2
}

fun initAlarmState(intCode: Array<Int>, noun: Int, verb: Int) {
    intCode[1] = noun
    intCode[2] = verb
}

fun gravityAssist(): Int {
    val programIntCode = File("input02.txt").readText().toIntCode()
    restoreAlarmState(programIntCode)
    val program = Program(programIntCode)
    return program.run().first()
}

fun test(program: String, finalState: String) {
    check(Program(program).run().contentEquals(finalState.toIntCode()))
}

test("1,0,0,0,99", "2,0,0,0,99")
test("2,3,0,3,99", "2,3,0,6,99")
test("2,4,4,5,99,0", "2,4,4,5,99,9801")
test("1,1,1,4,99,5,6,0,99", "30,1,1,4,2,5,6,0,99")

println(gravityAssist())

/** Part 2 **/

fun gravityAssist2(): Int {
    val programIntCode = File("input02.txt").readText().toIntCode()

    for (noun in (55..99)) {
        for (verb in (20..99)) {
            val thisIntCode = programIntCode.copyOf()
            initAlarmState(thisIntCode, noun, verb)
            val program = Program(thisIntCode)
            val result =  try {
                program.run().first()
            } catch (e: java.lang.RuntimeException) { -1 }
            if (result == 19690720) {
                return 100 * noun + verb
            }
        }
    }
    throw java.lang.RuntimeException("Not Found")
}

println(gravityAssist2())
