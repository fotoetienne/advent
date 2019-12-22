#!/usr/bin/env kscript
@file:Include("fetchInput.kt")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-collections-immutable-jvm:0.3")
@file:KotlinOpts("-J-Xmx5g")

import java.util.*

/**
 * Advent of Code 2019 - Day 20
 * https://adventofcode.com/2019/day/20
 */

val input get() = getInput(20).readText()

typealias MazeArray = Array<CharArray>

data class Point(val x: Int, val y: Int) {
    val neighbors
        get() = listOf(copy(x = x - 1), copy(x = x + 1), copy(y = y - 1), copy(y = y + 1))
}

operator fun MazeArray.get(point: Point) = try {
    this[point.y][point.x]
} catch (e: Exception) {
    '#'
}

fun MazeArray.print() {
    println(" " + this[0].indices.map { it % 10 }.joinToString(""))
    for (y in this.indices) {
        print(y % 10)
        for (x in this[y].indices) {
            print(this[y][x])
        }
        println()
    }
}

fun Char.isPassage() = this == '.'

class Maze(val mazeInput: String) {
    val mazeArray = mazeInput.split("\n").map { it.toCharArray() }.toTypedArray()
    val portals = findPortals()
    lateinit var start: Point
    lateinit var finish: Point
    val mazeMap = createMazeMap()

    private fun findPortals(): Map<Point, Point> {
        val portals = mutableMapOf<Point, String>()

        for (y in mazeArray.indices) {
            for (x in mazeArray[y].indices) {
                val p = Point(x, y)
                if (mazeArray[p].isUpperCase()) {
                    val p2 = p.neighbors.first { mazeArray[it].isUpperCase() }
                    val entry = (p.neighbors + p2.neighbors).first { mazeArray[it].isPassage() }
                    val portalName = mazeArray[p].toString() + mazeArray[p2]
                    if (!portals.containsKey(entry)) {
                        when (portalName) {
                            "AA" -> start = entry
                            "ZZ" -> finish = entry
                            else -> portals[entry] = portalName
                        }
                    }
                }
            }
        }
        val portalsMap = portals.mapValues { (p, name) ->
            portals.entries.first { (p2, n2) -> p != p2 && name == n2 }.key
        }
        return portalsMap
    }

    private fun createMazeMap(): Map<Point, List<Point>> {
        val mazeMap = mutableMapOf<Point, List<Point>>()
        for (y in mazeArray.indices) {
            for (x in mazeArray[y].indices) {
                val p = Point(x, y)
                val neighbors = (p.neighbors.filter { mazeArray[it].isPassage() } + portals[p]).filterNotNull()
                if (neighbors.isNotEmpty())
                    mazeMap[p] = neighbors
            }
        }
        return mazeMap
    }

    fun bfs(): Int {
        val visited = mutableSetOf<Point>()
        val queue = LinkedList<Pair<Point, Int>>()
        visited.add(start)
        queue.add(Pair(start, 0))
        while (queue.size != 0) {
            val (p, dist) = queue.poll()
            if (p == finish) return dist
            for (neighbor in mazeMap.getValue(p)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor)
                    queue.add(Pair(neighbor, dist + 1))
                }
            }
        }
        return -1
    }
}

val testMaze1 = """         A           
         A           
  #######.#########  
  #######.........#  
  #######.#######.#  
  #######.#######.#  
  #######.#######.#  
  #####  B    ###.#  
BC...##  C    ###.#  
  ##.##       ###.#  
  ##...DE  F  ###.#  
  #####    G  ###.#  
  #########.#####.#  
DE..#######...###.#  
  #.#########.###.#  
FG..#########.....#  
  ###########.#####  
             Z       
             Z       """

val maze = Maze(testMaze1)
//maze.mazeArray.print()
println("part1: ${maze.bfs()}")

/** Part 2 **/

data class Point3D(val x: Int, val y: Int, val z: Int) {
    constructor(point: Point, z: Int) : this(point.x, point.y, z)

    val neighbors
        get() = listOf(copy(x = x - 1), copy(x = x + 1), copy(y = y - 1), copy(y = y + 1))
    val twoD = Point(x, y)
}

data class Portal(val name: String, val location: Point, val destination: Point, val type: Type) {
    enum class Type { INNER, OUTER }

    private val maxDepth = 1000

    fun follow(p: Point3D): Point3D? {
        val level = when (type) {
            Type.INNER -> p.z + 1
            Type.OUTER -> p.z - 1
        }
        return if (level in 0..maxDepth) Point3D(destination, level) else null
    }
}

