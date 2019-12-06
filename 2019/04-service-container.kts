#!/usr/bin/env kscript
import java.io.File
import kotlin.math.abs
import kotlin.math.min
import kotlin.system.measureTimeMillis

/**
 * Advent of Code 2019 - Day 3
 * https://adventofcode.com/2019/day/3
 */

val (lower, upper) = File("input04.txt").readText().split('-').map { it.toInt() }

fun hasDouble(password: String): Boolean {
    for (c in (1 until password.length)) {
        if (password[c] == password[c - 1]) {
            return true
        }
    }
    return false
}

fun monotonic(password: String): Boolean {
    for (c in (1 until password.length)) {
        if (password[c].toInt() < password[c - 1].toInt()) {
            return false
        }
    }
    return true
}

check(hasDouble("122345"))
check(!hasDouble("123456"))
check(hasDouble("123455"))
check(hasDouble("112345"))

check(monotonic("122345"))
check(!monotonic("123245"))

fun passwordsInRange(validate: (String) -> Boolean): Int {
    var count = 0
    for (i in (lower .. upper)) {
        if (validate(i.toString())) {
            count += 1
        }
    }
    return count
}

println(passwordsInRange {hasDouble(it) && monotonic(it)})

/* Part 2 */

fun hasDoubleNoTriple(password: String): Boolean {
    for (c in (1 until password.length)) {
        if (password[c] == password[c - 1]) {
            if (c == password.length - 1 || password[c] != password[c + 1]) {
                if (c == 1 || password[c] != password[c - 2]) {
                    return true
                }
            }
        }
    }
    return false
}

check(hasDoubleNoTriple("122345"))
check(!hasDoubleNoTriple("122245"))
check(!hasDoubleNoTriple("222345"))
check(hasDoubleNoTriple("122333"))
check(hasDoubleNoTriple("112345"))
check(hasDoubleNoTriple("112355"))
check(hasDoubleNoTriple("111122"))

println(passwordsInRange {hasDoubleNoTriple(it) && monotonic(it)})
