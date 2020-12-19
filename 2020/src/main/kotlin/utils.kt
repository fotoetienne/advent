import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.io.File
import java.math.BigInteger
import kotlin.math.pow

fun File.read() = readText().trim()
fun File.lines() = readLines().map { it.trim() }
fun File.splitCommas() = readText().trim().split(',')

fun <T, R> memo(f: (T) -> R) =
    mutableMapOf<T, R>().run {
        { x: T ->
            getOrPut(x) {
                f(x)
            }
        }
    }

fun <A, B>Iterable<A>.pmap(f: suspend (A) -> B): List<B> = runBlocking {
    map { async { f(it) } }.map { it.await() }
}

class modulo<T>(m: Long, block: () -> T) {

}

fun exp(x: Long, e: Int = 2) = e.toDouble().pow(x.toDouble()).toLong()
fun exp(x: Int, e: Int = 2) = exp(x.toLong(), e)

fun String.toInts() = split(',').map(String::toInt)

fun Collection<Number>.product() = reduce { a, b -> a.toLong() * b.toLong() }
fun Collection<BigInteger>.sum() = reduce { a, b -> a + b }

data class Point4d(val x: Int, val y: Int, val z: Int = 0, val w: Int = 0) {
    infix operator fun plus(other: Point4d) = Point4d(x + other.x, y + other.y, z + other.z, w + other.w)
}

fun String.rest() = drop(1)