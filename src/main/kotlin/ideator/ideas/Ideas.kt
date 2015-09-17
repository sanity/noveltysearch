package ideator.ideas

import com.google.common.base.CharMatcher
import com.google.common.base.Splitter
import com.google.common.collect.Iterables
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import ideator.TopN
import ideator.loadWordInfos
import ideator.pca.buildPCA
import ideator.word2vec.Vector
import ideator.word2vec.loadWordVectors
import org.slf4j.LoggerFactory
import java.nio.file.Paths
import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by ian on 9/4/15.
 */

val logger = LoggerFactory.getLogger("ideas")

val whitespaceRegex = "\\s+".toRegex()

data class Idea<U>(val source: U, val keywords: List<String>, val asVector: DoubleArray) {
    var reducedVector : DoubleArray? = null
}

fun <S> convertSourcesToIdeas(startups: Iterable<S>, stringExtractor : ((S) -> String)): List<Idea<S>> {
    val ideas: List<Idea<S>> = convertDescriptionsToConceptsAverage(startups, stringExtractor)

    val pcaInput = Array(ideas.size(), { i -> ideas[i].asVector })

    logger.info("Building PCA")

    val pca = buildPCA(pcaInput)

    logger.info("Built PCA, inputs: ${pca.pca.getInputDimsNo()}, outputs: ${pca.pca.getOutputDimsNo()}")

    for (idea in ideas) {
        idea.reducedVector = pca.transform(idea.asVector)
    }
    return ideas
}

data class KeywordVector(val keyword : String, val vector : Vector) : Comparable<KeywordVector> {
    override fun compareTo(other: KeywordVector): Int {
        return keyword.compareTo(other.keyword)
    }

}

private fun <U> convertDescriptionsToConceptsAverage(rawConcepts: Iterable<U>, textExtractor: ((U) -> String)): List<Idea<U>> {
    val wordCountProps = calculateWordCounts(rawConcepts, textExtractor)
    val wordInfos = loadWordInfos()
    val wordVectors = loadWordVectors(Paths.get("/Users/ian/Documents/word2vec/glove.6B.50d.txt.gz"))
    val concepts = ArrayList<Idea<U>>()
    for (rawConcept in rawConcepts) {
            val line = textExtractor.invoke(rawConcept)
            if (line != null) {
                val keywordVectors = TopN<KeywordVector, Double>(5)
                for (wordUC in Splitter.on(CharMatcher.JAVA_LETTER.negate()).trimResults().omitEmptyStrings().split(line)) {
                    val word = wordUC.toLowerCase()
                    val wordFreq = wordCountProps[word] ?: 0.0
                    if (word.length() > 1) {
                        val wordInfo = wordInfos.get(word)
                        if (wordInfo == null || wordInfo.partOfSpeech == 'v' || wordInfo.partOfSpeech == 'n') {
                            val vector = wordVectors.wordVectorMap[word]
                            if (vector != null) {
                                keywordVectors.add(KeywordVector(word, vector), -wordFreq)
                            }
                        }
                    }
                }
                if (keywordVectors.scores.isEmpty()) continue
                var totalVector = Vector(DoubleArray(keywordVectors.scores.values().first().vector.array.size()))
                for (kv in keywordVectors.scores.values()) {
                    totalVector = totalVector + kv.vector
                }
                var averageVector = totalVector / keywordVectors.scores.size().toDouble()
                concepts.add(Idea(keywords = keywordVectors.scores.values().map({kv -> kv.keyword}), asVector = averageVector.array, source = rawConcept))
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