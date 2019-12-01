#!/usr/bin/env kscript
import java.io.File

/**
 * Advent of Code 2019 - Day 1
 * https://adventofcode.com/2019/day/1
 */

val moduleMasses = File("input01.txt").readLines().map { it.toInt() }
fun moduleFuel(mass: Int) = mass / 3 - 2

check(moduleFuel(12) == 2)
check(moduleFuel(14) == 2)
check(moduleFuel(1969) == 654)
check(moduleFuel(100756) == 33583)

val fuelRequirements = moduleMasses.map(::moduleFuel).sum()
println("Sum of fuel requirements: $fuelRequirements")

/** Part 2 **/

fun fuelFuel(fuelMass: Int): Int = if (fuelMass < 0) 0 else fuelMass + fuelFuel(moduleFuel(fuelMass))
fun recursiveFuel(moduleMass: Int) = fuelFuel(moduleFuel(moduleMass))

check(recursiveFuel(12) == 2)
check(recursiveFuel(14) == 2)
check(recursiveFuel(1969) == 966)
check(recursiveFuel(100756) == 50346)

val recursiveFuelRequirements = moduleMasses.map(::recursiveFuel).sum()
println("Sum of recursive fuel requirements: $recursiveFuelRequirements")
