package fefu.prime.srastem.party.data

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
import fefu.prime.srastem.events.domain.EventCategory
import fefu.prime.srastem.events.domain.EventRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.launch
import kotlin.random.Random

/** In-memory stand-in for the server. Shared by every repository instance in this process. */
object MockPartyBackend {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()
    private val rooms = mutableMapOf<String, MutableStateFlow<PartyRoom>>()

    fun getFeedItems(filters: PartyFilters, candidates: List<PartyItem>? = null): List<PartyItem> =
        (candidates ?: itemsFor(filters)).filter { it.matches(filters) }

    suspend fun create(name: String, participantName: String): PartySession = mutex.withLock {
        val id = Random.nextInt(100000, 999999).toString()
        val hostId = "member-${Random.nextInt(1000, 9999)}"
        val room = PartyRoom(
            id = id,
            name = name.ifBlank { "Вечер пятницы" },
            hostId = hostId,
            participants = listOf(PartyParticipant(hostId, participantName, true, false)),
            filters = PartyFilters(),
            phase = PartyPhase.LOBBY,
            items = emptyList(),
            votes = emptyMap(),
        )
        rooms[id] = MutableStateFlow(room)
        PartySession(hostId, room, "srastem://party/$id")
    }

    suspend fun join(roomId: String, participantName: String): PartySession = mutex.withLock {
        val id = roomId.trim().substringAfterLast('/').uppercase()
        val flow = rooms[id] ?: rooms[id.lowercase()]
        ?: error("Комната $id не найдена. Проверьте ссылку-приглашение.")
        val room = flow.value
        check(room.phase == PartyPhase.LOBBY || room.phase == PartyPhase.FEED) {
            "Выбор в этой комнате уже завершён"
        }
        val participantId = "member-${Random.nextInt(1000, 9999)}"
        flow.value = room.copy(
            participants = room.participants + PartyParticipant(
                participantId,
                participantName,
                true,
                false
            )
        )
        PartySession(participantId, flow.value, "srastem://party/$id")
    }

    fun observe(roomId: String): StateFlow<PartyRoom> = rooms[roomId.uppercase()]?.asStateFlow()
        ?: rooms[roomId.lowercase()]?.asStateFlow()
        ?: error("Комната $roomId не найдена")

    suspend fun updateFilters(roomId: String, participantId: String, filters: PartyFilters) =
        mutex.withLock {
            val flow = requireRoom(roomId)
            require(flow.value.hostId == participantId) { "Фильтры может менять только организатор" }
            flow.value = flow.value.copy(filters = filters)
        }

    suspend fun start(roomId: String, participantId: String, candidates: List<PartyItem>? = null) = mutex.withLock {
        val flow = requireRoom(roomId)
        require(flow.value.hostId == participantId) { "Запустить выбор может только организатор" }
        check(flow.value.phase == PartyPhase.LOBBY)
        flow.value = flow.value.copy(
            phase = PartyPhase.FEED,
            items = (candidates ?: itemsFor(flow.value.filters)).filter { it.matches(flow.value.filters) }.take(3),
            participants = flow.value.participants + PartyParticipant(
                "demo-guest",
                "Друг",
                true,
                false
            ),
        )
        simulateRemotePlayer(roomId)
    }

    suspend fun loadMore(roomId: String, participantId: String, nextItemIndex: Int, candidates: List<PartyItem>? = null): PartyRoom =
        mutex.withLock {
            val flow = requireRoom(roomId)
            val room = flow.value
            require(room.participants.any { it.id == participantId })
            if (room.phase != PartyPhase.FEED || room.items.size - nextItemIndex > 2 || room.items.size >= MAX_FEED_ITEMS) {
                return@withLock room
            }
            val available = (candidates ?: itemsFor(room.filters)).filter { it.matches(room.filters) }
                .filterNot { candidate -> room.items.any { it.id == candidate.id } }
            val appended = available.firstOrNull() ?: return@withLock room
            flow.value = room.copy(items = room.items + appended)
            flow.value
        }

