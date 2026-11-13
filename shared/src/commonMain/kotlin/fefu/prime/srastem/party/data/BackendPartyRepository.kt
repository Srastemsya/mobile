package fefu.prime.srastem.party.data

import fefu.prime.srastem.backend.BackendApi
import fefu.prime.srastem.backend.jsonObjectOf
import fefu.prime.srastem.backend.string
import fefu.prime.srastem.feed.data.FeedContract
import fefu.prime.srastem.feed.data.FeedMappers
import fefu.prime.srastem.feed.data.encodeQueryComponent
import fefu.prime.srastem.feed.domain.FeedCard
import fefu.prime.srastem.feed.domain.FeedFilters
import fefu.prime.srastem.feed.domain.RoomPhase
import fefu.prime.srastem.feed.domain.RoomSnapshot
import fefu.prime.srastem.party.domain.PartyCategory
import fefu.prime.srastem.party.domain.PartyFilters
import fefu.prime.srastem.party.domain.PartyItem
import fefu.prime.srastem.party.domain.PartyParticipant
import fefu.prime.srastem.party.domain.PartyPhase
import fefu.prime.srastem.party.domain.PartyRepository
import fefu.prime.srastem.party.domain.PartyRoom
import fefu.prime.srastem.party.domain.PartySession
import fefu.prime.srastem.party.domain.toFeedFilters
import fefu.prime.srastem.party.domain.toPartyFeedType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject

/**
 * Унаследованный репозиторий комнаты (сценарии FE-F04…FE-F06 до переноса на use cases ленты).
 * После рефакторинга FE-F00 не парсит контракт сам: query и тела строит [FeedContract],
 * DTO разбирает [FeedMappers] из `feed/data`, а доменные проекции комнаты строятся из
 * канонического [RoomSnapshot].
 */
class BackendPartyRepository(private val api: BackendApi) : PartyRepository {
    private val pendingFilters = mutableMapOf<String, PartyFilters>()

    override suspend fun getFeedItems(filters: PartyFilters): List<PartyItem> {
        val feedFilters = filters.toFeedFilters()
        val all = mutableListOf<PartyItem>()
        var cursor: String? = null
        do {
            val page = api.get(FeedContract.candidatesQuery(feedFilters, cursor)).jsonObject
            all += FeedMappers.page(page).items.map(::partyItem)
            cursor = page.string("nextCursor")
        } while (cursor != null)
        return all
    }

    override suspend fun createRoom(name: String, participantName: String): PartySession {
        val createScope = "create-room:${name.trim().hashCode()}"
        val created = api.post(
            "/v1/rooms",
            jsonObjectOf("name" to name.trim().ifBlank { null }),
            api.idempotencyKey(createScope)
        ).value?.jsonObject
            ?: error("Сервер не вернул комнату")
        val id = created.string("id") ?: error("Сервер не вернул ID комнаты")
        val inviteScope = "invite-room:$id"
        val invite = api.postEmpty(
            "/v1/rooms/$id/invites",
            api.idempotencyKey(inviteScope)
        ).value?.jsonObject
        api.clearIdempotencyKey(inviteScope)
        val inviteUrl = invite?.string("url") ?: error("Сервер не вернул приглашение")
        api.clearIdempotencyKey(createScope)
        return PartySession(
            api.userId(),
            partyRoom(FeedMappers.room(created, api.userId())),
            inviteUrl
        )
    }

    override suspend fun joinRoom(roomId: String, participantName: String): PartySession {
        val token = extractInviteToken(roomId)
        val joinScope = "join-room:${token.hashCode()}"
        val joined = api.post(
            "/v1/room-invites/join",
            jsonObjectOf("token" to token),
            api.idempotencyKey(joinScope)
        ).value?.jsonObject
            ?: error("Сервер не вернул комнату")
        api.clearIdempotencyKey(joinScope)
        return PartySession(api.userId(), partyRoom(FeedMappers.room(joined, api.userId())), roomId)
    }

    override suspend fun updateFilters(
        roomId: String,
        participantId: String,
        filters: PartyFilters
    ) {
        pendingFilters[roomId] = filters
    }

    override suspend fun startFeed(roomId: String, participantId: String) {
        val room = FeedMappers.room(api.get("/v1/rooms/$roomId").jsonObject, api.userId())
        val members = room.members.size.coerceAtLeast(1)
        val filters = (pendingFilters[roomId] ?: PartyFilters()).toFeedFilters(members)
        val scope = "start-room:$roomId"
        api.post(
            "/v1/rooms/$roomId/start",
            FeedContract.startBody(filters),
            api.idempotencyKey(scope)
        )
        api.clearIdempotencyKey(scope)
        pendingFilters.remove(roomId)
    }

    override suspend fun loadMoreItems(
        roomId: String,
        participantId: String,
        nextItemIndex: Int
    ): PartyRoom =
        partyRoom(FeedMappers.room(api.get("/v1/rooms/$roomId").jsonObject, api.userId()))

