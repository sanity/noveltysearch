package ideator.scratch

import com.google.common.base.CharMatcher
import com.google.common.base.Splitter
import com.google.common.collect.Iterables
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import com.google.gson.GsonBuilder
import ideator.ingest.angelList.Startup
import ideator.ingest.angelList.Startups
import ideator.ingest.angelList.startupsFile
import ideator.ingest.gson
import ideator.loadWordInfos
import ideator.pca.buildPCA
import ideator.word2vec.Vector
import ideator.word2vec.loadWordVectors
import org.slf4j.LoggerFactory
import java.awt.Color
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by ian on 9/4/15.
 */

val logger = LoggerFactory.getLogger("scratch")

val whitespaceRegex = "\\s+".toRegex()

data class Concept<U>(val wrapped: U, val keywords: List<String>, val asVector: DoubleArray) {
    var reducedVector : DoubleArray? = null
}

data class SigmaNode(val id : String, val label : String, val x : Double, val y : Double, val size : Int = 1, val color : String)

data class Nodes(val nodes : MutableList<SigmaNode>)

val NUM_KEYWORDS = 8

fun main(args: Array<String>) {
    val startupsAsString = String(Files.readAllBytes(startupsFile))
    val startups = gson.fromJson(startupsAsString, javaClass<Startups>())
    val concepts: List<Concept<Startup>> = convertDescriptionsToConceptsAverage(startups.startups, {s -> s.productDesc})

    val pcaInput = Array(concepts.size(), { i -> concepts[i].asVector})

    logger.info("Building PCA")

    val pca = buildPCA(pcaInput)

    logger.info("Built PCA, inputs: ${pca.pca.getInputDimsNo()}, outputs: ${pca.pca.getOutputDimsNo()}")

    for (idea in concepts) {
        idea.reducedVector = pca.transform(idea.asVector)
    }

    Collections.sort(concepts, {(a, b) -> a.reducedVector!![0].compareTo(b.reducedVector!![0])})

    val gson = GsonBuilder().setPrettyPrinting().create()

    PrintStream(Files.newOutputStream(Paths.get("/Users/ian/Documents/github/noveltysearch/sigmajs/data.json"))).use {
        val ideasAsSigmaNodes = Nodes(ArrayList<SigmaNode>())
        var id = 1
        for (concept in concepts) {
            ideasAsSigmaNodes.nodes.add(SigmaNode(
                    id = "n"+id.toString(),
                    label = "${concept.wrapped.name} - ${Sets.newHashSet(concept.keywords)}",
                    x = concept.reducedVector!![0]*1000,
                    y = concept.reducedVector!![1]*1000,
                    color = qualityToColor(concept.wrapped.quality),
                    size = 2))
            id++
        }
        gson.toJson(ideasAsSigmaNodes, it)
    }
}

fun qualityToColor(quality: Int): String {
    val qualityAsDouble : Float = (quality.toFloat() / 10.0f)
    return colorToHex(Color(0.5f-(qualityAsDouble/2.0f), qualityAsDouble, 0.0f))
}

fun colorToHex(col : Color) : String {
    return "#"+(Integer.toHexString(col.getRGB()).substring(2))
}

