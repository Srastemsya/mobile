package fefu.prime.srastem.feed.domain

/** Одиночная выдача подборки: контракт Discovery v1, `GET /feed/candidates`. */
interface FeedRepository {
    suspend fun getCandidates(filters: FeedFilters, cursor: String? = null): FeedPage
}
