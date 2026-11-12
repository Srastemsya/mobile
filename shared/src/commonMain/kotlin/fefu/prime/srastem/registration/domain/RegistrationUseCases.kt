package fefu.prime.srastem.registration.domain

class GetRegistrationCompleted(private val repository: RegistrationRepository) {
    suspend operator fun invoke() = repository.isCompleted()
}

class RequestRegistrationCode(private val repository: RegistrationRepository) {
    suspend operator fun invoke(contact: String, method: RegistrationMethod) =
        repository.requestCode(contact, method)
}

class VerifyRegistrationCode(private val repository: RegistrationRepository) {
    suspend operator fun invoke(contact: String, method: RegistrationMethod, code: String) =
        repository.verifyCode(contact, method, code)
}

class CompleteRegistration(private val repository: RegistrationRepository) {
    suspend operator fun invoke(profile: RegistrationProfile) = repository.complete(profile)
}
