package fefu.prime.srastem.feed.data

import fefu.prime.srastem.backend.BackendException
import fefu.prime.srastem.feed.domain.FeedCard
import fefu.prime.srastem.feed.domain.CatalogDetails
import fefu.prime.srastem.feed.domain.FeedError
import fefu.prime.srastem.feed.domain.FeedFilters
import fefu.prime.srastem.feed.domain.FeedKind
import fefu.prime.srastem.feed.domain.FeedPage
import fefu.prime.srastem.feed.domain.FeedRef
import fefu.prime.srastem.feed.domain.RoomMember
import fefu.prime.srastem.feed.domain.RoomPhase
import fefu.prime.srastem.feed.domain.RoomSnapshot
import fefu.prime.srastem.feed.domain.SwipeDecision
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Мапперы DTO мобильного контракта → доменные модели ленты. Транспортные типы наружу не утекают. */
internal object FeedMappers {

    fun errorOf(exception: BackendException): FeedError = when {
        exception.status == 403 && exception.code == "PROFILE_INCOMPLETE" -> FeedError.ProfileIncomplete()
        exception.status == 410 && exception.code == "INVITE_EXPIRED" -> FeedError.InviteExpired()
        exception.status == 404 -> FeedError.RoomNotFound()
        exception.status == 409 && exception.code in setOf("STALE_ROUND", "INVALID_STATE") -> FeedError.InvalidState()
        exception.status == 409 && exception.code == "CURSOR_EXPIRED" -> FeedError.CursorExpired()
        else -> FeedError.Transport(exception.message)
    }

    fun errorOf(exception: Exception): FeedError =
        if (exception is BackendException) errorOf(exception) else FeedError.Transport(exception.message ?: "Ошибка соединения с сервером")

    fun page(json: JsonObject): FeedPage = FeedPage(
        items = json["items"]?.jsonArray.orEmpty().map { card(it.jsonObject) },
        nextCursor = json["nextCursor"]?.takeUnless { it == JsonNull }?.jsonPrimitive?.content,
    )

    fun card(json: JsonObject): FeedCard {
        val ref = json["ref"]?.jsonObject
        val kind = kind(ref?.get("kind")?.jsonPrimitive?.content)
        val priceMinor = json["price"]?.takeUnless { it == JsonNull }?.jsonObject
            ?.get("amountMinor")?.jsonPrimitive?.content?.toLongOrNull()
        val startsAt = json["startsAt"]?.takeUnless { it == JsonNull }?.jsonPrimitive?.content
        return FeedCard(
            ref = FeedRef(kind, ref?.get("id")?.jsonPrimitive?.content.orEmpty()),
            title = json["title"]?.jsonPrimitive?.content.orEmpty(),
            category = json["categoryId"]?.jsonPrimitive?.content.orEmpty(),
            priceLabel = priceLabel(priceMinor),
            priceMinor = priceMinor,
            addressOrDate = json["location"]?.takeUnless { it == JsonNull }?.jsonObject
                ?.get("address")?.jsonPrimitive?.content.orEmpty()
                .ifBlank { startsAt.orEmpty() },
            description = json["description"]?.jsonPrimitive?.content.orEmpty(),
            coverUrl = json["cover"]?.takeUnless { it == JsonNull }?.jsonObject
                ?.get("url")?.jsonPrimitive?.content,
        )
    }

    fun details(json: JsonObject): CatalogDetails {
        // PLACE and other non-event details wrap their full card as {card, description}; EVENT
        // details use the Event object directly.
        val card = json["card"]?.takeUnless { it == JsonNull }?.jsonObject ?: json
        val refObject = card["ref"]?.jsonObject
        val kind = kind(refObject?.get("kind")?.jsonPrimitive?.content ?: card["kind"]?.jsonPrimitive?.content)
        val id = refObject?.get("id")?.jsonPrimitive?.content ?: card["id"]?.jsonPrimitive?.content.orEmpty()
        val priceMinor = card["price"]?.takeUnless { it == JsonNull }?.jsonObject
            ?.get("amountMinor")?.jsonPrimitive?.content?.toLongOrNull()
        return CatalogDetails(
            ref = FeedRef(kind, id),
            title = card["title"]?.jsonPrimitive?.content.orEmpty(),
            description = json["description"]?.takeUnless { it == JsonNull }?.jsonPrimitive?.content
                ?: card["description"]?.takeUnless { it == JsonNull }?.jsonPrimitive?.content.orEmpty(),
            categoryId = card["categoryId"]?.jsonPrimitive?.content.orEmpty(),
            address = card["location"]?.takeUnless { it == JsonNull }?.jsonObject
                ?.get("address")?.jsonPrimitive?.content.orEmpty(),
            startsAt = card["startsAt"]?.takeUnless { it == JsonNull }?.jsonPrimitive?.content,
            endsAt = card["endsAt"]?.takeUnless { it == JsonNull }?.jsonPrimitive?.content,
            priceLabel = priceLabel(priceMinor),
            admissionLabel = card["admission"]?.takeUnless { it == JsonNull }?.jsonObject?.let { admission ->
                when (admission["type"]?.jsonPrimitive?.content) {
                    "OPEN" -> "Свободный вход"
                    "REGISTRATION" -> admission["remainingCount"]?.jsonPrimitive?.content?.let { "Осталось мест: $it" } ?: "По записи"
                    else -> null
                }
            },
            coverUrl = card["cover"]?.takeUnless { it == JsonNull }?.jsonObject?.get("url")?.jsonPrimitive?.content,
        )
    }

