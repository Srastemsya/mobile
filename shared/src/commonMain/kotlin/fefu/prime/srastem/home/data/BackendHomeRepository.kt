package fefu.prime.srastem.home.data

import fefu.prime.srastem.backend.BackendApi
import fefu.prime.srastem.backend.string
import fefu.prime.srastem.home.domain.HomeCollection
import fefu.prime.srastem.home.domain.HomeContent
import fefu.prime.srastem.home.domain.HomeQuickCategory
import fefu.prime.srastem.home.domain.HomeUpcoming
import fefu.prime.srastem.home.domain.HomeRepository
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

class BackendHomeRepository(private val api: BackendApi) : HomeRepository {
    override suspend fun getHome(): HomeContent {
        val root = api.get("/v1/home").jsonObject
        val collections = root["collections"]?.jsonArray.orEmpty().map { element ->
            val item = element.jsonObject
            val filters = item["filters"]?.jsonObject
            val category = filters?.string("categoryId")?.let(::categoryLabel) ?: "Подборка"
            HomeCollection(
                id = item.string("id").orEmpty(),
                title = item.string("title").orEmpty(),
                subtitle = category,
                category = category,
                categoryId = filters?.string("categoryId"),
                filters = filters?.mapValues { (_, value) -> value.jsonPrimitive.contentOrNull.orEmpty() }
                    .orEmpty(),
            )
        }
        val categories = root["quickCategories"]?.jsonArray.orEmpty().map { element ->
            val category = element.jsonObject
            HomeQuickCategory(
                id = category.string("id").orEmpty(),
                label = category.string("label").orEmpty(),
                kinds = category["kinds"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }
                    .orEmpty(),
            )
        }
        val upcoming = root["upcoming"]?.jsonArray.orEmpty().mapNotNull { element ->
            val card = element.jsonObject
            val location = card["location"]?.jsonObject
            val id = card["ref"]?.jsonObject?.string("id") ?: return@mapNotNull null
            HomeUpcoming(
                id = id,
                title = card.string("title").orEmpty(),
                categoryId = card.string("categoryId").orEmpty(),
                startsAt = card.string("startsAt").orEmpty(),
                address = location?.string("address").orEmpty(),
            )
        }
        return HomeContent(
            city = "Владивосток",
            collections = collections,
            quickCategories = categories,
            upcoming = upcoming
        )
    }

    private fun categoryLabel(id: String): String = when (id) {
        "culture" -> "Культура"
        "outdoors" -> "На свежем воздухе"
        "food" -> "Еда и напитки"
        "dev-swipe-places" -> "Места"
        "dev-swipe-events" -> "События"
        else -> "Подборка"
    }
}
