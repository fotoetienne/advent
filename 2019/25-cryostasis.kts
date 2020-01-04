#!/usr/bin/env kscript
@file:Include("fetchInput.kt")
@file:Include("intcodeComputer.kt")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")
@file:KotlinOpts("-J-Xmx5g")

import kotlinx.coroutines.channels.Channel

/**
 * Advent of Code 2019 - Day 25
 * https://adventofcode.com/2019/day/25
 */

val intCode get() = getInput(25).read().toIntCode()

data class Point(val x: Int, val y: Int)

typealias ShipMap = MutableMap<Point, Char>

fun ShipMap.print(currentLocation: Point? = null) {
    for (y in (keys.map(Point::y).min()!! .. keys.map(Point::y).max()!!)) {
        for (x in (keys.map(Point::x).min()!! .. keys.map(Point::x).max()!!)) {
            if (currentLocation != null && x == currentLocation.x && y == currentLocation.y) print('O')
            else print(getOrDefault(Point(x,y), ' '))
        }
        println()
    }
}

inner class Droid(intCode: IntCode, instructions: List<String>? = null) {
    var location = Point(0, 0)
    val shipMap: ShipMap = mutableMapOf(location to 'X')
    val inputBuffer = Channel<Char>(100)
    val instructionSeq = instructions?.asIterable()?.iterator()
    val instructionHistory = mutableListOf<String>()

    val computer = Computer(intCode) {
        if (inputBuffer.isEmpty) getInput()
        val next = inputBuffer.poll()
        next?.toLong() ?: throw RuntimeException("Input buffer is empty")
    }

    private fun updateMap(cmd: String) {
        location = when (cmd) {
            "north" -> location.copy(y = location.y - 1)
            "south" -> location.copy(y = location.y + 1)
            "west" -> location.copy(x = location.x - 1)
            "east" -> location.copy(x = location.x + 1)
            else -> location
        }
        shipMap.putIfAbsent(location, '.')
        if (cmd.startsWith("take")) shipMap.put(location, '*')
        shipMap.print(location)
    }

    private fun nextInput(): String {
        return if (instructionSeq != null && instructionSeq.hasNext()) instructionSeq.next().also { println(it) }
        else readLine()!!
    }

    private fun getInput() {
        val input = when (val inst = nextInput()) {
            "k" -> "north"
            "j" -> "south"
            "h" -> "west"
            "l" -> "east"
            " " -> "inv"
            else -> inst.trim()
        }

        instructionHistory.add(input)
        println(instructionHistory.takeLast(10))
        updateMap(input)
        (input + "\n").forEach { inputBuffer.offer(it) }
    }

    fun run() {
        while (true) {
            val status = computer.runOnce()
            computer.outputChannel.poll()?.toChar()?.let(::print)
        }
    }
}

val inst = "west, west, take antenna, south, take spool of cat6, north, west, south, south, north, north, " +
        "east, east, north, north, west, take semiconductor, east, south, west, south, take hypercube, north, east, " +
        "south, east, south, take mouse, west, take whirled peas, east, east, take shell, west, north, east, south, " +
        "south, take hologram, north, north, west, west, west, west, south, south, " +
        "drop hologram, drop spool of cat6, drop shell, drop whirled peas, inv, south"


val droid = Droid(intCode, inst.split(", "))

droid.run()


// Dont's take: photons, escape pod, molton lava, giant electromagnet,

// Too heavy: shell, spool of cat6, hologram

// droid >
// hypercube
// hypercube + antenna
// hypercube + antenna + semiconductor
// hypercube + antenna + whirled peas
// mouse
// whirled peas + semiconductor
// whirled peas + semiconductor + hypercube
// whirled peas + semiconductor + hypercube

// droid <
// hypercube + mouse
// hypercube + spool of cat6
// hypercube + antenna + whirled peas + shell
// hypercube + antenna + shell
// hypercube + shell
// shell
// spool of cat6
// hologram
// hypercube + whirled peas + mouse + antenna + semiconductor
// whirled peas + mouse + antenna + semiconductor
// whirled peas + antenna + semiconductor

// droid ==
// hypercube, antenna, semiconductor, mouse