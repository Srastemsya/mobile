package fefu.prime.srastem.feed.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fefu.prime.srastem.feed.domain.FeedKind
import fefu.prime.srastem.feed.domain.RoomSnapshot
import ru.srastemsya.uikit.SrAvatarGroup
import ru.srastemsya.uikit.SrButton
import ru.srastemsya.uikit.SrColors
import ru.srastemsya.uikit.SrIcon
import ru.srastemsya.uikit.SrIconButton
import ru.srastemsya.uikit.SrTopBar
import ru.srastemsya.uikit.SrType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoomVotingScreen(
    state: RoomLobbyState,
    room: RoomSnapshot,
    viewModel: RoomLobbyViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val card = room.candidates.getOrNull(state.voteIndex)
    val initials = room.members.map { member ->
        member.displayName.trim().split(Regex("\\s+")).filter(String::isNotBlank)
            .take(2).joinToString("") { it.take(1).uppercase() }.ifBlank { "?" }
    }
    Column(
        modifier.fillMaxSize().background(FeedDesign.ScreenGradient)
            .windowInsetsPadding(WindowInsets.statusBars)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SrTopBar(
            title = if (room.filters?.kind == FeedKind.EVENT) "Выбор события" else "Выбор места",
            onBack = { viewModel.leave(); onBack() },
            actions = {
                if (state.isOwner) {
                    SrIconButton(SrIcon.Filter, "Фильтры комнаты", viewModel::openVotingFilters)
                }
            },
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                "Комната: ${room.name.orEmpty()}",
                style = SrType.Label,
                color = SrColors.Muted,
            )
            SrAvatarGroup(initials)
        }
        if (card != null) {
            val categoryLabel = state.categories.firstOrNull { it.id == card.category }?.label
                ?: card.category.takeIf { it.isNotBlank() && !it.contains('-') }
                ?: if (card.ref.kind == FeedKind.EVENT) "Активность" else "Место"
            val metadata = listOfNotNull(
                categoryLabel,
                card.priceLabel.takeIf(String::isNotBlank),
                card.addressOrDate.takeIf(String::isNotBlank),
            )
            FeedSwipeCardStack(
                card = card,
                metadata = metadata,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.busy,
                onSwipe = viewModel::vote,
            )
        } else {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (state.busy) CircularProgressIndicator(color = SrColors.Primary)
                else Text("Все карточки просмотрены", style = SrType.Title, color = SrColors.Text)
            }
            if (state.error != null && !state.busy) {
                SrButton("Повторить отправку", viewModel::finish, Modifier.fillMaxWidth())
            }
        }
        state.error?.let { Text(it, style = SrType.Label, color = SrColors.Error) }
    }
    if (state.votingFiltersOpen) {
        ModalBottomSheet(onDismissRequest = viewModel::closeVotingFilters) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Фильтры комнаты", style = SrType.Title, color = SrColors.Text)
                Text(
                    "Настройки раунда доступны для просмотра. Изменить их можно перед следующим запуском.",
                    style = SrType.Body,
                    color = SrColors.Muted,
                )
                Text(
                    if (room.filters?.kind == FeedKind.EVENT) "События" else "Места",
                    style = SrType.Label,
                    color = SrColors.Text,
                )
            }
        }
    }
}
