package fefu.prime.srastem.map.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.srastemsya.uikit.SrButton
import ru.srastemsya.uikit.SrButtonStyle
import ru.srastemsya.uikit.SrChip
import ru.srastemsya.uikit.SrColors
import ru.srastemsya.uikit.SrGlassMode
import ru.srastemsya.uikit.SrGlassSurface
import ru.srastemsya.uikit.SrIcon
import ru.srastemsya.uikit.SrIconButton
import ru.srastemsya.uikit.SrSelection
import ru.srastemsya.uikit.SrShapes
import ru.srastemsya.uikit.SrType

internal data class MapFilterSelection(
    val date: String = "Любая дата",
    val budget: String = "Любой",
    val category: String = "Все",
    val onlyFreeSpots: Boolean = false,
)

@Composable
internal fun MapFilterSheet(
    selection: MapFilterSelection,
    onSelectionChange: (MapFilterSelection) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SrGlassSurface(modifier = modifier, shape = SrShapes.Card, mode = SrGlassMode.Liquid) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Фильтры", style = SrType.Heading, color = SrColors.Text, modifier = Modifier.weight(1f))
                SrIconButton(SrIcon.Close, "Закрыть фильтры", onClose)
            }
            FilterSection("Когда", listOf("Любая дата", "Сегодня", "Выходные"), selection.date) {
                onSelectionChange(selection.copy(date = it))
            }
            FilterSection("Бюджет на человека", listOf("Любой", "До 1 000 ₽", "До 3 000 ₽"), selection.budget) {
                onSelectionChange(selection.copy(budget = it))
            }
            FilterSection("Категория", listOf("Все", "Квизы", "Бары", "Активности"), selection.category) {
                onSelectionChange(selection.copy(category = it))
            }
            SrSelection(
                label = "Есть свободные места",
                checked = selection.onlyFreeSpots,
                onChange = { onSelectionChange(selection.copy(onlyFreeSpots = it)) },
            )
            SrButton("Показать результаты", onApply, Modifier.fillMaxWidth())
            SrButton("Сбросить фильтры", onReset, Modifier.fillMaxWidth(), style = SrButtonStyle.Secondary)
        }
    }
}

@Composable
private fun FilterSection(title: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = SrType.Label, color = SrColors.Text)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            options.forEach { label ->
                SrChip(label = label, selected = selected == label, onClick = { onSelect(label) })
            }
        }
    }
}
