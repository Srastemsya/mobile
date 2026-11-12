package fefu.prime.srastem.di

import com.russhwolf.settings.Settings
import fefu.prime.srastem.onboarding.data.SettingsOnboardingRepository
import fefu.prime.srastem.onboarding.domain.CompleteOnboarding
import fefu.prime.srastem.onboarding.domain.GetOnboardingCompleted
import fefu.prime.srastem.onboarding.presentation.OnboardingViewModel
import fefu.prime.srastem.registration.data.SettingsMockRegistrationRepository
import fefu.prime.srastem.registration.domain.CompleteRegistration
import fefu.prime.srastem.registration.domain.GetRegistrationCompleted
import fefu.prime.srastem.registration.domain.RequestRegistrationCode
import fefu.prime.srastem.registration.domain.VerifyRegistrationCode
import fefu.prime.srastem.registration.presentation.RegistrationViewModel
import fefu.prime.srastem.party.data.MockPartyRepository
import fefu.prime.srastem.party.presentation.PartyViewModel
import fefu.prime.srastem.party.domain.PartyRepository
import fefu.prime.srastem.profile.data.SettingsProfileRepository
import fefu.prime.srastem.profile.presentation.ProfileViewModel
import fefu.prime.srastem.events.data.MockEventRepository
import fefu.prime.srastem.events.domain.EventRepository
import fefu.prime.srastem.events.domain.EventUseCases
import fefu.prime.srastem.events.presentation.EventViewModel
import fefu.prime.srastem.home.data.MockHomeRepository
import fefu.prime.srastem.home.domain.GetHome
import fefu.prime.srastem.home.presentation.HomeViewModel
import fefu.prime.srastem.map.data.EventMapCatalogRepository
import fefu.prime.srastem.map.domain.ObserveMapMarkers
import fefu.prime.srastem.map.presentation.MapCatalogViewModel

class AppContainer(
    settings: Settings = Settings(),
    private val configuredPartyRepository: PartyRepository? = null,
    private val eventRepository: EventRepository = MockEventRepository(),
) {
    private val partyRepository: PartyRepository = configuredPartyRepository ?: MockPartyRepository(eventRepository)
    private val onboardingRepository = SettingsOnboardingRepository(settings)
    private val registrationRepository = SettingsMockRegistrationRepository(settings)
    private val profileRepository = SettingsProfileRepository(settings, eventRepository)
    private val homeRepository = MockHomeRepository()

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

    fun createProfileViewModel() = ProfileViewModel(profileRepository)

    fun createEventViewModel() = EventViewModel(EventUseCases(eventRepository))

    fun createHomeViewModel() = HomeViewModel(GetHome(homeRepository))

    fun createMapCatalogViewModel() = MapCatalogViewModel(ObserveMapMarkers(EventMapCatalogRepository(eventRepository)))
}
