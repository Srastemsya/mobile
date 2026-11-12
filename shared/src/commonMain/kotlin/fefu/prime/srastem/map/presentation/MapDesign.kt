package fefu.prime.srastem.map.presentation

import androidx.compose.ui.graphics.Brush
import ru.srastemsya.uikit.SrColors

internal object MapDesign {
    val Accent = SrColors.Primary
    val TextPrimary = SrColors.Text
    val TextSecondary = SrColors.Muted
    val BorderSubtle = SrColors.Border
    val GlassBorder = SrColors.GlassEdge

    val ScreenGradient = Brush.horizontalGradient(
        colorStops = arrayOf(
            0f to SrColors.Base,
            0.55f to SrColors.Tint,
            1f to SrColors.GlassIris,
        ),
    )
}
