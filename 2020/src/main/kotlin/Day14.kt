import java.lang.StringBuilder
import kotlin.math.pow

typealias Bitmask = Array<Boolean?>
typealias Memory = MutableMap<Long, Long>

object Day14 : AocPuzzle<Day14.Computer>(14) {
    sealed class Instruction {
        data class Mask(val mask: Bitmask) : Instruction()
        data class Mem(val location: Long, val value: Long) : Instruction()
    }

    data class Computer(
        val initProgram: List<Instruction>,
        var mask: Bitmask = Array(36) { false },
        val memory: Memory = mutableMapOf(),
    )

    fun Bitmask.asString() = joinToString("") {
        when (it) {
            true -> "1"
            false -> "0"
            null -> "X"
        }
    }

    fun parseMask(s: String): Bitmask = s.map {
        when (it) {
            '0' -> false
            '1' -> true
            'X' -> null
            else -> error("Invalid character")
        }
    }.reversed().toTypedArray()

    override fun parseInput(input: String): Computer {
        val instructions = input.lines().map {
            val (k, v) = it.split(" = ")
            when (k) {
                "mask" -> Instruction.Mask(parseMask(v))
                else -> {
                    val location = Regex("""mem\[(\d+)]""").matchEntire(k)?.groupValues?.get(1)?.toLong()
                        ?: error("Unable to parse line: $it")
                    Instruction.Mem(location, v.toLong())
                }
            }
        }
        return Computer(instructions)
    }

    fun Long.applyMask(mask: Bitmask): Long {
        var out = this
        mask.forEachIndexed { index, b ->
            if (b == null) return@forEachIndexed
            out = if (b) {
                out or exp(index)
            } else {
                out and (exp(36) - 1 - exp(index))
            }
        }
        return out
    }

    override fun part1(computer: Computer): Any {
        computer.initProgram.forEach {
            when (it) {
                is Instruction.Mask -> computer.mask = it.mask
                is Instruction.Mem -> computer.memory[it.location] = it.value.applyMask(computer.mask)
            }
        }
        return computer.memory.values.sum()
    }

    fun Long.decode(mask: Bitmask): Collection<Long> {
        val binaryValue = toBinaryString().toMutableList()
        mask.forEachIndexed { index, b ->
            when (b) {
                true -> binaryValue[index] = '1'
                null -> binaryValue[index] = 'X'
            }
        }
        var longValues = listOf<Long>(0)
        binaryValue.forEachIndexed { index, c ->
            longValues = when (c) {
                '1' -> longValues.map { it + exp(index) }
                'X' -> longValues + longValues.map { it  + exp(index) }
                else -> longValues
            }
        }
        return longValues
    }

    fun Long.toBinaryString(): String {
        var v = this
        val out = mutableListOf<Char>()
        for (i in (64 downTo 0)) {
            val x = exp(i)
            if (v >= x) {
                v -= x
                out.add('1')
            } else {
                out.add('0')
            }
        }
        return out.reversed().joinToString("")
    }

    override fun part2(computer: Computer): Any {
        computer.initProgram.forEach { instruction ->
            when (instruction) {
                is Instruction.Mask -> computer.mask = instruction.mask
                is Instruction.Mem -> instruction.location.decode(computer.mask).forEach { decodedLocation ->
                    computer.memory[decodedLocation] = instruction.value
                }
            }
        }
        return computer.memory.values.sum()
    }

    val exampleData = """
        mask = XXXXXXXXXXXXXXXXXXXXXXXXXXXXX1XXXX0X
        mem[8] = 11
        mem[7] = 101
        mem[8] = 0
    """.trimIndent()

    override val part1Tests = listOf(TestSet(exampleData, 165))

    val example2 = """mask = 000000000000000000000000000000X1001X
mem[42] = 100
mask = 00000000000000000000000000000000X0XX
mem[26] = 1"""
    override val part2Tests = listOf(TestSet(example2, 208))
}

fun main() {
    Day14.testAndRun()
}