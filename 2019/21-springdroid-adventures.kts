#!/usr/bin/env kscript
@file:Include("fetchInput.kt")
@file:Include("intcodeComputer.kt")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")
@file:KotlinOpts("-J-Xmx5g")

/**
 * Advent of Code 2019 - Day 21
 * https://adventofcode.com/2019/day/21
 */

val intCode get() = getInput(21).read().toIntCode()

inner class SpringDroid(intCode: IntCode, droidScript: String) {
    private val droidScriptIterator = droidScript.iterator()
    val computer = Computer(intCode) { droidScriptIterator.nextChar().toInt() }

    fun run() {
        computer.run()
        var out: Long? = 0
        while (out != null) {
            out = computer.readOutput()
            if (out != null) {
                if (out <= 'z'.toLong())
                    print(out.toChar())
                else
                    print(out)
            }
        }
        println()
    }
}

// !(A | B | C) & D
val droidScript = """
    |NOT A J
    |NOT B T
    |OR T J
    |NOT C T
    |OR T J
    |AND D J
    |WALK
""".trimMargin() + "\n"

SpringDroid(intCode, droidScript).run()

/** Part 2 **/

// Part 1 + ensure droid can either jump or move forward
// after landing
// PART1 & (E | H)
val droidScript2 = """
    |NOT A J
    |NOT B T
    |OR T J
    |NOT C T
    |OR T J
    |AND D J
    |NOT E T
    |NOT T T
    |OR H T
    |AND T J
    |RUN
""".trimMargin() + "\n"

println("Part 2")
SpringDroid(intCode, droidScript2).run()
