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
import androidx.compose.runtime.LaunchedEffect
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
import fefu.prime.srastem.feed.presentation.RoomLobbyViewModel
import fefu.prime.srastem.feed.presentation.RoomLobbyScreen
import fefu.prime.srastem.feed.presentation.FeedModeScreen
import fefu.prime.srastem.feed.presentation.FeedModeViewModel
import fefu.prime.srastem.feed.presentation.FeedModeEvent
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
    feedModeViewModel: FeedModeViewModel,
    roomLobbyViewModel: RoomLobbyViewModel,
    profileViewModel: ProfileViewModel,
    eventViewModel: EventViewModel,
    homeViewModel: HomeViewModel,
    mapCatalogViewModel: MapCatalogViewModel,
) {
    val onboardingState by onboardingViewModel.uiState.collectAsState()
    val registrationState by registrationViewModel.uiState.collectAsState()

    LaunchedEffect(registrationState.isLoading, registrationState.isVisible) {
        if (!registrationState.isLoading && !registrationState.isVisible) {
            // Protected catalog routes become available only after the identity flow completes.
            homeViewModel.refresh()
            eventViewModel.refreshEvents()
            mapCatalogViewModel.refresh()
            feedModeViewModel.onRegistrationCompleted()
        }
    }

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

            else -> MainNavigation(
                viewModel,
                partyViewModel,
                feedModeViewModel,
                roomLobbyViewModel,
                profileViewModel,
                eventViewModel,
                homeViewModel,
                mapCatalogViewModel,
                onFeedProfileCompletionRequested = registrationViewModel::openProfileCompletion,
            )
        }
    }
}

