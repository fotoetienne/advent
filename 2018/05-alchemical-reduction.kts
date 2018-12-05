#!/usr/bin/env kscript
import java.io.File
import java.util.*
import kotlin.math.abs

/**
 * Advent of Code 2018 - Day 5
 * https://adventofcode.com/2018/day/5
 */

var polymer = File("input05.txt").readText().trim().toCharArray()

fun react(polymer: CharArray, omit: Char? = null) = Stack<Char>().apply {
    for (char in polymer)
        if (isNotEmpty() && abs(char - peek()) == 'a' - 'A') pop()  // Found a pair. React!
        else if (char.toUpperCase() != omit) push(char)          // Not a pair :(
}.size

println("Part 1 - Reacted size: ${react(polymer)}")

val minSize = ('A'..'Z').map { react(polymer, omit = it) }.min()
println("Part 2 - Reacted size with problem pair removed: $minSize")
