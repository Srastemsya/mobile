package fefu.prime.srastem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import fefu.prime.srastem.di.AppContainer

class MainActivity : ComponentActivity() {
    private lateinit var partyViewModel: fefu.prime.srastem.party.presentation.PartyViewModel
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        mainViewModel = MainViewModel()
        val appContainer = AppContainer()
        val onboardingViewModel = appContainer.createOnboardingViewModel()
        val registrationViewModel = appContainer.createRegistrationViewModel()
        partyViewModel = appContainer.createPartyViewModel()
        val feedModeViewModel = appContainer.createFeedModeViewModel()
        val profileViewModel = appContainer.createProfileViewModel()
        val eventViewModel = appContainer.createEventViewModel()
        val homeViewModel = appContainer.createHomeViewModel()
        val mapCatalogViewModel = appContainer.createMapCatalogViewModel()
        intent.dataString?.let { link ->
            // Deeplink-приглашение комнаты (FE-F04): экран комнаты открывается поверх ленты.
            mainViewModel.navigateTo(Destination.RoomScreen)
            partyViewModel.acceptInvite(link)
        }
        setContent {
            NavigationMain(mainViewModel, onboardingViewModel, registrationViewModel, partyViewModel, feedModeViewModel, profileViewModel, eventViewModel, homeViewModel, mapCatalogViewModel)
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.dataString?.let { link ->
            mainViewModel.navigateTo(Destination.RoomScreen)
            partyViewModel.acceptInvite(link)
        }
    }
}
