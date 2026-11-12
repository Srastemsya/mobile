package fefu.prime.srastem.map.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ru.srastemsya.uikit.SrColors
import ru.srastemsya.uikit.SrGlyph
import ru.srastemsya.uikit.SrIcon

enum class MapPinType { Event, Place, Activity, Quiz }

private val MapPinShape = GenericShape { size, _ ->
    moveTo(size.width / 2f, size.height)
    cubicTo(size.width * .43f, size.height * .80f, 0f, size.height * .59f, 0f, size.height * .36f)
    cubicTo(0f, size.height * .14f, size.width * .18f, 0f, size.width * .5f, 0f)
    cubicTo(size.width * .82f, 0f, size.width, size.height * .14f, size.width, size.height * .36f)
    cubicTo(size.width, size.height * .59f, size.width * .57f, size.height * .80f, size.width / 2f, size.height)
    close()
}

@Composable
fun SrMapPin(type: MapPinType, selected: Boolean, modifier: Modifier = Modifier, description: String? = null) {
    val icon = when (type) {
        MapPinType.Event -> SrIcon.Events
        MapPinType.Place -> SrIcon.Location
        MapPinType.Activity -> SrIcon.Spark
        MapPinType.Quiz -> SrIcon.Star
    }
    val accent = when (type) {
        MapPinType.Event -> SrColors.Primary
        MapPinType.Place -> Color(0xFF3E9B7B)
        MapPinType.Activity -> Color(0xFFE68C45)
        MapPinType.Quiz -> Color(0xFF9862CB)
    }
    Box(
        modifier.size(48.dp, 58.dp).shadow(if (selected) 8.dp else 4.dp, MapPinShape)
            .background(Color.White, MapPinShape)
            .then(if (description != null) Modifier.semantics { contentDescription = description } else Modifier),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            Modifier.padding(top = 4.dp).size(39.dp).background(if (selected) accent else accent.copy(alpha = .13f), androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            SrGlyph(icon, modifier = Modifier.fillMaxSize().padding(9.dp), tint = if (selected) Color.White else accent, size = 21.dp)
        }
    }
}
