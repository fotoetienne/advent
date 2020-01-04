#!/usr/bin/env kscript
@file:Include("fetchInput.kt")
@file:Include("intcodeComputer.kt")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")
@file:KotlinOpts("-J-Xmx5g")

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

/**
 * Advent of Code 2019 - Day 23
 * https://adventofcode.com/2019/day/23
 */

val intCode get() = getInput(23).read().toIntCode()

data class Packet(val destination: Long, val x: Long, val y: Long)

inner class Droid(intCode: IntCode, val address: Int) {
    val inbox = Channel<Packet>(1024)
    val outbox = Channel<Packet>(1024)
    var inBuffer: Long? = address.toLong()
    var idleCount: Long = 0

    val computer = Computer(intCode) {
        if (inBuffer != null) {
            inBuffer!!.also { inBuffer = null }
        } else {
            inbox.poll()?.let { (_, x, y) ->
                x.also { inBuffer = y }
            } ?: -1
        }
            .also { if (it == -1L) idleCount++ else idleCount = 0 }
//            .also { if (it != -1) println("Droid $address receive $it") }
    }

    fun receive(packet: Packet) {
        if (!inbox.offer(packet)) throw RuntimeException("Mailbox $address full")
    }

    suspend fun run() {
        var out: Long? = 0
        var destination: Long? = null
        var x: Long? = null
        var y: Long? = null
        while (out != null) {
            computer.runAsync()
            out = computer.outputChannel.receive()
            if (out != null) {
//                println("Droid $address out: $out")
                when {
                    destination == null -> destination = out
                    x == null -> x = out
                    y == null -> {
                        y = out
                        val success = outbox.offer(Packet(destination, x, y))
                        destination = null
                        x = null
                        y = null
                        if (!success) throw RuntimeException("Droid $address outbox full")
                    }
                }
            }
            delay(10)
        }
        println("Droid $address exiting")
    }
}


suspend fun Computer.runAsync(): Boolean {
    try {
        var i = 0
        while (outputChannel.isEmpty) {
            if (currentOp.op == 3L) delay(1)
            if (i == 100) {
                delay(1)
                i = 0
            }
            if (!doOp()) return false
        }
    } catch (e: Exception) {
        throw e
    }
    return true
}

inner class DroidNet(intCode: IntCode, nDroids: Int = 50, val debug: Boolean = false, val natEnabled: Boolean = false) {
    val droids = (0 until nDroids).map { Droid(intCode, it) }
    val nat = if (natEnabled) Nat(droids, ::log) else null

    fun log(s: String) {
        if (debug) println(s)
    }

    fun send(packet: Packet) {
        when {
            packet.destination < droids.size ->
                droids[packet.destination.toInt()].receive(packet)
            packet.destination == 255L && nat != null -> {
                nat.receive(packet)
            }
            else -> {
                println("No droid at address ${packet.destination}")
                throw RuntimeException(packet.toString())
            }
        }
    }

    fun run() {
        println("Starting Droidnet")
        runBlocking {
            withContext(Dispatchers.Default) {
                val postman = launch {
                    while (true) {
                        for (droid in droids) {
                            val packet = droid.outbox.poll()
                            if (packet != null) {
                                log("${droid.address} -> $packet")
                                send(packet)
                            }
                        }
                        delay(10)
                    }
                }
                val droidJobs = droids.map { droid ->
                    //                    println("Launching droid ${droid.address}")
                    launch {
                        log("Launched droid ${droid.address}")
                        droid.run()
                    }
                }
                nat?.start()
                droidJobs.joinAll()
            }
        }
    }
}

// Part 1: (uncomment to run)
//DroidNet(intCode, 50).run()

/** Part 2 **/

val Droid.idle get() = inbox.isEmpty && inBuffer == null && idleCount > 10

inner class Nat(val droids: List<Droid>, val log: (String) -> Unit) {
    var memory: Packet? = null
    var idleCount = 0
    val IDLE_MAX = droids.size
    val sendLog: MutableList<Packet> = mutableListOf()

    fun receive(packet: Packet) {
        memory = packet.copy(destination = 0L)
    }

    fun send(): Boolean {
        if (memory == null) return false
        val packet = memory!!
        println("NAT -> $packet")
        droids[0].receive(packet)
        sendLog.add(packet)
        memory = null
        return true
    }

    suspend fun start() = coroutineScope {
        launch {
            while (true) {
                run()
                delay(1)
            }
        }
    }

    suspend fun run() {
        val idle = droids.all { it.idle }
        if (idle) idleCount++ else idleCount = 0
        if (idleCount > IDLE_MAX) {
            val status = send()
            if (!status) println("Nothing to send")
            delay(10)
            idleCount = 0
        }
        if (sendLog.size > 2) {
            val (a, b) = sendLog.takeLast(2)
            if (a.y == b.y) {
                log("Repeat: ${a.y}")
                throw RuntimeException("NAT Repeated ${a.y}")
            }
        }
    }
}

//val testCode = "3,60,1005,60,18,1101,0,1,61,4,61,104,1011,104,1,1105,1,22,1101,0,0,61,3,62,1007,62,0,64,1005,64,22,3,63,1002,63,2,63,1007,63,256,65,1005,65,48,1101,0,255,61,4,61,4,62,4,63,1105,1,22,99".toIntCode()
//DroidNet(testCode, 2).run()

DroidNet(intCode, 50, natEnabled = true).run()
// Not 1
// Not 5051