    override suspend fun submitVote(
        roomId: String,
        participantId: String,
        itemId: String,
        vote: fefu.prime.srastem.feed.domain.SwipeDecision
    ) {
        val room = FeedMappers.room(api.get("/v1/rooms/$roomId").jsonObject, api.userId())
        val roundId = room.roundId ?: error("Раунд завершён")
        val candidate = room.candidates.firstOrNull { it.ref.id == itemId }
            ?: error("Карточка больше недоступна")
        api.put(
            "/v1/rooms/$roomId/rounds/$roundId/votes/${candidate.ref.kind.name}/$itemId",
            jsonObjectOf("choice" to if (vote == fefu.prime.srastem.feed.domain.SwipeDecision.LIKE) "LIKE" else "PASS"),
        )
    }

    override suspend fun completeFeed(roomId: String, participantId: String) {
        val roundId =
            FeedMappers.room(api.get("/v1/rooms/$roomId").jsonObject, api.userId()).roundId
                ?: error("Раунд уже завершён")
        api.putEmpty("/v1/rooms/$roomId/rounds/$roundId/completion")
    }

    override fun observeRoom(roomId: String): Flow<PartyRoom> = flow {
        var room =
            partyRoom(FeedMappers.room(api.get("/v1/rooms/$roomId").jsonObject, api.userId()))
        emit(room)
        while (room.phase != PartyPhase.MATCH && room.phase != PartyPhase.FINISHED) {
            try {
                val result =
                    api.getResult("/v1/rooms/$roomId?afterVersion=${room.version}&waitSeconds=25")
                if (result.status == 204) continue
                val snapshot =
                    result.value?.jsonObject?.let { FeedMappers.room(it, api.userId()) } ?: continue
                if (snapshot.version >= room.version) {
                    room = partyRoom(snapshot)
                    emit(room)
                }
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                delay(1_500)
            }
        }
    }

    /** Проекция канонического snapshot комнаты в модель унаследованного UI комнаты. */
    private fun partyRoom(snapshot: RoomSnapshot): PartyRoom {
        val items = snapshot.candidates.map(::partyItem)
        return PartyRoom(
            id = snapshot.id,
            name = snapshot.name.orEmpty(),
            hostId = snapshot.ownerId,
            participants = snapshot.members.map { member ->
                PartyParticipant(
                    id = member.userId,
                    name = member.displayName.ifBlank { "Участник" },
                    isConnected = true,
                    completedFeed = member.userId == api.userId() && snapshot.myFinished,
                )
            },
            filters = snapshot.filters?.toPartyFilters() ?: pendingFilters[snapshot.id]
            ?: PartyFilters(),
            phase = when (snapshot.phase) {
                RoomPhase.LOBBY -> PartyPhase.LOBBY
                RoomPhase.VOTING -> if (snapshot.myFinished) PartyPhase.WAITING else PartyPhase.FEED
                RoomPhase.REFRESHING -> PartyPhase.FEED
                RoomPhase.MATCHED -> PartyPhase.MATCH
                RoomPhase.EXHAUSTED, RoomPhase.CLOSED -> PartyPhase.FINISHED
            },
            items = items,
            votes = snapshot.myVotes.mapValues { (_, decision) -> mapOf(api.userId() to decision) },
            matchedItem = snapshot.winner?.let { winner -> items.firstOrNull { it.id == winner.id } },
            version = snapshot.version,
            roundId = snapshot.roundId,
            finishedCount = snapshot.finishedCount,
        )
    }

    private fun partyItem(card: FeedCard): PartyItem = PartyItem(
        id = card.ref.id,
        title = card.title,
        category = categoryLabel(card.category),
        price = card.priceLabel,
        location = card.addressOrDate,
        description = card.description.ifBlank { card.title },
        imageKey = api.resolveMediaUrl(card.coverUrl).orEmpty(),
        pricePerPerson = card.priceMinor?.div(100)?.toInt() ?: Int.MAX_VALUE,
    )

    private fun FeedFilters.toPartyFilters(): PartyFilters = PartyFilters(
        feedType = kind.toPartyFeedType(),
        category = when (categoryId) {
            "food" -> PartyCategory.FOOD
            "outdoors" -> PartyCategory.ACTIVITIES
            else -> PartyCategory.ALL
        },
        budgetPerPerson = ((budgetTotalMinor ?: 200_000L) / 100 / (partySize
            ?: 1).coerceAtLeast(1)).toInt(),
        whenLabel = localDate ?: "Сегодня",
    )

    private fun categoryLabel(categoryId: String): String = when (categoryId) {
        "food" -> "Еда и бары"
        "outdoors" -> "Активность"
        "culture" -> "Культура"
        else -> "Событие"
    }

    private fun extractInviteToken(value: String): String {
        val token = value.substringAfter("token=", missingDelimiterValue = "").substringBefore('&')
            .ifBlank { value.takeIf { !it.contains("://") } ?: "" }
        require(token.isNotBlank()) { "В ссылке-приглашении нет токена" }
        return token
    }
}
