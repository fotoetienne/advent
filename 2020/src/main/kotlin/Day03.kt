object Day03 : AocPuzzle<Array<BooleanArray>>(3) {
    override fun parseInput(input: String): Array<BooleanArray> {
        return input.lines().map { line ->
            line.map { char ->
                when (char) {
                    '.' -> false
                    '#' -> true
                    else -> error("Invalid character")
                }
            }.toBooleanArray()
        }.toTypedArray()
    }

    class Toboggan(val map: Array<BooleanArray>, val slope: Coord = Coord(3, 1)) {
        var pos: Coord = Coord(0, 0)
        val width = map.first().size
        var trees = 0

        fun isHittingTree() = map[pos.y][pos.x]

        fun stillOnSlope() = pos.y < map.size

        fun move() = Coord((pos.x + slope.x) % width, pos.y + slope.y).also { pos = it }

        fun run(): Int {
            while (stillOnSlope()) {
                if (isHittingTree()) trees += 1
                move()
            }
            return trees
        }
    }

    override fun part1(input: Array<BooleanArray>): Int {
        return Toboggan(input).run()
    }

    override fun part2(input: Array<BooleanArray>): Int {
        val slopes = listOf(Coord(1, 1), Coord(3, 1), Coord(5, 1), Coord(7, 1), Coord(1, 2))
        return slopes.map { Toboggan(input, it).run() }.reduce { a, b -> a * b }
    }

    data class Coord(val x: Int, val y: Int)

    val example1 = """..##.......
#...#...#..
.#....#..#.
..#.#...#.#
.#...##..#.
..#.##.....
.#.#.#....#
.#........#
#.##...#...
#...##....#
.#..#...#.#"""

    override val part1Tests = listOf(TestSet(example1, 7))
    override val part2Tests = listOf(TestSet(example1, 336))
}

fun main() = Day03.testAndRun()