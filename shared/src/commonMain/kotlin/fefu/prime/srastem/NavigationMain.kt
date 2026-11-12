package fefu.prime.srastem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.request.maxBitmapSize
import fefu.prime.srastem.onboarding.presentation.OnboardingScreen
import fefu.prime.srastem.onboarding.presentation.OnboardingViewModel
import fefu.prime.srastem.registration.presentation.RegistrationScreen
import fefu.prime.srastem.registration.presentation.RegistrationViewModel
import fefu.prime.srastem.map.presentation.MapScreen
import fefu.prime.srastem.party.presentation.PartyScreen
import fefu.prime.srastem.party.presentation.PartyViewModel
import fefu.prime.srastem.profile.presentation.ProfileScreen
import fefu.prime.srastem.profile.presentation.ProfileViewModel
import fefu.prime.srastem.profile.presentation.ProfilePage
import fefu.prime.srastem.profile.domain.EventStatus
import fefu.prime.srastem.profile.domain.ProfileEvent
import fefu.prime.srastem.events.presentation.EventViewModel
import fefu.prime.srastem.events.presentation.EventScreen
import fefu.prime.srastem.events.presentation.EventPage
import fefu.prime.srastem.home.presentation.HomeScreen
import fefu.prime.srastem.home.presentation.HomeViewModel
import fefu.prime.srastem.map.presentation.MapCatalogViewModel
import ru.srastemsya.uikit.SrGlassHost
import ru.srastemsya.uikit.SrColors
import ru.srastemsya.uikit.SrTheme

@Composable
fun NavigationMain(
    viewModel: MainViewModel,
    onboardingViewModel: OnboardingViewModel,
    registrationViewModel: RegistrationViewModel,
    partyViewModel: PartyViewModel,
    profileViewModel: ProfileViewModel,
    eventViewModel: EventViewModel,
    homeViewModel: HomeViewModel,
    mapCatalogViewModel: MapCatalogViewModel,
) {
    val onboardingState by onboardingViewModel.uiState.collectAsState()
    val registrationState by registrationViewModel.uiState.collectAsState()

    SrTheme {
        when {
            onboardingState.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F8FC)),
            )

            onboardingState.isVisible -> OnboardingScreen(
                state = onboardingState,
                onSlideChanged = onboardingViewModel::onSlideChanged,
                onContinue = onboardingViewModel::onContinue,
            )

            registrationState.isLoading -> Box(
                modifier = Modifier.fillMaxSize().background(Color(0xFFF8F8FC)),
            )

            registrationState.isVisible -> RegistrationScreen(
                state = registrationState,
                onPhoneSelected = registrationViewModel::onPhoneSelected,
                onEmailSelected = registrationViewModel::onEmailSelected,
                onContactChanged = registrationViewModel::onContactChanged,
                onRequestCode = registrationViewModel::onRequestCode,
                onCodeChanged = registrationViewModel::onCodeChanged,
                onVerifyCode = registrationViewModel::onVerifyCode,
                onNameChanged = registrationViewModel::onNameChanged,
                onBirthDateChanged = registrationViewModel::onBirthDateChanged,
                onComplete = registrationViewModel::onComplete,
                onBack = registrationViewModel::onBack,
            )

            else -> MainNavigation(viewModel, partyViewModel, profileViewModel, eventViewModel, homeViewModel, mapCatalogViewModel)
        }
    }
}

