#!/usr/bin/env kscript
import java.io.File

/**
 * Advent of Code 2018 - Day 3
 * https://adventofcode.com/2018/day/3
 */

val lines = File("input03.txt").readLines()

data class Claim(val id: Int, val left: Int, val top: Int, val width: Int, val height: Int) {
    fun squares() =
        (left until left + width).flatMap { x -> (top until top + height).map { y -> "$x,$y" } }
}

val claimRegex = Regex("""#(\d+) @ (\d+),(\d+): (\d+)x(\d+)""")

fun parseClaim(s: String) =
    (claimRegex.matchEntire(s) ?: throw RuntimeException("Can't parse claim: $s"))
        .groupValues.map { it.toIntOrNull() ?: 0 }
        .let { Claim(it[1], it[2], it[3], it[4], it[5]) }

val claims = lines.map(::parseClaim)

val fabric = claims.flatMap(Claim::squares)
    .foldRight(mutableMapOf<String, Int>()) { square, fabric ->
        fabric.apply { compute(square) { _, v -> v?.inc() ?: 1 } }
    }

val conflictingSquares = fabric.values.filter { it > 1 }.size

println("Conflicting Squares Inches: $conflictingSquares")

for (claim in claims)
    if (claim.squares().all { square -> fabric[square] == 1 })
        println("The Good Claim: $claim")
