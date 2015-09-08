package ideator.ingest.angelList

import java.time.Instant

/**
 * Created by ian on 9/7/15.
 */


data class Startups(
        val startups: List<Startup>,
        val total: Int,
        val perPage: Int,
        val page: Int,
        val lastPage: Int
)

data class Startup(
        val id: Int,
        val hidden: Boolean,
        val communityProfile: Boolean,
        val name: String,
        val angellistUrl: String,
        val logoUrl: String,
        val thumbUrl: String,
        val quality: Int,
        val productDesc: String,
        val highConcept: String,
        val followerCount: Int,
        val companyUrl: String,
        val createdAt: Instant,
        val updatedAt: Instant,
        val twitterUrl: String,
        val blogUrl: String,
        val videoUrl: String,
        val markets: Set<Tag>,
        val locations: Set<Tag>,
        val status: Status,
        val screenshots: Set<Screenshot>
)

data class Tag(
        val id: Int,
        val tagType: String,
        val name: String,
        val displayName: String,
        val angellistUrl: String
)

data class Status(
        val message: String,
        val createdAt: Instant
)

data class Screenshot(
        val thumb: String,
        val original: String
)