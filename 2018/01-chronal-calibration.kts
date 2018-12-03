#!/usr/bin/env kscript
import java.io.File

/**
 * Advent of Code 2018 - Day 1
 * https://adventofcode.com/2018/day/1
 */

val frequencyDeltas = File("01-input.txt").readLines().map { it.toInt() }

println("Cumulative frequency: ${frequencyDeltas.sum()}")

/** Part 2 **/

var currentFrequency = 0
val frequencies = mutableSetOf<Int>()

for (frequencyDelta in generateSequence(::frequencyDeltas).flatten()) {
    if (!frequencies.add(currentFrequency))
        break
    currentFrequency += frequencyDelta
}

println("First repeated frequency: $currentFrequency")
