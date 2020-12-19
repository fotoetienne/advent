// Template
object Day10 : AocPuzzle<List<Int>>(10) {
    override fun parseInput(input: String) = input.lines().map(String::toInt)

    override fun part1(input: List<Int>): Int {
        val adapters = mutableSetOf(*input.toTypedArray())
        val usedAdapters = mutableSetOf<Int>()
        var oneJolt = 0
        var twoJolt = 0
        var threeJolt = 0

        while (adapters.isNotEmpty()) {
            val currentJoltage = usedAdapters.maxOrNull() ?: 0
            val jump = if (adapters.contains(currentJoltage + 1)) {
                oneJolt += 1
                1
            } else if (adapters.contains(currentJoltage + 2)) {
                twoJolt += 1
                2
            } else if (adapters.contains(currentJoltage + 3)) {
                threeJolt += 1
                3
            } else {
                error("No eligable adabter found")
            }
            val nextAdapter = currentJoltage + jump
//            log("$nextAdapter:$jump")
            usedAdapters.add(nextAdapter)
            adapters.remove(nextAdapter)
        }
        threeJolt += 1
        return oneJolt * threeJolt
    }

    fun jolts(joltage: Int, adapters: Set<Int>, cache: MutableMap<Int, Long>): Long =
        cache.getOrPut(joltage) {
            if (joltage == adapters.maxOrNull()) 1
            else (1..3).map { jump ->
                if (adapters.contains(joltage + jump)) jolts(joltage + jump, adapters, cache) else 0
            }.sum()
        }



    override fun part2(input: List<Int>): Any {
        val adapters = setOf(*input.toTypedArray())
        return jolts(0, adapters, mutableMapOf())
    }

    val exampleData = """
        16
        10
        15
        5
        1
        11
        7
        19
        6
        12
        4
    """.trimIndent()

    val largerExample = """28
33
18
42
31
14
46
20
48
47
24
23
49
45
19
38
39
11
1
32
25
35
8
17
7
9
4
2
34
10
3"""

    override val part1Tests = listOf(TestSet(exampleData, 7 * 5), TestSet(largerExample, 22 * 10))
    override val part2Tests = listOf(TestSet(exampleData, 8L), TestSet(largerExample, 19208L))
}

fun main() = Day10.testAndRun()