#!/usr/bin/env kscript
import java.io.File

/**
 * Advent of Code 2018 - Day 4
 * https://adventofcode.com/2018/day/4
 */

val lines = File("input04.txt").readLines().sorted()

data class Guard(val id: Int) {
    val sleepHist = IntArray(60)

    fun recordSleep(fellAsleep: Int, wokeUp: Int) =
        (fellAsleep until wokeUp).forEach { i -> sleepHist[i] += 1 }

    fun totalSleep() = sleepHist.sum()
    fun maxSleepFreq() = sleepHist.max() ?: 0
    fun sleepiestMinute() = sleepHist.indexOf(maxSleepFreq())
}

val guards = mutableMapOf<Int, Guard>()
var currentGuard: Guard? = null
var fellAsleep: Int = 0

for (line in lines) {
    val thisMinute = line.substring(15, 17).toInt()
    when (line.substring(19, 24)) {
        "falls" -> fellAsleep = thisMinute
        "wakes" -> currentGuard?.recordSleep(fellAsleep, thisMinute)
        "Guard" -> {
            val guardId = Regex("\\d+").find(line, 24)!!.value.toInt()
            currentGuard = guards.getOrPut(guardId, { Guard(guardId) })
        }
    }
}

print("Strategy 1 - Find the guard with the most total minutes of sleep: ")
guards.values.maxBy { it.totalSleep() }
    ?.apply { println("${id * sleepiestMinute()}") }

print("Strategy 2 - Find the guard who is most frequently asleep on the same minute: ")
guards.values.maxBy { it.maxSleepFreq() }
    ?.apply { println("${id * sleepiestMinute()}") }
