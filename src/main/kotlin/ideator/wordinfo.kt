package ideator

import com.google.common.base.Splitter
import com.google.common.collect.Maps

import java.nio.file.Files
import java.nio.file.Paths

/**
 * Created by ian on 9/5/15.
 */

fun loadWordInfos(): Map<String, WordInfo> {
    val wordInfos : MutableMap<String, WordInfo> = Maps.newHashMap()
    Files.lines(Paths.get("/Users/ian/Documents/word2vec/word-freq-fixed.csv"))
        .skip(1)
        .map({line -> Splitter.on(',').trimResults().split(line).toArrayList()})
        .filter({splitLine -> splitLine.size() == 5})
        .map({splitLine -> WordInfo(word = splitLine[1], rank = splitLine[0].toInt(), partOfSpeech = splitLine[2].toCharList().first(), dispersion = splitLine[4].toDouble())})
        .forEach({wi -> if (!wordInfos.containsKey(wi.word)) wordInfos.put(wi.word, wi)})
    return wordInfos
}

data class WordInfo(val word : String, val rank : Int, val partOfSpeech : Char, val dispersion : Double) {

}
