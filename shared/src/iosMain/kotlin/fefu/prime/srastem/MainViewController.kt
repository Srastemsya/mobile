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
    val profileViewModel = appContainer.createProfileViewModel()
    val eventViewModel = appContainer.createEventViewModel()
    val homeViewModel = appContainer.createHomeViewModel()
    val mapCatalogViewModel = appContainer.createMapCatalogViewModel()
    if (roomLink != null) {
        mainViewModel.navigateTo(Destination.FeedScreen)
        partyViewModel.acceptInvite(roomLink)
    }

    return ComposeUIViewController {
        NavigationMain(mainViewModel, onboardingViewModel, registrationViewModel, partyViewModel, profileViewModel, eventViewModel, homeViewModel, mapCatalogViewModel)
    }
}
