package fefu.prime.srastem.onboarding.domain

class CompleteOnboarding(
    private val repository: OnboardingRepository,
) {
    suspend operator fun invoke() = repository.markCompleted()
}
