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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ru.srastemsya.uikit.SrButton
import ru.srastemsya.uikit.SrButtonStyle
import ru.srastemsya.uikit.SrColors
import ru.srastemsya.uikit.SrChoiceSurface
import ru.srastemsya.uikit.SrGlyph
import ru.srastemsya.uikit.SrIcon
import ru.srastemsya.uikit.SrShapes
import ru.srastemsya.uikit.SrTopBar
import ru.srastemsya.uikit.SrType
import ru.srastemsya.uikit.SrChip
import ru.srastemsya.uikit.SrField
import ru.srastemsya.uikit.SrFieldKind
import ru.srastemsya.uikit.SrDate
import ru.srastemsya.uikit.SrDateField
import ru.srastemsya.uikit.SrIconButton
import fefu.prime.srastem.feed.domain.SwipeDecision
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Экран «Выбираем вместе» (FE-F00, фрейм Figma 280-10679).
 *
 * Фон — градиент Главной из токенов UI Kit (`SrColors.Base/Tint/GlassIris`), карточки режимов —
 * стекло UI Kit (`SrColors.Glass` + `SrColors.GlassEdge` + `SrShapes.Card`); платформы без
 * поддержки эффекта получают подложку `color/surface/solid` через уведомления-панели.
 * Экран выбора и последующие шаги сценария открываются без таббара.
 */
