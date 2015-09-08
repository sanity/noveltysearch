import Goal
import getCompressedSize
import printlnWithLinebreaks
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.util.*
import java.util.zip.DeflaterOutputStream

/**
 * Created by ian on 8/26/15.
 */

fun main(args: Array<String>) {
    val random = Random()
    var stringSoFar = ("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris lacus lectus, " +
            "aliquet at auctor in, porta ut lectus. Vestibulum molestie pellentesque fringilla. Curabitur " +
            "pretium suscipit massa quis cursus. Mauris sed enim est. Maecenas posuere turpis id ligula " +
            "volutpat congue id a eros. Nunc malesuada diam ut dui dignissim, sed tristique metus aliquam. " +
            "Vivamus sed volutpat diam. Cras vitae iaculis velit. Ut eu urna lectus.")
    do {
        var bestSizeSoFar : Int? = null
        var bestCandidate : String? = null
        var goal = if (random.nextDouble() < 0.9) Goal.NOVELTY else Goal.FAMILIARITY

        for (newChar in(('A'.toInt() .. 'z'.toInt()).plus(' '.toInt()))) {
            val newCharAsChar = newChar.toChar()
            val candidateString : String = stringSoFar + newCharAsChar
            val compressedSize = getCompressedSize(candidateString)
            if (bestSizeSoFar == null || (if (goal == Goal.NOVELTY) (compressedSize > bestSizeSoFar) else (compressedSize < bestSizeSoFar))) {
                bestCandidate = candidateString
                bestSizeSoFar = compressedSize
            }
        }
        stringSoFar = if (bestCandidate != null) bestCandidate else throw NullPointerException("No best candidate found, this shouldn't happen")
    } while (stringSoFar.length() < 5000)
    printlnWithLinebreaks(stringSoFar, 100)
}

fun printlnWithLinebreaks(stringSoFar: String, breakEvery: Int) {
    var remainingString = stringSoFar
    while (remainingString.length() > breakEvery) {
        println(remainingString.substring(0, breakEvery))
        remainingString = remainingString.substring(breakEvery)
    }
    println(remainingString)
}

fun getCompressedSize(string : String): Int {
    val buffer = ByteArrayOutputStream()
    val outputStream = DeflaterOutputStream(buffer)
    val bytes = string.toByteArray(Charset.forName("UTF-8"))
    outputStream.write(bytes)
    outputStream.close()
    var size = buffer.toByteArray().size()
    return size
}

enum class Goal {
    NOVELTY, FAMILIARITY
}