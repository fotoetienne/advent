#!/usr/bin/env kscript
import java.io.File

/**
 * Advent of Code 2018 - Day 8
 * https://adventofcode.com/2018/day/8
 */

var license = File("input08.txt").readText()
    .split(" ")
    .map { it.trim().toInt() }

fun countNode(i: Int): Pair<Int, Int> {
    val children = license[i]
    val metadata = license[i + 1]
    var count = 0
    var cursor = i + 1

    for (child in (1..children)) {
        val (childCount, childEnd) = countNode(cursor + 1)
        count += childCount
        cursor = childEnd
    }

    count += license.slice((1..metadata).map { it + cursor }).sum()

    cursor += metadata
    return Pair(count, cursor)
}

println(countNode(0).first)

/** Part 2 */

fun nodeValue(i: Int): Pair<Int, Int> {
    val children = license[i]
    val metadata = license[i + 1]

    if (children == 0) {
        return Pair(license.subList(i + 2, i + 2 + metadata).sum(), i + 1 + metadata)
    }

    val childValues = mutableListOf<Int>()
    var cursor = i + 1
    for (child in (1..children)) {
        val (childValue, childEnd) = nodeValue(cursor + 1)
        childValues.add(childValue)
        cursor = childEnd
    }


    var count = 0
    for (metadatum in license.slice((1..metadata).map { it + cursor })) {
        if (metadatum <= childValues.size) {
            count += childValues[metadatum - 1]
        }
    }

    cursor += metadata
    return Pair(count, cursor)
}

println(nodeValue(0).first)

