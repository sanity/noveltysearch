package ideator.word2vec

import java.util.*

data class WordVector(val word: String, val vector: Vector) : Comparable<WordVector> {
    override fun compareTo(other: WordVector): Int {
        return word.compareTo(other.word)
    }
}