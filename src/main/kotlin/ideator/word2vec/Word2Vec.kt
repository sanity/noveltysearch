package ideator.word2vec

import com.google.common.collect.Lists
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang
import java.lang.Double.parseDouble
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.zip.GZIPInputStream

/**
 * Created by ian on 9/2/15.
 */

val logger = LoggerFactory.getLogger("Word2Vec")

val random = Random()

fun loadWordVectors(file: Path): WordVectors {
    logger.info("Loading word vectors from ${file}")
    val wordVectors = HashMap<String, Vector>()
    for (x in 0 .. 40) {
        print("-")
    }
    println()
    BufferedReader(InputStreamReader(GZIPInputStream(Files.newInputStream(file)))).use {
        var lineCount = 0
        do {
            if (lineCount % 10000 == 0) {
                print("*")
            }
            val line = it.readLine()
            if (line != null) {
                val splitLine = line.trim().split("\\s+".toRegex())
                val word = splitLine[0].toLowerCase()
                try {
                    val vector = Vector(splitLine.subList(1, splitLine.size()).asSequence().map(lang.Double::parseDouble).toArrayList().toDoubleArray())
                    wordVectors.put(word, vector)
                } catch (e : NumberFormatException) {
                    logger.error("Failed to parse: \"${line}\"", e)
                    System.exit(-1)
                }
            }
            lineCount++
        } while (line != null)
        println()
    }
    logger.info("${wordVectors.size()} word vectors loaded from ${file}")
    return WordVectors(wordVectors)
}

