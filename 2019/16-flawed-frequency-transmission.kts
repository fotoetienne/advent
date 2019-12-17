#!/usr/bin/env kscript
@file:Include("fetchInput.kt")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")

import java.util.stream.Collectors
import kotlin.math.abs

/**
 * Advent of Code 2019 - Day 16
 * https://adventofcode.com/2019/day/16
 */

fun String.toIntList() = trim().map { it.toString().toInt() }

fun <T> List<T>.repeat() = sequence { while (true) yieldAll(this@repeat) }
fun <T> List<T>.repeat(n: Int) = sequence { repeat(n) { yieldAll(this@repeat) } }

fun pattern(repeat: Int = 0, base: List<Int> = listOf(0,1,0,-1)) =
    base.flatMap { x -> Array(repeat + 1) {x}.toList() }.repeat().drop(1)

fun Int.lastDigit() = (abs(this) % 10)

fun phase(input: List<Int>): List<Int> {
    val size = input.size
    return input.indices.toList().stream().parallel().map {idx ->
        val pattern = pattern(idx).take(input.size).toList()
        var sum = 0
        for (i in input.indices) {
            sum = (sum + input[i] * pattern[i]).lastDigit()
        }
        sum.lastDigit()
    }.collect(Collectors.toList())
}

fun multiPhase(signal: List<Int>, nRounds: Int, offset: Int = 0): Int {
    val signalLength = signal.size
    var s = signal
    for (round in (1..nRounds)) {
        s = phase(s)
    }
    return s.drop(offset).take(8).joinToString("").toInt()
}

val testSignal1 = "12345678".toIntList()
check(multiPhase(testSignal1, 4) == 81029498)
check(multiPhase("80871224585914546619083218645595".toIntList(), 100) == 24714512)

val input get() = getInput(16).read()
print("part 1: ")
println(multiPhase(input.toIntList(), 100))

/** Part 2 **/

// Kindof cheating. I only calculate the last half of the array
// In practice, the offset is past the half-way point, so this works.
fun phase2(input: IntArray, offset: Int = input.size / 2): IntArray {
    val out = IntArray(input.size)
    var x = 0
    for (idx in (input.lastIndex downTo  offset)) {
        x = (x + input[idx]) % 10
        out[idx] = x
    }
    return out
}

fun multiPhase2(signal: List<Int>, nRounds: Int, offset: Int = 0): Int {
    var s = signal.toIntArray()
    for (round in (1..nRounds)) {
        s = phase2(s)
    }
    return s.drop(offset).take(8).joinToString("").toInt()
}

print("part 2: ")

val testSignal2 = "03036732577212944063491565474664".toIntList().repeat(10000).toList()
val testOffset2 = testSignal2.take(7).joinToString("").toInt()

check(multiPhase2(testSignal2, 100, offset = testOffset2) == 84462026)

val bigSignal = input.toIntList().repeat(10000).toList()
val messageOffset = bigSignal.take(7).joinToString("").toInt()
val part2 = multiPhase2(bigSignal, 100, offset = messageOffset)
println(part2)