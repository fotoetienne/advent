#!/usr/bin/env kscript
@file:KotlinOpts("-J-Xmx16g")
@file:KotlinOpts("-J-Xms2g")
@file:CompilerOpts("-jvm-target 1.8")

import java.io.File

/**
 * Advent of Code 2018 - Day 9
 * https://adventofcode.com/2018/day/9
 */

var lines = File("input09.txt").readText()

val nPlayers = 10
val lastMarble = 1618
//val nPlayers = 459
//val lastMarble = 71790

val players = (1..nPlayers).map { 0 }.toMutableList()

fun insertMarble(marbles: MutableList<Int>, currentMarbleIndex: Int, nextMarble: Int): Int {
    val nextMarbleIndex = (currentMarbleIndex + 2) % marbles.size
    marbles.add(nextMarbleIndex, nextMarble)
    return nextMarbleIndex
}

fun getBonus(marbles: MutableList<Int>, currentMarbleIndex: Int): Pair<Int, Int> {
    val bonusMarbleIndex = (currentMarbleIndex - 7 + marbles.size) % marbles.size
    val bonus = marbles.removeAt(bonusMarbleIndex)
    return Pair(bonus, bonusMarbleIndex)
}

val marbles = mutableListOf(0)
var currentMarbleIndex = 1
var player = 1

for (marble in (1..lastMarble)) {
    if (marble % 23 == 0) {
        players[player] += marble
        val (bonus, bonusIndex) = getBonus(marbles, currentMarbleIndex)
        players[player] += bonus
        currentMarbleIndex = bonusIndex
    } else {
        currentMarbleIndex = insertMarble(marbles, currentMarbleIndex, marble)
    }

    if (player + 1 == nPlayers) player = 0 else player += 1
}

println(players.max())

