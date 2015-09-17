package ideator.ui.graphGenerator

import com.google.common.collect.Sets
import com.google.gson.GsonBuilder
import ideator.ideas.Idea
import ideator.ideas.convertSourcesToIdeas
import ideator.ingest
import ideator.ingest.angelList.Startup
import ideator.ingest.angelList.Startups
import ideator.ingest.angelList.startupsFile
import org.slf4j.LoggerFactory
import java.awt.Color
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.ArrayList

/**
 * Created by ian on 9/8/15.
 */

val logger = LoggerFactory.getLogger("GraphGenerator")

data class Nodes(val nodes : MutableList<SigmaNode>)
data class SigmaNode(val id : String, val label : String, val x : Double, val y : Double, val size : Int = 1, val color : String)

fun main(args: Array<String>) {
    System.setProperty("java.awt.headless", "true")
    val startupsAsString = String(Files.readAllBytes(startupsFile))
    val startups = ingest.gson.fromJson(startupsAsString, javaClass<Startups>())
    val ideas: List<Idea<Startup>> = convertSourcesToIdeas(startups.startups, { s -> s.productDesc })

    val gson = GsonBuilder().setPrettyPrinting().create()

    val dataFile = Paths.get("/Users/ian/Documents/github/noveltysearch-web/data.json")
    logger.info("Writing node data to ${dataFile}")
    PrintStream(Files.newOutputStream(dataFile)).use {
        val ideasAsSigmaNodes = Nodes(ArrayList<SigmaNode>())
        var id = 1
        for (concept in ideas) {
            ideasAsSigmaNodes.nodes.add(SigmaNode(
                    id = "n"+id.toString(),
                    label = "${concept.source.name} - ${Sets.newHashSet(concept.keywords)}",
                    x = concept.reducedVector!![0]*1000,
                    y = concept.reducedVector!![1]*1000,
                    color = qualityToColor(concept.source.quality),
                    size = 2))
            id++
        }
        gson.toJson(ideasAsSigmaNodes, it)
    }
}

fun qualityToColor(quality: Int): String {
    val qualityAsDouble : Float = (quality.toFloat() / 10.0f)
    return colorToHex(Color(0.5f - (qualityAsDouble / 2.0f), qualityAsDouble, 0.0f))
}

fun colorToHex(col : Color) : String {
    return "#"+(Integer.toHexString(col.getRGB()).substring(2))
}