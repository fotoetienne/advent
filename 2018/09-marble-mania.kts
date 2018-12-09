#!/usr/bin/env kscript
@file:KotlinOpts("-J-Xmx2g")
@file:KotlinOpts("-J-Xms2g")
@file:CompilerOpts("-jvm-target 1.8")

import java.io.File

/**
 * Advent of Code 2018 - Day 9
 * https://adventofcode.com/2018/day/9
 */

var lines = File("input09.txt").readText()

val nPlayers = 459
val lastMarble = 7179000L
//val nPlayers = 10
//val lastMarble = 1618
//val nPlayers = 9
//val lastMarble = 25

val players = (1..nPlayers).map { 0L }.toMutableList()

class Marble<T>(val value: T, previous: Marble<T>? = null, next: Marble<T>? = null) {
    var previous: Marble<T> = previous ?: this
    var next: Marble<T> = next ?: this

    fun moveClockwise(n: Int): Marble<T> =
        if (n == 0) this else next.moveClockwise(n - 1)

    fun moveCounterClockwise(n: Int): Marble<T> =
        if (n == 0) this else previous.moveCounterClockwise(n - 1)

    fun remove() = next
        .also {
            it.previous = previous
            previous.next = it
        }

    fun add(value: T) = Marble(value, this, next)
            .also {
                next.previous = it
                next = it
            }

    fun insertMarble(nextMarble: T) =
        this.moveClockwise(1).add(nextMarble)

    fun getBonus() =
        this.moveCounterClockwise(7)
            .let { Pair(it.value, it.remove()) }

    override fun toString(): String {
        val list = mutableListOf(value)
        var nextMarble = next
        while (nextMarble.value != value) {
            list.add(nextMarble.value)
            nextMarble = nextMarble.next
        }
        return list.toString()
    }
}

var currentMarble = Marble(0L)
val marbles = currentMarble
var player = 1

for (marble in (1..lastMarble)) {
//    if (marble % 10_000 == 0L) println(marble)
//    print("[$marble]")
    if (marble % 23 == 0L) {
        players[player] += marble
        val (bonus, nextMarble) = currentMarble.getBonus()
        players[player] += bonus
        currentMarble = nextMarble
    } else {
        currentMarble = currentMarble.insertMarble(marble)
    }
//    println(marbles)

    if (player + 1 == nPlayers) player = 0 else player += 1
}

println(players.max())