class RecursiveMaze(val mazeInput: String) {
    val mazeArray = mazeInput.split("\n").map { it.toCharArray() }.filter { it.isNotEmpty() }.toTypedArray()
    val innerCorner = Point(2, 2)
    val outerCorner = Point(mazeArray[0].lastIndex - 2, mazeArray.lastIndex - 2)
    lateinit var start: Point3D
    lateinit var finish: Point3D
    val portals = findPortals()
    val mazeMap = createMazeMap()

    fun followPortal(point: Point3D) = portals[point.twoD]?.follow(point)

    fun neighbors(p: Point3D) = (mazeMap.getValue(p.twoD).map { Point3D(it, p.z) }
            + followPortal(p)).filterNotNull()

    fun isOuter(point: Point) = point.x == innerCorner.x || point.x == outerCorner.x
            || point.y == innerCorner.y || point.y == outerCorner.y

    private fun findPortals(): Map<Point, Portal> {
        val portals = mutableMapOf<Point, String>()

        for (y in mazeArray.indices) {
            for (x in mazeArray[y].indices) {
                val p = Point(x, y)
                if (mazeArray[p].isUpperCase()) {
                    val p2 = p.neighbors.first { mazeArray[it].isUpperCase() }
                    val entry = (p.neighbors + p2.neighbors).first { mazeArray[it].isPassage() }
                    val portalName = mazeArray[p].toString() + mazeArray[p2]
                    if (!portals.containsKey(entry)) {
                        when (portalName) {
                            "AA" -> start = Point3D(entry, 0)
                            "ZZ" -> finish = Point3D(entry, 0)
                            else -> portals[entry] = portalName
                        }
                    }
                }
            }
        }
        return portals.mapValues { (p, name) ->
            Portal(
                name, p,
                destination = portals.entries.first { (p2, n2) -> p != p2 && name == n2 }.key,
                type = if (isOuter(p)) Portal.Type.OUTER else Portal.Type.INNER
            )
        }
    }

    private fun createMazeMap(): Map<Point, List<Point>> {
        val mazeMap = mutableMapOf<Point, List<Point>>()
        for (y in mazeArray.indices) {
            for (x in mazeArray[y].indices) {
                val p = Point(x, y)
                val neighbors = p.neighbors.filter { mazeArray[it].isPassage() }
                if (neighbors.isNotEmpty())
                    mazeMap[p] = neighbors
            }
        }
        return mazeMap
    }

    fun bfs(): Int {
        val visited = mutableSetOf<Point3D>()
        val queue = LinkedList<Pair<Point3D, Int>>()
        visited.add(start)
        queue.add(Pair(start, 0))
        while (queue.size != 0) {
            val (p, dist) = queue.poll()
            if (p == finish) return dist
            for (neighbor in neighbors(p)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor)
                    queue.add(Pair(neighbor, dist + 1))
                }
            }
        }
        return -1
    }
}

val testMaze2 = """             Z L X W       C                 
             Z P Q B       K                 
  ###########.#.#.#.#######.###############  
  #...#.......#.#.......#.#.......#.#.#...#  
  ###.#.#.#.#.#.#.#.###.#.#.#######.#.#.###  
  #.#...#.#.#...#.#.#...#...#...#.#.......#  
  #.###.#######.###.###.#.###.###.#.#######  
  #...#.......#.#...#...#.............#...#  
  #.#########.#######.#.#######.#######.###  
  #...#.#    F       R I       Z    #.#.#.#  
  #.###.#    D       E C       H    #.#.#.#  
  #.#...#                           #...#.#  
  #.###.#                           #.###.#  
  #.#....OA                       WB..#.#..ZH
  #.###.#                           #.#.#.#  
CJ......#                           #.....#  
  #######                           #######  
  #.#....CK                         #......IC
  #.###.#                           #.###.#  
  #.....#                           #...#.#  
  ###.###                           #.#.#.#  
XF....#.#                         RF..#.#.#  
  #####.#                           #######  
  #......CJ                       NM..#...#  
  ###.#.#                           #.###.#  
RE....#.#                           #......RF
  ###.###        X   X       L      #.#.#.#  
  #.....#        F   Q       P      #.#.#.#  
  ###.###########.###.#######.#########.###  
  #.....#...#.....#.......#...#.....#.#...#  
  #####.#.###.#######.#######.###.###.#.#.#  
  #.......#.......#.#.#.#.#...#...#...#.#.#  
  #####.###.#####.#.#.#.#.###.###.#.###.###  
  #.......#.....#.#...#...............#...#  
  #############.#.#.###.###################  
               A O F   N                     
               A A D   M                     """

val maze2 = RecursiveMaze(input)
//maze2.mazeArray.print()
println("part2: ${maze2.bfs()}")
