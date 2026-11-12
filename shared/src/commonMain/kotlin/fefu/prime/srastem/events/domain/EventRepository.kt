package fefu.prime.srastem.events.domain

import kotlinx.coroutines.flow.Flow

/** App-facing event contract. Implementations can be local mocks or backed by the release REST API. */
interface EventRepository {
    val currentUserId: String
    fun observeEvents(): Flow<List<SrEvent>>
    suspend fun getEvent(id: String): SrEvent?
    suspend fun createEvent(draft: EventDraft): SrEvent
    suspend fun joinEvent(eventId: String, userId: String)
    suspend fun leaveEvent(eventId: String, userId: String)
    suspend fun removeParticipant(eventId: String, organizerId: String, participantId: String)
    suspend fun deleteEvent(eventId: String, organizerId: String)
    suspend fun refreshEvents() = Unit
    suspend fun getEventCategories(): List<EventCatalogCategory> = emptyList()
}

/** Application-facing operations used by presentation; keeps ViewModel code independent of data adapters. */
class EventUseCases(private val repository: EventRepository) {
    val currentUserId: String get() = repository.currentUserId
    fun observeEvents() = repository.observeEvents()
    suspend fun getEvent(id: String) = repository.getEvent(id)
    suspend fun createEvent(draft: EventDraft) = repository.createEvent(draft)
    suspend fun joinEvent(eventId: String, userId: String) = repository.joinEvent(eventId, userId)
    suspend fun leaveEvent(eventId: String, userId: String) = repository.leaveEvent(eventId, userId)
    suspend fun removeParticipant(eventId: String, organizerId: String, participantId: String) = repository.removeParticipant(eventId, organizerId, participantId)
    suspend fun deleteEvent(eventId: String, organizerId: String) = repository.deleteEvent(eventId, organizerId)
    suspend fun refreshEvents() = repository.refreshEvents()
    suspend fun getEventCategories() = repository.getEventCategories()
}