@Composable
fun FeedModeScreen(
    state: FeedModeState,
    onEvent: (FeedModeEvent) -> Unit,
    onGoHome: () -> Unit = {},
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
    if (state.filterOpen) {
        Column(
            modifier.fillMaxSize().background(FeedDesign.ScreenGradient)
                .windowInsetsPadding(WindowInsets.statusBars).verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SrTopBar(title = "Фильтры", onBack = { onEvent(FeedModeEvent.DismissNotice) })
            Text("Что ищем?", style = SrType.Display, color = SrColors.Text)
            Text(
                "Вы выбираете вместе на одном устройстве.",
                style = SrType.Body,
                color = SrColors.Muted
            )
            Text("Тип подборки", style = SrType.Label, color = SrColors.Muted)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SrChip(
                    "Места",
                    selected = state.kind == fefu.prime.srastem.feed.domain.FeedKind.PLACE,
                    onClick = { onEvent(FeedModeEvent.KindChanged(fefu.prime.srastem.feed.domain.FeedKind.PLACE)) })
                SrChip(
                    "События",
                    selected = state.kind == fefu.prime.srastem.feed.domain.FeedKind.EVENT,
                    onClick = { onEvent(FeedModeEvent.KindChanged(fefu.prime.srastem.feed.domain.FeedKind.EVENT)) })
            }
            Text("Категории", style = SrType.Label, color = SrColors.Muted)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SrChip(
                    "Все",
                    selected = state.categoryId == null,
                    onClick = { onEvent(FeedModeEvent.CategoryChanged(null)) })
                state.categories.filter { state.kind.name in it.kinds }.forEach { category ->
                    SrChip(
                        category.label,
                        selected = state.categoryId == category.id,
                        onClick = { onEvent(FeedModeEvent.CategoryChanged(category.id)) })
                }
            }
            SrField(
                state.budgetRubles,
                { onEvent(FeedModeEvent.BudgetChanged(it)) },
                "Общий бюджет, ₽",
                kind = SrFieldKind.Number,
                placeholder = "Необязательно",
                errorMessage = state.fieldError
            )
            if (state.kind == fefu.prime.srastem.feed.domain.FeedKind.EVENT) SrDateField(
                value = state.localDate.asSrDate(),
                onSelect = { date ->
                    onEvent(FeedModeEvent.DateChanged(
                        "${date.year.toString().padStart(4, '0')}-${date.month.toString().padStart(2, '0')}-${date.day.toString().padStart(2, '0')}"
                    ))
                },
                label = "Дата события",
                errorMessage = if (state.fieldError?.contains("дату") == true) state.fieldError else null,
            )
            state.feedError?.let { Text(it, style = SrType.Body, color = SrColors.Error) }
            if (state.busy) CircularProgressIndicator(color = SrColors.Primary)
            SrButton(
                "Начать выбор",
                { onEvent(FeedModeEvent.SubmitFilters) },
                enabled = !state.busy,
                loading = state.busy
            )
            Spacer(Modifier.height(24.dp))
        }
        return
    }
    if (state.chosen != null) {
        val chosen = state.chosen
        val details = state.catalogDetails
        if (state.selectedDetailsOpen && chosen.ref.kind == fefu.prime.srastem.feed.domain.FeedKind.PLACE && details != null) {
            FeedCatalogDetailsScreen(
                details = details,
                onBack = { onEvent(FeedModeEvent.CloseCatalogDetails) },
                onGoHome = onGoHome,
                modifier = modifier,
            )
            return
        }
        Column(
            modifier.fillMaxSize().background(FeedDesign.ScreenGradient)
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(top = 8.dp),
        ) {
            SrTopBar(
                title = "Выбор сделан",
                onBack = { onEvent(FeedModeEvent.CloseSelection) },
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Box(Modifier.fillMaxWidth().height(250.dp).background(SrColors.Solid)) {
                FeedCardCover(chosen.coverUrl, chosen.title, Modifier.fillMaxSize())
            }
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SrChip(
                    if (chosen.ref.kind == fefu.prime.srastem.feed.domain.FeedKind.PLACE) "Место выбрано" else "Событие выбрано",
                    selected = true,
                    onClick = {},
                )
                Text(chosen.title, style = SrType.Display, color = SrColors.Text)
                val categoryLabel = state.categories.firstOrNull { it.id == chosen.category }?.label
                val attributes = listOfNotNull(
                    categoryLabel,
                    chosen.priceLabel.takeIf(String::isNotBlank),
                    chosen.addressOrDate.takeIf(String::isNotBlank),
                )
                if (attributes.isNotEmpty()) Text(attributes.joinToString(" · "), style = SrType.Body, color = SrColors.Muted)
                Text(chosen.description.ifBlank { "Описание пока не добавлено." }, style = SrType.Body, color = SrColors.Text)
                state.detailsError?.let { Text(it, style = SrType.Body, color = SrColors.Error) }
            }
            SrButton(
                if (chosen.ref.kind == fefu.prime.srastem.feed.domain.FeedKind.PLACE) "К месту" else "К мероприятию",
                { onEvent(FeedModeEvent.OpenCatalogDetails) },
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                enabled = !state.loadingDetails,
                loading = state.loadingDetails,
            )
        }
        return
    }
    if (state.cards.isNotEmpty() || state.swipe.loadingNextPool || state.feedError != null || state.feedExhausted) {
        val swipe = state.swipe
        val card = swipe.current
        val landscape =
            LocalConfiguration.current.screenWidthDp > LocalConfiguration.current.screenHeightDp
        Column(
            modifier.fillMaxSize().background(FeedDesign.ScreenGradient)
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .then(if (landscape) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SrTopBar(
                title = if (swipe.loadingNextPool || state.feedExhausted) "Лента" else if (state.kind == fefu.prime.srastem.feed.domain.FeedKind.EVENT) "Выбор события" else "Выбор места",
                onBack = { onEvent(FeedModeEvent.ChangeFilters) },
                actions = {
                    if (!swipe.loadingNextPool && !state.feedExhausted) SrIconButton(
                        SrIcon.Filter,
                        description = "Изменить фильтры",
                        onClick = { onEvent(FeedModeEvent.ChangeFilters) },
                    )
                },
            )
            if (!swipe.loadingNextPool && !state.feedExhausted) Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SrGlyph(SrIcon.People, tint = SrColors.Muted)
                Text(
                    state.contextLabel,
                    Modifier.padding(start = 8.dp),
                    style = SrType.Label,
                    color = SrColors.Muted,
                )
            }
            if (swipe.loadingNextPool) {
                FeedRefreshSkeleton(
                    Modifier.fillMaxWidth()
                        .then(if (landscape) Modifier.height(400.dp) else Modifier.weight(1f)),
                )
            } else if (card != null) {
                val categoryLabel = state.categories.firstOrNull { it.id == card.category }?.label
                val kindLabel =
                    if (state.kind == fefu.prime.srastem.feed.domain.FeedKind.EVENT) "Событие" else "Место"
                val cardMetadata = listOfNotNull(
                    categoryLabel?.takeIf { it.isNotBlank() } ?: kindLabel,
                    card.priceLabel.takeIf { it.isNotBlank() },
                    card.addressOrDate.takeIf { it.isNotBlank() },
                )
                FeedSwipeCardStack(
                    card = card,
                    metadata = cardMetadata,
                    modifier = Modifier.fillMaxWidth()
                        .then(if (landscape) Modifier.height(340.dp) else Modifier.weight(1f)),
                    onSwipe = { onEvent(FeedModeEvent.SwipeDecided(it)) },
                )
            } else if (state.feedExhausted) {
                Text("Больше вариантов нет", style = SrType.Display, color = SrColors.Text)
                Text("Под ваши параметры больше нет подходящих мест и мероприятий", style = SrType.Body, color = SrColors.Muted)
                Column(
                    Modifier.fillMaxWidth().clip(SrShapes.Card).background(SrColors.Solid)
                        .border(1.dp, SrColors.Border, SrShapes.Card).padding(20.dp),
                ) { Text("Вы просмотрели всё", style = SrType.Title, color = SrColors.Text) }
                SrButton("Изменить фильтры", { onEvent(FeedModeEvent.ChangeFilters) }, Modifier.fillMaxWidth())
            } else {
                Text(
                    state.feedError ?: "Не удалось обновить подборку",
                    style = SrType.Body,
                    color = SrColors.Error
                )
                SrButton(
                    "Повторить",
                    { onEvent(FeedModeEvent.RetryNextPage) },
                    style = SrButtonStyle.Secondary
                )
            }
        }
        return
    }
    Column(
        modifier
            .fillMaxSize()
            .background(FeedDesign.ScreenGradient)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SrTopBar(title = "Лента")
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 20.dp),
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
            } else if (state.error == null) {
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
                SrButton(
                    "Повторить",
                    { onEvent(FeedModeEvent.RetryProfileCheck) },
                    style = SrButtonStyle.Secondary
                )
            }
            if (state.busy) {
                CircularProgressIndicator(
                    Modifier.align(Alignment.CenterHorizontally),
                    color = SrColors.Primary
                )
            }
            Spacer(Modifier.height(96.dp))
        }
    }
}

