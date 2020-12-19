import kotlin.system.measureTimeMillis

object Day08 : AocPuzzle<List<Day08.Instruction>>(8) {
    override fun parseInput(input: String) = input.lines().map(::Instruction)

    enum class Operation {
        ACC, JMP, NOP
    }

    data class Instruction(val operation: Operation, val argument: Int) {
        constructor(line: String) : this(
            operation = Operation.valueOf(line.substringBefore(' ').toUpperCase()),
            argument = line.substringAfter(' ').toInt()
        )
    }

    fun runProgram(instructions: List<Instruction>): Pair<Boolean, Int> {
        var i = 0
        var accumulator = 0
        val instructionsExecuted = BooleanArray(instructions.size)

        while (!instructionsExecuted[i]) {
            instructionsExecuted[i] = true
            val (operation, argument) = instructions[i]
//            println("$i: $operation $argument")
            when (operation) {
                Operation.ACC -> {
                    accumulator += argument
                    i += 1
                }
                Operation.JMP -> {
                    i += argument
                }
                Operation.NOP -> {
                    i += 1
                }
            }
            if (i == instructions.size) {
                // Program terminates
                return Pair(true, accumulator)
            }
        }

        // Program doesn't terminate
        return Pair(false, accumulator)
    }

    override fun part1(instructions: List<Instruction>): Any {
        return runProgram(instructions).second
    }

    override fun part2(instructions: List<Instruction>): Any {
        val instructionVariants = instructions.indices.mapNotNull { i ->
            val op = when (instructions[i].operation) {
                Operation.ACC -> null
                Operation.JMP -> Operation.NOP
                Operation.NOP -> Operation.JMP
            }
            if (op != null) {
                instructions.toMutableList().let { list ->
                    list[i] = list[i].copy(operation = op)
                    list.toList()
                }
            } else null
        }

        instructionVariants.forEach { instructionSet ->
            val (terminates, returnValue) = runProgram(instructionSet)
            if (terminates) return returnValue
        }

        error("Never terminates :(")
    }

    private const val exampleData = """nop +0
acc +1
jmp +4
acc +3
jmp -3
acc -99
acc +1
jmp -4
acc +6"""

    override val part1Tests: List<TestSet> = listOf(TestSet(exampleData, 5))
    override val part2Tests: List<TestSet> = listOf(TestSet(exampleData, 8))
}

fun main() = Day08.testAndRun()
