package fefu.prime.srastem.home.presentation

import fefu.prime.srastem.home.domain.GetHome
import fefu.prime.srastem.home.domain.HomeCollection
import fefu.prime.srastem.home.domain.HomeContent
import fefu.prime.srastem.home.domain.HomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun protectedHomeRequestWaitsForExplicitRefreshAndThenShowsCollections() = runTest {
        var requests = 0
        val content = HomeContent(
            city = "Владивосток",
            collections = listOf(HomeCollection("sample", "Подборка", "2 события", "Подборка")),
        )
        val viewModel = HomeViewModel(GetHome(object : HomeRepository {
            override suspend fun getHome(): HomeContent {
                requests++
                return content
            }
        }))

        assertEquals(0, requests)
        assertTrue(viewModel.state.value.isLoading)

        viewModel.refresh()

        assertEquals(1, requests)
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(content.collections, viewModel.state.value.content?.collections)
    }
}
