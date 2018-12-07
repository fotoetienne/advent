#!/usr/bin/env kscript
import java.io.File

/**
 * Advent of Code 2018 - Day 7
 * https://adventofcode.com/2018/day/7
 */

var lines = File("input07.txt").readLines()

data class Step(val id: Char) {
    val dependencies = mutableSetOf<Step>()
    var done = false
    fun ready() = !done && (dependencies.isEmpty() || dependencies.all { it.done })
    var time = id.toInt() - 4
    fun work() {
        time -= 1
        if (time == 0) done = true
    }
}

val steps = mutableMapOf<Char, Step>()

for (line in lines) {
    val dependency = steps.getOrPut(line[5]) { Step(line[5]) }
    val step = steps.getOrPut(line[36]) { Step(line[36]) }
    step.dependencies.add(dependency)
}

val stepSequence = mutableListOf<Char>()

while (steps.values.any { !it.done }) {
    val nextSteps = steps.values.filter { it.ready() }.sortedBy { it.id }
    val nextStep = nextSteps.first()
    stepSequence.add(nextStep.id)
    nextStep.done = true
}

println(stepSequence.toCharArray())

/** Part 2 **/

steps.values.map { it.done = false } // Reset

var time = 0
while (steps.values.any { !it.done }) {
    val nextSteps = steps.values.filter { it.ready() }.sortedBy { it.id }.take(5)
        .map { it.work() }
    time += 1
}

println(time)