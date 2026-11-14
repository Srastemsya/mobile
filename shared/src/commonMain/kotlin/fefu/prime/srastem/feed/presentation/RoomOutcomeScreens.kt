package fefu.prime.srastem.feed.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fefu.prime.srastem.feed.domain.CatalogDetails
import fefu.prime.srastem.feed.domain.FeedCard
import fefu.prime.srastem.feed.domain.FeedKind
import fefu.prime.srastem.feed.domain.RoomSnapshot
import ru.srastemsya.uikit.SrButton
import ru.srastemsya.uikit.SrCard
import ru.srastemsya.uikit.SrChip
import ru.srastemsya.uikit.SrColors
import ru.srastemsya.uikit.SrGlyph
import ru.srastemsya.uikit.SrIcon
import ru.srastemsya.uikit.SrTopBar
import ru.srastemsya.uikit.SrType

/** The room snapshot remains observed by RoomLobbyViewModel while this screen is visible. */
@Composable
internal fun RoomWaitScreen(onBack: () -> Unit, onStart: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().background(FeedDesign.ScreenGradient)
            .windowInsetsPadding(WindowInsets.statusBars)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        SrTopBar("Лента", onBack = onBack)
        Spacer(Modifier.height(24.dp))
        Text("Ваш голос принят", style = SrType.Display, color = SrColors.Text)
        Text(
            "Если большинство выберет один вариант, покажем его всем. Иначе обновим подборку.",
            style = SrType.Body, color = SrColors.Muted,
        )
        SrCard(Modifier.fillMaxWidth(.72f)) {
            SrGlyph(SrIcon.Clock, tint = SrColors.Primary)
            Text("Ждём участников", style = SrType.Title, color = SrColors.Text)
            Text("Голос отправлен", style = SrType.Body, color = SrColors.Muted)
        }
        SrButton("В начало ленты", onStart, Modifier.fillMaxWidth())
    }
}

@Composable
internal fun RoomMatchScreen(
    room: RoomSnapshot,
    details: CatalogDetails?,
    loading: Boolean,
    error: String?,
    onBack: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val winner = room.winner
    val candidate: FeedCard? = room.candidates.firstOrNull { it.ref == winner }
    val title = details?.title ?: candidate?.title.orEmpty()
    val description = details?.description ?: candidate?.description.orEmpty()
    val attributes = listOfNotNull(
        details?.priceLabel?.takeIf(String::isNotBlank) ?: candidate?.priceLabel?.takeIf(String::isNotBlank),
        details?.address?.takeIf(String::isNotBlank) ?: candidate?.addressOrDate?.takeIf(String::isNotBlank),
    ).joinToString(" · ")
    Column(
        modifier.fillMaxSize().background(FeedDesign.ScreenGradient)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Box(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            SrTopBar("Матч найден", onBack = onBack)
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Box(Modifier.fillMaxWidth().aspectRatio(390f / 396f).background(SrColors.Solid)) {
                FeedCardCover(details?.coverUrl ?: candidate?.coverUrl, title, Modifier.fillMaxSize())
            }
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SrChip("Выбор большинства", selected = true, onClick = {})
                Text(title.ifBlank { "Загружаем выбор" }, style = SrType.Display, color = SrColors.Text,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (attributes.isNotBlank()) Text(attributes, style = SrType.Label, color = SrColors.Text)
                Text(description.ifBlank { "Описание пока не добавлено." }, style = SrType.Body, color = SrColors.Muted)
                error?.let { Text(it, style = SrType.Body, color = SrColors.Error) }
            }
        }
        SrButton(
            if (winner?.kind == FeedKind.EVENT) "К мероприятию" else "К месту",
            onOpen,
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            enabled = winner != null && !loading,
            loading = loading,
        )
        Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars))
    }
}
