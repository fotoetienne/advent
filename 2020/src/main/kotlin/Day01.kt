object Day01 : AocPuzzle<List<Int>>(1) {
    override fun parseInput(input: String) = input.lines().map(String::toInt)

    override fun part1(entries: List<Int>): Any {
        for (a in entries) {
            for (b in entries) {
                if (a + b == 2020) {
                    return a * b
                }
            }
        }
        error("No answer found")
    }

    override fun part2(entries: List<Int>): Any {
        for (a in entries) {
            for (b in entries) {
                for (c in entries) {
                    if (a + b + c == 2020) {
                        return a * b * c
                    }
                }
            }
        }
        error("No answer found")
    }

    val exampleData = """1721
979
366
299
675
1456"""

    override val part1Tests = listOf(TestSet(exampleData, 514579))
    override val part2Tests = listOf(TestSet(exampleData, 241861950))
}

fun main() = Day01.testAndRun()