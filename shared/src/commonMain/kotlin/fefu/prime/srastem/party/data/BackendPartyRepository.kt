package fefu.prime.srastem.party.data

import fefu.prime.srastem.backend.BackendApi
import fefu.prime.srastem.backend.jsonObjectOf
import fefu.prime.srastem.backend.string
import fefu.prime.srastem.currentDateParts
import fefu.prime.srastem.party.domain.PartyCategory
import fefu.prime.srastem.party.domain.PartyFeedType
import fefu.prime.srastem.party.domain.PartyFilters
import fefu.prime.srastem.party.domain.PartyItem
import fefu.prime.srastem.party.domain.PartyParticipant
import fefu.prime.srastem.party.domain.PartyPhase
import fefu.prime.srastem.party.domain.PartyRepository
import fefu.prime.srastem.party.domain.PartyRoom
import fefu.prime.srastem.party.domain.PartySession
import fefu.prime.srastem.party.domain.PartyVote
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class BackendPartyRepository(private val api: BackendApi) : PartyRepository {
    private val pendingFilters = mutableMapOf<String, PartyFilters>()

    override suspend fun getFeedItems(filters: PartyFilters): List<PartyItem> {
        val kind = if (filters.feedType == PartyFeedType.EVENTS) "EVENT" else "PLACE"
        val categoryId = categoryId(filters)
        val date = localDate(filters)
        val partySize = 1
        val budgetMinor = filters.budgetPerPerson.coerceAtLeast(0) * 100
        val all = mutableListOf<PartyItem>()
        var cursor: String? = null
        do {
            val query = buildString {
                append("/v1/feed/candidates?kind=").append(kind)
                categoryId?.let { append("&categoryId=").append(it) }
                date?.let { append("&localDate=").append(it) }
                append("&budgetTotalMinor=").append(budgetMinor)
                append("&partySize=").append(partySize)
                cursor?.let { append("&cursor=").append(it.encodeQueryComponent()) }
            }
            val page = api.get(query).jsonObject
            all += page["items"]?.jsonArray.orEmpty().map { candidate -> parseCard(candidate.jsonObject) }
            cursor = page.string("nextCursor")
        } while (cursor != null)
        return all
    }

    override suspend fun createRoom(name: String, participantName: String): PartySession {
        val createScope = "create-room:${name.trim().hashCode()}"
        val created = api.post("/v1/rooms", jsonObjectOf("name" to name.trim().ifBlank { null }), api.idempotencyKey(createScope)).value?.jsonObject
            ?: error("Сервер не вернул комнату")
        val id = created.string("id") ?: error("Сервер не вернул ID комнаты")
        val inviteScope = "invite-room:$id"
        val invite = api.postEmpty("/v1/rooms/$id/invites", api.idempotencyKey(inviteScope)).value?.jsonObject
        api.clearIdempotencyKey(inviteScope)
        val inviteUrl = invite?.string("url") ?: error("Сервер не вернул приглашение")
        api.clearIdempotencyKey(createScope)
        val room = parseRoom(created)
        return PartySession(api.userId(), room, inviteUrl)
    }

    override suspend fun joinRoom(roomId: String, participantName: String): PartySession {
        val token = extractInviteToken(roomId)
        val joinScope = "join-room:${token.hashCode()}"
        val joined = api.post("/v1/room-invites/join", jsonObjectOf("token" to token), api.idempotencyKey(joinScope)).value?.jsonObject
            ?: error("Сервер не вернул комнату")
        api.clearIdempotencyKey(joinScope)
        return PartySession(api.userId(), parseRoom(joined), roomId)
    }

    override suspend fun updateFilters(roomId: String, participantId: String, filters: PartyFilters) {
        pendingFilters[roomId] = filters
    }

    override suspend fun startFeed(roomId: String, participantId: String) {
        val room = api.get("/v1/rooms/$roomId").jsonObject
        val members = room["members"]?.jsonArray?.size ?: 1
        val filters = pendingFilters[roomId] ?: PartyFilters()
        val scope = "start-room:$roomId"
        api.post("/v1/rooms/$roomId/start", startBody(filters, members), api.idempotencyKey(scope))
        api.clearIdempotencyKey(scope)
        pendingFilters.remove(roomId)
    }

    override suspend fun loadMoreItems(roomId: String, participantId: String, nextItemIndex: Int): PartyRoom =
        parseRoom(api.get("/v1/rooms/$roomId").jsonObject)

    override suspend fun submitVote(roomId: String, participantId: String, itemId: String, vote: PartyVote) {
        val room = api.get("/v1/rooms/$roomId").jsonObject
        val round = room["round"]?.takeUnless { it == JsonNull }?.jsonObject ?: error("Раунд завершён")
        val roundId = round.string("id") ?: error("Сервер не вернул ID раунда")
        val candidate = round["candidates"]?.jsonArray.orEmpty().firstOrNull { card ->
            card.jsonObject["ref"]?.jsonObject?.string("id") == itemId
        }?.jsonObject ?: error("Карточка больше недоступна")
        val kind = candidate["ref"]?.jsonObject?.string("kind") ?: "EVENT"
        val choice = if (vote == PartyVote.LIKE) "LIKE" else "PASS"
        api.put(
            "/v1/rooms/$roomId/rounds/$roundId/votes/$kind/$itemId",
            jsonObjectOf("choice" to choice),
        )
    }

    override suspend fun completeFeed(roomId: String, participantId: String) {
        val room = api.get("/v1/rooms/$roomId").jsonObject
        val roundId = room["round"]?.takeUnless { it == JsonNull }?.jsonObject?.string("id")
            ?: error("Раунд уже завершён")
        api.putEmpty("/v1/rooms/$roomId/rounds/$roundId/completion")
    }

    override fun observeRoom(roomId: String): Flow<PartyRoom> = flow {
        var snapshot = api.get("/v1/rooms/$roomId").jsonObject
        var room = parseRoom(snapshot)
        emit(room)
        while (room.phase != PartyPhase.MATCH && room.phase != PartyPhase.FINISHED) {
            try {
                val result = api.getResult("/v1/rooms/$roomId?afterVersion=${room.version}&waitSeconds=25")
                if (result.status == 204) continue
                snapshot = result.value?.jsonObject ?: continue
                val next = parseRoom(snapshot)
                if (next.version >= room.version) {
                    room = next
                    emit(room)
                }
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                delay(1_500)
            }
        }
    }

    private fun startBody(filters: PartyFilters, members: Int): JsonObject {
        val budgetMinor = filters.budgetPerPerson.coerceAtLeast(0).toLong() * 100L * members.coerceAtLeast(1)
        return buildJsonObject {
            put("filters", buildJsonObject {
                put("kind", JsonPrimitive(if (filters.feedType == PartyFeedType.EVENTS) "EVENT" else "PLACE"))
                categoryId(filters)?.let { put("categoryId", JsonPrimitive(it)) }
                localDate(filters)?.let { put("localDate", JsonPrimitive(it)) }
                put("budgetTotal", buildJsonObject {
                    put("amountMinor", JsonPrimitive(budgetMinor))
                    put("currency", JsonPrimitive("RUB"))
                })
            })
        }
    }

    private fun parseRoom(json: JsonObject): PartyRoom {
        val id = json.string("id").orEmpty()
        val owner = json.string("ownerId").orEmpty()
        val version = json["version"]?.jsonPrimitive?.contentOrNull?.toLongOrNull() ?: 0L
        val filtersObject = json["filters"]?.takeUnless { it == JsonNull }?.jsonObject
        val filters = filtersObject?.let(::parseFilters) ?: pendingFilters[id] ?: PartyFilters()
        val round = json["round"]?.takeUnless { it == JsonNull }?.jsonObject
        val members = json["members"]?.jsonArray.orEmpty().map { member ->
            val item = member.jsonObject
            PartyParticipant(
                id = item.string("userId").orEmpty(),
                name = item.string("displayName") ?: "Участник",
                isConnected = true,
                completedFeed = item.string("userId") == api.userId() &&
                    json["round"]?.takeUnless { it == JsonNull }?.jsonObject?.get("myFinished")?.toString() == "true",
            )
        }
        val candidates = round?.get("candidates")?.jsonArray.orEmpty().map { parseCard(it.jsonObject) }
        val votes = round?.get("myVotes")?.jsonArray.orEmpty().mapNotNull { vote ->
            val item = vote.jsonObject
            val candidateId = item["candidate"]?.jsonObject?.string("id") ?: return@mapNotNull null
            val value = if (item.string("choice") == "LIKE") PartyVote.LIKE else PartyVote.SKIP
            candidateId to mapOf(api.userId() to value)
        }.toMap()
        val resultRef = json["result"]?.takeUnless { it == JsonNull }?.jsonObject?.get("winner")?.jsonObject
        val matched = resultRef?.string("id")?.let { winner -> candidates.firstOrNull { it.id == winner } }
        val state = json.string("state")
        val ownFinished = round?.get("myFinished")?.toString() == "true"
        val phase = when {
            state == "LOBBY" -> PartyPhase.LOBBY
            state == "VOTING" && ownFinished -> PartyPhase.WAITING
            state == "VOTING" || state == "REFRESHING" -> PartyPhase.FEED
            state == "MATCHED" -> PartyPhase.MATCH
            else -> PartyPhase.FINISHED
        }
        return PartyRoom(
            id = id,
            name = json.string("name") ?: "",
            hostId = owner,
            participants = members,
            filters = filters,
            phase = phase,
            items = candidates,
            votes = votes,
            matchedItem = matched,
            version = version,
            roundId = round?.string("id"),
            finishedCount = round?.get("finishedCount")?.jsonPrimitive?.intOrNull ?: 0,
        )
    }

    private fun parseFilters(json: JsonObject): PartyFilters {
        val kind = json.string("kind")
        val amount = json["budgetTotal"]?.takeUnless { it == JsonNull }?.jsonObject
            ?.get("amountMinor")?.jsonPrimitive?.intOrNull ?: 200_000
        val partySize = json["partySize"]?.jsonPrimitive?.intOrNull?.coerceAtLeast(1) ?: 1
        return PartyFilters(
            feedType = if (kind == "EVENT") PartyFeedType.EVENTS else PartyFeedType.PLACES,
            category = when (json.string("categoryId")) {
                "food" -> PartyCategory.FOOD
                "outdoors" -> PartyCategory.ACTIVITIES
                else -> PartyCategory.ALL
            },
            budgetPerPerson = amount / 100 / partySize,
            whenLabel = json.string("localDate") ?: "Сегодня",
        )
    }

    private fun parseCard(card: JsonObject): PartyItem {
        val ref = card["ref"]?.jsonObject
        val location = card["location"]?.jsonObject
        val price = card["price"]?.takeUnless { it == JsonNull }?.jsonObject?.get("amountMinor")?.jsonPrimitive?.intOrNull
        val category = when (card.string("categoryId")) {
            "food" -> "Еда и бары"
            "outdoors" -> "Активность"
            "culture" -> "Культура"
            else -> "Событие"
        }
        val priceLabel = when {
            price == null -> "Цена неизвестна"
            price == 0 -> "Бесплатно"
            else -> "${price / 100} ₽"
        }
        val image = card["cover"]?.takeUnless { it == JsonNull }?.jsonObject?.string("url")?.let(api::resolveMediaUrl)
        return PartyItem(
            id = ref?.string("id").orEmpty(),
            title = card.string("title").orEmpty(),
            category = category,
            price = priceLabel,
            location = location?.string("address").orEmpty(),
            description = card.string("description").orEmpty().ifBlank { card.string("title").orEmpty() },
            imageKey = image.orEmpty(),
            pricePerPerson = price?.div(100) ?: Int.MAX_VALUE,
        )
    }

    private fun categoryId(filters: PartyFilters): String? = when (filters.category) {
        PartyCategory.ALL -> null
        PartyCategory.FOOD -> "food"
        PartyCategory.ACTIVITIES -> "outdoors"
    }

    private fun localDate(filters: PartyFilters): String? {
        if (filters.feedType == PartyFeedType.PLACES) return null
        val match = Regex("(\\d{1,2})\\.(\\d{1,2})\\.(\\d{4})").find(filters.whenLabel)
        val parts = match?.destructured?.toList() ?: if (filters.whenLabel.contains("Сегодня", true)) {
            currentDateParts().map(Int::toString)
        } else return null
        val (year, month, day) = if (match == null) parts.reversed() else listOf(parts[2], parts[1], parts[0])
        return "${year.padStart(4, '0')}-${month.padStart(2, '0')}-${day.padStart(2, '0')}"
    }

    private fun extractInviteToken(value: String): String {
        val token = value.substringAfter("token=", missingDelimiterValue = "").substringBefore('&')
            .ifBlank { value.takeIf { !it.contains("://") } ?: "" }
        require(token.isNotBlank()) { "В ссылке-приглашении нет токена" }
        return token
    }
}

private fun String.encodeQueryComponent(): String = buildString {
    encodeToByteArray().forEach { byte ->
        val value = byte.toInt() and 0xff
        if ((value in 0x41..0x5a) || (value in 0x61..0x7a) || (value in 0x30..0x39) || value == 0x2d || value == 0x2e || value == 0x5f || value == 0x7e) {
            append(value.toChar())
        } else {
            append('%')
            append("0123456789ABCDEF"[value ushr 4])
            append("0123456789ABCDEF"[value and 0x0f])
        }
    }
}
