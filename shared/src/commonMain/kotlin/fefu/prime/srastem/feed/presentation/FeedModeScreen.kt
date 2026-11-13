package fefu.prime.srastem.feed.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ru.srastemsya.uikit.SrButton
import ru.srastemsya.uikit.SrButtonStyle
import ru.srastemsya.uikit.SrColors
import ru.srastemsya.uikit.SrGlyph
import ru.srastemsya.uikit.SrIcon
import ru.srastemsya.uikit.SrShapes
import ru.srastemsya.uikit.SrTopBar
import ru.srastemsya.uikit.SrType

/**
 * Экран «Выбираем вместе» (FE-F00, фрейм Figma 280-10679).
 *
 * Фон — градиент Главной из токенов UI Kit (`SrColors.Base/Tint/GlassIris`), карточки режимов —
 * стекло UI Kit (`SrColors.Glass` + `SrColors.GlassEdge` + `SrShapes.Card`); платформы без
 * поддержки эффекта получают подложку `color/surface/solid` через уведомления-панели.
 * Последующие шаги сценария ([FeedModeState.stub]) открываются поверх без таббара,
 * с кнопкой «Назад» в `SrTopBar`.
 */
@Composable
fun FeedModeScreen(
    state: FeedModeState,
    onEvent: (FeedModeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val stub = state.stub
    if (stub != null) {
        FeedStepScreen(
            stepTitle = stub.title,
            message = stub.message,
            onBack = { onEvent(FeedModeEvent.DismissNotice) },
            modifier = modifier,
        )
        return
    }
    Column(
        modifier
            .fillMaxSize()
            .background(FeedDesign.ScreenGradient)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SrTopBar(title = "Лента")
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Выбираем вместе", style = SrType.Display, color = SrColors.Text)
            Text(
                "Найдите место или событие, которое понравится всей компании.",
                style = SrType.Body,
                color = SrColors.Muted,
            )
            if (state.profileIncomplete) {
                ProfileIncompleteNotice(
                    busy = state.busy,
                    onCompleteProfile = { onEvent(FeedModeEvent.CompleteProfileSelected) },
                )
            } else {
                ModeChoiceCard(
                    icon = SrIcon.Profile,
                    title = "На одном устройстве",
                    description = "Смотрите карточки вместе на одном телефоне и задайте общий бюджет.",
                    enabled = !state.busy,
                    onClick = { onEvent(FeedModeEvent.SingleDeviceSelected) },
                )
                ModeChoiceCard(
                    icon = SrIcon.People,
                    title = "На нескольких устройствах",
                    description = "Создайте комнату и пригласите друзей. У каждого будет своя лента.",
                    enabled = !state.busy,
                    onClick = { onEvent(FeedModeEvent.MultiDeviceSelected) },
                )
            }
            state.error?.let { message ->
                Text(message, style = SrType.Label, color = SrColors.Error)
                SrButton("Понятно", { onEvent(FeedModeEvent.DismissNotice) }, style = SrButtonStyle.Secondary)
            }
            if (state.busy) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally), color = SrColors.Primary)
            }
            Spacer(Modifier.height(96.dp))
        }
    }
}

/**
 * Последующий шаг сценария ленты: открывается поверх экрана выбора без таббара,
 * «Назад» в `SrTopBar` возвращает на предыдущий шаг без потери состояния вкладки.
 * В пакете FE-F00 так выглядят точки входа нереализованных сценариев FE-F01/FE-F04.
 */
@Composable
fun FeedStepScreen(
    stepTitle: String,
    message: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .background(FeedDesign.ScreenGradient)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SrTopBar(title = "Лента", onBack = onBack)
        Column(
            Modifier.fillMaxWidth()
                .clip(SrShapes.Card)
                .background(SrColors.Solid)
                .border(1.dp, SrColors.Border, SrShapes.Card)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stepTitle, style = SrType.Title, color = SrColors.Primary)
            Text(message, style = SrType.Body, color = SrColors.Muted)
            SrButton("Вернуться к выбору режима", onBack, style = SrButtonStyle.Secondary)
        }
    }
}

/** Стекло-карточка режима: иконка + заголовок в одной строке, описание ниже (фрейм 280-10679). */
@Composable
private fun ModeChoiceCard(
    icon: SrIcon,
    title: String,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth()
            .clip(SrShapes.Card)
            .background(SrColors.Glass)
            .border(1.dp, SrColors.GlassEdge, SrShapes.Card)
            .clickable(enabled, role = Role.Button, onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            SrGlyph(icon, tint = SrColors.Text)
            Text(title, style = SrType.Title, color = SrColors.Text)
        }
        Text(description, style = SrType.Body, color = SrColors.Muted)
    }
}

/** AC-06: сценарии ленты доступны только с завершённым профилем. */
@Composable
private fun ProfileIncompleteNotice(busy: Boolean, onCompleteProfile: () -> Unit) {
    Column(
        Modifier.fillMaxWidth()
            .clip(SrShapes.Card)
            .background(SrColors.Solid)
            .border(1.dp, SrColors.Border, SrShapes.Card)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            SrGlyph(SrIcon.Warning, tint = SrColors.Error)
            Text("Нужен завершённый профиль", style = SrType.Title, color = SrColors.Text)
        }
        Text(
            "Подборка мест и событий доступна после завершения регистрации: имя и дата рождения.",
            style = SrType.Body,
            color = SrColors.Muted,
        )
        SrButton("Завершить регистрацию", onCompleteProfile, enabled = !busy, loading = busy)
    }
}

/** Общие оформительские токены экранов ленты: фон — градиент Главной из палитры UI Kit. */
internal object FeedDesign {
    val ScreenGradient: Brush = Brush.horizontalGradient(
        colorStops = arrayOf(
            0f to SrColors.Base,
            0.55f to SrColors.Tint,
            1f to SrColors.GlassIris,
        ),
    )
}
