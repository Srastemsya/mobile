package fefu.prime.srastem.feed.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import fefu.prime.srastem.feed.domain.CatalogDetails
import ru.srastemsya.uikit.SrArtwork
import ru.srastemsya.uikit.SrButton
import ru.srastemsya.uikit.SrColors
import ru.srastemsya.uikit.SrGlyph
import ru.srastemsya.uikit.SrIcon
import ru.srastemsya.uikit.SrIllustration
import ru.srastemsya.uikit.SrTopBar
import ru.srastemsya.uikit.SrType

@Composable
internal fun FeedCatalogDetailsScreen(
    details: CatalogDetails,
    onBack: () -> Unit,
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxSize().background(FeedDesign.ScreenGradient)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SrTopBar(title = "О месте", onBack = onBack)
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            androidx.compose.foundation.layout.Box(
                Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(20.dp))
                    .background(SrColors.Solid),
            ) {
                SrArtwork(SrIllustration.VoteCompact, Modifier.fillMaxSize(), details.title)
                details.coverUrl?.takeIf(String::isNotBlank)?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = "${details.title} — обложка",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
            Text(details.categoryId.toPlaceCategory(), style = SrType.Label, color = SrColors.Muted)
            Text(details.title, style = SrType.Display, color = SrColors.Text)
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                    .background(SrColors.Solid)
                    .border(1.dp, SrColors.Border, RoundedCornerShape(20.dp)).padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (details.startsAt != null) DetailRow(SrIcon.Events, details.startsAt)
                if (details.address.isNotBlank()) DetailRow(SrIcon.Location, details.address)
                details.admissionLabel?.let { DetailRow(SrIcon.People, it) }
            }
            Text(details.priceLabel, style = SrType.Label, color = SrColors.Muted)
            Text("О месте", style = SrType.Title, color = SrColors.Text)
            Text(details.description.ifBlank { "Описание пока не добавлено." }, style = SrType.Body, color = SrColors.Text)
        }
        SrButton("На главный экран", onGoHome, Modifier.fillMaxWidth())
    }
}

@Composable
private fun DetailRow(icon: SrIcon, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SrGlyph(icon, tint = SrColors.Primary)
        Text(value, style = SrType.Body, color = SrColors.Text)
    }
}

private fun String.toPlaceCategory(): String = when (lowercase()) {
    "food" -> "Еда и бары"
    "outdoors" -> "Прогулки"
    "games" -> "Игры"
    "culture" -> "Культура"
    else -> "Место"
}
