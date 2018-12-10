#!/usr/bin/env kscript
@file:KotlinOpts("-J-Xmx2g")
@file:KotlinOpts("-J-Xms2g")

import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.io.File
import javax.imageio.ImageIO

/**
 * Advent of Code 2018 - Day 10
 * https://adventofcode.com/2018/day/10
 */

data class Star(var posX: Int, var posY: Int, val velX: Int, val velY: Int, val t: Int = 0) {
    fun move(t: Int): Star = this.copy(posX = posX + velX * t, posY = posY + velY * t, t = this.t + t)
}

val starRegex = Regex("""position=< *(-?\d+), *(-?\d+)> velocity=< *(-?\d+), *(-?\d+)>""")
fun parseStar(line: String): Star {
    val starMatch = starRegex.matchEntire(line)?.groupValues?.map { it.toIntOrNull() }!!
    return Star(starMatch[1]!!, starMatch[2]!!, starMatch[3]!!, starMatch[4]!!)
}

var stars = File("input10.txt").readLines().map(::parseStar)

fun constellationRange(stars: List<Star>): Int {
    val maxX = stars.map { it.posX }.max()!!
    val minX = stars.map { it.posX }.min()!!
    val maxY = stars.map { it.posY }.max()!!
    val minY = stars.map { it.posY }.min()!!
    return (maxX - minX + maxY - minY)
}

val smallest = (0..100000).map { t -> stars.map { it.move(t) } }
    .minBy { constellation -> constellationRange(constellation) }!!

fun makeImage(stars: List<Star>) {
    val image = BufferedImage(400, 400, TYPE_INT_RGB)
    for (star in stars) {
        image.setRGB(star.posX, star.posY, Color.WHITE.rgb)
    }
    ImageIO.write(image, "png", File("10-${smallest.first().t}.png"))
}

makeImage(smallest)
println("t=${smallest.first().t}")
