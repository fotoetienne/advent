import org.asynchttpclient.Dsl.asyncHttpClient
import org.asynchttpclient.Response
import java.io.File
import java.util.concurrent.CompletableFuture

class FetchInput {
    /**
     * Advent of Code 2020
     * https://adventofcode.com/
     *
     * fetch Input
     */

    val year = "2020"

    val adventCookie by lazy { System.getenv("ADVENT_COOKIE") ?: throw RuntimeException("Need a cookie") }

    fun getInput(day: Int): File {
        downloadInput(day).get()
        return openInputFile(day)
    }


    fun httpGet(uri: String, cookie: String): CompletableFuture<Response> {
        val client = asyncHttpClient()
        return client.prepareGet(uri)
            .addHeader("Cookie", cookie)
            .execute().toCompletableFuture()
            .thenApply { client.close(); it }
    }


    fun inputFileName(day: Int) = "input%02d.txt".format(day)

    fun inputUri(day: Int) = "https://adventofcode.com/$year/day/$day/input"

    fun downloadInput(day: Int): CompletableFuture<Boolean> {
        val file = File(inputFileName(day))
        if (file.exists()) {
//        println("${inputFileName(day)} already exists")
            return CompletableFuture.completedFuture(true)
        }
        println("fetching ${inputUri(day)}")
        return httpGet(inputUri(day), adventCookie)
            .thenApply { response ->
                file.writeBytes(response.responseBodyAsBytes)
                true
            }.exceptionally { e -> e.printStackTrace(); false }
    }

    fun openInputFile(day: Int) = File(inputFileName(day))

    fun main(args: Array<String>) {
        if (args.isNotEmpty()) {
            val day = args.first().toInt()
            println("Fetching day $day")
            downloadInput(day).thenApply { success ->
                if (success) println("success") else println("fail")
            }
        } else {
            println("Usage: ./fetchInput.kt <day>")
        }
    }
}