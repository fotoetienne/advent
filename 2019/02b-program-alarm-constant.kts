#!/usr/bin/env kscript

import java.io.File

/**
 * Advent of Code 2019 - Day 2 - Part 2
 * https://adventofcode.com/2019/day/2
 *
 * Constant time solution
 *
 * This solution create variables n and v for noun and verb respectively.
 * We can then compile the program into an algebraic equation which may be reduced.
 * Finally, we solve for the values for n and v that fit the constraints.
 */

class Program(val intCode: Array<String>) {
    constructor(intCodeString: String) : this(intCodeString.toIntCode())

    var pointer = 0
    var steps = 0

    private fun biFunc(f: (String, String) -> String): Boolean {
        val x = intCode[pointer + 1]
        val y = intCode[pointer + 2]
        val outputPosition = intCode[pointer + 3].toInt()
        intCode[outputPosition] = f("[$x]", "[$y]")
        pointer += 4
        steps += 1
        return true
    }

    private fun doOp() =
        when (val currentOp = intCode[pointer]) {
            "1" -> biFunc { a, b -> "($a + $b)" }
            "2" -> biFunc { a, b -> "($a * $b)" }
            "99" -> terminate()
            else -> biFunc { a, b -> "($a op[$currentOp] $b)" }
        }

    fun run(): Array<String> {
        while (doOp()) {
        }
        return intCode
    }

    private fun terminate(): Boolean {
        return false
    }
}

val pointerRegex = Regex("\\[\\d+\\]")

fun List<String>.compile(): String {
    fun resolvePointer(s: String): String {
        val i = s.trim('[', ']').toInt()
        return this[i]
    }

    var out = this[0]

    while (out.contains(pointerRegex)) {
        out = out.replace(pointerRegex) { matchResult ->
            resolvePointer(matchResult.value)
        }
    }

    return out
}

fun String.toIntCode() = split(',').toTypedArray()

fun initAlarmState(intCode: Array<String>, noun: String = "n", verb: String = "v") {
    intCode[1] = noun
    intCode[2] = verb
}

fun loadProgramIntCode() = File("input02.txt").readText().toIntCode()
fun gravityAssist(programIntCode: Array<String>): List<String> {
    initAlarmState(programIntCode)
    val program = Program(programIntCode)
    val final = program.run()
    return final.toList()
}

println("19690720 = " + gravityAssist(loadProgramIntCode()).compile())

// Result:
//     19690720 = (2 + (v + (4 + (5 + ((4 * (((5 + (2 * (3 + (2 + (5 * (1 + (1 + (3 + (3 + (5 + ((2 * (1 + (2 * (5 + ((1 + (2 * (4 + ((4 + (1 + ((((3 + (5 * n)) * 5) + 3) * 2))) * 2)))) + 3))))) * 2))))))))))) + 2) + 1)) * 4)))))

// Use a symbolic algebra library such as https://github.com/yuemingl/SymJava to reduce. I'll just use wolfram alpha for now:
//     19690720 = (256000 * n) + v + 234699

// We need n and v such that n,v are positive and small (<= 99)
// 19690720 = (256000 * n) + v + 234699

// There should be only one integer solution, so let's set v = 0 and solve for n
// n =  (v - 19456021) / -256000 = 19456021 / 256000 = 76.0001 ~= 76
val n =  19456021 / 256000

// Now plug in n = 76 and solve for v
// v = 19456021 - 256000 * n = 19456021 - 256000 * 76 = 21
val v = 19456021 - 256000 * n

// Final answer is 100 * 76 + 21 = 7621
val answer = 100 * n + v
println(answer)

// Verify
fun compiled(n: Int, v: Int) =
    (2 + (v + (4 + (5 + ((4 * (((5 + (2 * (3 + (2 + (5 * (1 + (1 + (3 + (3 + (5 + ((2 * (1 + (2 * (5 + ((1 + (2 * (4 + ((4 + (1 + ((((3 + (5 * n)) * 5) + 3) * 2))) * 2)))) + 3))))) * 2))))))))))) + 2) + 1)) * 4)))))

check(compiled(76, 21) == 19690720)
