#!/usr/bin/env kscript
//INCLUDE fetchInput.kt

import java.lang.Integer.MAX_VALUE

/**
 * Advent of Code 2019 - Day 8
 * https://adventofcode.com/2019/day/8
 */

val input = getInput(8).read()//.map(Character::getNumericValue)

val width = 25
val height = 6
val z = input.length / width / height

typealias Row = MutableList<Char>
typealias Image = MutableList<Row>
typealias Stack = MutableList<Image>

var i = 0
val stack: Stack = mutableListOf()
for (x in (0 until z)) {
    val image: Image = mutableListOf()
    for (y in (0 until height)) {
        val row: Row = mutableListOf()
        for (x in (0 until width)) {
            row.add(input[i])
            i++
        }
        image.add(row)
    }
    stack.add(image)
}

var minZeros = MAX_VALUE
var onesByTwos = 0
for (image in stack) {
    val zeros = image.map { row -> row.filter { it == '0' }.size }.sum()
    if (zeros < minZeros) {
        minZeros = zeros
        val ones = image.map { row -> row.filter { it == '1' }.size }.sum()
        val twos = image.map { row -> row.filter { it == '2' }.size }.sum()
        onesByTwos = ones * twos
    }
}

println("part 1: $onesByTwos")

val finalImage: Image = mutableListOf()

i = 0
for (y in (0 until height)) {
    val row: Row = mutableListOf()
    for (x in (0 until width)) {
        row.add('2')
        i++
    }
    finalImage.add(row)
}

for (image in stack) {
    for (y in (0 until height)) {
        val row = image[y]
        val finalRow = finalImage[y]
        for (x in (0 until width)) {
            val pixel = row[x]
            if (pixel != '2' && finalRow[x] == '2')
                finalRow[x] = pixel
        }
    }
}

for (row in finalImage) {
    for (pixel in row) {
        when (pixel) {
            '0' -> print("X")
            '1' -> print(".")
            '2' -> print(" ")
        }

    }
    println()
}
