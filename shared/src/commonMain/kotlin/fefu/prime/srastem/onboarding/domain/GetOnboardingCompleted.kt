package fefu.prime.srastem.onboarding.domain

class GetOnboardingCompleted(
    private val repository: OnboardingRepository,
) {
    suspend operator fun invoke(): Boolean = repository.isCompleted()
}
