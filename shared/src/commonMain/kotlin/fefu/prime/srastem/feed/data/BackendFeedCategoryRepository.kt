package fefu.prime.srastem.feed.data

import fefu.prime.srastem.backend.BackendApi
import fefu.prime.srastem.backend.string
import fefu.prime.srastem.feed.domain.FeedCategory
import fefu.prime.srastem.feed.domain.FeedCategoryRepository
import fefu.prime.srastem.feed.domain.FeedKind
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

class BackendFeedCategoryRepository(private val api: BackendApi) : FeedCategoryRepository {
    override suspend fun getCategories(): List<FeedCategory> =
        api.get("/v1/catalog/categories").jsonObject["items"]?.jsonArray.orEmpty().map { element ->
            val item = element.jsonObject
            FeedCategory(
                item.string("id").orEmpty(),
                item.string("label").orEmpty(),
                item["kinds"]?.jsonArray.orEmpty().mapNotNull { raw ->
                    raw.jsonPrimitive.contentOrNull?.let { runCatching { FeedKind.valueOf(it) }.getOrNull() }
                })
        }
}
