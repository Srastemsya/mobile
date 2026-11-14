package fefu.prime.srastem.registration.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fefu.prime.srastem.registration.domain.RegistrationMethod
import ru.srastemsya.uikit.SrAuthCode
import ru.srastemsya.uikit.SrButton
import ru.srastemsya.uikit.SrButtonStyle
import ru.srastemsya.uikit.SrCard
import ru.srastemsya.uikit.SrColors
import ru.srastemsya.uikit.SrDate
import ru.srastemsya.uikit.SrDateField
import ru.srastemsya.uikit.SrField
import ru.srastemsya.uikit.SrFieldKind
import ru.srastemsya.uikit.SrSelect
import ru.srastemsya.uikit.SrType

@Composable
fun RegistrationScreen(
    state: RegistrationUiState,
    onPhoneSelected: () -> Unit,
    onEmailSelected: () -> Unit,
    onContactChanged: (String) -> Unit,
    onRequestCode: () -> Unit,
    onCodeChanged: (String) -> Unit,
    onVerifyCode: () -> Unit,
    onNameChanged: (String) -> Unit,
    onBirthDateChanged: (String) -> Unit,
    onComplete: () -> Unit,
    onBack: () -> Unit,
) {
    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(SrColors.Base, SrColors.GlassIris, SrColors.GlassRose)))) {
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.step != RegistrationStep.METHOD) {
                SrButton("Назад", onBack, style = SrButtonStyle.Secondary)
                val index = when (state.step) {
                    RegistrationStep.METHOD, RegistrationStep.CONTACT -> 0
                    RegistrationStep.CODE -> 1
                    RegistrationStep.PROFILE -> 2
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { step ->
                        Box(Modifier.weight(1f).height(4.dp).background(if (step <= index) SrColors.Primary else SrColors.Border, ru.srastemsya.uikit.SrShapes.Pill))
                    }
                }
            }

            when (state.step) {
                RegistrationStep.METHOD -> SrCard(Modifier.fillMaxWidth()) {
                    Text("Срастёмся", style = SrType.Display, color = SrColors.Text)
                    Text("Для жюри доступен быстрый вход по демо-аккаунту.", style = SrType.Body, color = SrColors.Muted)
                    SrButton("Быстрый вход для жюри", onEmailSelected, Modifier.fillMaxWidth())
                    SrButton("Войти по номеру телефона", onPhoneSelected, Modifier.fillMaxWidth(), style = SrButtonStyle.Secondary)
                }

                RegistrationStep.CONTACT -> SrCard(Modifier.fillMaxWidth()) {
                    Text(if (state.method == RegistrationMethod.PHONE) "Вход по телефону" else "Вход по почте", style = SrType.Heading, color = SrColors.Text)
                    if (state.method == RegistrationMethod.EMAIL) {
                        Text("Выберите аккаунт. Код подтверждения введётся и проверится автоматически.", style = SrType.Body, color = SrColors.Muted)
                        SrSelect(
                            value = state.contact.takeIf(String::isNotBlank),
                            options = JudgeDemoAccounts.emails,
                            onSelect = onContactChanged,
                            label = "Демо-аккаунт",
                            placeholder = "Выберите аккаунт жюри",
                            enabled = !state.isBusy,
                            errorMessage = state.error.takeIf { state.errorField == RegistrationField.CONTACT },
                        )
                    } else {
                        Text("Введите номер телефона, чтобы получить одноразовый код", style = SrType.Body, color = SrColors.Muted)
                        SrField(
                            value = state.contact,
                            onValueChange = onContactChanged,
                            label = "Номер телефона",
                            placeholder = "+7 (___) ___-__-__",
                            kind = SrFieldKind.Phone,
                            errorMessage = state.error.takeIf { state.errorField == RegistrationField.CONTACT },
                            enabled = !state.isBusy,
                        )
                    }
                    SrButton(
                        if (state.method == RegistrationMethod.EMAIL) "Войти" else "Получить код",
                        onRequestCode,
                        Modifier.fillMaxWidth(),
                        enabled = !state.isBusy && (state.method != RegistrationMethod.EMAIL || state.contact.isNotBlank()),
                        loading = state.isBusy,
                    )
                }

                RegistrationStep.CODE -> SrCard(Modifier.fillMaxWidth()) {
                    Text("Подтвердите контакт", style = SrType.Heading, color = SrColors.Text)
                    SrAuthCode(
                        code = state.code,
                        onCodeChange = onCodeChanged,
                        onResend = onRequestCode,
                        length = 4,
                        enabled = !state.isBusy,
                        canResend = !state.isBusy && state.resendSecondsRemaining == 0,
                        resendLabel = if (state.resendSecondsRemaining > 0) {
                            "Отправить код ещё раз через ${state.resendSecondsRemaining / 60}:${(state.resendSecondsRemaining % 60).toString().padStart(2, '0')}"
                        } else "Отправить код ещё раз",
                        errorMessage = state.error.takeIf { state.errorField == RegistrationField.CODE },
                        instruction = "Код отправлен на ${state.contact}",
                    )
                    SrButton("Подтвердить", onVerifyCode, Modifier.fillMaxWidth(), enabled = !state.isBusy, loading = state.isBusy)
                }

                RegistrationStep.PROFILE -> SrCard(Modifier.fillMaxWidth()) {
                    Text("Завершить регистрацию", style = SrType.Heading, color = SrColors.Text)
                    Text("Заполните данные профиля", style = SrType.Body, color = SrColors.Muted)
                    SrField(
                        state.name,
                        onNameChanged,
                        "Имя",
                        placeholder = "Как к вам обращаться",
                        errorMessage = state.error.takeIf { state.errorField == RegistrationField.NAME },
                        enabled = !state.isBusy,
                    )
                    SrDateField(
                        value = state.birthDate.asSrDate(),
                        onSelect = { onBirthDateChanged(it.formatted()) },
                        label = "Дата рождения",
                        errorMessage = state.error.takeIf { state.errorField == RegistrationField.BIRTH_DATE },
                        enabled = !state.isBusy,
                    )
                    state.error?.takeIf { state.errorField == null }?.let {
                        Text(it, style = SrType.Caption, color = SrColors.Error)
                    }
                    SrButton("Срастёмся", onComplete, Modifier.fillMaxWidth(), enabled = !state.isBusy, loading = state.isBusy)
                    Text(
                        "Нажимая кнопку, вы принимаете условия использования и политику конфиденциальности.",
                        style = SrType.Caption,
                        color = SrColors.Muted,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun String.asSrDate(): SrDate? {
    val pieces = split('.')
    if (pieces.size != 3) return null
    val day = pieces[0].toIntOrNull() ?: return null
    val month = pieces[1].toIntOrNull() ?: return null
    val year = pieces[2].toIntOrNull() ?: return null
    return runCatching { SrDate(year, month, day) }.getOrNull()
}
