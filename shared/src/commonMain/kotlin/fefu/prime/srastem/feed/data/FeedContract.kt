package fefu.prime.srastem.feed.data

import fefu.prime.srastem.feed.domain.FeedFilters
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/**
 * Построение запросов и тел мобильного контракта Discovery v1 для сценариев ленты.
 * Единственный источник формы query/тел: используют `BackendFeedRepository`,
 * `BackendRoomRepository` и унаследованный репозиторий комнаты (`party/data`).
 */
internal object FeedContract {

    /** `GET /v1/feed/candidates`: зеркало query-параметров контракта. */
    fun candidatesQuery(filters: FeedFilters, cursor: String?): String = buildString {
        append("/v1/feed/candidates?kind=").append(filters.kind.name)
        filters.categoryId?.let { append("&categoryId=").append(it.encodeQueryComponent()) }
        filters.localDate?.let { append("&localDate=").append(it.encodeQueryComponent()) }
        filters.budgetTotalMinor?.let { append("&budgetTotalMinor=").append(it) }
        filters.partySize?.let { append("&partySize=").append(it) }
        cursor?.let { append("&cursor=").append(it.encodeQueryComponent()) }
    }

    /** Тело `POST /v1/rooms/{id}/start`: фильтры фиксируются атомарно со стартом раунда. */
    fun startBody(filters: FeedFilters): JsonObject = buildJsonObject {
        put("filters", buildJsonObject {
            put("kind", JsonPrimitive(filters.kind.name))
            filters.categoryId?.let { put("categoryId", JsonPrimitive(it)) }
            filters.localDate?.let { put("localDate", JsonPrimitive(it)) }
            filters.budgetTotalMinor?.let { amount ->
                put("budgetTotal", buildJsonObject {
                    put("amountMinor", JsonPrimitive(amount))
                    put("currency", JsonPrimitive("RUB"))
                })
            }
            filters.partySize?.let { put("partySize", JsonPrimitive(it)) }
        })
    }
}