/** Stable refresh placeholder; the previous card and swipe targets are not composed while loading. */
@Composable
private fun FeedRefreshSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 7.dp)
                .height(430.dp).clip(SrShapes.Card).background(SrColors.Solid)
                .border(1.dp, SrColors.Border, SrShapes.Card).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(Modifier.fillMaxWidth().height(290.dp).clip(SrShapes.Card).background(SrColors.Tint))
            Box(Modifier.fillMaxWidth(0.72f).height(20.dp).clip(SrShapes.Pill).background(SrColors.Tint))
            Box(Modifier.fillMaxWidth(0.9f).height(12.dp).clip(SrShapes.Pill).background(SrColors.Tint))
            Box(Modifier.fillMaxWidth(0.55f).height(12.dp).clip(SrShapes.Pill).background(SrColors.Tint))
        }
        Spacer(Modifier.height(18.dp))
        CircularProgressIndicator(color = SrColors.Primary)
        Spacer(Modifier.height(12.dp))
        Text("Обновляем подборку", style = SrType.Title, color = SrColors.Text)
        Text("Ищем новые варианты по вашим фильтрам", style = SrType.Body, color = SrColors.Muted)
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
        SrTopBar(title = "Выбираем вместе", onBack = onBack)
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
    SrChoiceSurface(
        modifier = Modifier.fillMaxWidth()
            .clickable(enabled, role = Role.Button, onClick = onClick),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SrGlyph(icon, tint = SrColors.Text)
                Text(title, style = SrType.Title, color = SrColors.Text)
            }
            Text(description, style = SrType.Body, color = SrColors.Muted)
        }
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
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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

private fun String.asSrDate(): SrDate? {
    val parts = split('-')
    if (parts.size != 3) return null
    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val day = parts[2].toIntOrNull() ?: return null
    return runCatching { SrDate(year, month, day) }.getOrNull()
}

private fun displayCatalogTime(value: String): String = if (value.length >= 16 && value[4] == '-') {
    "${value.substring(8, 10)}.${value.substring(5, 7)} · ${value.substring(11, 16)}"
} else value