/*
private fun convertDescriptionsToConceptsAppend(descriptions: Iterable<String>): List<Concept> {
    val wordInfos = loadWordInfos()
    val wordVectors = loadWordVectors(Paths.get("/Users/ian/Documents/word2vec/glove.6B.50d.txt.gz"))
    val concepts: MutableList<Concept> = ArrayList()
    for (line in descriptions) {
        if (line != null) {
            val topKeywords = TopN<String, Double>(NUM_KEYWORDS)
            for (wordUC in Splitter.on(CharMatcher.JAVA_LETTER.negate()).trimResults().omitEmptyStrings().split(line)) {
                val word = wordUC.toLowerCase()
                if (word.length() > 2) {
                    val wordInfo = wordInfos.get(word)
                    if (wordInfo == null || wordInfo.partOfSpeech == 'v' || wordInfo.partOfSpeech == 'n') {
                        val vector = wordVectors.wordVectorMap[word]
                        if (vector != null) {
                            topKeywords.add(word, wordInfo?.rank?.toDouble() ?: 10000.0)
                        }
                    }
                }
            }
            val keywords = topKeywords.scores.values().reverse().toArrayList()
            val vector = DoubleArray(50 * NUM_KEYWORDS)
            for (keywordIx in keywords.indices) {
                val keyword = keywords[keywordIx]
                val wordVector = wordVectors.wordVectorMap[keyword]!!
                for (vectorIx in wordVector.array.indices) {
                    vector[(keywordIx * 50) + vectorIx] = wordVector.array[vectorIx]
                }
            }
            concepts.add(Concept(description = line, keywords = keywords, asVector = vector))
        }
    }
    return concepts
}
*/
private fun <U> convertDescriptionsToConceptsAverage(rawConcepts: Iterable<U>, textExtractor: ((U) -> String)): List<Concept<U>> {
    val wordCountProps = calculateWordCounts(rawConcepts, textExtractor)
    val wordInfos = loadWordInfos()
    val wordVectors = loadWordVectors(Paths.get("/Users/ian/Documents/word2vec/glove.6B.50d.txt.gz"))
    val concepts = ArrayList<Concept<U>>()
    for (rawConcept in rawConcepts) {
            val line = textExtractor.invoke(rawConcept)
            if (line != null) {
                val keywords = ArrayList<String>()
                val vectors = ArrayList<Vector>()
                for (wordUC in Splitter.on(CharMatcher.JAVA_LETTER.negate()).trimResults().omitEmptyStrings().split(line)) {
                    val word = wordUC.toLowerCase()
                    val wordFreq = wordCountProps[word] ?: 0.0
                    if (word.length() > 2 && wordFreq < 0.03 ) {
                        val wordInfo = wordInfos.get(word)
                        if (wordInfo == null || wordInfo.partOfSpeech == 'v' || wordInfo.partOfSpeech == 'n') {
                            val vector = wordVectors.wordVectorMap[word]
                            if (vector != null && vector.magnitude() > 5.2) {
                                vectors.add(vector)
                                keywords.add(word)
                            }
                        }
                    }
                }
                if (vectors.isEmpty()) continue
                var totalVector = Vector(DoubleArray(vectors[0].array.size()))
                for (v in vectors) {
                    totalVector = totalVector + v
                }
                var averageVector = totalVector / vectors.size().toDouble()
                concepts.add(Concept(keywords = keywords, asVector = averageVector.array, wrapped = rawConcept))
            }
    }
    return concepts
}

private fun <U> calculateWordCounts(concepts: Iterable<U>, textExtractor: (U) -> String): MutableMap<String, Double> {
    val descriptions = concepts.map(textExtractor)
    val descriptionCount = Iterables.size(descriptions)
    val wordCounts = countWords(descriptions)
    val wordCountProps: MutableMap<String, Double> = Maps.newHashMap()
    for ((word, count) in wordCounts) {
        wordCountProps.put(word, count.toDouble() / descriptionCount.toDouble())
    }
    return wordCountProps
}

private fun countWords(sentences: Iterable<String>) : Map<String, Int> {
    val mutableWordCounts = HashMap<String, AtomicInteger>()
    for (sentence in sentences) {
        if (sentence != null) {
            val words = Sets.newHashSet(sentence.split(whitespaceRegex))
            for (word in words) {
                mutableWordCounts.computeIfAbsent(word.toLowerCase(), { w -> AtomicInteger(0) }).incrementAndGet()
            }
        }
    }
    val wordCounts = HashMap<String, Int>()
    mutableWordCounts.forEach({(w, c) ->
        wordCounts.put(w, c.get())
    })
    return wordCounts
}