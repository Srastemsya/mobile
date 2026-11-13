package fefu.prime.srastem

import androidx.compose.ui.window.ComposeUIViewController
import fefu.prime.srastem.di.AppContainer
import platform.UIKit.UIViewController

fun MainViewController(roomLink: String?): UIViewController {
    val mainViewModel = MainViewModel()
    val appContainer = AppContainer()
    val onboardingViewModel = appContainer.createOnboardingViewModel()
    val registrationViewModel = appContainer.createRegistrationViewModel()
    val partyViewModel = appContainer.createPartyViewModel()
    val feedModeViewModel = appContainer.createFeedModeViewModel()
    val profileViewModel = appContainer.createProfileViewModel()
    val eventViewModel = appContainer.createEventViewModel()
    val homeViewModel = appContainer.createHomeViewModel()
    val mapCatalogViewModel = appContainer.createMapCatalogViewModel()
    if (roomLink != null) {
        // Deeplink-приглашение комнаты (FE-F04): экран комнаты открывается поверх ленты.
        mainViewModel.navigateTo(Destination.RoomScreen)
        partyViewModel.acceptInvite(roomLink)
    }

    return ComposeUIViewController {
        NavigationMain(mainViewModel, onboardingViewModel, registrationViewModel, partyViewModel, feedModeViewModel, profileViewModel, eventViewModel, homeViewModel, mapCatalogViewModel)
    }
}
