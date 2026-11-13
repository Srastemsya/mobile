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
import ru.srastemsya.uikit.SrVoteCard
import ru.srastemsya.uikit.SrVoteActions
import ru.srastemsya.uikit.SrVoteCardSize
import ru.srastemsya.uikit.SrArtwork
import ru.srastemsya.uikit.SrIllustration
import fefu.prime.srastem.feed.domain.SwipeDecision
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

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
    if (state.filterOpen) {
        Column(modifier.fillMaxSize().background(FeedDesign.ScreenGradient).windowInsetsPadding(WindowInsets.statusBars).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SrTopBar(title = "Фильтры", onBack = { onEvent(FeedModeEvent.DismissNotice) })
            Text("Что ищем?", style = SrType.Display, color = SrColors.Text)
            Text("Вы выбираете вместе на одном устройстве", style = SrType.Body, color = SrColors.Muted)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SrChip("Места", selected = state.kind == fefu.prime.srastem.feed.domain.FeedKind.PLACE, onClick = { onEvent(FeedModeEvent.KindChanged(fefu.prime.srastem.feed.domain.FeedKind.PLACE)) })
                SrChip("События", selected = state.kind == fefu.prime.srastem.feed.domain.FeedKind.EVENT, onClick = { onEvent(FeedModeEvent.KindChanged(fefu.prime.srastem.feed.domain.FeedKind.EVENT)) })
            }
            if (state.kind == fefu.prime.srastem.feed.domain.FeedKind.EVENT) SrDateField(
                value = state.localDate.asSrDate(),
                onSelect = { date -> onEvent(FeedModeEvent.DateChanged("${date.year.toString().padStart(4, '0')}-${date.month.toString().padStart(2, '0')}-${date.day.toString().padStart(2, '0')}")) },
                label = "Дата события",
                errorMessage = if (state.fieldError?.contains("дату") == true) state.fieldError else null,
            )
            Text("Категории", style = SrType.Title, color = SrColors.Text)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SrChip("Все", selected = state.categoryId == null, onClick = { onEvent(FeedModeEvent.CategoryChanged(null)) })
                state.categories.filter { state.kind.name in it.kinds }.forEach { category ->
                    SrChip(category.label, selected = state.categoryId == category.id, onClick = { onEvent(FeedModeEvent.CategoryChanged(category.id)) })
                }
            }
            SrField(state.budgetRubles, { onEvent(FeedModeEvent.BudgetChanged(it)) }, "Общий бюджет, ₽", kind = SrFieldKind.Number, placeholder = "Необязательно", errorMessage = state.fieldError)
            SrField(state.partySizeText, { onEvent(FeedModeEvent.PartySizeChanged(it)) }, "Человек", kind = SrFieldKind.Number, placeholder = "2", errorMessage = if (state.fieldError?.contains("человек") == true) state.fieldError else null)
            state.feedError?.let { Text(it, style = SrType.Body, color = SrColors.Error) }
            if (state.busy) CircularProgressIndicator(color = SrColors.Primary)
            SrButton("Показать подборку", { onEvent(FeedModeEvent.SubmitFilters) }, enabled = !state.busy, loading = state.busy)
            Spacer(Modifier.height(24.dp))
        }
        return
    }
    if (state.cards.isNotEmpty() || state.feedError != null) {
        val swipe = state.swipe
        val card = swipe.current
        val landscape = LocalConfiguration.current.screenWidthDp > LocalConfiguration.current.screenHeightDp
        Column(modifier.fillMaxSize().background(FeedDesign.ScreenGradient).windowInsetsPadding(WindowInsets.statusBars).then(if (landscape) Modifier.verticalScroll(rememberScrollState()) else Modifier).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SrTopBar(title = if (state.kind == fefu.prime.srastem.feed.domain.FeedKind.EVENT) "Выбор события" else "Выбор места", onBack = { onEvent(FeedModeEvent.ChangeFilters) }, actions = { })
            Text(state.contextLabel, style = SrType.Label, color = SrColors.Muted)
            if (swipe.loadingNextPool) {
                Box(Modifier.fillMaxWidth().height(if (landscape) 150.dp else 280.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator(color = SrColors.Primary)
                        Text("Обновляем подборку", style = SrType.Title, color = SrColors.Text)
                    }
                }
            } else if (card != null) {
                val categoryLabel = state.categories.firstOrNull { it.id == card.category }?.label
                val kindLabel = if (state.kind == fefu.prime.srastem.feed.domain.FeedKind.EVENT) "Событие" else "Место"
                val cardMetadata = listOfNotNull(
                    categoryLabel?.takeIf { it.isNotBlank() } ?: kindLabel,
                    card.priceLabel.takeIf { it.isNotBlank() },
                    card.addressOrDate.takeIf { it.isNotBlank() },
                )
                val scope = rememberCoroutineScope()
                val dragX = remember(card.ref) { Animatable(0f) }
                fun decideWithAnimation(decision: SwipeDecision) {
                    scope.launch {
                        dragX.animateTo(if (decision == SwipeDecision.LIKE) 900f else -900f, tween(180))
                        onEvent(FeedModeEvent.SwipeDecided(decision))
                        dragX.snapTo(0f)
                    }
                }
                Box(Modifier.fillMaxWidth().height(if (landscape) 245.dp else 390.dp), contentAlignment = Alignment.Center) {
                    if (swipe.index + 2 < swipe.cards.size) SrVoteCard(swipe.cards[swipe.index + 2].title, emptyList(), { SrArtwork(SrIllustration.VoteCompact, Modifier.fillMaxSize()) }, Modifier.fillMaxWidth().offset(y = 16.dp).graphicsLayer(rotationZ = 3f), size = SrVoteCardSize.Tall)
                    if (swipe.index + 1 < swipe.cards.size) SrVoteCard(swipe.cards[swipe.index + 1].title, emptyList(), { SrArtwork(SrIllustration.VoteCompact, Modifier.fillMaxSize()) }, Modifier.fillMaxWidth().offset(y = 8.dp).graphicsLayer(rotationZ = -3f), size = SrVoteCardSize.Tall)
                    SrVoteCard(card.title, emptyList(), { SrArtwork(SrIllustration.VoteCompact, Modifier.fillMaxSize(), card.title) }, Modifier.then(if (landscape) Modifier.width(250.dp).height(245.dp).verticalScroll(rememberScrollState()) else Modifier.fillMaxWidth()).graphicsLayer(translationX = dragX.value, rotationZ = dragX.value / 24f).pointerInput(card.ref) { detectHorizontalDragGestures(onHorizontalDrag = { _, amount -> scope.launch { dragX.snapTo(dragX.value + amount) } }, onDragEnd = { val decision = when { dragX.value > 80 -> SwipeDecision.LIKE; dragX.value < -80 -> SwipeDecision.SKIP; else -> null }; scope.launch { if (decision != null) { dragX.animateTo(if (decision == SwipeDecision.LIKE) 900f else -900f, tween(180)); onEvent(FeedModeEvent.SwipeDecided(decision)) }; dragX.animateTo(0f, tween(1)) } }) }, size = if (landscape) SrVoteCardSize.Compact else SrVoteCardSize.Tall, description = card.description.takeIf { it.isNotBlank() })
                }
                Text(cardMetadata.joinToString(" · "), style = SrType.Body, color = SrColors.Text)
                Text("${(swipe.index + 1).coerceAtMost(swipe.cards.size)} / ${swipe.cards.size}", style = SrType.Label, color = SrColors.Muted)
                if (!landscape) SrVoteActions(onPass = { decideWithAnimation(SwipeDecision.SKIP) }, onLike = { decideWithAnimation(SwipeDecision.LIKE) }, onUndo = if (swipe.canUndo) ({ onEvent(FeedModeEvent.UndoSwipe) }) else null, progress = swipe.progress)
                else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { SrVoteActions(onPass = { decideWithAnimation(SwipeDecision.SKIP) }, onLike = { decideWithAnimation(SwipeDecision.LIKE) }, onUndo = if (swipe.canUndo) ({ onEvent(FeedModeEvent.UndoSwipe) }) else null, progress = swipe.progress) }
            } else Text("Больше вариантов нет", style = SrType.Title, color = SrColors.Text)
            state.feedError?.let { Text(it, style = SrType.Body, color = SrColors.Muted) }
            SrButton("Изменить фильтры", { onEvent(FeedModeEvent.ChangeFilters) }, style = SrButtonStyle.Secondary)
        }
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
                SrButton("Повторить", { onEvent(FeedModeEvent.RetryProfileCheck) }, style = SrButtonStyle.Secondary)
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
    SrChoiceSurface(
        modifier = Modifier.fillMaxWidth().clickable(enabled, role = Role.Button, onClick = onClick),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
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

private fun String.asSrDate(): SrDate? {
    val parts = split('-')
    if (parts.size != 3) return null
    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val day = parts[2].toIntOrNull() ?: return null
    return runCatching { SrDate(year, month, day) }.getOrNull()
}
