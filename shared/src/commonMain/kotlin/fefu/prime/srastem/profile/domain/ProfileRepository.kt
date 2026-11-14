package fefu.prime.srastem.profile.domain

data class ProfileDetails(
    val name: String,
    val city: String,
    val birthDate: String,
    val about: String,
    val photoUri: String?,
    val photoScale: Float = 1.24f,
    val photoOffsetX: Float = 0f,
    val photoOffsetY: Float = 0f,
)

data class ProfileEvent(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val status: EventStatus,
    val kind: ProfileSavedKind = ProfileSavedKind.EVENT,
    val coverKey: String? = null,
)

enum class EventStatus { UPCOMING, COMPLETED }
enum class ProfileSavedKind { EVENT, HOME_COLLECTION }

interface ProfileRepository {
    suspend fun getProfile(): ProfileDetails
    suspend fun saveProfile(profile: ProfileDetails)
    suspend fun getSavedEvents(): List<ProfileEvent>
    suspend fun saveEvent(event: ProfileEvent)
    suspend fun removeSavedEvent(id: String)
    suspend fun getMyEvents(): List<ProfileEvent>
}
