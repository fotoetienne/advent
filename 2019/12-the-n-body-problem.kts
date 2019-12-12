#!/usr/bin/env kscript
@file:Include("fetchInput.kt")

import kotlin.math.abs

/**
 * Advent of Code 2019 - Day 12
 * https://adventofcode.com/2019/day/12
 */

class Moon(vararg initialPosition: Int) {
    val position: IntArray = initialPosition
    val velocity: IntArray = intArrayOf(0, 0, 0)

    val potentialEnergy get() = position.map(::abs).sum()
    val kineticEnergy get() = velocity.map(::abs).sum()
    val totalEnergy get() = potentialEnergy * kineticEnergy
}

typealias SolarSystem = List<Moon>

val moonRegex = """<x=(-?\d+), y=(-?\d+), z=(-?\d+)>""".toRegex()

fun parseMoon(s: String): Moon {
    val (_, x, y, z) = moonRegex.matchEntire(s)?.groupValues
        ?: throw RuntimeException("Can't parse moon $s")
    return Moon(x.toInt(), y.toInt(), z.toInt())
}

fun updateVelocity(moons: SolarSystem) {
    for (dimension in (0..2)) {
        for (i in (moons.indices)) {
            for (j in (i until moons.size)) {
                val moonA = moons[i]
                val moonB = moons[j]
                val comp = compareValues(moonA.position[dimension], moonB.position[dimension])
                moonA.velocity[dimension] -= comp
                moonB.velocity[dimension] += comp
            }
        }
    }
}

fun updatePosition(moon: Moon, dimensions: IntRange = (0..2)) {
    for (dimension in dimensions) {
        moon.position[dimension] += moon.velocity[dimension]
    }
}

fun updatePositions(moons: SolarSystem) = moons.forEach { moon ->
    updatePosition(moon)
}

fun runOnce(moons: SolarSystem) {
    updateVelocity(moons)
    updatePositions(moons)
}

fun totalEnergy(moons: SolarSystem) = moons.sumBy { it.totalEnergy }

fun energyAfter(moons: SolarSystem, iterations: Int): Int {
    for (n in (0 until iterations)) {
        runOnce(moons)
    }
    return totalEnergy(moons)
}

fun testMoons() = """<x=-1, y=0, z=2>
<x=2, y=-10, z=-7>
<x=4, y=-8, z=8>
<x=3, y=5, z=-1>""".lines().map(::parseMoon)

check(energyAfter(testMoons(), 10) == 179)

fun inputMoons() = getInput(12).lines().map(this::parseMoon)
println("part1: ${energyAfter(inputMoons(), 1000)}")

/** part 2 **/

fun Moon.equalOnDimension(dimension: Int, other: Moon) =
    position[dimension] == other.position[dimension] && velocity[dimension] == other.velocity[dimension]

fun SolarSystem.equalOnDimension(dimension: Int, other: SolarSystem) =
    indices.all { i -> get(i).equalOnDimension(dimension, other[i]) }

fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)

fun lcm(a: Long, b: Long): Long = a / gcd(a, b) * b

fun findLife(moonGen: () -> SolarSystem, iterations: Long): Long {
    val initialState = moonGen()
    val moons = moonGen()
    val cycle = longArrayOf(0, 0, 0)
    for (i in (1..iterations)) {
        runOnce(moons)
        for (dimension in (0..2)) {
            if (initialState.equalOnDimension(dimension, moons)) cycle[dimension] = i
        }
        if (cycle.all { it != 0L }) return lcm(lcm(cycle[0], cycle[1]), cycle[2])
    }
    return -1
}

check(findLife(::testMoons, 3000) == 2772L)

print("part2: ")
println(findLife(::inputMoons, 1_000_000))
