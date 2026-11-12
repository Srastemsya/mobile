package fefu.prime.srastem.events.domain

enum class EventCategory(val title: String) {
    WALK("Прогулка"), ACTIVE("Активный отдых"), GAMES("Игры"), FOOD("Еда и бары"), OTHER("Другое")
}

enum class EventAdmission(val title: String) { FREE("Свободный вход"), BOOKING("По записи") }

data class EventParticipant(val id: String, val name: String, val registrationId: String? = null)

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
    val confirmedCount: Int = participants.size,
    val isAttending: Boolean = false,
    val canRegister: Boolean = admission == EventAdmission.BOOKING,
    val canCancelRegistration: Boolean = true,
    val canDelete: Boolean = true,
) {
    val isFull: Boolean get() = capacity != null && confirmedCount >= capacity
    val remainingPlaces: Int? get() = capacity?.let { (it - confirmedCount).coerceAtLeast(0) }
}

data class EventDraft(
    val coverUri: String? = null,
    val latitude: Double = 43.1155,
    val longitude: Double = 131.8855,
    val title: String = "",
    val category: EventCategory = EventCategory.WALK,
    val categoryId: String = "outdoors",
    val startsAt: String = "15.11.2026 · 16:00",
    val address: String = "Спортивная набережная",
    val description: String = "",
    val admission: EventAdmission = EventAdmission.FREE,
    val capacity: String = "8",
    val priceRub: String = "0",
)

data class EventCatalogCategory(val id: String, val label: String)

fun eventAttendanceSummary(confirmedCount: Int?, capacity: Int?, remainingCount: Int? = null): String? {
    if (capacity == null) return null
    val confirmed = confirmedCount ?: 0
    val remaining = remainingCount ?: (capacity - confirmed).coerceAtLeast(0)
    return "Записалось $confirmed · осталось $remaining мест"
}