@Composable
private fun MainNavigation(viewModel: MainViewModel, partyViewModel: PartyViewModel, profileViewModel: ProfileViewModel, eventViewModel: EventViewModel, homeViewModel: HomeViewModel, mapCatalogViewModel: MapCatalogViewModel) {
    val currentDestination by viewModel.currentDestination.collectAsState()
    val partyState by partyViewModel.state.collectAsState()
    val profileState by profileViewModel.state.collectAsState()
    val eventState by eventViewModel.state.collectAsState()
    val eventListUnderGlass = currentDestination == Destination.EventsScreen && eventState.page == EventPage.LIST

    SrGlassHost(
        background = {
            Box(Modifier.fillMaxSize().background(SrColors.Base)) {
                if (eventListUnderGlass) {
                    EventScreen(
                        eventViewModel,
                        modifier = Modifier.fillMaxSize(),
                        onOpenMap = { viewModel.navigateTo(Destination.MapScreen) },
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!eventListUnderGlass) {
                DestinationContent(
                    destination = currentDestination,
                    onNavigate = viewModel::navigateTo,
                    modifier = Modifier.fillMaxSize(),
                    partyViewModel = partyViewModel,
                    profileViewModel = profileViewModel,
                    eventViewModel = eventViewModel,
                    homeViewModel = homeViewModel,
                    mapCatalogViewModel = mapCatalogViewModel,
                )
            }
            val showBottomNavigation = currentDestination != Destination.MapScreen &&
                (currentDestination != Destination.FeedScreen || partyState.page == fefu.prime.srastem.party.presentation.PartyPage.MODE) &&
                (currentDestination != Destination.Profile || profileState.page == ProfilePage.HOME) &&
                (currentDestination != Destination.EventsScreen || eventState.page == EventPage.LIST) &&
                (currentDestination != Destination.RoomScreen || partyState.page == fefu.prime.srastem.party.presentation.PartyPage.MODE)
            if (showBottomNavigation) {
                BottomNavigationBar(
                    selectedDestination = currentDestination,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onNavigate = viewModel::navigateTo,
                )
            }
        }
    }
}

@Composable
private fun DestinationContent(
    destination: Destination,
    onNavigate: (Destination) -> Unit = {},
    partyViewModel: PartyViewModel,
    profileViewModel: ProfileViewModel,
    eventViewModel: EventViewModel,
    homeViewModel: HomeViewModel,
    mapCatalogViewModel: MapCatalogViewModel,
    modifier: Modifier = Modifier,
) {
    if (destination == Destination.MapScreen) {
        MapScreen(
            modifier = modifier,
            onNavigate = onNavigate,
            catalogViewModel = mapCatalogViewModel,
            onOpenEvent = { id ->
                eventViewModel.openEvent(id)
                onNavigate(Destination.EventsScreen)
            },
            onSaveEvent = { marker ->
                val category = when (marker.type) {
                    fefu.prime.srastem.map.domain.MapMarkerType.Event -> "Событие"
                    fefu.prime.srastem.map.domain.MapMarkerType.Place -> "Место"
                    fefu.prime.srastem.map.domain.MapMarkerType.Activity -> "Активность"
                }
                profileViewModel.saveEvent(
                    ProfileEvent(marker.id, marker.title, marker.subtitle, category, EventStatus.UPCOMING),
                )
            },
        )
        return
    }
    if (destination == Destination.MainScreen) {
        HomeScreen(
            homeViewModel,
            modifier = modifier,
            onOpenFeed = { onNavigate(Destination.FeedScreen) },
            onOpenEvents = { onNavigate(Destination.EventsScreen) },
            onOpenMap = { onNavigate(Destination.MapScreen) },
        )
        return
    }
    if (destination == Destination.FeedScreen) {
        PartyScreen(
            viewModel = partyViewModel,
            onOpenEvent = { id ->
                eventViewModel.openEvent(id)
                onNavigate(Destination.EventsScreen)
            },
            onOpenPlace = { onNavigate(Destination.MapScreen) },
        )
        return
    }
    if (destination == Destination.Profile) {
        ProfileScreen(profileViewModel, modifier = modifier, onOpenEvent = { id ->
            eventViewModel.openEvent(id)
            onNavigate(Destination.EventsScreen)
        })
        return
    }
    if (destination == Destination.EventsScreen) {
        EventScreen(eventViewModel, modifier = modifier, onOpenMap = { onNavigate(Destination.MapScreen) })
        return
    }
    if (destination == Destination.RoomScreen) {
        PartyScreen(
            viewModel = partyViewModel,
            onOpenEvent = { id ->
                eventViewModel.openEvent(id)
                onNavigate(Destination.EventsScreen)
            },
            onOpenPlace = { onNavigate(Destination.MapScreen) },
        )
        return
    }
    val title = bottomNavItems.firstOrNull { it.route == destination }?.title ?: "Экран"

    Box(
        modifier = modifier.background(
            Brush.horizontalGradient(
                colorStops = arrayOf(
                    0f to Color(0xFFF8F8FD),
                    0.55f to Color(0xFFECECFF),
                    1f to Color(0xFFD8D8FF)
                )
            )
        ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = ru.srastemsya.uikit.SrType.Heading,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Содержимое экрана «$title»",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
            )
        }
    }
}
