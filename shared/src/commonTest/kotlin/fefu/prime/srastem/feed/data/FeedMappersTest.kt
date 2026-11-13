package fefu.prime.srastem.feed.data

import fefu.prime.srastem.backend.BackendException
import fefu.prime.srastem.feed.domain.FeedError
import fefu.prime.srastem.feed.domain.FeedFilters
import fefu.prime.srastem.feed.domain.FeedKind
import fefu.prime.srastem.feed.domain.RoomPhase
import fefu.prime.srastem.feed.domain.SwipeDecision
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Маппинг DTO мобильного контракта → доменная модель ленты и кодов ошибок (AC-06, AC-08). */
class FeedMappersTest {

    private val roomJson = """
        {
          "id": "room-7",
          "name": "Вечер пятницы",
          "ownerId": "user-1",
          "state": "VOTING",
          "version": 5,
          "members": [
            { "userId": "user-1", "displayName": "Алекс" },
            { "userId": "user-2", "displayName": "Мария" }
          ],
          "filters": {
            "kind": "EVENT",
            "categoryId": "food",
            "localDate": "2026-11-13",
            "budgetTotal": { "amountMinor": 300000, "currency": "RUB" },
            "partySize": 2
          },
          "round": {
            "id": "round-3",
            "candidates": [
              {
                "ref": { "kind": "EVENT", "id": "e-1" },
                "title": "Квиз: Владивосток",
                "categoryId": "culture",
                "price": { "amountMinor": 70000, "currency": "RUB" },
                "location": { "address": "Центр города" },
                "description": "Командная игра.",
                "cover": null
              }
            ],
            "myVotes": [
              { "candidate": { "kind": "EVENT", "id": "e-1" }, "choice": "LIKE" }
            ],
            "myFinished": false,
            "finishedCount": 1
          },
          "result": { "winner": { "kind": "EVENT", "id": "e-1" } }
        }
    """.trimIndent()

    @Test
    fun roomSnapshotMirrorsContractAndKeepsOwnVotesOnly() {
        val snapshot = FeedMappers.room(
            kotlinx.serialization.json.Json.parseToJsonElement(roomJson).jsonObject,
            ownUserId = "user-1",
        )

        assertEquals("room-7", snapshot.id)
        assertEquals(RoomPhase.VOTING, snapshot.phase)
        assertEquals(5L, snapshot.version)
        assertEquals(2, snapshot.members.size)
        assertEquals(FeedFilters(FeedKind.EVENT, "food", "2026-11-13", 300_000, 2), snapshot.filters)
        assertEquals("round-3", snapshot.roundId)
        assertEquals(mapOf("e-1" to SwipeDecision.LIKE), snapshot.myVotes)
        assertEquals(false, snapshot.myFinished)
        assertEquals(1, snapshot.finishedCount)
        assertEquals("e-1", snapshot.winner?.id)
        assertEquals("Квиз: Владивосток", snapshot.candidates.single().title)
        assertEquals(70_000L, snapshot.candidates.single().priceMinor)
    }

    @Test
    fun unknownPhasesAndKindsFallBackToClosedAndPlace() {
        val json = kotlinx.serialization.json.Json.parseToJsonElement(
            """{ "id": "r", "ownerId": "u", "state": "SOMETHING_NEW", "version": 1 }""",
        ).jsonObject

        val snapshot = FeedMappers.room(json, "u")

        assertEquals(RoomPhase.CLOSED, snapshot.phase)
        assertNull(snapshot.roundId)
        assertTrue(snapshot.candidates.isEmpty())
    }

    @Test
    fun contractErrorCodesMapToDomainErrors() {
        assertIs<FeedError.ProfileIncomplete>(
            FeedMappers.errorOf(BackendException(403, "PROFILE_INCOMPLETE", "Профиль не завершён")),
        )
        assertIs<FeedError.InviteExpired>(FeedMappers.errorOf(BackendException(410, "INVITE_EXPIRED", "Истекло")))
        assertIs<FeedError.RoomNotFound>(FeedMappers.errorOf(BackendException(404, "NOT_FOUND", "Нет комнаты")))
        assertIs<FeedError.InvalidState>(FeedMappers.errorOf(BackendException(409, "STALE_ROUND", "Старый раунд")))
        assertIs<FeedError.CursorExpired>(FeedMappers.errorOf(BackendException(409, "CURSOR_EXPIRED", "Курсор")))
        assertIs<FeedError.Transport>(FeedMappers.errorOf(BackendException(500, "SERVER_ERROR", "Упало")))
    }

    @Test
    fun candidatesQueryMirrorsContractParameters() {
        val query = FeedContract.candidatesQuery(
            FeedFilters(FeedKind.EVENT, categoryId = "food", localDate = "2026-11-13", budgetTotalMinor = 300_000, partySize = 2),
            cursor = "abc def",
        )

        assertEquals(
            "/v1/feed/candidates?kind=EVENT&categoryId=food&localDate=2026-11-13&budgetTotalMinor=300000&partySize=2&cursor=abc%20def",
            query,
        )
    }

    @Test
    fun eventCandidateQuerySerializesValidatedFiltersAsHttpQuery() {
        // Mirrors the public GET /v1/feed/candidates query contract: budget is RUB minor units.
        val query = FeedContract.candidatesQuery(
            FeedFilters(
                kind = FeedKind.EVENT,
                categoryId = "music",
                localDate = "2026-11-20",
                budgetTotalMinor = 125_000L,
                partySize = 3,
            ),
            cursor = null,
        )

        assertEquals(
            "/v1/feed/candidates?kind=EVENT&categoryId=music&localDate=2026-11-20&budgetTotalMinor=125000&partySize=3",
            query,
        )
    }

    @Test
    fun placeCandidateQueryOmitsEventDateAndNullOptionalFilters() {
        val query = FeedContract.candidatesQuery(
            FeedFilters(kind = FeedKind.PLACE, categoryId = null, localDate = null, budgetTotalMinor = null, partySize = null),
            cursor = null,
        )

        assertEquals("/v1/feed/candidates?kind=PLACE", query)
    }

    @Test
    fun startBodyCarriesTotalBudgetInMinorUnits() {
        val body = FeedContract.startBody(FeedFilters(FeedKind.PLACE, budgetTotalMinor = 450_00, partySize = 3))
        val filters = body["filters"]!!.jsonObject

        assertEquals("PLACE", filters["kind"].toString().trim('"'))
        assertEquals("45000", filters["budgetTotal"]!!.jsonObject["amountMinor"].toString())
        assertEquals("3", filters["partySize"].toString())
    }
}
