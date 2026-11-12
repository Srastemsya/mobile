package fefu.prime.srastem.home.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.srastemsya.uikit.SrArtwork
import ru.srastemsya.uikit.SrButton
import ru.srastemsya.uikit.SrCard
import ru.srastemsya.uikit.SrColors
import ru.srastemsya.uikit.SrGlyph
import ru.srastemsya.uikit.SrIcon
import ru.srastemsya.uikit.SrIllustration
import ru.srastemsya.uikit.SrType

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onOpenFeed: () -> Unit,
    onOpenEvents: () -> Unit,
    onOpenMap: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("срастёмся", Modifier.weight(1f), style = SrType.Title, color = SrColors.Primary)
            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                SrGlyph(SrIcon.Bell, description = "Уведомления", size = 24.dp)
            }
        }
        Text("Куда срастёмся\nсегодня?", Modifier.padding(top = 16.dp), style = SrType.Display, color = SrColors.Text)
        Text(state.content?.city ?: "Владивосток", Modifier.padding(top = 4.dp), style = SrType.Body, color = SrColors.Muted)

        SrCard(Modifier.fillMaxWidth().padding(top = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Подобрать под вайб", style = SrType.Heading, color = SrColors.Text)
                    Text("Найдём идеи для вашей компании", style = SrType.Body, color = SrColors.Muted)
                    SrButton("Подобрать", onOpenMap)
                }
                SrGlyph(SrIcon.Spark, Modifier.size(36.dp), tint = SrColors.Primary, size = 32.dp)
            }
        }
        SrCard(Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Свайпать события", style = SrType.Heading, color = SrColors.Text)
                    Text("Выберите вместе, куда пойти", style = SrType.Body, color = SrColors.Muted)
                    SrButton("Начать", onOpenFeed)
                }
                SrArtwork(SrIllustration.Outdoor, Modifier.size(96.dp).clip(RoundedCornerShape(20.dp)), "Идеи для отдыха")
            }
        }

        Row(Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Популярное рядом", Modifier.weight(1f), style = SrType.Heading, color = SrColors.Text)
            Text("Смотреть все", Modifier.clickable(role = Role.Button, onClick = onOpenEvents), style = SrType.Label, color = SrColors.Primary)
        }
        if (state.isLoading) {
            Text("Загружаем подборки…", style = SrType.Body, color = SrColors.Muted)
        } else if (state.error != null) {
            Text(state.error.orEmpty(), style = SrType.Body, color = SrColors.Error)
            SrButton("Повторить", viewModel::refresh)
        } else {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                state.content?.collections.orEmpty().forEachIndexed { index, item ->
                    SrCard(Modifier.width(228.dp).clickable(role = Role.Button, onClick = onOpenMap)) {
                        SrArtwork(
                            if (index % 2 == 0) SrIllustration.Indoor else SrIllustration.Outdoor,
                            Modifier.fillMaxWidth().height(132.dp).clip(RoundedCornerShape(18.dp)), item.title,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(item.category, style = SrType.Caption, color = SrColors.Primary)
                        Text(item.title, style = SrType.Title, color = SrColors.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(item.subtitle, style = SrType.Caption, color = SrColors.Muted)
                    }
                }
            }
        }
        Spacer(Modifier.height(112.dp))
    }
}
