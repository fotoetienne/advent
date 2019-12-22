#!/usr/bin/env kscript
@file:Include("fetchInput.kt")
@file:KotlinOpts("-J-Xmx5g")

import java.math.BigInteger
import java.math.BigInteger.ONE

/**
 * Advent of Code 2019 - Day 22
 * https://adventofcode.com/2019/day/22
 */

val input get() = getInput(22).lines()

typealias Deck = List<Int>

fun Deck.dealIntoNewStack() = asReversed()

fun Deck.cut(n: Int) = if (n > 0) drop(n) + take(n) else takeLast(-n) + dropLast(-n)

fun Deck.dealWithIncrement(n: Int): Deck {
    val result = IntArray(size)
    for (i in indices) {
        val idx = (i * n) % size
        result[idx] = get(i)
    }
    return result.toList()
}

fun Deck.doOp(s: String): Deck {
    val words = s.split(" ")
    if (words[0] == "cut") {
        val n = words[1].toInt()
        return cut(n)
    } else if (words[1] == "with") {
        val n = words[3].toInt()
        return dealWithIncrement(n)
    } else if (words[1] == "into") {
        return dealIntoNewStack()
    } else {
        throw RuntimeException("Unknown instruction $s")
    }
}

fun shuffle(instructions: List<String>, nCards: Int = 10007, position: Int = 2019): Int {
    var deck = (0 until nCards).toList()
    for (inst in instructions) {
        deck = deck.doOp(inst)
    }
    return deck.indexOfFirst { it == position }
}

val part1 = shuffle(input)
println("part 1: $part1")

/** Part 2 **/

val N_CARDS = 119315717514047.toBigInteger()
val N_SHUFFLES = 101741582076661.toBigInteger()
val TARGET_CARD = 2020.toBigInteger()

// A "Virtual" deck
inner class VDeck(val size: Long, val position: Long) {
    val nCards = size.toBigInteger()
    var pointer = position.toBigInteger()

    fun dealIntoNewStack() {
        pointer = nCards - pointer - 1.toBigInteger()
    }

    fun cut(n: BigInteger) {
        pointer = (pointer - n) % nCards
    }

    fun reverseDealIntoNewStack() = dealIntoNewStack()

    fun reverseCut(n: BigInteger) {
        pointer = (pointer + nCards + n) % nCards
    }

    fun dealWithIncrement(n: BigInteger) {
        pointer = (pointer * n) % nCards
    }

    fun reverseDealWithIncrement(n: BigInteger) {
        val inv = n.modInverse(nCards)
        pointer = pointer * inv % nCards
    }

    fun doOp(s: String) {
        val words = s.split(" ")
        when {
            words[0] == "cut" -> {
                val n = words[1].toBigInteger()
                cut(n)
            }
            words[1] == "with" -> {
                val n = words[3].toBigInteger()
                dealWithIncrement(n)
            }
            words[1] == "into" -> {
                dealIntoNewStack()
            }
            else -> {
                throw RuntimeException("Unknown instruction $s")
            }
        }
    }

    fun reverseOp(s: String) {
        val words = s.split(" ")
        when {
            words[0] == "cut" -> {
                val n = words[1].toBigInteger()
                reverseCut(n)
            }
            words[1] == "with" -> {
                val n = words[3].toBigInteger()
                reverseDealWithIncrement(n)
            }
            words[1] == "into" -> {
                reverseDealIntoNewStack()
            }
            else -> {
                throw RuntimeException("Unknown instruction $s")
            }
        }
    }
}

// Virtual Shuffle
fun vshuffle(
    instructions: List<String>,
    nCards: Long = N_CARDS.toLong(),
    pos: Long = TARGET_CARD.toLong(),
    repeat: Long = N_SHUFFLES.toLong(),
    reverse: Boolean = false
): BigInteger {
    val deck = VDeck(nCards, pos)
    val inst = if (reverse) instructions.reversed() else instructions
    for (i in (0 until repeat))
        for (inst in inst) {
            if (reverse) deck.reverseOp(inst)
            else deck.doOp(inst)
        }
    return deck.pointer
}

check(vshuffle(input, 10007, 2019, 1, reverse = false) == part1.toBigInteger())
check(vshuffle(input, 10007, part1.toLong(), 1, reverse = true) == 2019.toBigInteger())

// Some help from https://www.reddit.com/r/adventofcode/comments/ee0rqi/2019_day_22_solutions/fbnifwk/

// Let f(x) be a reverse shuffle
// All operations are linear, thus there exist integers A and B such that f(x) = a*x + b (mod n).

// x = 2020
// y = f(x) = f(2020)
// z = f(f(x)) = f(y) = f(f(2020)

// f(x) = a*x + b (mod n)
// y = a*x + b
// z = a*y + b
// y-z = a*(x-y)
// a = (y-z)/(x-y)
// b = y - a*x

// a = (y - z) * modInv(x - y, n) % n
// b = (y - a*x) % n
fun basis(
    x: BigInteger = TARGET_CARD,
    n: BigInteger = N_CARDS
): Pair<BigInteger, BigInteger> {
    val y = vshuffle(input, pos = x.toLong(), repeat = 1, reverse = true)
    val z = vshuffle(input, pos = y.toLong(), repeat = 1, reverse = true)
    val a = (y - z) * (x - y).modInverse(n)
    val b = (y - a * x) % n
    return Pair(a, b)
}

val (a, b) = basis()

fun bshuffle(x: BigInteger) = x * a % N_CARDS + b

check(bshuffle(2020.toBigInteger()) == vshuffle(input, repeat = 1, reverse = true))

/*
f(f(f(x))) = A*(A*(A*x+B)+B)+B
           = A^3*x + A^2*B + A*B + B
In general:

f^n(x) = A^n*x + A^(n-1)*B + A^(n-2)*B + ... + B
       = A^n*x + (A^(n-1) + A^(n-2) + ... + 1) * B
       = A^n*x + (A^n-1) / (A-1) * B
 */
fun nshuffle(nShuffles: BigInteger, x: BigInteger) =
    (a.modPow(nShuffles, N_CARDS) * x +
            (a.modPow(nShuffles, N_CARDS) - ONE)
            * (a - ONE).modInverse(N_CARDS) * b) % N_CARDS

check(nshuffle(ONE, TARGET_CARD) == bshuffle(TARGET_CARD))
check(nshuffle(2.toBigInteger(), TARGET_CARD) == bshuffle(bshuffle(TARGET_CARD)))
println("part 2: ${nshuffle(N_SHUFFLES, TARGET_CARD)}")
