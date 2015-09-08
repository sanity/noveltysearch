package ideator

import com.google.common.collect.TreeMultimap

/**
 * Created by ian on 9/5/15.
 */

class TopN<Item : Comparable<Item>, Score : Comparable<Score>>(val n : Int) {
    val scores : TreeMultimap<Score, Item> = TreeMultimap.create()

    fun add(item : Item, score : Score) {
        scores.put(score, item)
        while (scores.size() > n) {
            val lowestEntry = scores.entries().first()
            scores.remove(lowestEntry.getKey(), lowestEntry.getValue())
        }
    }

}

