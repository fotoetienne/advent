#!/usr/bin/env kscript
@file:Include("fetchInput.kt")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-collections-immutable-jvm:0.3")
@file:KotlinOpts("-J-Xmx5g")

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.collections.immutable.persistentListOf
import java.lang.Integer.compare
import java.util.*
import kotlin.math.min
import kotlin.system.measureTimeMillis

/**
 * Advent of Code 2019 - Day 18
 * https://adventofcode.com/2019/day/18
 */

val input get() = getInput(18).readText()

typealias TunnelMap = Array<CharArray>
typealias Bag = Set<Char>

fun String.parseTunnelMap() = split("\n")
    .map { it.toCharArray() }.toTypedArray()


fun TunnelMap.printScreen() {
    for (row in this) {
        for (c in row) {
            print(c)
        }
        println()
    }
}

data class Point(val x: Int, val y: Int)

val TunnelMap.allKeys get() = flatMap { row -> row.filter { it.isKey() } }.toSet()

data class TunnelState(val tunnelMap: TunnelMap, val inventory: Bag = emptySet()) {
    constructor(tunnelMap: String) : this(tunnelMap.parseTunnelMap())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TunnelState

        if (!tunnelMap.contentDeepEquals(other.tunnelMap)) return false
        if (inventory != other.inventory) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tunnelMap.contentDeepHashCode()
        result = 31 * result + inventory.hashCode()
        return result
    }
}

operator fun TunnelMap.get(point: Point) = try {
    this[point.y][point.x]
} catch (e: Exception) {
    '#'
}

val Point.neighbors: List<Point>
    get() = listOf(Point(x - 1, y), Point(x + 1, y), Point(x, y - 1), Point(x, y + 1))

fun Char.isDoor() = this in 'A'..'Z'
fun Char.isKey() = this in 'a'..'z'

fun TunnelMap.find(c: Char): Point? {
    for (i in indices)
        for (j in this[i].indices)
            if (this[i][j] == c)
                return Point(j, i)
    return null
}

data class KeyPath(val cost: Int, val doors: Set<Char>)

fun TunnelMap.keyPaths(start: Char) = keyPaths(find(start)!!)

fun TunnelMap.keyPaths(start: Point): Map<Char, KeyPath> {
    val paths: MutableMap<Char, KeyPath> = mutableMapOf()
    val queue: Deque<Triple<Point, Int, String>> = ArrayDeque()
    queue.add(Triple(start, 0, ""))
    val done: MutableSet<Point> = mutableSetOf(start)
    while (queue.isNotEmpty()) {
        val (position, cost, doors) = queue.poll()
        val newCost = cost + 1
        for (neighbor in position.neighbors) {
            val ch = get(neighbor)
            if (ch == '#') continue
            if (!done.contains(neighbor)) {
                done.add(neighbor)
                if (ch.isKey()) {
                    paths[ch] = KeyPath(newCost, doors.toSet())
                }
                queue.add(Triple(neighbor, newCost, if (ch.isDoor()) doors + ch.toLowerCase() else doors))
            }
        }
    }
    return paths
}

fun TunnelMap.calculatePaths(): Map<Char, Map<Char, KeyPath>> = (allKeys + '@').map { it to keyPaths(it) }.toMap()

data class KeyState(val cost: Int, val c: Char, val ks: Set<Char>)

fun TunnelMap.part1(): Int {
    val nKeys = allKeys.size
    val paths = calculatePaths()
    val queue = PriorityQueue<KeyState>(5000, kotlin.Comparator { a, b -> compare(a.cost, b.cost) }) // PriorityQ
    queue.add(KeyState(0, '@', persistentHashSetOf()))
    val done = mutableSetOf<Pair<Char, Set<Char>>>()
    var minCost = 0
    while (queue.isNotEmpty()) {
        val (cost, currentKey, inventory) = queue.remove()
        minCost = cost
        val keyPair = Pair(currentKey, inventory)
        if (!done.contains(keyPair)) {
            done.add(keyPair)
            if (inventory.size == nKeys) break
            for ((nextKey, path) in paths.getValue(currentKey)) {
                if (!inventory.contains(nextKey) && (path.doors - inventory).isEmpty()) {
                    queue.add(KeyState(cost + path.cost, nextKey, inventory + nextKey))
                }
            }
        }
    }
    return minCost
}

