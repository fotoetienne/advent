object Day17 : AocPuzzle<Set<Day17.Cube>>(17) {
    data class Cube(val x: Int, val y: Int, val z: Int)

    override fun parseInput(input: String): Set<Cube> {
        return input.lines().mapIndexed { y, row ->
            row.mapIndexed { x, c ->
                if (c == '#') Cube(x, y, 0) else null
            }
        }.flatten().filterNotNull().toSet()
    }

    infix operator fun Cube.plus(other: Cube) = Cube(x + other.x, y + other.y, z + other.z)

    val directions = (-1..1).flatMap { x ->
        (-1..1).flatMap { y ->
            (-1..1).map { z -> Cube(x, y, z) }
        }
    }.filter { it != Cube(0, 0, 0) }

    fun Cube.neighbors() = directions.map { this + it }

    fun Set<Cube>.activeNeighbors(cube: Cube) = cube.neighbors().filter { contains(it) }

    fun Set<Cube>.isActive(cube: Cube) = contains(cube)

    fun Set<Cube>.conway(): Set<Cube> {
        return filter { cube ->
            (2..3).contains(activeNeighbors(cube).size)
        }.toSet() + flatMap { it.neighbors() }.toSet().filter { !isActive(it) && activeNeighbors(it).size == 3 }
    }

    override fun part1(input: Set<Cube>): Any {
        var activeCubes = input
        for (i in (1..6)) {
            activeCubes = activeCubes.conway()
        }
        return activeCubes.size
    }

    data class HyperCube(val x: Int, val y: Int, val z: Int, val w: Int)
    fun Cube.toHyperCube() = HyperCube(x,y,z,0)
    infix operator fun HyperCube.plus(other: HyperCube) = HyperCube(x + other.x, y + other.y, z + other.z, w + other.w)
    val hyperDirections = (-1..1).flatMap { x ->
        (-1..1).flatMap { y ->
            (-1..1).flatMap { z ->
                (-1..1).map { w ->
                    HyperCube(x, y, z, w)
                }
            }
        }
    }.filter { it != HyperCube(0, 0, 0, 0) }

    fun HyperCube.neighbors() = hyperDirections.map { this + it }

    fun Set<HyperCube>.activeNeighbors(cube: HyperCube) = cube.neighbors().filter { contains(it) }

    fun Set<HyperCube>.isActive(cube: HyperCube) = contains(cube)

    fun Set<HyperCube>.hyperConway(): Set<HyperCube> {
        return filter { cube ->
            (2..3).contains(activeNeighbors(cube).size)
        }.toSet() + flatMap { it.neighbors() }.toSet().filter { !isActive(it) && activeNeighbors(it).size == 3 }
    }

    override fun part2(input: Set<Cube>): Any {
        var activeCubes = input.map { it.toHyperCube() }.toSet()
        for (i in (1..6)) {
            activeCubes = activeCubes.hyperConway()
        }
        return activeCubes.size
    }

    val exampleData = """
        .#.
        ..#
        ###
    """.trimIndent()

    override val part1Tests = listOf(TestSet(exampleData, 112))
    override val part2Tests = listOf(TestSet(exampleData, 848))
}

fun main() = Day17.testAndRun()