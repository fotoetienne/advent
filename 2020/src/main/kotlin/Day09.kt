class Day09(val contextLength: Int = 25) : AocPuzzle<List<Long>>(9) {
    override fun parseInput(input: String) = input.lines().map(String::toLong)

    fun findMatch(numbers: Collection<Long>, sum: Long): Boolean {
        for (a in numbers) {
            for (b in numbers) {
                if (a + b == sum) return true
            }
        }
        return false
    }

    override fun part1(numbers: List<Long>): Long {
        for (i in (contextLength until numbers.lastIndex)) {
            val context = numbers.subList(i - contextLength, i)
            val success = findMatch(context, numbers[i])
            if (!success) return numbers[i]
        }
        error("No result found :(")
    }

    override fun part2(numbers: List<Long>): Long {
        val invalidNumber = part1(numbers)

        var begin = 0
        var end = 0
        var sum = numbers[0]

        while (sum != invalidNumber) {
            if (sum < invalidNumber) {
                end += 1
                sum += numbers[end]
            } else if (sum > invalidNumber) {
                sum -= numbers[begin]
                begin += 1
            }
        }
        val contiguousSet = numbers.subList(begin, end + 1)
        return contiguousSet.minOrNull()!! + contiguousSet.maxOrNull()!!
    }

    val exampleData = """35
20
15
25
47
40
62
55
65
95
102
117
150
182
127
219
299
277
309
576"""

    override val part1Tests = listOf(TestSet(exampleData, 127L))
    override val part2Tests = listOf(TestSet(exampleData, 62L))
}

fun main() {
    Day09(5).test()
    Day09(25).run()
}