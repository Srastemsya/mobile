package fefu.prime.srastem.feed.domain

/**
 * Зеркало DTO `Room` из BE-04: фаза, состав, раунд и результат.
 * Чужие голоса в snapshot отсутствуют — только свои (`myVotes`).
 * Snapshot с меньшей [version] игнорируется клиентом.
 */
data class RoomSnapshot(
    val id: String,
    val name: String?,
    val ownerId: String,
    val phase: RoomPhase,
    val version: Long,
    val members: List<RoomMember>,
    val filters: FeedFilters?,
    val roundId: String?,
    val candidates: List<FeedCard>,
    val myVotes: Map<String, SwipeDecision>,
    val myFinished: Boolean,
    val finishedCount: Int,
    val winner: FeedRef?,
    val roundNumber: Int? = null,
    val expiresAt: String? = null,
    val memberCount: Int = 0,
    val resultBasis: String? = null,
    val resultLikes: Int? = null,
    val resultMemberCount: Int? = null,
)
