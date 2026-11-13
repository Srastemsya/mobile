package fefu.prime.srastem

import androidx.lifecycle.ViewModel
import fefu.prime.srastem.di.AppContainer

/** Keeps the app's manually assembled shared ViewModels across Activity configuration changes. */
class ActivityStateHolder : ViewModel() {
    private val container = AppContainer()
    val main = MainViewModel()
    val onboarding = container.createOnboardingViewModel()
    val registration = container.createRegistrationViewModel()
    val party = container.createPartyViewModel()
    val feed = container.createFeedModeViewModel()
    val profile = container.createProfileViewModel()
    val events = container.createEventViewModel()
    val home = container.createHomeViewModel()
    val map = container.createMapCatalogViewModel()
}
