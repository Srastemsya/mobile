package fefu.prime.srastem.registration.data

import com.russhwolf.settings.Settings
import fefu.prime.srastem.backend.BackendApi
import fefu.prime.srastem.backend.jsonObjectOf
import fefu.prime.srastem.backend.string
import fefu.prime.srastem.registration.domain.RegistrationMethod
import fefu.prime.srastem.registration.domain.RegistrationProfile
import fefu.prime.srastem.registration.domain.RegistrationRepository
import kotlinx.serialization.json.jsonObject

class BackendRegistrationRepository(
    private val api: BackendApi,
    private val settings: Settings,
) : RegistrationRepository {
    override suspend fun isCompleted(): Boolean {
        if (api.accessToken() == null) return false
        return try {
            val profile = api.get("/v1/me").jsonObject
            val complete = profile["profileComplete"]?.toString() == "true"
            api.setProfileComplete(complete)
            profile.string("displayName")?.let { settings.putString(KEY_PROFILE_NAME, it) }
            profile.string("birthDate")?.let { settings.putString(KEY_PROFILE_BIRTH_DATE, it) }
            complete
        } catch (_: Exception) {
            api.isProfileComplete()
        }
    }

    override suspend fun requestCode(contact: String, method: RegistrationMethod) {
        val destination = when (method) {
            RegistrationMethod.PHONE -> normalizePhone(contact)
            RegistrationMethod.EMAIL -> contact.trim().lowercase()
        }
        val scope = "challenge:${method.name}:${destination.hashCode()}"
        val result = try {
            api.post(
                "/v1/auth/challenges",
                jsonObjectOf("channel" to if (method == RegistrationMethod.PHONE) "SMS" else "EMAIL", "destination" to destination),
                api.idempotencyKey(scope),
            ).value?.jsonObject ?: error("Сервер не вернул challenge")
        } catch (error: fefu.prime.srastem.backend.BackendException) {
            // A 429 is a completed rejection too: keeping its idempotency key would replay
            // the same cached rate-limit response after the resend window has elapsed.
            if (error.status in 400..499) api.clearIdempotencyKey(scope)
            throw error
        }
        api.clearIdempotencyKey(scope)
        val challengeId = result.string("challengeId") ?: error("Сервер не вернул challengeId")
        settings.putString(KEY_CHALLENGE_ID, challengeId)
        settings.putString(KEY_CHALLENGE_DESTINATION, destination)
        settings.putString(KEY_CHALLENGE_METHOD, method.name)
    }

    override suspend fun verifyCode(contact: String, method: RegistrationMethod, code: String): Boolean {
        val challengeId = settings.getStringOrNull(KEY_CHALLENGE_ID) ?: error("Сначала запросите код")
        val destination = when (method) {
            RegistrationMethod.PHONE -> normalizePhone(contact)
            RegistrationMethod.EMAIL -> contact.trim().lowercase()
        }
        check(settings.getStringOrNull(KEY_CHALLENGE_DESTINATION) == destination &&
            settings.getStringOrNull(KEY_CHALLENGE_METHOD) == method.name) { "Запросите новый код для этого контакта" }
        val scope = "verify:$challengeId:${code.hashCode()}"
        val session = api.post(
            "/v1/auth/challenges/$challengeId/verify",
            jsonObjectOf("code" to code),
            api.idempotencyKey(scope),
        ).value?.jsonObject ?: error("Сервер не вернул сессию")
        api.saveSession(session)
        api.clearIdempotencyKey(scope)
        settings.remove(KEY_CHALLENGE_ID)
        return true
    }

    override suspend fun complete(profile: RegistrationProfile) {
        val birthDate = profile.birthDate.split('.')
        require(birthDate.size == 3) { "Введите дату в формате ДД.ММ.ГГГГ" }
        val date = "${birthDate[2]}-${birthDate[1].padStart(2, '0')}-${birthDate[0].padStart(2, '0')}"
        api.put("/v1/me/registration", jsonObjectOf("displayName" to profile.name.trim(), "birthDate" to date))
        api.setProfileComplete(true)
        settings.putString(KEY_PROFILE_NAME, profile.name.trim())
        settings.putString(KEY_PROFILE_BIRTH_DATE, date)
    }

    private fun normalizePhone(input: String): String {
        val digits = input.filter(Char::isDigit)
        val national = when {
            digits.length == 11 && (digits.startsWith('7') || digits.startsWith('8')) -> digits.drop(1)
            digits.length == 10 -> digits
            else -> error("Введите корректный номер телефона")
        }
        return "+7$national"
    }

    private companion object {
        const val KEY_CHALLENGE_ID = "backend_challenge_id"
        const val KEY_CHALLENGE_DESTINATION = "backend_challenge_destination"
        const val KEY_CHALLENGE_METHOD = "backend_challenge_method"
        const val KEY_PROFILE_NAME = "registration_profile_name"
        const val KEY_PROFILE_BIRTH_DATE = "registration_profile_birth_date"
    }
}