    fun room(json: JsonObject, ownUserId: String): RoomSnapshot {
        val round = json["round"]?.takeUnless { it == JsonNull }?.jsonObject
        val candidates = round?.get("candidates")?.jsonArray.orEmpty().map { card(it.jsonObject) }
        return RoomSnapshot(
            id = json["id"]?.jsonPrimitive?.content.orEmpty(),
            name = json["name"]?.takeUnless { it == JsonNull }?.jsonPrimitive?.content,
            ownerId = json["ownerId"]?.jsonPrimitive?.content.orEmpty(),
            phase = phase(json["state"]?.jsonPrimitive?.content),
            version = json["version"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L,
            members = json["members"]?.jsonArray.orEmpty().map { member ->
                RoomMember(
                    userId = member.jsonObject["userId"]?.jsonPrimitive?.content.orEmpty(),
                    displayName = member.jsonObject["displayName"]?.jsonPrimitive?.content.orEmpty(),
                )
            },
            filters = json["filters"]?.takeUnless { it == JsonNull }?.jsonObject?.let(::filters),
            roundId = round?.get("id")?.jsonPrimitive?.content,
            candidates = candidates,
            myVotes = round?.get("myVotes")?.jsonArray.orEmpty().mapNotNull { vote ->
                val item = vote.jsonObject
                val candidate = item["candidate"]?.jsonObject ?: return@mapNotNull null
                val id = candidate["id"]?.jsonPrimitive?.content ?: return@mapNotNull null
                id to decision(item["choice"]?.jsonPrimitive?.content)
            }.toMap(),
            myFinished = round?.get("myFinished")?.jsonPrimitive?.content == "true",
            finishedCount = round?.get("finishedCount")?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
            winner = json["result"]?.takeUnless { it == JsonNull }?.jsonObject
                ?.get("winner")?.jsonObject?.let { winner ->
                    FeedRef(kind(winner["kind"]?.jsonPrimitive?.content), winner["id"]?.jsonPrimitive?.content.orEmpty())
                },
        )
    }

    fun filters(json: JsonObject): FeedFilters = FeedFilters(
        kind = kind(json["kind"]?.jsonPrimitive?.content),
        categoryId = json["categoryId"]?.takeUnless { it == JsonNull }?.jsonPrimitive?.content,
        localDate = json["localDate"]?.takeUnless { it == JsonNull }?.jsonPrimitive?.content,
        budgetTotalMinor = json["budgetTotal"]?.takeUnless { it == JsonNull }?.jsonObject
            ?.get("amountMinor")?.jsonPrimitive?.content?.toLongOrNull(),
        partySize = json["partySize"]?.takeUnless { it == JsonNull }?.jsonPrimitive?.content?.toIntOrNull(),
    )

    fun kind(value: String?): FeedKind = if (value == "EVENT") FeedKind.EVENT else FeedKind.PLACE

    fun phase(value: String?): RoomPhase = when (value) {
        "LOBBY" -> RoomPhase.LOBBY
        "VOTING" -> RoomPhase.VOTING
        "REFRESHING" -> RoomPhase.REFRESHING
        "MATCHED" -> RoomPhase.MATCHED
        "EXHAUSTED" -> RoomPhase.EXHAUSTED
        else -> RoomPhase.CLOSED
    }

    fun decision(value: String?): SwipeDecision = if (value == "LIKE") SwipeDecision.LIKE else SwipeDecision.SKIP

    fun priceLabel(amountMinor: Long?): String = when {
        amountMinor == null -> "Цена неизвестна"
        amountMinor == 0L -> "Бесплатно"
        amountMinor < 1000 -> "${amountMinor} коп."
        else -> "${amountMinor / 100} ₽"
    }
}
