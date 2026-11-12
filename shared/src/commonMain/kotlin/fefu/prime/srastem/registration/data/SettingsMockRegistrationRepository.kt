package fefu.prime.srastem.registration.data

import com.russhwolf.settings.Settings
import fefu.prime.srastem.registration.domain.RegistrationMethod
import fefu.prime.srastem.registration.domain.RegistrationProfile
import fefu.prime.srastem.registration.domain.RegistrationRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SettingsMockRegistrationRepository(
    private val settings: Settings,
) : RegistrationRepository {
    private val mutex = Mutex()

    override suspend fun isCompleted(): Boolean = settings.getBoolean(KEY_REGISTERED, false)

    override suspend fun requestCode(contact: String, method: RegistrationMethod) = mutex.withLock {
        settings.putString(KEY_PENDING_CONTACT, contact)
        settings.putString(KEY_PENDING_METHOD, method.name)
        settings.putBoolean(KEY_CHALLENGE_CONSUMED, false)
    }

    override suspend fun verifyCode(
        contact: String,
        method: RegistrationMethod,
        code: String,
    ): Boolean = mutex.withLock {
        val expected = if (method == RegistrationMethod.PHONE) DEMO_PHONE_CODE else DEMO_EMAIL_CODE
        val valid = !settings.getBoolean(KEY_CHALLENGE_CONSUMED, false) &&
                settings.getStringOrNull(KEY_PENDING_CONTACT) == contact &&
                settings.getStringOrNull(KEY_PENDING_METHOD) == method.name && code == expected
        if (valid) settings.putBoolean(KEY_CHALLENGE_CONSUMED, true)
        valid
    }

    override suspend fun complete(profile: RegistrationProfile) {
        settings.putString(KEY_PROFILE_NAME, profile.name)
        settings.putString(KEY_PROFILE_BIRTH_DATE, profile.birthDate)
        profile.contact?.let { settings.putString(KEY_PROFILE_CONTACT, it) }
        profile.method?.let { settings.putString(KEY_PROFILE_METHOD, it.name) }
        settings.putBoolean(KEY_REGISTERED, true)
    }

    companion object {
        /** Local-only value from the handoff contract. Never render this in the UI or API response. */
        const val DEMO_PHONE_CODE = "0427"
        const val DEMO_EMAIL_CODE = "0427"

        private const val KEY_REGISTERED = "registration_completed"
        private const val KEY_PENDING_CONTACT = "registration_pending_contact"
        private const val KEY_PENDING_METHOD = "registration_pending_method"
        private const val KEY_CHALLENGE_CONSUMED = "registration_challenge_consumed"
        private const val KEY_PROFILE_NAME = "registration_profile_name"
        private const val KEY_PROFILE_BIRTH_DATE = "registration_profile_birth_date"
        private const val KEY_PROFILE_CONTACT = "registration_profile_contact"
        private const val KEY_PROFILE_METHOD = "registration_profile_method"
    }
}
