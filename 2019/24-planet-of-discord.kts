#!/usr/bin/env kscript
@file:Include("fetchInput.kt")
@file:KotlinOpts("-J-Xmx5g")

/**
 * Advent of Code 2019 - Day 24
 * https://adventofcode.com/2019/day/24
 */

val input get() = getInput(24).read()

typealias Eris = BooleanArray

fun Eris.neighbors(i: Int): List<Int> {
    val left = if (i % 5 != 0) i - 1 else null
    val right = if (i % 5 != 4) i + 1 else null
    return listOf(left, right, i - 5, i + 5).filterNotNull().filter { it in 0..24 }
}

fun Eris.adjacentBugs(i: Int) = neighbors(i).map(this::get).sumBy { if (it) 1 else 0 }
fun Eris.next(i: Int): Boolean {
    val alive = get(i)
    val otherBugs = adjacentBugs(i)
    return if (alive && otherBugs != 1) false
    else if (!alive && (otherBugs == 1 || otherBugs == 2)) true
    else alive
}

fun Eris.next(): BooleanArray {
    return BooleanArray(25) { next(it) }
}

fun Eris.bioRating(): Int {
    var p = 1
    var rating = 0
    for (i in indices) {
        if (get(i)) rating += p
        p *= 2
    }
    return rating
}

fun String.toEris() = mapNotNull {
    when (it) {
        '#' -> true
        '.' -> false
        '?' -> false
        else -> null
    }
}.toBooleanArray()

fun Eris.string(): String {
    return map { if (it) '#' else '.' }
        .joinToString("")
        .chunked(5)
        .joinToString("\n")
}

fun Eris.print() = println(string())

check("""..... ..... ..... #.... .#...""".toEris().bioRating() == 2129920)

fun doubleBioRating(scan: String): Int {
    var state = scan.toEris()
    val prevStates = mutableSetOf<Int>()
    while (true) {
        val rating = state.bioRating()
//        state.print()
//        println(rating)
        if (prevStates.contains(rating)) {
            return rating
        } else {
            prevStates.add(rating)
            state = state.next()
        }
    }
}

//println("""....# #..#. #..## ..#.. #....""".toEris().next().string())

println("part 1: ${doubleBioRating(input)}")

/** Part 2 **/

typealias Pluto = Map<Int, Eris>

data class Point(val x: Int, val y: Int, val z: Int)

fun Eris.index(x: Int, y: Int) = y * 5 + x
operator fun Eris.get(x: Int, y: Int) = get(index(x, y))

fun Pluto.neighbors(x: Int, y: Int, z: Int): List<Point> {
    val neighbors = mutableListOf<Point>()
    if (y == 0) neighbors.add(Point(2, 1, z - 1))
    else neighbors.add(Point(x, y - 1, z))
    if (x == 0) neighbors.add(Point(1, 2, z - 1))
    else neighbors.add(Point(x - 1, y, z))
    if (y == 4) neighbors.add(Point(2, 3, z - 1))
    else neighbors.add(Point(x, y + 1, z))
    if (x == 4) neighbors.add(Point(3, 2, z - 1))
    else neighbors.add(Point(x + 1, y, z))

    if (x == 2 && y == 1) neighbors.addAll((0..4).map { i -> Point(i, 0, z + 1) })
    if (x == 2 && y == 3) neighbors.addAll((0..4).map { i -> Point(i, 4, z + 1) })
    if (x == 1 && y == 2) neighbors.addAll((0..4).map { i -> Point(0, i, z + 1) })
    if (x == 3 && y == 2) neighbors.addAll((0..4).map { i -> Point(4, i, z + 1) })

    neighbors.removeIf { it.x == 2 && it.y == 2 }
    return neighbors
}

fun Pluto.neighbors(point: Point) = neighbors(point.x, point.y, point.z)

operator fun Pluto.get(point: Point): Boolean {
    if (!containsKey(point.z)) return false
    if (point.x == 2 && point.y == 2) return false
    return getValue(point.z)[point.x, point.y]
}

fun Pluto.bugs() = values.sumBy { it.bugs() }
fun Eris.bugs() = sumBy { if (it) 1 else 0 }
fun Collection<Boolean>.bugs() = sumBy { if (it) 1 else 0 }


fun Pluto.adjacentBugs(point: Point) = neighbors(point).map { this[it] }.bugs()
fun Pluto.next(point: Point): Boolean {
    if (point.x == 2 && point.y == 2) return false
    val alive = get(point)
    val otherBugs = adjacentBugs(point)
    return if (alive && otherBugs != 1) false
    else if (!alive && (otherBugs == 1 || otherBugs == 2)) true
    else alive
}

fun Pluto.next(z: Int): BooleanArray? {
    val points = (0..4).map { x -> (0..4).map { y -> Point(x, y, z) } }.flatten()
        .map { next(it) }

    return if (points.bugs() > 0) points.toBooleanArray() else null
}

fun Pluto.next(): Pluto {
    val nextPluto = mutableMapOf<Int, Eris>()
    for (z in (keys.min()!! - 1 .. keys.max()!! + 1)) {
        next(z)?.let { nextPluto[z] = it }
    }
    return nextPluto
}

fun Pluto.string(): String {
    return entries.sortedBy { it.key }
        .joinToString("\n\n") { (z, eris) -> "Depth $z:\n" + eris.string() }
}

fun Pluto.print() = println(string())

fun String.toPluto(): Pluto = mapOf(0 to this.toEris())

fun Pluto.simulate(minutes: Int): Pluto {
    var pluto = this
    for (i in (1..minutes)) {
        pluto = pluto.next()
    }
    return pluto
}

fun testCase() {
    var pluto = """....# #..#. #.?## ..#.. #....""".toPluto().simulate(10)
    pluto.print()
    println("Total Bugs: ${pluto.bugs()}")
}

//testCase()

println("part 2: ${input.toPluto().simulate(200).bugs()}")
