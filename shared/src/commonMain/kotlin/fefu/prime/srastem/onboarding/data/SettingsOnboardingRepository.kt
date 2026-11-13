package fefu.prime.srastem.onboarding.data

import com.russhwolf.settings.Settings
import fefu.prime.srastem.onboarding.domain.OnboardingRepository

class SettingsOnboardingRepository(
    private val settings: Settings,
) : OnboardingRepository {
    override suspend fun isCompleted(): Boolean =
        settings.getBoolean(KEY_ONBOARDING_COMPLETED, false)

    override suspend fun markCompleted() {
        settings.putBoolean(KEY_ONBOARDING_COMPLETED, true)
    }

    private companion object {
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}
