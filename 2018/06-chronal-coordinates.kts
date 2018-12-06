#!/usr/bin/env kscript
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.io.File
import java.lang.Math.pow
import javax.imageio.ImageIO
import kotlin.math.abs

/**
 * Advent of Code 2018 - Day 6
 * https://adventofcode.com/2018/day/6
 */

var lines = File("input06.txt").readLines()

data class Coordinate(val x: Int, val y: Int, val i: Int) {
    fun distance(x: Int, y: Int) =
        abs(x - (this.x)) + abs(y - (this.y))

    var closestPoints = 0
    fun incClosest() {
        closestPoints += 1
    }

    var edge = false
    fun markEdge() {
        edge = true
    }
}

val coordinateRegex = Regex("""(\d+), (\d+)""")

fun parseCoordinate(i: Int, line: String): Coordinate {
    val (x, y) = coordinateRegex.matchEntire(line)?.destructured
        ?: throw RuntimeException("Can't parse: $line")
    return Coordinate(x.toInt(), y.toInt(), i)
}

val coordinates = lines.mapIndexed(::parseCoordinate)

//val xMax = coordinates.map { it.x }.max()!!
//val yMax = coordinates.map { it.y }.max()!!
//
//val xMin = coordinates.map { it.x }.min()!!
//val yMin = coordinates.map { it.y }.min()!!

//println("$xMax, $yMax, $xMin, $yMin")

val points = Array(500) { Array<Coordinate?>(500) { null } }

fun findClosest(x: Int, y: Int): Coordinate? {
    val closest = coordinates.sortedBy { it.distance(x, y) }
    return if (closest[0].distance(x, y) != closest[1].distance(x, y))
        closest[0]
    else null
}


for (x in (0 until 500))
    for (y in (0 until 500)) {
        val closest = findClosest(x, y)
        closest?.incClosest()
        points[x][y] = closest
    }

for (i in (0 until 500)) {
    findClosest(i, 0)?.markEdge()
    findClosest(i, 500)?.markEdge()
    findClosest(0, i)?.markEdge()
    findClosest(500, i)?.markEdge()
}

val largestRegion = coordinates.filter { !it.edge }.maxBy { it.closestPoints }!!

println("Largest Region: ${largestRegion.closestPoints}")

var safePoints = 0

for (x in (0 until 400))
    for (y in (0 until 400)) {
        val score = coordinates.map { it.distance(x, y) }.sum()
        if (score < 10000)
            safePoints += 1
    }

println("Safe Points: $safePoints")

// Visualisation for Parts 1 and 2

val maxSize = coordinates.map { it.closestPoints }.max()!!
val minSize = coordinates.map { it.closestPoints }.min()!!

fun regionColor(x: Int, y: Int): Int {
    val score = coordinates.map { it.distance(x, y) }.sum()
    val saturation = if (score < 10000) pow(score / 10000.0, 2.0) else 0.8 * pow(10000.0 / score, 4.0)
    val coordinate = points[x][y] ?: return 0
    val size = coordinate.closestPoints.toFloat()
    val hue: Float = ((size - minSize) / maxSize)

    return if (coordinate.edge) Color.HSBtoRGB(hue, saturation.toFloat(), 0.5F)
    else Color.HSBtoRGB(hue, saturation.toFloat(), 1.0F)
}

val regions = BufferedImage(400, 400, TYPE_INT_RGB)
for (x in (0 until 400)) {
    for (y in (0 until 400)) {
        regions.setRGB(x, y, regionColor(x, y))
    }
}

for (coordinate in coordinates) when {
    coordinate.edge -> regions.setRGB(coordinate.x, coordinate.y, Color.GRAY.rgb)
    coordinate == largestRegion -> regions.setRGB(coordinate.x, coordinate.y, Color.BLACK.rgb)
    else -> regions.setRGB(coordinate.x, coordinate.y, Color.WHITE.rgb)
}

ImageIO.write(regions, "png", File("06-visualization.png"))
