#!/usr/bin/env kscript
@file:Include("fetchInput.kt")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")
@file:KotlinOpts("-J-Xmx5g")

import kotlinx.coroutines.channels.Channel

/**
 * Advent of Code 2019 - Day 17
 * https://adventofcode.com/2019/day/17
 */

val program get() = getInput(17).read().toIntCode()

inner class Computer(intCode: IntCode, val getInput: () -> Int = { 0 }) {

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

data class Point(val x: Int, val y: Int)

fun asciiCalibrate(asciiProgram: IntCode): Int {
    val computer = Computer(asciiProgram) { readLine()!!.toInt() }
    val screen = mutableListOf(mutableListOf<Int>())
    while (true) {
        computer.runOnce()
        val out = computer.readOutput()?.toInt() ?: break
        if (out == 10) {
            screen.add(mutableListOf())
        } else {
            screen.last().add(out)
        }
//        print(out.toChar())
    }

    val intersections: MutableSet<Point> = mutableSetOf()
//    println("---")
    for (i in screen.indices) {
        for (j in screen[i].indices) {
            val current = screen[i][j]
            if (current == 35
                && i > 0 && j > 0
                && i < screen.lastIndex
                && j < screen[i + 1].lastIndex
                && screen[i - 1][j] == 35
                && screen[i + 1][j] == 35
                && screen[i][j - 1] == 35
                && screen[i][j + 1] == 35
            ) {
                intersections.add(Point(i, j))
                print("O")
            } else {
                print(current.toChar())
            }
        }
        println()
    }

//    for (point in intersections) {
//        println(point)
//    }

    return intersections.map { it.x * it.y }.sum()
}

val testInput = """..#..........
..#..........
#######...###
#.#...#...#.#
#############
..#...#...#..
..#####...^..""".map { it.toInt() }

//println("part 1: ${asciiCalibrate(program)}")

/** Part 2 **/

val fullRoutine = ("R,6,L,10,R,10,R,10,L,10,L,12,R,10,R,6,L,10,R,10,R,10,L,10,L,12,R,10,R,6,L,10,R,10," +
        "R,10,R,6,L,12,L,10,R,6,L,10,R,10,R,10,R,6,L,12,L,10,L,10,L,12,R,10,R,6,L,12,L,10").split(",")

// A = R,6,L,10,R,10,R,10
// B = L,10,L,12,R,10,
// C = R,10,R,6,L,12,L,10

// A,B,A,B
// R,6,L,10,R,10,
// C,R,6,L,10,R,10,
// C,L,10,L,12,
// C

fun encode(routine: List<String>, dict: Map<List<String>, String>): Pair<MutableList<String>, MutableList<String>> {
    val currentPath = mutableListOf<String>()
    val compiledRoutine = mutableListOf<String>()

    for (inst in routine) {
        currentPath.add(inst)
        if (dict.contains(currentPath)) {
            compiledRoutine.add(dict[currentPath]!!)
            currentPath.clear()
        }
    }

    return Pair(compiledRoutine, currentPath)
}

fun ngrams(l: List<String>, n: Int): MutableMap<List<String>, Int> {
    val ngramHist = mutableMapOf<List<String>, Int>()
    for (i in (0..l.lastIndex - n + 1)) {
        val ngram = l.subList(i, i + n)
        ngramHist.compute(ngram) { _, count -> (count ?: 0) + 1 }
    }
    return ngramHist
}

fun multiGrams(l: List<String>, ns: IntRange) =
    ns.map { n -> ngrams(l, n) }.reduce { acc, m ->
        m.forEach { (k, cnt) ->
            acc.compute(k) { _, n -> (n ?: 0) + cnt }
        }
        acc
    }

data class MovementFunction(val movements: List<String>, val frequency: Int = 0) {
    var name = ""
    val size = movements.size
    val score get() = size * frequency
    fun print() = movements.joinToString(",")
    fun valid() = print().length <= 20
}

fun movements(routine: List<String>) =
    multiGrams(routine, (2..10))
        .map { (movements, freq) -> MovementFunction(movements, freq) }
        .filter { it.valid() }
        .sortedByDescending { it.score }

//println(movements(fullRoutine).take(20).joinToString("\n"))

fun <T> Collection<T>.combinations(n: Int): Sequence<List<T>> {
    if (n == 0) return sequenceOf(emptyList())
    if (isEmpty()) return sequenceOf()
    return drop(1).combinations(n - 1).map { listOf(first()) + it } +
            drop(1).combinations(n)
}

check((1..9).toSet().combinations(3).count() == 84)

fun compress(routine: List<String>, functionSet: List<MovementFunction>): List<String>? {
    functionSet.let { (a, b, c) -> a.name = "A"; b.name = "B"; c.name = "C" }
    val out = mutableListOf<String>()
    var i = 0
    while (i < routine.size) {
        val function = functionSet
            .filter { it.size <= routine.size - i }
            .filter {
                //            println("${it.movements} ${routine.subList(i, i + it.size)}")
                routine.subList(i, i + it.size) == it.movements
            }.maxBy { it.size }
            ?: return null
        out.add(function.name)
        i += function.size
    }
    return out
}

check(
    compress(
        fullRoutine, listOf(
            MovementFunction(fullRoutine.subList(0, 20)),
            MovementFunction(fullRoutine.subList(20, 40)),
            MovementFunction(fullRoutine.subList(40, fullRoutine.size))
        )
    ) == listOf("A", "B", "C")
)

fun findValidFunctionSets(routine: List<String>, nOptions: Int = 100) =
    movements(routine).take(nOptions).combinations(3).filter { compress(routine, it) != null }

val functionSets = findValidFunctionSets(fullRoutine)

println("Found ${functionSets.count()} possible solution(s) for part 2")

val dict = functionSets.first()

val compiledRoutine = (listOf(compress(fullRoutine, dict)!!) + dict.map { it.movements })
    .joinToString("\n") { line -> line.joinToString(",") }

println(compiledRoutine)

fun notifyRobots(asciiProgram: IntCode, movementRoutine: String, print: Boolean = false): Int {
    asciiProgram[0] = 2
    val printInst = if (print) "y" else "n"
    val asciiInput = "$movementRoutine\n$printInst\n".iterator()
    val computer = Computer(asciiProgram) { asciiInput.nextChar().toInt() }
    val screen = mutableListOf(mutableListOf<Int>())
    var out = -1
    while (true) {
        computer.runOnce()
        out = computer.readOutput()?.toInt() ?: break
        if (out == 10) {
            screen.add(mutableListOf())
        } else {
            screen.last().add(out)
        }
        print(out.toChar())
    }
    return out
}

println(notifyRobots(program, compiledRoutine))