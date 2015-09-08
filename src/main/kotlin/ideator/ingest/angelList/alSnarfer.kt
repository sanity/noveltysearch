package ideator.ingest.angelList

import com.google.common.collect.Lists
import com.mashape.unirest.http.Unirest
import ideator.ingest.gson
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Created by ian on 9/7/15.
 */

val accessToken = "e7e1b45a8c68d5d645d6539a36de57ce24d76f8b6d7a528f"
val startupsFile = Paths.get("al-startups.json")

fun main(args: Array<String>) {
    val allStartups : MutableList<Startup> = Lists.newArrayList()
    var page = 1
    while (true) {
        val responseAsString = Unirest.get("https://api.angel.co/1/startups?filter=raising&page=${page}")
                .header("Authorization", accessToken)
                .asString().getBody()
        val startups = gson.fromJson(responseAsString, javaClass<Startups>())
        allStartups.addAll(startups.startups)
        println("Retrieved page ${startups.page} of ${startups.lastPage}, ${allStartups.size()} so far")
        if (page == startups.lastPage) break
        page++
        Thread.sleep(2000)
    }

    Files.write(startupsFile, gson.toJson(Startups(allStartups, 0, 0, 0, 0)).toByteArray())
}