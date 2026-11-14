package fefu.prime.srastem.events.data

import fefu.prime.srastem.events.domain.EventAdmission
import fefu.prime.srastem.events.domain.EventCategory
import fefu.prime.srastem.events.domain.EventDraft
import fefu.prime.srastem.events.domain.EventParticipant
import fefu.prime.srastem.events.domain.EventRepository
import fefu.prime.srastem.events.domain.SrEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** In-memory stand-in for the release event REST API. */
class MockEventRepository : EventRepository {
    private val mutex = Mutex()
    private val events = MutableStateFlow(seedEvents())
    private var nextId = 1

    override val currentUserId = DEMO_USER_ID
    override fun observeEvents(): Flow<List<SrEvent>> = events.asStateFlow()
    override suspend fun getEvent(id: String) = events.value.firstOrNull { it.id == id }

    override suspend fun createEvent(draft: EventDraft): SrEvent = mutex.withLock {
        val event = SrEvent(
            id = "local-${nextId++}",
            title = draft.title.trim(),
            category = draft.category,
            startsAt = draft.startsAt,
            address = draft.address,
            description = draft.description.trim(),
            admission = draft.admission,
            capacity = if (draft.admission == EventAdmission.BOOKING) draft.capacity.toIntOrNull() else null,
            priceRub = if (draft.admission == EventAdmission.BOOKING) draft.priceRub.toIntOrNull()
                ?: 0 else 0,
            organizerId = DEMO_USER_ID,
            organizerName = "Ренат",
            coverUri = draft.coverUri,
            latitude = draft.latitude,
            longitude = draft.longitude,
        )
        events.value = listOf(event) + events.value
        event
    }

    override suspend fun joinEvent(eventId: String, userId: String) = mutex.withLock {
        val event = eventOrThrow(eventId)
        require(!event.isPast) { "Регистрация на прошедшее событие закрыта" }
        require(event.organizerId != userId) { "Вы организатор этого события" }
        require(!event.isFull) { "Свободных мест больше нет" }
        require(event.participants.none { it.id == userId }) { "Вы уже записаны" }
        update(event.copy(participants = event.participants + EventParticipant(userId, "Ренат")))
    }

    override suspend fun leaveEvent(eventId: String, userId: String) = mutex.withLock {
        val event = eventOrThrow(eventId)
        update(event.copy(participants = event.participants.filterNot { it.id == userId }))
    }

    override suspend fun removeParticipant(
        eventId: String,
        organizerId: String,
        participantId: String
    ) = mutex.withLock {
        val event = eventOrThrow(eventId)
        require(event.organizerId == organizerId) { "Удалять участников может только организатор" }
        update(event.copy(participants = event.participants.filterNot { it.id == participantId }))
    }

    override suspend fun deleteEvent(eventId: String, organizerId: String) = mutex.withLock {
        val event = eventOrThrow(eventId)
        require(event.organizerId == organizerId) { "Удалять событие может только организатор" }
        events.value = events.value.filterNot { it.id == eventId }
        Unit
    }

    private fun eventOrThrow(id: String) = events.value.firstOrNull { it.id == id }
        ?: error("Событие больше недоступно")

    private fun update(event: SrEvent) {
        events.value = events.value.map { if (it.id == event.id) event else it }
    }

    private fun seedEvents() = listOf(
        SrEvent(
            id = "tokarevsky-sunset", title = "Закат на Токаревском", category = EventCategory.WALK,
            startsAt = "Пт, 13 ноя · 17:00", address = "Токаревская кошка, Владивосток",
            description = "Встретимся у начала косы и прогуляемся до маяка. Возьмите тёплую одежду и термос. Можно прийти одному или с друзьями.",
            admission = EventAdmission.FREE, capacity = null, priceRub = 0,
            organizerId = "organizer-sasha", organizerName = "Саша",
            latitude = 42.8952, longitude = 131.8494,
        ),
        SrEvent(
            id = "sup-tobizina", title = "Сапы на Тобизина", category = EventCategory.ACTIVE,
            startsAt = "Сб, 14 ноя · 10:00", address = "Мыс Тобизина, остров Русский",
            description = "Встретимся на Русском острове, пройдём инструктаж и выйдем на воду вдоль мыса. Сап, весло и жилет входят в стоимость.",
            admission = EventAdmission.BOOKING, capacity = 8, priceRub = 0,
            organizerId = "organizer-marina", organizerName = "Марина",
            latitude = 42.9923, longitude = 131.8465,
            participants = listOf(
                EventParticipant("sup-guest-1", "Аня"), EventParticipant("sup-guest-2", "Миша"),
                EventParticipant("sup-guest-3", "Лена"), EventParticipant("sup-guest-4", "Игорь"),
            ),
        ),
        SrEvent(
            id = "quiz-svetlanskaya", title = "Квиз на Светланской", category = EventCategory.GAMES,
            startsAt = "Сб, 14 ноя · 19:00", address = "Светланская, 22, Владивосток",
            description = "Собираемся командами, отвечаем на вопросы о городе и знакомимся. Можно прийти одному.",
            admission = EventAdmission.BOOKING, capacity = 8, priceRub = 0,
            organizerId = "organizer-dima", organizerName = "Дима",
            latitude = 43.1157, longitude = 131.8856,
            participants = listOf(
                EventParticipant("quiz-guest-1", "Аня"),
                EventParticipant("quiz-guest-2", "Миша")
            ),
        ),
        SrEvent(
            id = "promenade-demo", title = "Прогулка по набережной", category = EventCategory.WALK,
            startsAt = "Вс, 15 ноя · 16:00", address = "Спортивная набережная, Владивосток",
            description = "Прогуляемся вдоль моря и выпьем кофе. Встречаемся у фонтана на Спортивной набережной.",
            admission = EventAdmission.FREE, capacity = 8, priceRub = 0,
            organizerId = DEMO_USER_ID, organizerName = "Ренат",
            latitude = 43.1155, longitude = 131.8855,
            participants = listOf(
                EventParticipant("guest-anna", "Анна Ким"),
                EventParticipant("guest-ilya", "Илья Волков")
            ),
        ),
        SrEvent(
            id = "shkota-past",
            title = "Закат на Шкота",
            category = EventCategory.WALK,
            startsAt = "2 июня · Владивосток",
            address = "Остров Шкота, Владивосток",
            description = "Вечерняя прогулка у моря.",
            admission = EventAdmission.FREE,
            capacity = null,
            priceRub = 0,
            organizerId = DEMO_USER_ID,
            organizerName = "Ренат",
            isPast = true,
        ),
    )

    companion object {
        const val DEMO_USER_ID = "demo-user"
    }
}
