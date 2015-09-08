package ideator.ingest.productHunt

import java.util.*

/**
 * Created by ian on 8/31/15.
 */

data class User(val id : Int = -1,
                val name : String = "",
                val headline : String = "",
                val createdAt : String = "",
                val username : String = "",
                val websiteUrl : String = "",
                val imageUrl : Map<String, String> = Collections.emptyMap())

data class Post(val id : Int = -1,
                val name : String = "",
                val tagline : String = "",
                val day : String = "",
                val createdAt : String = "",
                val featured : Boolean = false,
                val commentsCount : Int = -1,
                val votesCount : Int = -1,
                val discussionUrl : String = "",
                val redirectUrl : String = "",
                val screenshotUrl : Map<String, String> = Collections.emptyMap(),
                val user : User = User())

data class Posts(val posts : List<Post> = ArrayList())