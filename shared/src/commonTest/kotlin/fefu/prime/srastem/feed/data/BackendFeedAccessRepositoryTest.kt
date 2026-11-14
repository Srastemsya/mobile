package fefu.prime.srastem.feed.data

import fefu.prime.srastem.backend.BackendException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class BackendFeedAccessRepositoryTest {
    @Test
    fun readsCompletedProfile() = runTest {
        val repository = BackendFeedAccessRepository { Json.parseToJsonElement("{\"profileComplete\":true}") }
        assertTrue(repository.isProfileComplete())
    }

    @Test
    fun profileIncomplete403DeniesAccess() = runTest {
        val repository = BackendFeedAccessRepository { throw BackendException(403, "PROFILE_INCOMPLETE", "incomplete") }
        assertFalse(repository.isProfileComplete())
    }

    @Test
    fun networkFailureIsNotTreatedAsCompletedProfile() = runTest {
        val repository = BackendFeedAccessRepository { throw IllegalStateException("offline") }
        assertFailsWith<IllegalStateException> { repository.isProfileComplete() }
    }
}
