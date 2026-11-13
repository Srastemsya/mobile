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

@Composable
fun RoomLobbyScreen(viewModel: RoomLobbyViewModel, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsState()
    val clipboard = LocalClipboardManager.current
    Column(modifier.fillMaxSize().background(FeedDesign.ScreenGradient).windowInsetsPadding(WindowInsets.statusBars)
        .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SrTopBar(title = if (state.filtersOpen) "Фильтры" else if (state.room == null) "Новая комната" else "Комната", onBack = { if (state.filtersOpen) viewModel.closeFilters() else { viewModel.leave(); onBack() } })
        val room = state.room
        if (state.filtersOpen) {
            Text("Что ищем?", style = SrType.Display, color = SrColors.Text)
            Text("Организатор задаёт фильтры для всей комнаты.", style = SrType.Body, color = SrColors.Muted)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SrChip("Места", selected = state.kind == fefu.prime.srastem.feed.domain.FeedKind.PLACE, onClick = { viewModel.setKind(fefu.prime.srastem.feed.domain.FeedKind.PLACE) })
                SrChip("События", selected = state.kind == fefu.prime.srastem.feed.domain.FeedKind.EVENT, onClick = { viewModel.setKind(fefu.prime.srastem.feed.domain.FeedKind.EVENT) })
            }
            if (state.kind == fefu.prime.srastem.feed.domain.FeedKind.EVENT) SrField(state.localDate, viewModel::setDate, "Дата (ГГГГ-ММ-ДД)")
            SrField(state.budget, viewModel::setBudget, "Общий бюджет, ₽", kind = SrFieldKind.Number)
            SrButton("Запустить выбор", viewModel::start, Modifier.fillMaxWidth(), enabled = !state.busy && state.canStart, loading = state.busy)
        } else if (room == null) {
            Text(if (state.joined) "Присоединяемся к комнате" else "Создайте комнату", style = SrType.Display, color = SrColors.Text)
            if (!state.joined) {
                Text("Друзья смогут присоединиться по ссылке.", style = SrType.Body, color = SrColors.Muted)
                SrField(state.name, viewModel::setName, "Название комнаты", placeholder = "Вечер пятницы", errorMessage = state.error)
                SrButton("Создать", viewModel::create, Modifier.fillMaxWidth(), enabled = !state.busy && state.name.isNotBlank(), loading = state.busy)
            }
        } else {
            Text(room.name.orEmpty(), style = SrType.Display, color = SrColors.Text)
            Text("Пригласите друзей, прежде чем начать выбор", style = SrType.Body, color = SrColors.Muted)
            Text("${room.members.size} участников подключились", style = SrType.Title, color = SrColors.Text)
            room.members.forEach { member -> Text(member.displayName, style = SrType.Body, color = SrColors.Text) }
            if (state.isOwner && room.phase == fefu.prime.srastem.feed.domain.RoomPhase.LOBBY) {
                val invite = state.invite
                if (invite == null) SrButton("Пригласить друзей", viewModel::issueInvite, Modifier.fillMaxWidth(), loading = state.busy)
                else {
                    Text(if (state.copied) "Ссылка скопирована" else "Пригласить в комнату", style = SrType.Title, color = SrColors.Text)
                    Text(invite.url, style = SrType.Body, color = SrColors.Primary)
                    SrButton(if (state.copied) "Скопировано" else "Скопировать ссылку", { clipboard.setText(AnnotatedString(invite.url)); viewModel.copied() }, Modifier.fillMaxWidth())
                    SrButton("Новая ссылка", viewModel::issueInvite, Modifier.fillMaxWidth(), style = SrButtonStyle.Secondary)
                }
                SrButton("Начать выбор", viewModel::openFilters, Modifier.fillMaxWidth(), enabled = state.canStart)
            } else if (room.phase == fefu.prime.srastem.feed.domain.RoomPhase.LOBBY) {
                Text("Ждём, пока организатор начнёт выбор", style = SrType.Body, color = SrColors.Muted)
            } else Text("Выбор начался · ${room.candidates.size} вариантов в пуле", style = SrType.Body, color = SrColors.Muted)
        }
        state.error?.let { Text(it, style = SrType.Body, color = SrColors.Error) }
    }
}
