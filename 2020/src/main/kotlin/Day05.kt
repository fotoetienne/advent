object Day05: AocPuzzle<List<Day05.Seat>>(5) {
    override fun parseInput(input: String) = input.lines().map(::parseSeat)

    data class Seat(val row: Int, val column: Int, val seatId: Int = row * 8 + column)

    fun parseSeat(s: String): Seat {
        val row = s.subSequence(0,7).map { if (it == 'B') 1 else 0 }.joinToString("").toInt(2)
        val col = s.subSequence(7,10).map { if (it == 'R') 1 else 0 }.joinToString("").toInt(2)
        return Seat(row, col)
    }

    override fun part1(input: List<Seat>): Int {
        return input.map(Seat::seatId).maxOrNull()!!
    }

    override fun part2(input: List<Seat>): Seat {
        val rows = input.map(Seat::row).toSet()
        val maxRow = rows.max()!!
        val minRow = rows.min()!!
        val seats = Array(maxRow + 1) { BooleanArray(8) }
        for (seat in input) {
            seats[seat.row][seat.column] = true
        }

        if (debug) {
            log("")
            log("01234567")
            for ((row, cols) in seats.withIndex()) {
                for ((col, seatFilled) in cols.withIndex()) {
                    log(if (seatFilled) '.' else '*', newline = false)
                }
                log(row)
            }
        }

        for ((row, cols) in seats.sliceArray(minRow + 1..maxRow - 1).withIndex()) {
            for ((col, seatFilled) in cols.withIndex()) {
                if (!seatFilled) return Seat(row + minRow + 1, col)
            }
        }
        error("No empty seats found :(")
    }

    val exampleData = """
        BFFFBBFRRR
        FFFBBBFRRR
        BBFFBBFRLL
    """.trimIndent()

    override val part1Tests = listOf(TestSet(exampleData, 820))
//    override val part2Tests = listOf(TestSet(exampleData, 0))
}

fun main() = Day05.testAndRun()