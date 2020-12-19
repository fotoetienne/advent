import java.math.BigInteger
import kotlin.math.exp
import kotlin.system.measureTimeMillis

object Day13 : AocPuzzle<Day13.BusSchedule>(13) {
    override fun parseInput(input: String): BusSchedule {
        val (departure, busList) = input.lines()
        return BusSchedule(departure.toInt(), busList.split(',').map { if (it == "x") null else it.toInt() })
    }

    data class BusSchedule(val departureTime: Int, val busses: List<Int?>)

    override fun part1(input: BusSchedule) = input.busses.filterNotNull()
        .map { bus ->
            val nextBus = bus * ((input.departureTime / bus) + 1)
            val waitingTime = nextBus - input.departureTime
            Pair(bus, waitingTime)
        }.minByOrNull { it.second }!!
        .run { first * second }

    /**
     * Determine if a given start time, t, satisfied part 2
     */
    fun tryTime(busses: List<Pair<Int, Int>>, t: Long): Boolean {
        return busses.all { (busId, offset) ->
            val departureTime = t + offset
            departureTime % busId == 0L
        }
    }

    /**
     * Brute Force solution for Chinese remainder theorem
     *
     * https://en.wikipedia.org/wiki/Chinese_remainder_theorem#Systematic_search
     */
    fun chineseRemainderBF(congruenceSystem: Collection<Pair<Int, Int>>): Long {
        val N = congruenceSystem.fold(1L) { product, (a, n) -> n * product }
        return (0..N).find { x ->
            congruenceSystem.all { (a, n) -> x % n == a.toLong() % n }
        } ?: error("No solution found")
    }

    /**
     * Chinese remainder via Bézout's identity
     */
    fun chineseRemainderBZ(congruenceSystem: Collection<Pair<Int, Int>>): BigInteger {
        // chinese remainder on a single pair moduli
        val chinesePair = { (a1, n1): Pair<BigInteger, BigInteger>, (a2, n2): Pair<BigInteger, BigInteger> ->
            val (m1, m2) = bézoitIdentity(n1, n2)
            val N = n1 * n2
            val x = ((a1 * m2) % N * n2) % N + ((a2 * m1) % N * n1) %N
            (x % N + N) % N
        }

        return congruenceSystem
            .map { it.first.toBigInteger() to it.second.toBigInteger() }
            .reduce { a, b ->
                chinesePair(a, b) to (a.second * b.second)
            }.first
    }

    fun chineseRemainderTest() {
        val congruenceSystem = listOf(0 to 3, 3 to 4, 4 to 5)
        if (chineseRemainderBF(congruenceSystem) != 39L) error("fail")
        if (chineseRemainderBZ(congruenceSystem) != 39.toBigInteger()) error("fail")
        println("Chinese Remainder test passed")
    }

    /**
     * Determine Bézoit Identity numbers via the Extended Euclidean Algorithm
     *
     * i.e. given {a, b} return {x,y} such that
     *    gcd(a, b) = a * x + b * y
     *
     */
    fun bézoitIdentity(a: BigInteger, b: BigInteger): Pair<BigInteger, BigInteger> {
        val r = mutableListOf(a, b)
        val s = listOf(1, 0).map { it.toBigInteger() }.toMutableList()
        val t = listOf(0, 1).map { it.toBigInteger() }.toMutableList()
        var q = mutableListOf<BigInteger>()
        var i = 1
        while (r.last() != 0.toBigInteger()) {
            val qi = r[i - 1] / r[i]
            q.add(qi)
            r.add(r[i - 1] - qi * r[i])
            s.add(s[i - 1] - qi * s[i])
            t.add(t[i - 1] - qi * t[i])
            i++
        }
        return Pair(s[i - 1], t[i - 1])
    }

    fun bézoitIdentityTest() {
        fun testCase(a: Int, b: Int, expected: Pair<Int, Int>) {
            val result = bézoitIdentity(a.toBigInteger(), b.toBigInteger())
            if (result != expected.first.toBigInteger() to expected.second.toBigInteger())
                error("Fail: Expected $expected, got $result")

        }
        testCase(240, 46, Pair(-9, 47))
        testCase(12, 42, Pair(-3, 1))
        testCase(3, 4, Pair(-1, 1))
        println("Bézoit Identity test passed")
    }

    /**
     * Brute force solution to part 2
     */
    fun part2BF(input: BusSchedule): Long {
        val busses = input.busses
            .mapIndexedNotNull { i, busId -> if (busId == null) null else busId to i }
        return (0L..Long.MAX_VALUE).find { tryTime(busses, it) } ?: error("No time found :(")
    }

    /**
     * Part 2 using Chinese remainder theorem
     */
    fun part2CN(input: BusSchedule): BigInteger {
        val busses = input.busses
            .mapIndexedNotNull { i, busId -> if (busId == null) null else busId to i }
        val congruenceSystem = busses.map { (b, t) -> b - t to b }
        return chineseRemainderBZ(congruenceSystem)
    }

    /**
     * Part 2 using a sieve
     */
    fun part2SV(input: BusSchedule): Long {
        val busses = input.busses
            .mapIndexedNotNull { i, busId -> if (busId == null) null else busId to i }
            .sortedByDescending { it.first }
        var startTime = 0L
        var product = 1L

        for ((b, t) in busses) {
            while ((startTime + t) % b != 0L) {
                startTime += product
            }
            product *= b
            log("t + $t = 0 (mod $b)")
        }

        return startTime
    }

    /**
     * for busses b1,b2,b3 where all b are prime
     * with time t1,t2,t3
     * find x such that
     * 0 = (x + t1) % b1 = (x + t2) % b2 = (x + t3) % b3
     * or
     * b1 - t1 = x (mod b1) && b2 - t2 = x (mod b2) && c3 - t3 = x (mod b3)
     *
     * or
     *
     * x = a (mod b) for all b
     *   where a = b - t
     *
     * Because all busses are prime, all times for each bus form a finite field or ring of integers module b
     *
     * We thus need to find the least common element of these finite fields
     *
     * Because all busses are prime, there exists a modular multiplicative inverse , b', for b
     * such that a * b' = 1 (mod b)
     *
     * Further, the set of busses are elements of a Residue Number System (RNS)
     *
     * The set of a define an integer present within the residue number system
     *
     *
     * Example: 17, x, 13, 19
     * RNS = {13, 17, 19}
     * xn = {13 - 2, 17, 19 - 3} = {11, 17, 16}
     *
     */
    override fun part2(input: BusSchedule): Any {
        return part2SV(input)
    }

    val exampleData = """
        939
        7,13,x,x,59,x,31,19
    """.trimIndent()

    override val part1Tests = listOf(TestSet(exampleData, 295))
    override val part2Tests = listOf(
        TestSet("0\n17,x,13,19", 3417),
        TestSet(exampleData, 1068781),
        TestSet("0\n67,7,59,61", 754018),
        TestSet("0\n67,x,7,59,61", 779210),
        TestSet("0\n67,7,x,59,61", 1261476),
        TestSet("0\n1789,37,47,1889", 1202161486),
    )
}

fun main() {
    Day13.bézoitIdentityTest()
    Day13.chineseRemainderTest()
    Day13.testAndRun()
}