package fefu.prime.srastem.onboarding.domain

interface OnboardingRepository {
    suspend fun isCompleted(): Boolean
    suspend fun markCompleted()
}
