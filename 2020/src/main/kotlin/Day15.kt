object Day15 : AocPuzzle<List<Int>>(15) {
    override fun parseInput(input: String) = input.split(',').map(String::toInt)

    override fun part1(input: List<Int>): Any {
        val numbers = input.dropLast(1).toMutableList()
        var previous = input.last()
        while (numbers.size < 2020) {
            val preprevious = numbers.lastIndexOf(previous)
            val next = if (preprevious == -1) {
                0
            } else {
                numbers.size - preprevious
            }
            numbers.add(previous)
            previous = next
        }
        log(numbers)
        return numbers.last()
    }

    override fun part2(input: List<Int>): Any {
        val numbers = input.dropLast(1)
            .mapIndexed { index, i -> i to index + 1 }.toMap().toMutableMap()
        var lastSpoken = input.last()
        for (i in (input.size until 30000000)) {
            val previousOccurrence = numbers[lastSpoken]
            numbers[lastSpoken] = i
            lastSpoken = if (previousOccurrence == null) 0 else i - previousOccurrence
        }
        return lastSpoken
    }

    val exampleData = "0,3,6"

    override val part1Tests = listOf(
        TestSet(exampleData, 436)
    )
    override val part2Tests = listOf(
        TestSet(exampleData, 175594),
//        TestSet("1,3,2", 2578),
//        TestSet("2,1,3", 3544142),
//        TestSet("1,2,3", 261214),
//        TestSet("2,3,1", 6895259),
//        TestSet("3,2,1", 18),
//        TestSet("3,1,2", 362),
    )
}

fun main() = Day15.testAndRun(false)