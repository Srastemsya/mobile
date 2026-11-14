package fefu.prime.srastem.registration.domain

enum class RegistrationMethod { PHONE, EMAIL }

data class RegistrationProfile(
    val name: String,
    val birthDate: String,
    val contact: String?,
    val method: RegistrationMethod?,
)

interface RegistrationRepository {
    suspend fun isCompleted(): Boolean
    suspend fun requestCode(contact: String, method: RegistrationMethod)
    suspend fun verifyCode(contact: String, method: RegistrationMethod, code: String): Boolean
    suspend fun complete(profile: RegistrationProfile)
}
