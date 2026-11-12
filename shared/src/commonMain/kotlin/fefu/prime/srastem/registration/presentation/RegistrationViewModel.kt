package fefu.prime.srastem.registration.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fefu.prime.srastem.currentDateParts
import fefu.prime.srastem.registration.domain.CompleteRegistration
import fefu.prime.srastem.registration.domain.GetRegistrationCompleted
import fefu.prime.srastem.registration.domain.RegistrationMethod
import fefu.prime.srastem.registration.domain.RegistrationProfile
import fefu.prime.srastem.registration.domain.RequestRegistrationCode
import fefu.prime.srastem.registration.domain.VerifyRegistrationCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

enum class RegistrationStep { METHOD, CONTACT, CODE, PROFILE }
enum class RegistrationField { CONTACT, CODE, NAME, BIRTH_DATE }

data class RegistrationUiState(
    val isLoading: Boolean = true,
    val isVisible: Boolean = false,
    val step: RegistrationStep = RegistrationStep.METHOD,
    val method: RegistrationMethod? = null,
    val contact: String = "",
    val code: String = "",
    val name: String = "",
    val birthDate: String = "",
    val error: String? = null,
    val errorField: RegistrationField? = null,
    val isBusy: Boolean = false,
)

class RegistrationViewModel(
    private val getRegistrationCompleted: GetRegistrationCompleted,
    private val requestRegistrationCode: RequestRegistrationCode,
    private val verifyRegistrationCode: VerifyRegistrationCode,
    private val completeRegistration: CompleteRegistration,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val completed = getRegistrationCompleted()
                _uiState.value = RegistrationUiState(isLoading = false, isVisible = !completed)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.value = RegistrationUiState(isLoading = false, isVisible = true, error = error.message ?: "Не удалось открыть регистрацию")
            }
        }
    }

    fun onPhoneSelected() = selectMethod(RegistrationMethod.PHONE)
    fun onEmailSelected() = selectMethod(RegistrationMethod.EMAIL)

    private fun selectMethod(method: RegistrationMethod) {
        _uiState.value = _uiState.value.copy(
            step = RegistrationStep.CONTACT,
            method = method,
            contact = "",
            error = null,
            errorField = null,
        )
    }

    fun onContactChanged(value: String) {
        val formatted =
            if (_uiState.value.method == RegistrationMethod.PHONE) formatRussianPhone(value) else value
        _uiState.value = _uiState.value.copy(contact = formatted, error = null, errorField = null)
    }

    fun onRequestCode() {
        val state = _uiState.value
        val method = state.method ?: return
        val contact = state.contact.trim()
        val valid = when (method) {
            RegistrationMethod.PHONE -> phoneSubscriberDigits(contact).length == 10
            RegistrationMethod.EMAIL -> contact.matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
        }
        if (!valid) {
            _uiState.value = state.copy(
                error = if (method == RegistrationMethod.PHONE) "Введите номер телефона" else "Введите корректную почту",
                errorField = RegistrationField.CONTACT,
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isBusy = true, error = null)
            try {
                requestRegistrationCode(contact, method)
                _uiState.value = _uiState.value.copy(step = RegistrationStep.CODE, isBusy = false, code = "")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(isBusy = false, error = error.message ?: "Не удалось отправить код", errorField = RegistrationField.CONTACT)
            }
        }
    }

    fun onCodeChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            code = value.filter(Char::isDigit).take(4),
            error = null,
            errorField = null
        )
    }

    fun onVerifyCode() {
        val state = _uiState.value
        val method = state.method ?: return
        if (state.code.length != 4) {
            _uiState.value = state.copy(
                error = "Введите 4 цифры из демо-кода",
                errorField = RegistrationField.CODE
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isBusy = true, error = null)
            try {
                val valid = verifyRegistrationCode(state.contact.trim(), method, state.code)
                _uiState.value = _uiState.value.copy(
                    isBusy = false,
                    step = if (valid) RegistrationStep.PROFILE else RegistrationStep.CODE,
                    error = if (valid) null else "Код неверен или срок его действия истёк",
                    errorField = if (valid) null else RegistrationField.CODE,
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(isBusy = false, error = error.message ?: "Не удалось проверить код", errorField = RegistrationField.CODE)
            }
        }
    }

    fun onNameChanged(value: String) =
        _uiState.update { it.copy(name = value, error = null, errorField = null) }

    fun onBirthDateChanged(value: String) = _uiState.update {
        it.copy(birthDate = value.filter { char -> char.isDigit() || char == '.' }.take(10), error = null, errorField = null)
    }

    fun onComplete() {
        val state = _uiState.value
        val birthDate = parseBirthDate(state.birthDate)
        val eligible = birthDate?.let(::isAdult) == true
        if (state.name.isBlank() || !eligible) {
            _uiState.value = state.copy(
                error = if (state.name.isBlank()) "Введите имя пользователя" else if (birthDate == null) "Введите корректную дату рождения" else "Регистрация доступна с 18 лет",
                errorField = if (state.name.isBlank()) RegistrationField.NAME else RegistrationField.BIRTH_DATE,
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isBusy = true, error = null)
            try {
                completeRegistration(
                    RegistrationProfile(
                        state.name.trim(),
                        state.birthDate,
                        state.contact.ifBlank { null },
                        state.method
                    )
                )
                _uiState.value = _uiState.value.copy(isBusy = false, isVisible = false)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(isBusy = false, error = error.message ?: "Не удалось сохранить профиль")
            }
        }
    }

    fun onBack() {
        _uiState.update {
            when (it.step) {
                RegistrationStep.METHOD -> it
                RegistrationStep.CONTACT -> it.copy(step = RegistrationStep.METHOD, error = null)
                RegistrationStep.CODE -> it.copy(step = RegistrationStep.CONTACT, error = null)
                RegistrationStep.PROFILE -> it.copy(
                    step = if (it.method == null) RegistrationStep.METHOD else RegistrationStep.CODE,
                    error = null
                )
            }
        }
    }

    private fun MutableStateFlow<RegistrationUiState>.update(transform: (RegistrationUiState) -> RegistrationUiState) {
        value = transform(value)
    }

    private fun formatRussianPhone(input: String): String {
        var digits = input.filter(Char::isDigit)
        if (input.trimStart().startsWith("+7") && digits.startsWith('7')) {
            digits = digits.drop(1)
        } else if (digits.length == 11 && (digits.startsWith('7') || digits.startsWith('8'))) {
            digits = digits.drop(1)
        } else if (digits.length > 10) {
            digits = digits.takeLast(10)
        }
        digits = digits.take(10)
        if (digits.isEmpty()) return if (input.trimStart().startsWith("+7")) "+7 (" else ""

        return buildString {
            append("+7 (")
            append(digits.take(3))
            if (digits.length >= 3) append(")")
            if (digits.length > 3) append(" ${digits.substring(3, minOf(6, digits.length))}")
            if (digits.length > 6) append("-${digits.substring(6, minOf(8, digits.length))}")
            if (digits.length > 8) append("-${digits.substring(8, minOf(10, digits.length))}")
        }
    }

    private fun phoneSubscriberDigits(contact: String): String {
        val digits = contact.filter(Char::isDigit)
        return when {
            contact.trimStart().startsWith("+7") && digits.startsWith('7') -> digits.drop(1)
            digits.length == 11 && (digits.startsWith('7') || digits.startsWith('8')) -> digits.drop(
                1
            )

            else -> digits
        }
    }

    private fun parseBirthDate(value: String): Triple<Int, Int, Int>? {
        val parts = value.split('.')
        if (parts.size != 3 || parts.any { it.isEmpty() }) return null
        val day = parts[0].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        val year = parts[2].toIntOrNull() ?: return null
        if (year !in 1900..2100 || month !in 1..12) return null
        val leap = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
        val daysInMonth = when (month) {
            2 -> if (leap) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }
        if (day !in 1..daysInMonth) return null
        return Triple(year, month, day)
    }

    private fun isAdult(date: Triple<Int, Int, Int>): Boolean {
        // Keep date eligibility deterministic across platforms until a backend supplies its own policy.
        val (currentYear, currentMonth, currentDay) = currentDateParts()
        val (year, month, day) = date
        val age = currentYear - year - if (currentMonth < month || (currentMonth == month && currentDay < day)) 1 else 0
        return age >= 18 && (year < currentYear || month < currentMonth || (month == currentMonth && day <= currentDay))
    }
}