    suspend fun vote(roomId: String, participantId: String, itemId: String, vote: PartyVote) =
        mutex.withLock {
            val flow = requireRoom(roomId)
            val room = flow.value
            require(room.phase == PartyPhase.FEED || room.phase == PartyPhase.WAITING)
            require(room.participants.any { it.id == participantId })
            require(room.items.any { it.id == itemId })
            val votes = room.votes.toMutableMap()
            val itemVotes = votes[itemId].orEmpty().toMutableMap()
            itemVotes[participantId] = vote
            votes[itemId] = itemVotes
            flow.value = room.copy(votes = votes)
        }

    suspend fun completeFeed(roomId: String, participantId: String) = mutex.withLock {
        val flow = requireRoom(roomId)
        val room = flow.value
        val participants =
            room.participants.map { if (it.id == participantId) it.copy(completedFeed = true) else it }
        flow.value = room.copy(participants = participants, phase = PartyPhase.WAITING)
        resolveWhenEveryoneIsDone(flow)
    }

    private fun simulateRemotePlayer(roomId: String) {
        scope.launch {
            delay(1_500)
            val flow = rooms[roomId] ?: return@launch
            delay(2_000)
            mutex.withLock {
                val room = flow.value
                if (room.phase != PartyPhase.FEED && room.phase != PartyPhase.WAITING) return@withLock
                val first = room.items.firstOrNull() ?: return@withLock
                val votes = room.votes.toMutableMap()
                votes[first.id] = votes[first.id].orEmpty() + ("demo-guest" to PartyVote.LIKE)
                flow.value = room.copy(
                    votes = votes,
                    participants = room.participants.map {
                        if (it.id == "demo-guest") it.copy(
                            completedFeed = true
                        ) else it
                    },
                )
                resolveWhenEveryoneIsDone(flow)
            }
        }
    }

    private fun resolveWhenEveryoneIsDone(flow: MutableStateFlow<PartyRoom>) {
        val room = flow.value
        if (room.participants.isNotEmpty() && room.participants.all { it.completedFeed }) {
            val needed = room.participants.size / 2 + 1
            val majority = room.items.mapIndexedNotNull { index, item ->
                val likeCount = room.votes[item.id].orEmpty().count { it.value == PartyVote.LIKE }
                if (likeCount >= needed) Triple(index, item, likeCount) else null
            }
            val winner = majority.maxWithOrNull(
                compareBy<Triple<Int, PartyItem, Int>> { it.third }.thenBy { -it.first },
            )?.second
            flow.value = room.copy(
                phase = if (winner == null) PartyPhase.FINISHED else PartyPhase.MATCH,
                matchedItem = winner
            )
        }
    }

    private fun requireRoom(roomId: String) =
        rooms[roomId.uppercase()] ?: error("Комната $roomId не найдена")

    private const val MAX_FEED_ITEMS = 9

    private fun PartyItem.matches(filters: PartyFilters): Boolean {
        val categoryMatches = filters.category == PartyCategory.ALL ||
            filters.category == PartyCategory.FOOD && category in listOf("Бар", "Еда и бары") ||
            filters.category == PartyCategory.ACTIVITIES && category == "Активность"
        return categoryMatches && pricePerPerson <= filters.budgetPerPerson
    }

