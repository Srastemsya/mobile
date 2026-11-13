package fefu.prime.srastem.feed.presentation

import fefu.prime.srastem.feed.domain.FeedError
import fefu.prime.srastem.feed.domain.FeedKind
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit-тесты ViewModel экрана «Выбираем вместе» (FE-F00): предвыбранный тип подборки (AC-05),
 * гейт завершённости профиля (AC-06), заглушки точек входа FE-F01/FE-F04 и возврат «Назад» (AC-03/AC-04).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FeedModeViewModelTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun tabEntryResetsPendingKindToPlaces() = runTest {
        val viewModel = FeedModeViewModel(profileGate = { true })

        viewModel.onEvent(FeedModeEvent.OpenedFromTab)

        assertEquals(FeedKind.PLACE, viewModel.state.value.pendingKind)
        assertFalse(viewModel.state.value.profileIncomplete)
    }

    @Test
    fun homeSwipeEntryPreselectsEventsKind() = runTest {
        val viewModel = FeedModeViewModel(profileGate = { true })

        viewModel.onEvent(FeedModeEvent.OpenedWithEventsKind)

        assertEquals(FeedKind.EVENT, viewModel.state.value.pendingKind)
    }

    @Test
    fun incompleteProfileBlocksModeCards() = runTest {
        val viewModel = FeedModeViewModel(profileGate = { false })

        viewModel.onEvent(FeedModeEvent.OpenedWithEventsKind)
        assertTrue(viewModel.state.value.profileIncomplete)

        viewModel.onEvent(FeedModeEvent.SingleDeviceSelected)
        assertNull(viewModel.state.value.stub)
    }

    @Test
    fun profileIncompleteErrorFromScenariosOpensCompletionGate() = runTest {
        val viewModel = FeedModeViewModel(profileGate = { true })
        viewModel.onEvent(FeedModeEvent.OpenedFromTab)

        viewModel.onError(FeedError.ProfileIncomplete())

        assertTrue(viewModel.state.value.profileIncomplete)
        assertNull(viewModel.state.value.stub)

        viewModel.onEvent(FeedModeEvent.CompleteProfileSelected)
        assertFalse(viewModel.state.value.profileIncomplete)
    }

    @Test
    fun transportErrorSurfacesMessageWithoutBlockingModes() = runTest {
        val viewModel = FeedModeViewModel(profileGate = { true })
        viewModel.onEvent(FeedModeEvent.OpenedFromTab)

        viewModel.onError(FeedError.Transport("Сервер недоступен"))

        assertEquals("Сервер недоступен", viewModel.state.value.error)
        assertFalse(viewModel.state.value.profileIncomplete)
        viewModel.onEvent(FeedModeEvent.DismissNotice)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun modeCardsOpenDocumentedStubsAndBackReturnsToChoice() = runTest {
        val viewModel = FeedModeViewModel(profileGate = { true })
        viewModel.onEvent(FeedModeEvent.OpenedWithEventsKind)

        viewModel.onEvent(FeedModeEvent.SingleDeviceSelected)
        val single = assertNotNull(viewModel.state.value.stub)
        assertTrue(single.message.contains("kind=EVENT"), "Заглушка FE-F01 обязана нести предвыбранный тип")

        viewModel.onEvent(FeedModeEvent.DismissNotice)
        assertNull(viewModel.state.value.stub)
        assertEquals(FeedKind.EVENT, viewModel.state.value.pendingKind)

        viewModel.onEvent(FeedModeEvent.MultiDeviceSelected)
        assertNotNull(viewModel.state.value.stub)
        viewModel.onEvent(FeedModeEvent.DismissNotice)
        assertNull(viewModel.state.value.stub)
    }

    @Test
    fun registrationCompletionRechecksGateWithoutLosingPendingKind() = runTest {
        var complete = false
        val viewModel = FeedModeViewModel(profileGate = { complete })
        viewModel.onEvent(FeedModeEvent.OpenedWithEventsKind)
        assertTrue(viewModel.state.value.profileIncomplete)

        complete = true
        viewModel.onRegistrationCompleted()

        assertFalse(viewModel.state.value.profileIncomplete)
        assertEquals(FeedKind.EVENT, viewModel.state.value.pendingKind)
    }

    @Test
    fun unavailableGateFailsOpenAndReactive403StillProtects() = runTest {
        val viewModel = FeedModeViewModel(profileGate = { error("offline") })

        viewModel.onEvent(FeedModeEvent.OpenedFromTab)

        assertFalse(viewModel.state.value.profileIncomplete)
        viewModel.onError(FeedError.ProfileIncomplete())
        assertTrue(viewModel.state.value.profileIncomplete)
    }
}
