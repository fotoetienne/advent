#!/usr/bin/env kscript
@file:Include("fetchInput.kt")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")
@file:KotlinOpts("-J-Xmx5g")

import kotlin.math.min

/**
 * Advent of Code 2019 - Day 18
 * https://adventofcode.com/2019/day/18
 */

val input get() = getInput(18).read()

typealias TunnelMap = Array<CharArray>
typealias Bag = Set<Char>

fun String.parseTunnelMap() = split("\n")
    .map { it.toCharArray() }.toTypedArray()

class Tunneler(val puzzleInput: String) {
    val tunnelMap = puzzleInput.split("\n")
        .map { it.split("").toMutableList() }.toMutableList()
    var x = 0
    var y = 0
    val xMax = tunnelMap[0].size
    val yMax = tunnelMap.size
    val keys = mutableMapOf<String, Boolean>()

    init {
        setInitialPosition()
        printScreen()
    }

    fun printScreen() {
        for (row in tunnelMap) {
            for (c in row) {
                print(c)
            }
            println()
        }
    }

    fun setInitialPosition() {
        for (i in tunnelMap.indices) {
            for (j in tunnelMap[i].indices) {
                if (tunnelMap[i][j] == "@") {
                    y = i
                    x = j
                }
            }
        }
    }
}

//val tunneler = Tunneler(input)

data class Point(val x: Int, val y: Int)

val TunnelMap.allKeys get() = flatMap { row -> row.filter { it.isKey() } }.toSet()

data class TunnelState(val tunnelMap: TunnelMap, val inventory: Bag = emptySet()) {
    constructor(tunnelMap: String) : this(tunnelMap.parseTunnelMap())

    val allKeys by lazy { tunnelMap.allKeys }
    val startingPosition by lazy { tunnelMap.find('@')!! }
}

operator fun TunnelMap.get(point: Point) = try {
    this[point.y][point.x]
} catch (e: Exception) {
    '#'
}

val Point.neighbors: List<Point>
    get() = listOf(Point(x - 1, y), Point(x + 1, y), Point(x, y - 1), Point(x, y + 1))

fun Char.isWall() = this == '#'
fun Char.isOpenPassage() = this == '.' || this == '@'
fun Char.isDoor() = this in 'A'..'Z'
fun Char.isKey() = this in 'a'..'z'
fun Bag.containsKeyFor(door: Char) = contains(door.toLowerCase())
fun Char.isOpenDoor(inventory: Bag) = isDoor() && inventory.containsKeyFor(this)
fun Char.isOpen(inventory: Bag) = isOpenPassage() || isOpenDoor(inventory) || isKey()
fun Char.isClosed(inventory: Bag) = !isOpen(inventory)

typealias KeyMap = Map<Char, Int>

fun KeyMap.merge(other: KeyMap) = (keys + other.keys).map { k ->
    k to min(getOrDefault(k, Int.MAX_VALUE), other.getOrDefault(k, Int.MAX_VALUE))
}.toMap()

fun TunnelState.accessibleKeys(position: Point, path: Set<Point> = emptySet()): Pair<Set<Point>, Map<Char, Int>> {
    var newPath = path + position
    val thisSpot = tunnelMap[position]
    return if (thisSpot.isKey() && !inventory.contains(thisSpot)) {
        Pair(newPath, mapOf(thisSpot to 0))
    } else if (thisSpot.isOpen(inventory)) {
        var keys = mapOf<Char, Int>()
        for (neighbor in position.neighbors) {
            if (newPath.contains(neighbor)) continue
            val (neighborPath, neighborKeys) = accessibleKeys(neighbor, newPath)
            newPath += neighborPath
            keys = keys.merge(neighborKeys)
        }
        Pair(newPath, keys.mapValues { (k, v) -> v + 1 })
    } else {
        Pair(newPath, emptyMap())
    }
}

fun TunnelMap.find(c: Char): Point? {
    for (i in indices)
        for (j in this[i].indices)
            if (this[i][j] == c)
                return Point(j, i)
    return null
}

val initialState = TunnelState(input.parseTunnelMap())

//val TunnelState.allKeys get() = tunnelMap.flatMap { row -> row.filter { it.isKey() } }.toSet()
//
//val ks = initialState.accessibleKeys(initialState.startingPosition)

fun TunnelState.getKeys(position: Point = startingPosition): Int? {
//    println("$position ${tunnelMap[position]}")
    val currentState = if (tunnelMap[position].isKey()) copy(inventory = inventory + tunnelMap[position]) else this
    if (currentState.inventory == allKeys) return 0
    val options = currentState.accessibleKeys(position).second
//    println(options)
    return options.entries.stream().parallel()
        .map { (k, dist) ->
            //        println("$k, neighbor of ${tunnelMap[position]}")
            currentState.getKeys(tunnelMap.find(k)!!)?.plus(dist)
        }.filter { it != null }
        .reduce { t: Int?, u: Int? -> min(t!!, u!!) }
        .orElse(null)
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

val test1 = TunnelState(testMap1)
check(test1.getKeys() == 8)

println("test 2:")
println(TunnelState(testMap2).getKeys())

println("test 3:")
println(TunnelState(testMap3).getKeys())

println("test 4:")
println(TunnelState(testMap4).getKeys())

println("test 5:")
println(TunnelState(testMap5).getKeys())

println("part 1:")
println(initialState.getKeys())