    private fun itemsFor(filters: PartyFilters): List<PartyItem> = when (filters.feedType) {
        PartyFeedType.PLACES -> listOf(
            PartyItem(
                "millionka",
                "Бар «Миллионка»",
                "Бар",
                "₽₽",
                "Светланская, 13",
                "Настойки и музыка в старом городе.",
                "millionka",
                1_500
            ),
            PartyItem(
                "coffee",
                "Кофейня у моря",
                "Еда и бары",
                "450 ₽",
                "Центр города",
                "Кофе и вид на бухту.",
                "coffee",
                450
            ),
            PartyItem(
                "restaurant",
                "Ресторан у набережной",
                "Еда и бары",
                "1 800 ₽",
                "Набережная",
                "Ужин с видом на море.",
                "restaurant",
                1_800
            ),
            PartyItem(
                "sup-rental",
                "Прокат сапов",
                "Активность",
                "1 800 ₽",
                "Русский остров",
                "Маршрут вдоль бухты для небольшой компании.",
                "sup-rental",
                1_800
            ),
        )

        PartyFeedType.EVENTS -> listOf(
            PartyItem(
                "sup",
                "Сапы на Тобизина",
                "Активность",
                "1 800 ₽",
                "Русский остров",
                "Прогулка вдоль скал на закате.",
                "sup",
                1_800
            ),
            PartyItem(
                "quiz",
                "Квиз: Владивосток",
                "Событие",
                "700 ₽",
                "Центр города",
                "Командная игра и новые знакомства.",
                "quiz",
                700
            ),
            PartyItem(
                "walk",
                "Прогулка у моря",
                "Активность",
                "Бесплатно",
                "Набережная",
                "Небольшая компания и свежий воздух.",
                "walk",
                0
            ),
        )
    }.filter { item ->
        val categoryMatches = filters.category == PartyCategory.ALL ||
                filters.category == PartyCategory.FOOD && item.category in listOf(
            "Бар",
            "Еда и бары"
        ) ||
                filters.category == PartyCategory.ACTIVITIES && item.category == "Активность"
        categoryMatches && item.pricePerPerson <= filters.budgetPerPerson
    }
}

class MockPartyRepository(private val eventRepository: EventRepository? = null) : PartyRepository {
    private suspend fun candidates(filters: PartyFilters): List<PartyItem>? {
        if (filters.feedType != PartyFeedType.EVENTS || eventRepository == null) return null
        return eventRepository.observeEvents().first().filterNot { it.isPast }.map { event ->
            PartyItem(
                id = event.id,
                title = event.title,
                category = when (event.category) {
                    EventCategory.FOOD -> "Еда и бары"
                    EventCategory.ACTIVE -> "Активность"
                    else -> "Событие"
                },
                price = if (event.priceRub == 0) "Бесплатно" else "${event.priceRub} ₽",
                location = event.address,
                description = event.description,
                imageKey = event.coverUri.orEmpty(),
                pricePerPerson = event.priceRub,
            )
        }
    }

    override suspend fun getFeedItems(filters: PartyFilters) =
        MockPartyBackend.getFeedItems(filters, candidates(filters))

    override suspend fun createRoom(name: String, participantName: String) =
        MockPartyBackend.create(name, participantName)

    override suspend fun joinRoom(roomId: String, participantName: String) =
        MockPartyBackend.join(roomId, participantName)

    override suspend fun updateFilters(
        roomId: String,
        participantId: String,
        filters: PartyFilters
    ) = MockPartyBackend.updateFilters(roomId, participantId, filters)

    override suspend fun startFeed(roomId: String, participantId: String) = run {
        val filters = MockPartyBackend.observe(roomId).value.filters
        MockPartyBackend.start(roomId, participantId, candidates(filters))
    }

    override suspend fun loadMoreItems(roomId: String, participantId: String, nextItemIndex: Int) = run {
        val filters = MockPartyBackend.observe(roomId).value.filters
        MockPartyBackend.loadMore(roomId, participantId, nextItemIndex, candidates(filters))
    }

    override suspend fun submitVote(
        roomId: String,
        participantId: String,
        itemId: String,
        vote: PartyVote
    ) = MockPartyBackend.vote(roomId, participantId, itemId, vote)

    override suspend fun completeFeed(roomId: String, participantId: String) =
        MockPartyBackend.completeFeed(roomId, participantId)

    override fun observeRoom(roomId: String) = MockPartyBackend.observe(roomId)
}
