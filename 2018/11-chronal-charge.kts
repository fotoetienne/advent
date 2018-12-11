#!/usr/bin/env kscript
@file:KotlinOpts("-J-Xmx4g")
@file:KotlinOpts("-J-Xms4g")
@file:CompilerOpts("-jvm-target 1.8")

import java.util.stream.IntStream
import java.util.stream.Stream
import kotlin.system.measureTimeMillis

/**
 * Advent of Code 2018 - Day 11
 * https://adventofcode.com/2018/day/11
 */

data class Grid(val x: Int, val y: Int, val size: Int = 3) {
    var powerLevel = 0

    init {
        for (i in (x until x + size))
            for (j in (y until y + size))
                powerLevel += fuelCellPower[i - 1][j - 1]
    }

    companion object {
        //        const val gridSerialNumber = 18
        const val gridSerialNumber = 3463

        val fuelCellPower =
            Array(300) { x ->
                IntArray(300) { y ->
                    fuelCellPower(x + 1, y + 1)
                }
            }

        inline fun hundredsDigit(x: Int) = (x / 100) % 10

        inline fun fuelCellPower(x: Int, y: Int): Int {
            val rackId = x + 10
            return hundredsDigit((rackId * y + gridSerialNumber) * rackId) - 5
        }
    }
}

fun rangeStream(start: Int, end: Int) = IntStream.rangeClosed(start, end).boxed().parallel()
fun <T> Stream<T>.maxBy(f: (T) -> Int) = this.max { a, b -> if (f(a) < f(b)) -1 else 1 }.orElse(null)!!

fun maxGrid(minSize: Int, maxSize: Int = minSize) =
    rangeStream(minSize, maxSize).flatMap { size ->
        rangeStream(1, 300 - size + 1).flatMap { x ->
            rangeStream(1, 300 - size + 1).map { y ->
                Grid(x, y, size)
            }
        }
    }.maxBy { it.powerLevel }

measureTimeMillis {
    println(maxGrid(3))
    println(maxGrid(1, 300))
}.let { t ->
    println("time: ${t / 1000.0} s")
}
