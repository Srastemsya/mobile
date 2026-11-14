package fefu.prime.srastem.feed.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Fake-репозитории ленты: сценарии успеха и ошибок без сети. */
class FakeFeedRepository : FeedRepository {
    var nextError: FeedError? = null
    var lastFilters: FeedFilters? = null
    var lastCursor: String? = null
    val page = FeedPage(
        items = listOf(
            FeedCard(
                ref = FeedRef(FeedKind.PLACE, "p1"),
                title = "Бар «Миллионка»",
                category = "food",
                priceLabel = "15 ₽",
                priceMinor = 1500,
                addressOrDate = "Светланская, 13",
                description = "Настойки и музыка.",
                coverUrl = null,
            ),
        ),
        nextCursor = "cursor-2",
    )

    override suspend fun getCandidates(filters: FeedFilters, cursor: String?): FeedPage {
        nextError?.let { throw it }
        lastFilters = filters
        lastCursor = cursor
        return page
    }

    override suspend fun getCatalogDetails(ref: FeedRef) = CatalogDetails(ref, "Title", "Description", "food", "Address", null, null, "Бесплатно", null, null)
}

class FakeRoomRepository : RoomRepository {
    var nextError: FeedError? = null
    var lastVote: SwipeDecision? = null
    var completedRounds = 0
    val snapshot = RoomSnapshot(
        id = "room-1",
        name = "Вечер пятницы",
        ownerId = "user-1",
        phase = RoomPhase.VOTING,
        version = 3,
        members = listOf(RoomMember("user-1", "Алекс")),
        filters = FeedFilters(kind = FeedKind.EVENT),
        roundId = "round-1",
        candidates = emptyList(),
        myVotes = mapOf("p1" to SwipeDecision.LIKE),
        myFinished = false,
        finishedCount = 1,
        winner = null,
    )

    override suspend fun createRoom(name: String?): RoomSnapshot = guardResult(snapshot)
    override suspend fun createInvite(roomId: String): RoomInvite = guardResult(RoomInvite("srastem://invite/t", null))
    override suspend fun joinRoom(token: String): RoomSnapshot = guardResult(snapshot)
    override suspend fun startRoom(roomId: String, filters: FeedFilters): RoomSnapshot = guardResult(snapshot)

    override suspend fun submitVote(
        roomId: String,
        roundId: String,
        candidate: FeedRef,
        decision: SwipeDecision,
    ): RoomSnapshot {
        guard(Unit)
        lastVote = decision
        return snapshot
    }

    override suspend fun completeRound(roomId: String, roundId: String): RoomSnapshot {
        guard(Unit)
        completedRounds += 1
        return snapshot
    }

    override suspend fun getSnapshot(roomId: String): RoomSnapshot = guard(snapshot)
    override fun observeRoom(roomId: String): Flow<RoomSnapshot> = flowOf(snapshot)

    private fun <T> guard(value: T): T {
        nextError?.let { throw it }
        return value
    }

    private fun <T> guardResult(value: T): T = guard(value)
}

class FeedUseCasesTest {

    @Test
    fun candidatesUseCaseReturnsPageAndPassesFilters() = runTest {
        val repository = FakeFeedRepository()
        val useCase = GetFeedCandidatesUseCase(repository)
        val filters = FeedFilters(kind = FeedKind.EVENT, partySize = 2, budgetTotalMinor = 300_000)

        val result = useCase(filters, "cursor-1")

        assertTrue(result.isSuccess)
        assertEquals("cursor-2", result.getOrNull()?.nextCursor)
        assertEquals(filters, repository.lastFilters)
        assertEquals("cursor-1", repository.lastCursor)
    }

    @Test
    fun candidatesUseCaseWrapsDomainErrors() = runTest {
        val repository = FakeFeedRepository().apply { nextError = FeedError.ProfileIncomplete() }

        val result = GetFeedCandidatesUseCase(repository)(FeedFilters())

        assertTrue(result.isFailure)
        assertIs<FeedError.ProfileIncomplete>(result.exceptionOrNull())
    }

    @Test
    fun roomUseCasesDelegateToOneOperationEach() = runTest {
        val repository = FakeRoomRepository()

        assertEquals("room-1", CreateRoomUseCase(repository)("Вечер пятницы").getOrNull()?.id)
        assertEquals("srastem://invite/t", CreateInviteUseCase(repository)("room-1").getOrNull()?.url)
        assertEquals("room-1", JoinRoomUseCase(repository)("token").getOrNull()?.id)
        assertEquals("room-1", StartRoomUseCase(repository)("room-1", FeedFilters()).getOrNull()?.id)
        SubmitVoteUseCase(repository)("room-1", "round-1", FeedRef(FeedKind.PLACE, "p1"), SwipeDecision.SKIP)
        assertEquals(SwipeDecision.SKIP, repository.lastVote)
        CompleteRoundUseCase(repository)("room-1", "round-1")
        assertEquals(1, repository.completedRounds)
        assertEquals(3, ObserveRoomUseCase(repository)("room-1").let { flow ->
            var version = 0L
            flow.collect { version = it.version }
            version
        })
    }

    @Test
    fun roomUseCasesWrapErrorsWithoutThrowing() = runTest {
        val repository = FakeRoomRepository().apply { nextError = FeedError.InvalidState() }

        val result = SubmitVoteUseCase(repository)("room-1", "round-1", FeedRef(FeedKind.PLACE, "p1"), SwipeDecision.LIKE)

        assertTrue(result.isFailure)
        assertIs<FeedError.InvalidState>(result.exceptionOrNull())
        assertNull(repository.lastVote)
    }
}
