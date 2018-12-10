#!/usr/bin/env kscript
@file:KotlinOpts("-J-Xmx2g")
@file:KotlinOpts("-J-Xms2g")

/**
 * Advent of Code 2018 - Day 9
 * https://adventofcode.com/2018/day/9
 */

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
}

fun marbleGame(nPlayers: Int, nMarbles: Int): Long {
    val players = (1..nPlayers).map { 0L }.toMutableList()

    var currentMarble = Marble(0L)
    var player = 1

    for (marble in (1..nMarbles)) {
        if (marble % 23 == 0) {
            players[player] += marble.toLong()
            val (bonus, nextMarble) = currentMarble.getBonus()
            players[player] += bonus
            currentMarble = nextMarble
        } else {
            currentMarble = currentMarble.insertMarble(marble.toLong())
        }

        if (player + 1 == nPlayers) player = 0 else player += 1
    }
    return players.max()!!
}

//val nPlayers = 9
//val lastMarble = 25
//val nPlayers = 10
//val lastMarble = 1618
val nPlayers = 459
val lastMarble = 71790

println(marbleGame(nPlayers, lastMarble))
println(marbleGame(nPlayers, lastMarble * 100))
