package fefu.prime.srastem.map.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fefu.prime.srastem.map.domain.MapMarker
import fefu.prime.srastem.map.domain.MapMarkerIcon
import ru.srastemsya.uikit.SrButton
import ru.srastemsya.uikit.SrColors
import ru.srastemsya.uikit.SrField
import ru.srastemsya.uikit.SrFieldKind
import ru.srastemsya.uikit.SrIcon
import ru.srastemsya.uikit.SrIconButton
import ru.srastemsya.uikit.SrListRow
import ru.srastemsya.uikit.SrNotificationRow
import ru.srastemsya.uikit.SrType
import ru.srastemsya.uikit.SrTopBar

@Composable
internal fun MapSearchScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<MapMarker>,
    suggestions: List<String> = emptyList(),
    recentSearches: List<String>,
    onBack: () -> Unit,
    onChooseQuery: (String) -> Unit,
    onSelectResult: (MapMarker) -> Unit,
    onShowOnMap: () -> Unit,
) {
    val isResults = query.isNotBlank()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SrTopBar(
            title = if (isResults) "Результаты" else "Поиск",
            onBack = onBack,
            actions = {
                if (query.isNotEmpty()) SrIconButton(SrIcon.Close, "Очистить поиск", onClick =  { onQueryChange("") })
            },
        )
        SrField(
            value = query,
            onValueChange = onQueryChange,
            label = "",
            placeholder = "Поиск по карте",
            kind = SrFieldKind.Search,
            modifier = Modifier.fillMaxWidth(),
        )

        if (!isResults) {
            Text("Недавнее", style = SrType.Title, color = SrColors.Text)
            recentSearches.forEach { recent ->
                SrNotificationRow(
                    recent, "Недавний запрос", unread = false,
                    icon = SrIcon.Clock, onOpen = { onChooseQuery(recent) },
                )
            }
            Spacer(Modifier.height(4.dp))
            Text("Популярные запросы", style = SrType.Title, color = SrColors.Text)
            SrNotificationRow(
                "Квизы поблизости", "События", unread = false,
                icon = SrIcon.Star, onOpen = { onChooseQuery("Квизы") },
            )
            SrNotificationRow(
                "Активный отдых", "Подборка на карте", unread = false,
                icon = SrIcon.Spark, onOpen = { onChooseQuery("Активный отдых") },
            )
        } else {
            if (suggestions.isNotEmpty()) {
                Text("Подсказки", style = SrType.Title, color = SrColors.Text)
                suggestions.take(5).forEach { suggestion ->
                    SrNotificationRow(suggestion, "Найдено в каталоге", unread = false, icon = SrIcon.Search, onOpen = { onChooseQuery(suggestion) })
                }
            }
            Text("Найдено ${results.size} ${resultsWord(results.size)}", style = SrType.Caption, color = SrColors.Muted)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                results.forEach { marker ->
                    val (icon, type) = when (marker.icon) {
                        MapMarkerIcon.Activity -> SrIcon.Spark to "Активность"
                        MapMarkerIcon.Place -> SrIcon.Location to "Место"
                        MapMarkerIcon.Quiz -> SrIcon.Star to "Событие"
                        MapMarkerIcon.Event -> SrIcon.Events to "Событие"
                    }
                    SrListRow(
                        title = marker.title,
                        value = "$type · ${marker.subtitle}",
                        icon = icon,
                        onClick = { onSelectResult(marker) },
                    )
                }
            }
            if (results.isNotEmpty()) {
                SrButton("Показать на карте", onShowOnMap, Modifier.fillMaxWidth())
            }
        }
    }
}

private fun resultsWord(count: Int): String = when {
    count % 100 in 11..14 -> "результатов"
    count % 10 == 1 -> "результат"
    count % 10 in 2..4 -> "результата"
    else -> "результатов"
}
