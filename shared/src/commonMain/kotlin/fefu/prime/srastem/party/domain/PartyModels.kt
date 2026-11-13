package fefu.prime.srastem.party.domain

import fefu.prime.srastem.feed.domain.FeedFilters
import fefu.prime.srastem.feed.domain.FeedKind
import fefu.prime.srastem.feed.domain.SwipeDecision
import kotlinx.coroutines.flow.Flow

/**
 * Рефакторинг FE-F00 (AC-08): каноническая доменная модель подборки живёт в `feed/domain`
 * (`FeedKind`, `FeedFilters`, `FeedCard`, `SwipeDecision`, `RoomSnapshot`). Фича комнаты `party`
 * продолжает работать через алиасы и граничные отображения ниже до переноса сценариев
 * FE-F01…FE-F06 на use cases ленты; новые сценарии пишутся сразу против `feed/domain`.
 */
typealias PartyVote = SwipeDecision

enum class PartyFeedType { PLACES, EVENTS }
enum class PartyCategory { ALL, FOOD, ACTIVITIES }
enum class PartyPhase { LOBBY, FEED, WAITING, MATCH, FINISHED }

fun PartyFeedType.toFeedKind(): FeedKind = when (this) {
    PartyFeedType.PLACES -> FeedKind.PLACE
    PartyFeedType.EVENTS -> FeedKind.EVENT
}

fun FeedKind.toPartyFeedType(): PartyFeedType = when (this) {
    FeedKind.PLACE -> PartyFeedType.PLACES
    FeedKind.EVENT -> PartyFeedType.EVENTS
}

data class PartyFilters(
    val feedType: PartyFeedType = PartyFeedType.PLACES,
    val category: PartyCategory = PartyCategory.ALL,
    val budgetPerPerson: Int = 2_000,
    val whenLabel: String = "Сегодня",
)

/**
 * Граничное отображение UI-фильтров комнаты в канонические фильтры контракта Discovery v1.
 * Бюджет комнаты общий: `budgetPerPerson * partySize` в копейках.
 */
fun PartyFilters.toFeedFilters(partySize: Int = 1): FeedFilters = FeedFilters(
    kind = feedType.toFeedKind(),
    categoryId = when (category) {
        PartyCategory.ALL -> null
        PartyCategory.FOOD -> "food"
        PartyCategory.ACTIVITIES -> "outdoors"
    },
    localDate = localDateOf(whenLabel).takeIf { feedType == PartyFeedType.EVENTS },
    budgetTotalMinor = budgetPerPerson.coerceAtLeast(0).toLong() * 100 * partySize.coerceAtLeast(1),
    partySize = partySize.coerceAtLeast(1),
)

data class PartyItem(
    val id: String,
    val title: String,
    val category: String,
    val price: String,
    val location: String,
    val description: String,
    val imageKey: String,
    val pricePerPerson: Int,
    val confirmedCount: Int? = null,
    val capacity: Int? = null,
    val remainingCount: Int? = null,
)

data class PartyParticipant(
    val id: String,
    val name: String,
    val isConnected: Boolean,
    val completedFeed: Boolean,
)

data class PartyRoom(
    val id: String,
    val name: String,
    val hostId: String,
    val participants: List<PartyParticipant>,
    val filters: PartyFilters,
    val phase: PartyPhase,
    val items: List<PartyItem>,
    val votes: Map<String, Map<String, PartyVote>>,
    val matchedItem: PartyItem? = null,
    val version: Long = 0,
    val roundId: String? = null,
    val finishedCount: Int = 0,
)

data class PartySession(
    val participantId: String,
    val room: PartyRoom,
    val invitationUrl: String,
)

interface PartyRepository {
    suspend fun getFeedItems(filters: PartyFilters): List<PartyItem>
    suspend fun createRoom(name: String, participantName: String): PartySession
    suspend fun joinRoom(roomId: String, participantName: String): PartySession
    suspend fun updateFilters(roomId: String, participantId: String, filters: PartyFilters)
    suspend fun startFeed(roomId: String, participantId: String)
    suspend fun loadMoreItems(roomId: String, participantId: String, nextItemIndex: Int): PartyRoom
    suspend fun submitVote(roomId: String, participantId: String, itemId: String, vote: PartyVote)
    suspend fun completeFeed(roomId: String, participantId: String)
    fun observeRoom(roomId: String): Flow<PartyRoom>
}

/** `ДД.ММ.ГГГГ` или «Сегодня» → `ГГГГ-ММ-ДД` для `localDate`; null, если дата не распознана. */
fun localDateOf(label: String): String? {
    val match = Regex("(\\d{1,2})\\.(\\d{1,2})\\.(\\d{4})").find(label)
    if (match != null) {
        val (day, month, year) = match.destructured.toList()
        return "${year.padStart(4, '0')}-${month.padStart(2, '0')}-${day.padStart(2, '0')}"
    }
    if (label.contains("Сегодня", ignoreCase = true)) {
        val (year, month, day) = fefu.prime.srastem.currentDateParts()
        return "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
    }
    return null
}
