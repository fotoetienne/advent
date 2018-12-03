#!/usr/bin/env kscript
import java.io.File

/**
 * Advent of Code 2018 - Day 2
 * https://adventofcode.com/2018/day/2
 */

val ids = File("input02.txt").readLines()

/** Returns frequency count of each character in a string */
fun hist(s: String) = s.groupingBy { it }.eachCount()

var doubles = 0
var triples = 0

for (id in ids)
    hist(id).values.let {
        if (it.contains(2)) doubles += 1
        if (it.contains(3)) triples += 1
    }

println("Checksum: ${doubles * triples}")

/** Part 2 **/

/**
 * Returns each permutation of removing a single character from a string
 * Appends the index of the character removed in order to disambiguate
 * Example: subsets("abc") = ["bc0","ac1","ab2"]
 */
fun subsets(s: String) = (0 until s.length).map { i ->
    s.substring(0, i) + s.substring(i + 1, s.length) + i.toString()
}

/**
 * Now just loop through all of the subsets and put them into a single set,
 * stopping when we find a duplicate (worst case O(n))
 */
val idSubsets = mutableSetOf<String>()
for (idSubset in ids.flatMap(::subsets))
    if (!idSubsets.add(idSubset)) {
        println("Id Match: ${idSubset.substring(0, 25)}") // Don't forget to remove the appended digits
        break
    }