@Composable
private fun MainNavigation(
    viewModel: MainViewModel,
    partyViewModel: PartyViewModel,
    feedModeViewModel: FeedModeViewModel,
    roomLobbyViewModel: RoomLobbyViewModel,
    profileViewModel: ProfileViewModel,
    eventViewModel: EventViewModel,
    homeViewModel: HomeViewModel,
    mapCatalogViewModel: MapCatalogViewModel,
    onFeedProfileCompletionRequested: () -> Unit = {},
) {
    val currentDestination by viewModel.currentDestination.collectAsState()
    val feedEventsEntry by viewModel.feedEventsEntry.collectAsState()
    val partyState by partyViewModel.state.collectAsState()
    val feedState by feedModeViewModel.state.collectAsState()
    val roomState by roomLobbyViewModel.state.collectAsState()
    val profileState by profileViewModel.state.collectAsState()
    val eventState by eventViewModel.state.collectAsState()
    val eventListUnderGlass =
        currentDestination == Destination.EventsScreen && eventState.page == EventPage.LIST

    FeedBackHandler(enabled = currentDestination == Destination.FeedScreen) {
        if (feedState.stub?.title?.startsWith("FE-F04") == true) {
            roomLobbyViewModel.leave(); feedModeViewModel.onEvent(FeedModeEvent.DismissNotice)
        } else if (feedState.stub != null || feedState.filterOpen || feedState.cards.isNotEmpty() || feedState.feedError != null) feedModeViewModel.onEvent(
            FeedModeEvent.DismissNotice
        )
        else viewModel.backFromFeed()
    }

    LaunchedEffect(currentDestination, feedState.profileIncomplete) {
        if (currentDestination == Destination.FeedScreen && feedState.profileIncomplete) {
            onFeedProfileCompletionRequested()
        }
    }

    LaunchedEffect(currentDestination) { if (currentDestination != Destination.FeedScreen) roomLobbyViewModel.leave() }

    LaunchedEffect(currentDestination, eventState.page) {
        if (currentDestination == Destination.EventsScreen && eventState.page == EventPage.LIST) {
            eventViewModel.refreshEvents()
        }
    }

    // AC-05: вход с Главной («Свайпать события») открывает выбор режима с предвыбранным kind=EVENT.
    LaunchedEffect(currentDestination, feedEventsEntry) {
        if (currentDestination == Destination.FeedScreen &&
            feedState.cards.isEmpty() && !feedState.filterOpen && feedState.stub == null &&
            !feedState.busy && feedState.error == null && !feedState.profileIncomplete
        ) {
            feedModeViewModel.onEvent(
                if (feedEventsEntry) FeedModeEvent.OpenedWithEventsKind else FeedModeEvent.OpenedFromHome,
            )
        }
    }

    LaunchedEffect(
        currentDestination,
        feedState.chosen?.ref,
        roomState.room?.phase,
        roomState.room?.winner,
        feedState.stub?.title,
    ) {
        val selectedEvent = when {
            currentDestination == Destination.FeedScreen && feedState.chosen?.ref?.kind ==
                fefu.prime.srastem.feed.domain.FeedKind.EVENT -> feedState.chosen?.ref

            currentDestination == Destination.FeedScreen &&
                feedState.stub?.title?.startsWith("FE-F04") == true &&
                roomState.room?.phase == fefu.prime.srastem.feed.domain.RoomPhase.MATCHED &&
                roomState.room!!.winner?.kind == fefu.prime.srastem.feed.domain.FeedKind.EVENT ->
                roomState.room!!.winner

            else -> null
        }
        selectedEvent?.let { ref ->
            feedModeViewModel.onEvent(FeedModeEvent.DismissNotice)
            eventViewModel.openEvent(ref.id, returnHomeAfterSelection = true)
            viewModel.navigateTo(Destination.EventsScreen)
        }
    }

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
                    feedModeViewModel = feedModeViewModel,
                    roomLobbyViewModel = roomLobbyViewModel,
                    profileViewModel = profileViewModel,
                    eventViewModel = eventViewModel,
                    homeViewModel = homeViewModel,
                    mapCatalogViewModel = mapCatalogViewModel,
                    onOpenFeedWithEventsKind = viewModel::navigateToFeedWithEventsKind,
                    onFeedProfileCompletionRequested = onFeedProfileCompletionRequested,
                    onGoHome = {
                        feedModeViewModel.onEvent(FeedModeEvent.DismissNotice)
                        roomLobbyViewModel.leave()
                        viewModel.navigateTo(Destination.MainScreen)
                    },
                )
            }
            // The swipe flow starts at device choice; its full-screen steps have no tab bar.
            val showBottomNavigation = currentDestination != Destination.MapScreen &&
                    currentDestination != Destination.FeedScreen &&
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
    feedModeViewModel: FeedModeViewModel,
    roomLobbyViewModel: RoomLobbyViewModel,
    profileViewModel: ProfileViewModel,
    eventViewModel: EventViewModel,
    homeViewModel: HomeViewModel,
    mapCatalogViewModel: MapCatalogViewModel,
    onOpenFeedWithEventsKind: () -> Unit = {},
    onFeedProfileCompletionRequested: () -> Unit = {},
    onGoHome: () -> Unit = {},
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
                    ProfileEvent(
                        marker.id,
                        marker.title,
                        marker.subtitle,
                        category,
                        EventStatus.UPCOMING
                    ),
                )
            },
        )
        return
    }
    if (destination == Destination.MainScreen) {
        HomeScreen(
            homeViewModel,
            modifier = modifier,
            onOpenFeed = onOpenFeedWithEventsKind,
            onOpenEvents = { onNavigate(Destination.EventsScreen) },
            onOpenMap = { onNavigate(Destination.MapScreen) },
        )
        return
    }
    if (destination == Destination.FeedScreen) {
        val feedState by feedModeViewModel.state.collectAsState()
        if (feedState.stub?.title?.startsWith("FE-F04") == true) {
            RoomLobbyScreen(
                roomLobbyViewModel,
                onBack = { feedModeViewModel.onEvent(FeedModeEvent.DismissNotice) },
                onGoHome = onGoHome,
                modifier = modifier
            )
            return
        }
        FeedModeScreen(
            state = feedState,
            onEvent = { event ->
                if (event == FeedModeEvent.CompleteProfileSelected) onFeedProfileCompletionRequested()
                if (event == FeedModeEvent.MultiDeviceSelected) roomLobbyViewModel.openCreate()
                feedModeViewModel.onEvent(event)
            },
            onGoHome = onGoHome,
            modifier = modifier,
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
        EventScreen(
            eventViewModel,
            modifier = modifier,
            onOpenMap = { onNavigate(Destination.MapScreen) },
            onGoHome = {
                eventViewModel.back()
                onGoHome()
            },
        )
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
