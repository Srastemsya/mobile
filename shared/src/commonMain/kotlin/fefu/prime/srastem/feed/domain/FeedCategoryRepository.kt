package fefu.prime.srastem.feed.domain

data class FeedCategory(val id: String, val label: String, val kinds: List<FeedKind>)

interface FeedCategoryRepository { suspend fun getCategories(): List<FeedCategory> }

class GetFeedCategoriesUseCase(private val repository: FeedCategoryRepository) {
    suspend operator fun invoke(): Result<List<FeedCategory>> = runCatching { repository.getCategories() }
}
