package ideator.word2vec

import com.google.common.collect.Lists
import com.google.common.collect.TreeMultimap
import java.util.*

class WordVectors(val wordVectorMap: Map<String, Vector>, val maxLruLinks: Int = 7, val maxClosestLinks : Int = 3) {

    data class WordVectorWithDistance(val wordVector : WordVector, val distance : Double)

    fun findExhaustive(vectorToFind : Vector, ignore : Set<Vector> = Collections.emptySet()) : WordVectorWithDistance? {
        var closestWV : WordVector? = null
        var closestDistance : Double? = null
        for ((word, vector) in wordVectorMap) {
            if (!ignore.contains(vector)) {
                val distance = vector.distance(vectorToFind)
                if (closestDistance == null || distance < closestDistance) {
                    closestDistance = distance
                    closestWV = WordVector(word, vector)
                }
            }
        }
        if (closestWV != null && closestDistance != null) {
            return WordVectorWithDistance(closestWV, closestDistance)
        } else {
            return null
        }
    }
}