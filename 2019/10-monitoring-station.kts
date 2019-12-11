#!/usr/bin/env kscript
@file:Include("fetchInput.kt")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")

import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Advent of Code 2019 - Day 10
 * https://adventofcode.com/2019/day/10
 */

val input = getInput(10).read()
//println(input)

data class Asteroid(val x: Int, val y: Int)

fun readMap(map: String): List<Asteroid> {
    val asteroids = mutableListOf<Asteroid>()
    for ((y, line) in map.lines().withIndex()) {
        for ((x, c) in line.withIndex()) {
            if (c == '#') asteroids.add(Asteroid(x, y))
        }
    }
    return asteroids
}

fun colinear(a: Asteroid, b: Asteroid, c: Asteroid): Boolean {
    return distance(a, c) + distance(b, c) - distance(a, b) < 0.00000001
}

fun distance(a: Asteroid, b: Asteroid) = sqrt((a.x - b.x + 0.0).pow(2) + (a.y - b.y + 0.0).pow(2))

fun lineOfSight(a: Asteroid, b: Asteroid, others: Collection<Asteroid>): Boolean {
    if (a == b) return false
    for (other in others) {
        if (b == other || a == other) continue
        if (colinear(a, b, other) && distance(a, other) < distance(a, b)) {
//            println("blocked: $a $b $other")
            return false
        }
    }
    return true
}

fun Asteroid.NlineOfSight(asteroids: Collection<Asteroid>): Int {
    return asteroids.filter { it != this && lineOfSight(this, it, asteroids) }.size
}

fun maxLineOfSight(asteroids: Collection<Asteroid>): Int? {
    val best = asteroids.maxBy { it.NlineOfSight(asteroids) }
    println("best: $best")
    return asteroids.map {
        val n = it.NlineOfSight(asteroids)
//        println("$it | ${n}")
        n
    }.max()
}


val map1 = readMap(
    """.#..#
.....
#####
....#
...##"""
)

//println(lineOfSight(Asteroid(3, 4), Asteroid(2, 2), map1))

//println(maxLineOfSight(map1))

val map2 = readMap(
    """......#.#.
#..#.#....
..#######.
.#.#.###..
.#..#.....
..#....#.#
#..#....#.
.##.#..###
##...#..#.
.#....####"""
)

//println(maxLineOfSight(map2))

val map3 = readMap(
    """#.#...#.#.
.###....#.
.#....#...
##.#.#.#.#
....#.#.#.
.##..###.#
..#...##..
..##....##
......#...
.####.###."""
)

//println(maxLineOfSight(map3))

val map4 = readMap(
    """.#..#..###
####.###.#
....###.#.
..###.##.#
##.##.#.#.
....###..#
..#.#..#.#
#..#.#.###
.##...##.#
.....#.#.."""
)

//println(maxLineOfSight(map4))

val map5 = readMap(
    """.#..##.###...#######
##.############..##.
.#.######.########.#
.###.#######.####.#.
#####.##.#.##.###.##
..#####..#.#########
####################
#.####....###.#.#.##
##.#################
#####.##.###..####..
..######..##.#######
####.##.####...##..#
.#####..#.######.###
##...#.##########...
#.##########.#######
.####.#.###.###.#.##
....##.##.###..#####
.#.#.###########.###
#.#.#.#####.####.###
###.##.####.##.#..##"""
)

//println(maxLineOfSight(map5))

println("part1: maxLineOfSight(readMap(input))")

/** Part 2 **/

fun nextAsteroid(laser: Asteroid, currentAngle: Double, asteroids: Collection<Asteroid>): Asteroid? {
    val potentialAsteroids = asteroids.filter { it != laser && angle(laser, it) > currentAngle }
    val nextAngle = potentialAsteroids
        .map { angle(laser, it) }.min()
        ?: return null
//    println("nextAngle: $nextAngle")
    return potentialAsteroids.filter { angle(laser, it) - nextAngle < 0.000000001 }
        .minBy { distance(laser, it) }
}

val PI = 3.141592653589793

fun angle(a: Asteroid, b: Asteroid): Double {
    val dX = a.x - b.x + 0.0
    val dY = a.y - b.y + 0.0
    val radians = atan2(dY, dX) - PI / 2
    return if (radians < 0) radians + 2 * PI else radians
}

//pi / 2 -> 0
//pi -> pi / 2
// -pi / 2 -> pi
// 0 -> 3 pi / 2

fun zap(laser: Asteroid, initialAsteroids: Collection<Asteroid>, nth: Int = 200): Asteroid? {
    var asteroids = initialAsteroids.toMutableList()
    var currentAngle = -0.0000000001
    for (i in (1..nth)) {
        val next = nextAsteroid(laser, currentAngle, asteroids)
        if (next != null) {
            if (i == nth) return next
            val asteroids = asteroids.filter { it != next }
            currentAngle = angle(laser, next)
        } else if (asteroids.isEmpty()) {
            return null
        } else {
            currentAngle = -0.0000000001
        }
    }
    return null
}

val map6 = readMap(
    """.#....#####...#..
##...##.#####..##
##...#...#.#####.
..#.....#...###..
..#.#.....#....##"""
)
val laser6 = Asteroid(x = 8, y = 3)
//println(map6.map { angle(laser6, it) })
//println(angle(laser6, Asteroid(8, 2)))
//println(angle(laser6, Asteroid(9, 3)))
//println(angle(laser6, Asteroid(8, 4)))
//println(angle(laser6, Asteroid(7, 3)))
//println(zap(laser6, map6, 2))
//println(angle(laser6, Asteroid(8, 1)))

//println(zap(Asteroid(11, 13), map5, 200))

val asteroid200 = zap(Asteroid(23, 19), readMap(input), 200)!!
println("part 2: ${asteroid200.x * 100 + asteroid200.y}")
