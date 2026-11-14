package fefu.prime.srastem.feed.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import ru.srastemsya.uikit.*
import fefu.prime.srastem.feed.domain.RoomPhase

@Composable
fun RoomLobbyScreen(
    viewModel: RoomLobbyViewModel,
    onBack: () -> Unit,
    onGoHome: () -> Unit = {},
    onOpenEvent: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val clipboard = LocalClipboardManager.current
    val room = state.room
    LaunchedEffect(room?.phase, room?.winner, state.busy, state.error) {
        if (room?.phase == RoomPhase.MATCHED && state.catalogDetails == null && state.error == null) {
            viewModel.preloadWinner()
        }
    }
    state.catalogDetails?.takeIf { state.winnerDetailsOpen }?.let { details ->
        FeedCatalogDetailsScreen(
            details = details,
            onBack = viewModel::closeWinner,
            onGoHome = onGoHome,
            modifier = modifier,
        )
        return
    }
    if (room?.phase == RoomPhase.VOTING && room.myFinished) {
        RoomWaitScreen(
            onBack = { viewModel.leave(); onBack() },
            onStart = { viewModel.leave(); onBack() },
            modifier = modifier,
        )
        return
    }
    if (room?.phase == RoomPhase.MATCHED) {
        RoomMatchScreen(
            room = room,
            details = state.catalogDetails,
            loading = state.busy,
            error = state.error,
            onBack = { viewModel.leave(); onBack() },
            onOpen = {
                if (room.winner?.kind == fefu.prime.srastem.feed.domain.FeedKind.EVENT) {
                    room.winner?.id?.let(onOpenEvent)
                } else viewModel.openWinner()
            },
            modifier = modifier,
        )
        return
    }
    if (room?.phase == RoomPhase.VOTING && !room.myFinished) {
        RoomVotingScreen(
            state = state,
            room = room,
            viewModel = viewModel,
            onBack = onBack,
            modifier = modifier,
        )
        return
    }
    Column(
        modifier.fillMaxSize().background(FeedDesign.ScreenGradient)
            .windowInsetsPadding(WindowInsets.statusBars)
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SrTopBar(
            title = if (state.filtersOpen) "Фильтры" else if (state.room == null) "Новая комната" else "Комната",
            onBack = {
                if (state.catalogDetails != null) viewModel.closeWinner() else if (state.filtersOpen) viewModel.closeFilters() else {
                    viewModel.leave(); onBack()
                }
            })
        if (state.filtersOpen) {
            Text("Что ищем?", style = SrType.Display, color = SrColors.Text)
            Text(
                "Организатор задаёт фильтры для всей комнаты.",
                style = SrType.Body,
                color = SrColors.Muted
            )
            Text("Тип подборки", style = SrType.Label, color = SrColors.Muted)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SrChip(
                    "Места",
                    selected = state.kind == fefu.prime.srastem.feed.domain.FeedKind.PLACE,
                    onClick = { viewModel.setKind(fefu.prime.srastem.feed.domain.FeedKind.PLACE) })
                SrChip(
                    "События",
                    selected = state.kind == fefu.prime.srastem.feed.domain.FeedKind.EVENT,
                    onClick = { viewModel.setKind(fefu.prime.srastem.feed.domain.FeedKind.EVENT) })
            }
            Text("Категории", style = SrType.Label, color = SrColors.Muted)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SrChip("Все", selected = state.categoryId == null, onClick = { viewModel.setCategory(null) })
                state.categories.filter { state.kind in it.kinds }.forEach { category ->
                    SrChip(category.label, selected = state.categoryId == category.id,
                        onClick = { viewModel.setCategory(category.id) })
                }
            }
            SrField(
                state.budget,
                viewModel::setBudget,
                "Общий бюджет, ₽",
                kind = SrFieldKind.Number,
                placeholder = "2 000",
                errorMessage = state.error?.takeIf { it.contains("бюджет") }
            )
            val selectedDate = state.localDate.split('-').takeIf { it.size == 3 }
                ?.mapNotNull { it.toIntOrNull() }?.takeIf { it.size == 3 }
                ?.let { parts -> runCatching { SrDate(parts[0], parts[1], parts[2]) }.getOrNull() }
            SrDateField(
                value = selectedDate,
                onSelect = { date ->
                    viewModel.setDate("${date.year.toString().padStart(4, '0')}-${date.month.toString().padStart(2, '0')}-${date.day.toString().padStart(2, '0')}")
                },
                label = "Дата события",
                enabled = state.kind == fefu.prime.srastem.feed.domain.FeedKind.EVENT,
            )
            SrButton(
                "Запустить выбор",
                viewModel::start,
                Modifier.fillMaxWidth(),
                enabled = !state.busy && state.canStart,
                loading = state.busy
            )
        } else if (room == null) {
            Text(
                if (state.joined) "Присоединяемся к комнате" else "Создайте комнату",
                style = SrType.Display,
                color = SrColors.Text
            )
            if (!state.joined) {
                Text(
                    "Друзья смогут присоединиться по ссылке.",
                    style = SrType.Body,
                    color = SrColors.Muted
                )
                SrField(
                    state.name,
                    viewModel::setName,
                    "Название комнаты",
                    placeholder = "Вечер пятницы",
                    errorMessage = state.error
                )
                SrButton(
                    "Создать комнату",
                    viewModel::create,
                    Modifier.fillMaxWidth(),
                    enabled = !state.busy,
                    loading = state.busy
                )
            }
        } else if (room.phase == RoomPhase.REFRESHING) {
            Text("Обновляем подборку", style = SrType.Display, color = SrColors.Text)
        } else if (room.phase == RoomPhase.CLOSED || room.phase == RoomPhase.EXHAUSTED) {
            Text(
                if (room.phase == RoomPhase.CLOSED) "Комната закрыта" else "Больше вариантов нет",
                style = SrType.Display,
                color = SrColors.Text
            )
            SrButton("К выбору режима", { viewModel.leave(); onBack() }, Modifier.fillMaxWidth())
        } else {
            Text(room.name.orEmpty(), style = SrType.Display, color = SrColors.Text)
            Text(
                "Пригласите друзей, прежде чем начать выбор.",
                style = SrType.Body,
                color = SrColors.Muted
            )
            SrAvatarGroup(room.members.map { member ->
                member.displayName.trim().split(Regex("\\s+")).filter(String::isNotBlank)
                    .take(2).joinToString("") { it.take(1).uppercase() }.ifBlank { "?" }
            }.take(3) + if (room.members.size > 3) listOf("+${room.members.size - 3}") else emptyList())
            Text(
                "${room.members.size} участников подключились",
                style = SrType.Label,
                color = SrColors.Muted
            )
            if (state.isOwner && room.phase == fefu.prime.srastem.feed.domain.RoomPhase.LOBBY) {
                val invite = state.invite
                SrRoomInvite(
                    title = if (state.copied) "Ссылка скопирована" else "Пригласить в комнату",
                    message = "Поделитесь ссылкой с друзьями.",
                    state = when {
                        state.copied -> SrInviteState.Copied
                        invite == null -> SrInviteState.Expired
                        else -> SrInviteState.Link
                    },
                    onAction = {
                        if (invite == null) viewModel.issueInvite()
                        else {
                            clipboard.setText(AnnotatedString(invite.url)); viewModel.copied()
                        }
                    },
                    modifier = Modifier.widthIn(max = 296.dp),
                )
                SrButton(
                    "К фильтрам",
                    viewModel::openFilters,
                    Modifier.fillMaxWidth(),
                    enabled = state.canStart
                )
            } else if (room.phase == fefu.prime.srastem.feed.domain.RoomPhase.LOBBY) {
                Text(
                    "Ждём, пока организатор начнёт выбор",
                    style = SrType.Body,
                    color = SrColors.Muted
                )
            } else Text(
                "Выбор начался · ${room.candidates.size} вариантов в пуле",
                style = SrType.Body,
                color = SrColors.Muted
            )
        }
        state.error?.let { Text(it, style = SrType.Body, color = SrColors.Error) }
    }
}
