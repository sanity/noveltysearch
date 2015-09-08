package ideator.ingest

import com.fatboyindustrial.gsonjavatime.Converters
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder

/**
 * Created by ian on 9/7/15.
 */

val gson = Converters.registerAll(GsonBuilder().setPrettyPrinting().setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)).create()