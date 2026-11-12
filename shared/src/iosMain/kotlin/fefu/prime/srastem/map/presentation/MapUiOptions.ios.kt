package fefu.prime.srastem.map.presentation

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import org.maplibre.compose.map.MapUiOptions
import ru.srastemsya.uikit.SrMapGlassHost

internal actual fun mapUiOptions(): MapUiOptions = MapUiOptions.Standard

internal actual fun supportsMapGlassSnapshots(): Boolean = false

@Composable
internal actual fun MapGlassHost(
    snapshot: ImageBitmap?,
    modifier: Modifier,
    map: @Composable BoxScope.() -> Unit,
    controls: @Composable BoxScope.() -> Unit,
) {
    SrMapGlassHost(
        map = map,
        modifier = modifier,
        controls = controls,
    )
}
