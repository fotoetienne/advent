#!/usr/bin/env kscript
import java.io.File
import java.util.*
import kotlin.math.abs

/**
 * Advent of Code 2018 - Day 5
 * https://adventofcode.com/2018/day/5
 */

var polymer = File("input05.txt").readText().trim().toCharArray()

fun react(polymer: CharArray, omitted: Char? = null): Int {
    val reactedPolymer = Stack<Char>()
    for (char in polymer) when {
        reactedPolymer.isEmpty() -> reactedPolymer.push(char)
        abs(char - reactedPolymer.peek()) == 'a' - 'A' -> reactedPolymer.pop()
        char.toUpperCase() != omitted -> reactedPolymer.push(char)
    }
    return reactedPolymer.size
}

val minSize = ('A'..'Z').fold(polymer.size) { minSize, problemPair ->
    minOf(minSize, react(polymer, problemPair))
}

println("Reacted size: ${react(polymer)}")
println("Reacted size with problem pair removed: $minSize")
