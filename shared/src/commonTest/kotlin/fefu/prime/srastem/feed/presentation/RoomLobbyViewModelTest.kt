package fefu.prime.srastem.feed.presentation

import fefu.prime.srastem.feed.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class RoomLobbyViewModelTest {
    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private class Rooms : RoomRepository {
        var snapshot = room()
        var createdName: String? = null
        var joinedToken: String? = null
        var startedFilters: FeedFilters? = null
        var inviteCount = 0
        var cancelCount = 0
        var joinError: Throwable? = null
        var voteError: Throwable? = null
        var observedSnapshot: RoomSnapshot? = null
        var freshSnapshot: RoomSnapshot? = null
        override suspend fun createRoom(name: String?): RoomSnapshot { createdName = name; return snapshot }
        override suspend fun createInvite(roomId: String): RoomInvite { inviteCount++; return RoomInvite("srastemsya://room-invites/join?token=abc", null) }
        override suspend fun joinRoom(token: String): RoomSnapshot { joinedToken = token; joinError?.let { throw it }; return snapshot }
        override suspend fun startRoom(roomId: String, filters: FeedFilters): RoomSnapshot { startedFilters = filters; return snapshot.copy(phase = RoomPhase.VOTING) }
        override suspend fun submitVote(roomId: String, roundId: String, candidate: FeedRef, decision: SwipeDecision): RoomSnapshot { voteError?.let { throw it }; return snapshot }
        override suspend fun completeRound(roomId: String, roundId: String) = snapshot
        override suspend fun getSnapshot(roomId: String) = freshSnapshot ?: snapshot
        override fun observeRoom(roomId: String): Flow<RoomSnapshot> = flow {
            try { emit(snapshot); observedSnapshot?.let { emit(it) }; awaitCancellation() } finally { cancelCount++ }
        }
    }
    private fun vm(rooms: Rooms, user: String = "owner") = RoomLobbyViewModel(
        CreateRoomUseCase(rooms), CreateInviteUseCase(rooms), JoinRoomUseCase(rooms),
        ObserveRoomUseCase(rooms), StartRoomUseCase(rooms), { user },
    )

    @Test fun blankNameIsRejectedAndCreationTrimsName() = runTest {
        val rooms = Rooms(); val vm = vm(rooms)
        vm.setName("   "); vm.create()
        assertNull(rooms.createdName)
        assertNotNull(vm.state.value.error)
        vm.setName("  Friday  "); vm.create()
        assertEquals("Friday", rooms.createdName)
        assertEquals("Friday", vm.state.value.room?.name)
        assertEquals(1, vm.state.value.room?.members?.size)
    }

    @Test fun ownerCanInviteAndCopiedStateIsVisible() = runTest {
        val rooms = Rooms(); val vm = vm(rooms)
        vm.setName("Friday"); vm.create()
        assertEquals(1, rooms.inviteCount)
        assertNotNull(vm.state.value.invite)
        vm.copied(); assertTrue(vm.state.value.copied)
    }

    @Test fun joinedMemberCannotInviteOrStart() = runTest {
        val rooms = Rooms(); val vm = vm(rooms, "guest")
        vm.acceptInvite("srastemsya://room-invites/join?token=abc")
        assertEquals("abc", rooms.joinedToken)
        assertFalse(vm.state.value.isOwner)
        assertFalse(vm.state.value.canStart)
        vm.issueInvite(); vm.openFilters(); vm.start()
        assertEquals(0, rooms.inviteCount)
        assertNull(rooms.startedFilters)
    }

    @Test fun ownerStartsOnlyWithTwoMembersAndPassesFilters() = runTest {
        val rooms = Rooms(); val vm = vm(rooms)
        vm.setName("Friday"); vm.create()
        assertFalse(vm.state.value.canStart)
        vm.start(); assertNull(rooms.startedFilters)
        vm.leave()
        rooms.snapshot = rooms.snapshot.copy(members = rooms.snapshot.members + RoomMember("guest", "Guest"))
        vm.setName("Friday"); vm.create()
        assertTrue(vm.state.value.canStart)
        vm.openFilters(); vm.setKind(FeedKind.EVENT); vm.setDate("2026-11-15")
        vm.setBudget("1000"); vm.start()
        assertEquals(FeedFilters(FeedKind.EVENT, localDate = "2026-11-15", budgetTotalMinor = 100000, partySize = null), rooms.startedFilters)
        assertEquals(RoomPhase.VOTING, vm.state.value.room?.phase)
    }

    @Test fun expiredInviteShowsActionableErrorAndLeaveCancelsObservation() = runTest {
        val rooms = Rooms(); val vm = vm(rooms, "guest")
        rooms.joinError = FeedError.InviteExpired()
        vm.acceptInvite("srastemsya://room-invites/join?token=expired")
        assertContains(vm.state.value.error.orEmpty(), "новую")
        rooms.joinError = null
        vm.acceptInvite("srastemsya://room-invites/join?token=abc")
        assertNotNull(vm.state.value.room)
        vm.leave()
        assertNull(vm.state.value.room)
        assertEquals(1, rooms.cancelCount)
    }

    @Test fun staleObservedVersionCannotReplaceCurrentRoom() = runTest {
        val rooms = Rooms()
        rooms.snapshot = rooms.snapshot.copy(version = 3)
        rooms.observedSnapshot = rooms.snapshot.copy(version = 2, phase = RoomPhase.CLOSED)
        val vm = vm(rooms)
        vm.setName("Friday"); vm.create()
        assertEquals(3, vm.state.value.room?.version)
        assertEquals(RoomPhase.LOBBY, vm.state.value.room?.phase)
    }

    @Test fun invalidVoteStateRereadsLiveSnapshot() = runTest {
        val rooms = Rooms()
        val card = FeedCard(FeedRef(FeedKind.PLACE, "place-1"), "Place", "", "", null, "", "", null)
        rooms.snapshot = rooms.snapshot.copy(phase = RoomPhase.VOTING, roundId = "old-round", candidates = listOf(card))
        val vm = RoomLobbyViewModel(
            CreateRoomUseCase(rooms), CreateInviteUseCase(rooms), JoinRoomUseCase(rooms),
            ObserveRoomUseCase(rooms), StartRoomUseCase(rooms), { "guest" },
            submitVote = SubmitVoteUseCase(rooms), getSnapshot = rooms::getSnapshot,
        )
        vm.acceptInvite("srastemsya://room-invites/join?token=abc")
        rooms.voteError = FeedError.InvalidState()
        rooms.freshSnapshot = rooms.snapshot.copy(version = 2, roundId = "new-round", myVotes = mapOf("place-1" to SwipeDecision.SKIP))
        vm.vote(SwipeDecision.LIKE)
        assertEquals("new-round", vm.state.value.room?.roundId)
        assertEquals(mapOf("place-1" to SwipeDecision.SKIP), vm.state.value.room?.myVotes)
        assertEquals(1, vm.state.value.voteIndex)
    }

    @Test fun restoreUsesFreshSnapshotAndOnlyOwnVotes() = runTest {
        val rooms = Rooms()
        rooms.snapshot = rooms.snapshot.copy(
            phase = RoomPhase.VOTING, roundId = "round-1", myFinished = true,
            candidates = listOf(FeedCard(FeedRef(FeedKind.PLACE, "place-1"), "Place", "", "", null, "", "", null)),
            myVotes = mapOf("place-1" to SwipeDecision.SKIP), finishedCount = 1, memberCount = 2,
        )
        val store = object : ActiveRoomStore {
            var id: String? = "room-1"
            override fun roomId(userId: String) = id.takeIf { userId == "guest" }
            override fun save(userId: String, roomId: String) { id = roomId }
            override fun clear() { id = null }
        }
        val vm = RoomLobbyViewModel(
            CreateRoomUseCase(rooms), CreateInviteUseCase(rooms), JoinRoomUseCase(rooms),
            ObserveRoomUseCase(rooms), StartRoomUseCase(rooms), { "guest" },
            getSnapshot = rooms::getSnapshot, activeRoomStore = store,
        )
        assertTrue(vm.restoreActiveRoom())
        assertTrue(vm.state.value.room?.myFinished == true)
        assertEquals(mapOf("place-1" to SwipeDecision.SKIP), vm.state.value.room?.myVotes)
        assertEquals(1, vm.state.value.voteIndex)
        vm.leave()
        assertNull(store.id)
    }

    companion object {
        private fun room() = RoomSnapshot("room-1", "Friday", "owner", RoomPhase.LOBBY, 1,
            listOf(RoomMember("owner", "Owner")), null, null, emptyList(), emptyMap(), false, 0, null)
    }
}
