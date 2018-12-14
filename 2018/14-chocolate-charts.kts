#!/usr/bin/env kscript
@file:KotlinOpts("-J-Xmx2g")

fun <T> List<T>.last(n: Int) = this.subList(this.size - n, this.size)

/**
 * Advent of Code 2018 - Day 14
 * https://adventofcode.com/2018/day/14
 */

val scoreboard = mutableListOf(3, 7)
var (elfA, elfB) = Pair(0, 1)

inline fun scoreRecipes() {
    val score = scoreboard[elfA] + scoreboard[elfB]

    if (score < 10) scoreboard.add(score)
    else scoreboard.apply { add(1); add(score - 10) }

    elfA = (elfA + scoreboard[elfA] + 1) % scoreboard.size
    elfB = (elfB + scoreboard[elfB] + 1) % scoreboard.size
}

while (scoreboard.size < 704321 + 10) scoreRecipes()
println(scoreboard.last(10).joinToString(""))

// Part 2

fun recipesLeftOf(input: List<Int>): Int? = when {
    scoreboard.size < input.size + 1 -> null
    scoreboard.last(input.size) == input -> scoreboard.size - 6
    scoreboard.last(input.size + 1).dropLast(1) == input -> scoreboard.size - 7
    else -> null
}

while (recipesLeftOf(listOf(7, 0, 4, 3, 2, 1)) == null) scoreRecipes()
println(recipesLeftOf(listOf(7, 0, 4, 3, 2, 1)))
