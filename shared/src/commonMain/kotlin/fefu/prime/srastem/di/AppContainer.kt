package fefu.prime.srastem.di

import com.russhwolf.settings.Settings
import fefu.prime.srastem.onboarding.data.SettingsOnboardingRepository
import fefu.prime.srastem.onboarding.domain.CompleteOnboarding
import fefu.prime.srastem.onboarding.domain.GetOnboardingCompleted
import fefu.prime.srastem.onboarding.presentation.OnboardingViewModel
import fefu.prime.srastem.registration.data.BackendRegistrationRepository
import fefu.prime.srastem.registration.domain.CompleteRegistration
import fefu.prime.srastem.registration.domain.GetRegistrationCompleted
import fefu.prime.srastem.registration.domain.RequestRegistrationCode
import fefu.prime.srastem.registration.domain.VerifyRegistrationCode
import fefu.prime.srastem.registration.presentation.RegistrationViewModel
import fefu.prime.srastem.party.data.BackendPartyRepository
import fefu.prime.srastem.party.presentation.PartyViewModel
import fefu.prime.srastem.party.domain.PartyRepository
import fefu.prime.srastem.feed.data.BackendFeedRepository
import fefu.prime.srastem.feed.data.BackendRoomRepository
import fefu.prime.srastem.feed.domain.CompleteRoundUseCase
import fefu.prime.srastem.feed.domain.CreateInviteUseCase
import fefu.prime.srastem.feed.domain.CreateRoomUseCase
import fefu.prime.srastem.feed.domain.FeedRepository
import fefu.prime.srastem.feed.domain.GetFeedCandidatesUseCase
import fefu.prime.srastem.feed.domain.JoinRoomUseCase
import fefu.prime.srastem.feed.domain.ObserveRoomUseCase
import fefu.prime.srastem.feed.domain.RoomRepository
import fefu.prime.srastem.feed.domain.StartRoomUseCase
import fefu.prime.srastem.feed.domain.SubmitVoteUseCase
import fefu.prime.srastem.feed.presentation.FeedModeViewModel
import fefu.prime.srastem.profile.data.SettingsProfileRepository
import fefu.prime.srastem.profile.presentation.ProfileViewModel
import fefu.prime.srastem.events.data.BackendEventRepository
import fefu.prime.srastem.events.domain.EventRepository
import fefu.prime.srastem.events.domain.EventUseCases
import fefu.prime.srastem.events.presentation.EventViewModel
import fefu.prime.srastem.home.data.BackendHomeRepository
import fefu.prime.srastem.backend.BackendApi
import fefu.prime.srastem.home.domain.GetHome
import fefu.prime.srastem.home.presentation.HomeViewModel
import fefu.prime.srastem.map.data.BackendMapCatalogRepository
import fefu.prime.srastem.map.domain.ObserveMapMarkers
import fefu.prime.srastem.map.presentation.MapCatalogViewModel

class AppContainer(
    private val settings: Settings = Settings(),
    private val configuredPartyRepository: PartyRepository? = null,
    configuredEventRepository: EventRepository? = null,
    configuredFeedRepository: FeedRepository? = null,
    configuredRoomRepository: RoomRepository? = null,
) {
    private val api = BackendApi(settings)
    private val eventRepository: EventRepository = configuredEventRepository ?: BackendEventRepository(api)
    private val partyRepository: PartyRepository = configuredPartyRepository ?: BackendPartyRepository(api)
    private val feedRepository: FeedRepository = configuredFeedRepository ?: BackendFeedRepository(api)
    private val roomRepository: RoomRepository = configuredRoomRepository ?: BackendRoomRepository(api)
    private val onboardingRepository = SettingsOnboardingRepository(settings)
    private val registrationRepository = BackendRegistrationRepository(api, settings)
    private val profileRepository = SettingsProfileRepository(settings, eventRepository)
    private val homeRepository = BackendHomeRepository(api)

    // Use cases ленты (FE-F00…FE-F06): по одному на операцию контракта Discovery v1.
    val getFeedCandidates = GetFeedCandidatesUseCase(feedRepository)
    val createRoom = CreateRoomUseCase(roomRepository)
    val createInvite = CreateInviteUseCase(roomRepository)
    val joinRoom = JoinRoomUseCase(roomRepository)
    val startRoom = StartRoomUseCase(roomRepository)
    val submitVote = SubmitVoteUseCase(roomRepository)
    val completeRound = CompleteRoundUseCase(roomRepository)
    val observeRoom = ObserveRoomUseCase(roomRepository)

    fun createOnboardingViewModel() = OnboardingViewModel(
        getOnboardingCompleted = GetOnboardingCompleted(onboardingRepository),
        completeOnboarding = CompleteOnboarding(onboardingRepository),
    )

    fun createRegistrationViewModel() = RegistrationViewModel(
        getRegistrationCompleted = GetRegistrationCompleted(registrationRepository),
        requestRegistrationCode = RequestRegistrationCode(registrationRepository),
        verifyRegistrationCode = VerifyRegistrationCode(registrationRepository),
        completeRegistration = CompleteRegistration(registrationRepository),
    )

    fun createPartyViewModel() = PartyViewModel(partyRepository)

    /**
     * Экран «Выбираем вместе» (FE-F00). Проверка завершённости профиля (AC-06) идёт через
     * `GET /v1/me profileComplete` того же репозитория, что использует поток регистрации.
     */
    fun createFeedModeViewModel() = FeedModeViewModel(
        profileGate = { GetRegistrationCompleted(registrationRepository)() },
    )

    fun createProfileViewModel() = ProfileViewModel(profileRepository)

    fun createEventViewModel() = EventViewModel(EventUseCases(eventRepository))

    fun createHomeViewModel() = HomeViewModel(GetHome(homeRepository))

    fun createMapCatalogViewModel() = MapCatalogViewModel(ObserveMapMarkers(BackendMapCatalogRepository(api)), settings)
}
