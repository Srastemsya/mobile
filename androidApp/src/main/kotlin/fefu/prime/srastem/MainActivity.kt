package fefu.prime.srastem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import fefu.prime.srastem.di.AppContainer

class MainActivity : ComponentActivity() {
    private lateinit var partyViewModel: fefu.prime.srastem.party.presentation.PartyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val mainViewModel = MainViewModel()
        val appContainer = AppContainer()
        val onboardingViewModel = appContainer.createOnboardingViewModel()
        val registrationViewModel = appContainer.createRegistrationViewModel()
        partyViewModel = appContainer.createPartyViewModel()
        val profileViewModel = appContainer.createProfileViewModel()
        val eventViewModel = appContainer.createEventViewModel()
        val homeViewModel = appContainer.createHomeViewModel()
        val mapCatalogViewModel = appContainer.createMapCatalogViewModel()
        intent.dataString?.let(partyViewModel::acceptInvite)
        setContent {
            NavigationMain(mainViewModel, onboardingViewModel, registrationViewModel, partyViewModel, profileViewModel, eventViewModel, homeViewModel, mapCatalogViewModel)
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.dataString?.let(partyViewModel::acceptInvite)
    }
}