val testMap1 = """#########
#b.A.@.a#
#########"""

val testMap2 = """########################
#f.D.E.e.C.b.A.@.a.B.c.#
######################.#
#d.....................#
########################"""

val testMap3 = """########################
#...............b.C.D.f#
#.######################
#.....@.a.B.c.d.A.e.F.g#
########################"""

val testMap4 = """#################
#i.G..c...e..H.p#
########.########
#j.A..b...f..D.o#
########@########
#k.E..a...g..B.n#
########.########
#l.F..d...h..C.m#
#################"""

val testMap5 = """########################
#@..............ac.GI.b#
###d#e#f################
###A#B#C################
###g#h#i################
########################"""

check(testMap1.parseTunnelMap().part1() == 8)

check(testMap2.parseTunnelMap().part1() == 86)

check(testMap3.parseTunnelMap().part1() == 132)

print("test 4: ")
measureTimeMillis {
    check(testMap4.parseTunnelMap().part1() == 136)
}.let { println("($it ms)") }

print("test 5: ")
measureTimeMillis {
    check(testMap5.parseTunnelMap().part1() == 81)
}.let { println("($it ms)") }

print("part 1: ")
measureTimeMillis {
    print(input.parseTunnelMap().part1())
}.let { println(" ($it ms)") }

/** Part 2 **/

val robots = persistentListOf('1', '2', '3', '4')

fun TunnelMap.splitMap(): TunnelMap {
    val (x, y) = find('@')!!
    this[y][x] = '#'
    this[y - 1][x] = '#'
    this[y + 1][x] = '#'
    this[y][x - 1] = '#'
    this[y][x + 1] = '#'
    this[y - 1][x - 1] = '1'
    this[y + 1][x + 1] = '2'
    this[y - 1][x + 1] = '3'
    this[y + 1][x - 1] = '4'
    return this
}

fun TunnelMap.calculatePaths4(): Map<Char, Map<Char, KeyPath>> = (allKeys + robots).map { it to keyPaths(it) }.toMap()

data class MultiKeyState(val cost: Int, val robots: PersistentList<Char>, val ks: PersistentSet<Char>)

fun TunnelMap.part2(): Int {
    splitMap()
    val nKeys = allKeys.size
    val paths = calculatePaths4()
    val queue = PriorityQueue<MultiKeyState>(5000, kotlin.Comparator { a, b -> compare(a.cost, b.cost) }) // PriorityQ
    queue.add(MultiKeyState(0, robots, persistentHashSetOf()))
    val done = mutableSetOf<Pair<List<Char>, Set<Char>>>()
    var minCost = 0
    while (queue.isNotEmpty()) {
        val (cost, currentKeys, inventory) = queue.remove()
        minCost = cost
        val keyPair = Pair(currentKeys, inventory)
        if (!done.contains(keyPair)) {
            done.add(keyPair)
            if (inventory.size == nKeys) break
            for (robot in currentKeys.indices) {
                for ((nextKey, kp) in paths.getValue(currentKeys[robot])) {
                    if (!inventory.contains(nextKey) && (kp.doors - inventory).isEmpty()) {
                        queue.add(MultiKeyState(cost + kp.cost, currentKeys.set(robot, nextKey), inventory.add(nextKey)))
                    }
                }
            }
        }
    }
    return minCost
}

//input.parseTunnelMap().split().printScreen()
print("part 2: ")
measureTimeMillis {
    print(input.parseTunnelMap().part2())
}.let { println(" ($it ms)") }

