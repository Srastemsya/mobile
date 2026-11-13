package fefu.prime.srastem.feed.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import ru.srastemsya.uikit.*
import fefu.prime.srastem.feed.domain.RoomPhase
import fefu.prime.srastem.feed.domain.SwipeDecision

@Composable
fun RoomLobbyScreen(
    viewModel: RoomLobbyViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val clipboard = LocalClipboardManager.current
    val room = state.room
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
        if (state.catalogDetails != null) {
            val details = state.catalogDetails!!
            Text(details.title, style = SrType.Display, color = SrColors.Text)
            Text(details.description, style = SrType.Body, color = SrColors.Text)
            Text(details.address, style = SrType.Body, color = SrColors.Muted)
            Text(details.priceLabel, style = SrType.Label, color = SrColors.Muted)
            SrButton("Назад к матчу", viewModel::closeWinner, Modifier.fillMaxWidth())
        } else if (state.filtersOpen) {
            Text("Что ищем?", style = SrType.Display, color = SrColors.Text)
            Text(
                "Организатор задаёт фильтры для всей комнаты.",
                style = SrType.Body,
                color = SrColors.Muted
            )
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
            if (state.kind == fefu.prime.srastem.feed.domain.FeedKind.EVENT) SrField(
                state.localDate,
                viewModel::setDate,
                "Дата (ГГГГ-ММ-ДД)"
            )
            SrField(
                state.budget,
                viewModel::setBudget,
                "Общий бюджет, ₽",
                kind = SrFieldKind.Number
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
                    enabled = !state.busy && state.name.isNotBlank(),
                    loading = state.busy
                )
            }
        } else if (room.phase == RoomPhase.VOTING) {
            Text(room.name.orEmpty(), style = SrType.Display, color = SrColors.Text)
            Text(
                "Раунд ${room.roundNumber ?: 1} · ${state.voteIndex.coerceAtMost(room.candidates.size)}/${room.candidates.size}",
                style = SrType.Label,
                color = SrColors.Muted
            )
            room.expiresAt?.let {
                Text(
                    "Раунд до $it",
                    style = SrType.Label,
                    color = SrColors.Muted
                )
            }
            if (room.myFinished) {
                Text("Ваш голос принят", style = SrType.Display, color = SrColors.Text)
                Text(
                    "Если большинство выберет один вариант, покажем его всем. Иначе обновим подборку.",
                    style = SrType.Body,
                    color = SrColors.Muted
                )
                Text(
                    "Завершили ${room.finishedCount} из ${room.memberCount}",
                    style = SrType.Label,
                    color = SrColors.Muted
                )
                if (state.reviewing) {
                    room.candidates.getOrNull(state.voteIndex)?.let { card ->
                        Text(
                            "${card.title}: ${room.myVotes[card.ref.id] ?: "—"}",
                            style = SrType.Body,
                            color = SrColors.Text
                        )
                    }
                    SrButton(
                        "Следующая",
                        viewModel::nextReview,
                        Modifier.fillMaxWidth(),
                        enabled = state.voteIndex < room.candidates.lastIndex
                    )
                    SrButton("Назад к ожиданию", viewModel::stopReview, Modifier.fillMaxWidth())
                } else SrButton(
                    "Просмотреть свои голоса",
                    viewModel::review,
                    Modifier.fillMaxWidth()
                )
            } else {
                val card = room.candidates.getOrNull(state.voteIndex)
                if (card == null) SrButton(
                    "Завершить голосование",
                    viewModel::finish,
                    Modifier.fillMaxWidth(),
                    enabled = !state.busy
                )
                else {
                    SrVoteCard(
                        card.title,
                        emptyList(),
                        {
                            SrArtwork(
                                SrIllustration.VoteCompact,
                                Modifier.fillMaxSize(),
                                card.title
                            )
                        },
                        Modifier.fillMaxWidth(),
                        size = SrVoteCardSize.Tall,
                        description = card.description.takeIf { it.isNotBlank() })
                    SrVoteActions(
                        onPass = { viewModel.vote(SwipeDecision.SKIP) },
                        onLike = { viewModel.vote(SwipeDecision.LIKE) },
                        onUndo = if (state.voteIndex > 0) viewModel::previous else null,
                        progress = (state.voteIndex + 1).toFloat() / room.candidates.size.coerceAtLeast(
                            1
                        )
                    )
                }
            }
        } else if (room.phase == RoomPhase.REFRESHING) {
            Text("Обновляем подборку", style = SrType.Display, color = SrColors.Text)
        } else if (room.phase == RoomPhase.MATCHED) {
            Text("Матч большинства", style = SrType.Display, color = SrColors.Text)
            Text(
                "${room.resultLikes ?: 0} из ${room.resultMemberCount ?: room.members.size} выбрали этот вариант",
                style = SrType.Body,
                color = SrColors.Muted
            )
            Text(
                "${room.winner?.kind ?: ""} · ${room.winner?.id ?: ""}",
                style = SrType.Body,
                color = SrColors.Text
            )
            SrButton(
                if (room.winner?.kind == fefu.prime.srastem.feed.domain.FeedKind.EVENT) "К мероприятию" else "К месту",
                viewModel::openWinner,
                Modifier.fillMaxWidth(),
                loading = state.busy
            )
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
            })
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
