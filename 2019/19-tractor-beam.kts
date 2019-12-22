#!/usr/bin/env kscript
@file:Include("fetchInput.kt")
@file:Include("intcodeComputer.kt")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")
@file:KotlinOpts("-J-Xmx5g")

import kotlinx.coroutines.channels.Channel

/**
 * Advent of Code 2019 - Day 19
 * https://adventofcode.com/2019/day/19
 */

val program get() = getInput(19).read().toIntCode()

data class Point(val x: Int, val y: Int)

fun getTractorStatus(point: Point): Int {
    val (x,y) = point
    val seq = sequenceOf(x,y).iterator()
    val computer = Computer(program) { seq.next() }
    computer.runOnce()
    return computer.readOutput()?.toInt()!!
}

fun beamSurfaceArea(size: Int): Int {
    val points =
        (0 until size).flatMap { x ->
        (0 until size).map { y ->
        Point(x,y) } }
    return points.map(::getTractorStatus).sum()
}

println("part1: ${beamSurfaceArea(50)}")

fun beamMap(size: Int, scale: Int = 1) {
    for (y in (0 until size)) {
        if (y % scale != 0) continue
        for (x in (0 until size)) {
            if (x % scale != 0) continue
            print(if (getTractorStatus(Point(x,y)) == 0) "." else "#")
        }
        println()
    }
}

//fun findRowEdges(y: Int): Pair<Int, Int> {
//    var left = 0
//    var right = 0
//    var x = 0
//    while(right == 0) {
//        val v = getTractorStatus(Point(x,y))
//        if (v == 1) left = x
//        if (left != 0 && v == 0) right = x-1
//        x++
//    }
//    return Pair(left,right)
//}
//
//fun beamWidthM(y: Int): Int? = beamWidthM(Point(findRowEdges(y).first, y))
//
//fun beamWidthM(point: Point): Int? {
//    var p = point
//    var v = 1
//    while(v == 1) {
//        if (p.x < 0 || p.y < 0) return null
//        v = getTractorStatus(p)
//        p = Point(p.x + 1, p.y - 1)
//    }
//    val edge = Point(p.x-1, p.y+1)
//    println("$point $p")
//    val manhattanDist = (p.x - edge.x) + (edge.y - p.y)
//    return manhattanDist
//}

fun findLandingSpot(size: Int): Point {
    var y = size
    var x = 0
    var topEdge = 0
    while (topEdge == 0) {
        y ++
        // find edge
        var v = 0
        while (v == 0) {
            x ++
            v = getTractorStatus(Point(x,y))
            if (x > y * 10) {
//                println("no beam $y")
                x = 0
                break
            }
        }
//        println("$x,$y")
        // check top edge
        topEdge = getTractorStatus(Point(x + size - 1, y - size + 1))
    }
    return Point(x,y - size + 1)
}


beamMap(1100, scale = 10)
val spot = findLandingSpot(100)
println(spot)
println("part 2: ${spot.x * 10000 + spot.y}")