package fefu.prime.srastem.feed.domain

/** Одна порция выдачи подборки (до 20 карточек) по фильтрам и курсору. */
class GetFeedCandidatesUseCase(private val repository: FeedRepository) {
    suspend operator fun invoke(filters: FeedFilters, cursor: String? = null): Result<FeedPage> =
        runCatching { repository.getCandidates(filters, cursor) }
}
