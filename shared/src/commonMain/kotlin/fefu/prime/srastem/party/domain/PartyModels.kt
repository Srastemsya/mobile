package fefu.prime.srastem.party.domain

import kotlinx.coroutines.flow.Flow

enum class PartyFeedType { PLACES, EVENTS }
enum class PartyCategory { ALL, FOOD, ACTIVITIES }
enum class PartyPhase { LOBBY, FEED, WAITING, MATCH, FINISHED }
enum class PartyVote { LIKE, SKIP }

data class PartyFilters(
    val feedType: PartyFeedType = PartyFeedType.PLACES,
    val category: PartyCategory = PartyCategory.ALL,
    val budgetPerPerson: Int = 2_000,
    val whenLabel: String = "Сегодня",
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
