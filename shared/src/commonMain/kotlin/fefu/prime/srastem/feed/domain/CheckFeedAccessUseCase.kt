package fefu.prime.srastem.feed.domain

/** Проверка доступа к сценариям ленты до входа в них. */
class CheckFeedAccessUseCase(private val repository: FeedAccessRepository) {
    suspend operator fun invoke(): Result<Boolean> = runCatching { repository.isProfileComplete() }
}
