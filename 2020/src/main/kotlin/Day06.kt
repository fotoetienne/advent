object Day06 : AocPuzzle<List<String>>(6) {
    override fun parseInput(input: String): List<String> {
        return input.split("\n\n")
    }

    override fun part1(input: List<String>): Any {
        return input.sumBy { group ->
            group.lines().map { it.toSet() }
                .reduce { a, b -> a.union(b) }
                .size
        }
    }

    override fun part2(input: List<String>): Any {
        return input.sumBy { group ->
            group.lines().map { it.toSet() }
                .reduce { a, b -> a.intersect(b) }
                .size
        }
    }

    val exampleInput = """
    abc

    a
    b
    c

    ab
    ac

    a
    a
    a
    a

    b
""".trimIndent()

    override val part1Tests = listOf(TestSet(exampleInput, 11))
    override val part2Tests = listOf(TestSet(exampleInput, 6))
}

fun main() = Day06.testAndRun()