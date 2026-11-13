package fefu.prime.srastem.feed.domain

class GetCatalogDetailsUseCase(private val repository: FeedRepository) {
    suspend operator fun invoke(ref: FeedRef): Result<CatalogDetails> = runCatching { repository.getCatalogDetails(ref) }
}
