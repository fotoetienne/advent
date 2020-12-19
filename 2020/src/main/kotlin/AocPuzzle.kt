import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

abstract class AocPuzzle<T>(val day: Int, var debug: Boolean = false) {
    open fun parseInput(input: String): T = input as T

    fun log(s: Any? = "", newline: Boolean = true) {
        if (debug) {
            if (newline) println(s) else print(s)
        }
    }

    val rawInput: String by lazy { FetchInput().getInput(day).read() }
    fun puzzleInput() = parseInput(rawInput)

    abstract fun part1(input: T): Any
    abstract fun part2(input: T): Any

    @OptIn(ExperimentalTime::class)
    private fun runPart(part: Int, f: (T) -> Any) {
        print("Part $part: ")
        try {
            var t = measureTime {
                val solution = f(puzzleInput())
                println(solution)
            }
            println("runtime: ${t.inMilliseconds} ms")
        } catch (e: NotImplementedError) {
            println("Part $part not yet implemented")
        }
    }

    fun run() {
        println("# Running Day $day #")
        runPart(1, ::part1)
        runPart(2, ::part2)
    }

    open val part1Tests: List<TestSet> = emptyList()
    open val part2Tests: List<TestSet> = emptyList()

    fun test() {
        println("# Tests for Day $day #")
        runPartTests(1, ::part1, part1Tests)
        runPartTests(2, ::part2, part2Tests)
    }

    private fun runPartTests(part: Int, f: (T) -> Any, tests: List<TestSet>) {
        if (tests.isEmpty()) {
            println("No tests for part $part")
        } else {
            print("Part $part: ")
            var sucesses = 0
            var failures = 0
            for ((index, it) in tests.withIndex()) {
                if (!runTest(f, it)) {
                    println("Test $index failed")
                    failures += 1
                } else {
                    sucesses += 1
                }
            }
            println("$sucesses successes, $failures failures")
        }
    }

    private fun runTest(f: (T) -> Any, testSet: TestSet): Boolean {
        val result = f(parseInput(testSet.input))
        if (result.toString() != testSet.expected.toString()) {
            println("Test Failed: Expected ${testSet.expected}, got $result")
            return false
        }
        return true
    }

    fun testAndRun(debugLogging: Boolean? = null) {
        if (debugLogging != null) debug = debugLogging
        test()
        run()
    }
}

data class TestSet(val input: String, val expected: Any)