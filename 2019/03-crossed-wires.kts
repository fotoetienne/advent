#!/usr/bin/env kscript
import java.io.File
import kotlin.math.abs
import kotlin.math.min
import kotlin.system.measureTimeMillis

/**
 * Advent of Code 2019 - Day 3
 * https://adventofcode.com/2019/day/3
 */

val (wire1, wire2) = File("input03.txt").readLines().map(::Wire)

data class Instruction(val direction: Char, val distance: Int) {
    constructor(s: String) : this(s.first(), s.drop(1).toInt())
}

data class Point(val x: Int, val y: Int) {
    var step: Int = 0

    fun manhattanDistance() = abs(x) + abs(y)

    fun move(direction: Char, distance: Int = 1): Point {
        return when (direction) {
            'U' -> copy(y = y + distance)
            'D' -> copy(y = y - distance)
            'R' -> copy(x = x + distance)
            'L' -> copy(x = x - distance)
            else -> throw RuntimeException("Invalid Instruction: $direction")
        }
    }
}

class Wire(instructions: List<Instruction>) {
    constructor(s: String) : this(s.split(',').map(::Instruction))
    val path = mutableSetOf<Point>()
    init {
        var position = Point(0, 0)
        var steps = 0
        for ((direction, distance) in instructions) {
            repeat(distance) {
                position = position.move(direction)
                steps += 1
                position.step = steps
                if (!path.contains(position)) path.add(position)
            }
        }
    }
}

class CrossedWires(val wire1: Wire, val wire2: Wire) {
    constructor(s1: String, s2: String) : this(Wire(s1), Wire(s2))

    // Part 1
    fun closestIntersection(): Int {
        var minDistance = Int.MAX_VALUE
        for (position in wire1.path) {
            if (wire2.path.contains(position)) {
                minDistance = min(minDistance, position.manhattanDistance())
            }
        }
        return minDistance
    }

    // Part 2
    fun minDelay(): Int {
        var minDelay = Int.MAX_VALUE
        for (wire1Position in wire1.path) {
            if (wire2.path.contains(wire1Position)) {
                val wire2Position = wire2.path.find { it == wire1Position }!!
                minDelay = min(minDelay, wire1Position.step + wire2Position.step)
            }
        }
        return minDelay
    }
}

val test0 = CrossedWires("R8,U5,L5,D3", "U7,R6,D4,L4")
check(test0.closestIntersection() == 6)

val test1 = CrossedWires(
    "R75,D30,R83,U83,L12,D49,R71,U7,L72",
    "U62,R66,U55,R34,D71,R55,D58,R83"
)
check(test1.closestIntersection() == 159)

val test2 = CrossedWires(
    "R98,U47,R26,D63,R33,U87,L62,D20,R33,U53,R51",
    "U98,R91,D20,R16,D67,R40,U7,R15,U6,R7"
)
check(test2.closestIntersection() == 135)

measureTimeMillis {
    println(CrossedWires(wire1, wire2).closestIntersection())
}.let { t -> println("Computed in $t ms") }

/* Part 2 */

check(test0.minDelay() == 30)
check(test1.minDelay() == 610)
check(test2.minDelay() == 410)

measureTimeMillis {
    println(CrossedWires(wire1, wire2).minDelay())
}.let { t -> println("Computed in $t ms") }
