package ideator.ingest.productHunt

import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpHeaders
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.common.base.Joiner
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import ideator.ingest.gson
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant
import java.util.ArrayList

/**
 * Created by ian on 8/31/15.
 */

fun main(args: Array<String>) {
    var olderThan: Int? = null
    PrintStream(Files.newOutputStream(Paths.get("ph-names-taglines.txt"))).use {
        while (true) {
            var retrievedPosts = retrievePosts(devToken = "Bearer 3a86e5b644162cb26541b561e065339e46b7b450a065189cd52aa1cb3fdfcc5a",
                    perPage = 50,
                    older = olderThan
            )
            retrievedPosts.forEach { p -> it.println("${p.name} | ${p.tagline}") }
            it.flush()
            Thread.sleep(1000)
            if (retrievedPosts.isEmpty()) {
                break
            } else {
                olderThan = retrievedPosts.last().id
            }
        }

    }
}


fun retrievePosts(devToken : String, older : Int? = null, newer : Int? = null, perPage : Int? = null) : List<Post> {
    val httpTransport = NetHttpTransport()
    val jsonFactory = GsonFactory()

    val requestFactory = httpTransport.createRequestFactory()

    val parametersAsList = ArrayList<String>()
    if (older != null) {
        parametersAsList.add("older=${older}")
    }
    if (newer != null) {
        parametersAsList.add("newer=${newer}")
    }
    if (perPage != null) {
        parametersAsList.add("per_page=${perPage}")
    }
    val parameters = StringBuilder()
    if (!parametersAsList.isEmpty()) {
        parameters.append('?')
        parameters.append(Joiner.on('&').join(parametersAsList))
    }

    val httpRequest = requestFactory.buildGetRequest(GenericUrl("https://api.producthunt.com/v1/posts/all${parameters.toString()}"))
            .setHeaders(HttpHeaders().setAuthorization(devToken).setIfNoneMatch("3518ce1cf986ae1a08260f3bede56124"))
    val httpResponse = httpRequest.execute()
    val jsonAsString = httpResponse.parseAsString()
    val posts = gson.fromJson(jsonAsString, javaClass<Posts>())

    return posts.posts
}