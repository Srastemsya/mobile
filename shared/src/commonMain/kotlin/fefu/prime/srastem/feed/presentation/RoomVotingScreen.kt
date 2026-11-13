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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fefu.prime.srastem.feed.domain.FeedKind
import fefu.prime.srastem.feed.domain.RoomSnapshot
import fefu.prime.srastem.feed.domain.SwipeDecision
import ru.srastemsya.uikit.SrAvatarGroup
import ru.srastemsya.uikit.SrButton
import ru.srastemsya.uikit.SrColors
import ru.srastemsya.uikit.SrTopBar
import ru.srastemsya.uikit.SrType
import ru.srastemsya.uikit.SrVoteActions

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
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SrTopBar(
            title = if (room.filters?.kind == FeedKind.EVENT) "Выбор события" else "Выбор места",
            onBack = { viewModel.leave(); onBack() },
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
        Text(
            "Раунд ${room.roundNumber ?: 1} · ${state.voteIndex + 1}/${room.candidates.size}",
            style = SrType.Caption,
            color = SrColors.Muted,
        )
        if (card != null) {
            val kindLabel = if (card.ref.kind == FeedKind.EVENT) "Событие" else "Место"
            val metadata = listOfNotNull(
                kindLabel,
                card.priceLabel.takeIf(String::isNotBlank),
                card.addressOrDate.takeIf(String::isNotBlank),
            )
            FeedSwipeCardStack(
                card = card,
                metadata = metadata,
                modifier = Modifier.fillMaxWidth().weight(1f),
                onSwipe = viewModel::vote,
            )
            SrVoteActions(
                onPass = { viewModel.vote(SwipeDecision.SKIP) },
                onLike = { viewModel.vote(SwipeDecision.LIKE) },
                onUndo = if (state.voteIndex > 0) viewModel::previous else null,
                progress = (state.voteIndex + 1).toFloat() /
                    room.candidates.size.coerceAtLeast(1),
            )
        } else {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                if (state.busy) CircularProgressIndicator(color = SrColors.Primary)
                else Text("Все карточки просмотрены", style = SrType.Title, color = SrColors.Text)
            }
            SrButton(
                "Завершить голосование",
                viewModel::finish,
                Modifier.fillMaxWidth(),
                enabled = !state.busy,
                loading = state.busy,
            )
        }
        state.error?.let { Text(it, style = SrType.Label, color = SrColors.Error) }
    }
}
