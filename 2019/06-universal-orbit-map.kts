#!/usr/bin/env kscript
import java.io.File

/**
 * Advent of Code 2019 - Day 6
 * https://adventofcode.com/2019/day/6
 */

typealias OrbitMap = Map<String, String>

fun buildOrbitMap(s: Collection<String>): OrbitMap = s.map { it.split(')') }.map { it[1] to it[0] }.toMap()

tailrec fun OrbitMap.orbitChain(body: String, chain: List<String> = emptyList()): List<String> =
    if (containsKey(body)) orbitChain(getValue(body), chain + body) else chain

fun orbitCount(orbits: Map<String, String>) = orbits.keys.map { orbits.orbitChain(it).size }.sum()

val testOrbits = buildOrbitMap("""COM)B B)C C)D D)E E)F B)G G)H D)I E)J J)K K)L""".split(' '))
check(orbitCount(testOrbits) == 42)

val orbitMap = buildOrbitMap(File("input06.txt").readLines())
println("Part 1: ${orbitCount(orbitMap)}")

/** Part 2 **/

fun orbitalTransfers(orbits: Map<String, String>): Int {
    val you = orbits.orbitChain("YOU").reversed()
    val santa = orbits.orbitChain("SAN").reversed()
    val common = you.indices.takeWhile { i -> you[i] == santa[i] }.size
    return you.size + santa.size - common * 2 - 2
}

val testOrbits2 = testOrbits + ("YOU" to "K") + ("SAN" to "I")
check(orbitalTransfers(testOrbits2) == 4)

println("Part 2: ${orbitalTransfers(orbitMap)}")