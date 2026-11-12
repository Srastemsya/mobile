package fefu.prime.srastem.events.domain

enum class EventCategory(val title: String) {
    WALK("Прогулка"), ACTIVE("Активный отдых"), GAMES("Игры"), FOOD("Еда и бары"), OTHER("Другое")
}

enum class EventAdmission(val title: String) { FREE("Свободный вход"), BOOKING("По записи") }

data class EventParticipant(val id: String, val name: String)

data class SrEvent(
    val id: String,
    val title: String,
    val category: EventCategory,
    val startsAt: String,
    val address: String,
    val description: String,
    val admission: EventAdmission,
    val capacity: Int?,
    val priceRub: Int,
    val organizerId: String,
    val organizerName: String,
    val coverUri: String? = null,
    val latitude: Double = 43.1155,
    val longitude: Double = 131.8855,
    val participants: List<EventParticipant> = emptyList(),
    val isPast: Boolean = false,
) {
    val isFull: Boolean get() = capacity != null && participants.size >= capacity
    val remainingPlaces: Int? get() = capacity?.let { (it - participants.size).coerceAtLeast(0) }
}

data class EventDraft(
    val coverUri: String? = null,
    val latitude: Double = 43.1155,
    val longitude: Double = 131.8855,
    val title: String = "",
    val category: EventCategory = EventCategory.WALK,
    val startsAt: String = "15.11.2026 · 16:00",
    val address: String = "Спортивная набережная",
    val description: String = "",
    val admission: EventAdmission = EventAdmission.FREE,
    val capacity: String = "8",
    val priceRub: String = "0",
)

