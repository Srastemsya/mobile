package fefu.prime.srastem.map.presentation

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import org.maplibre.compose.map.MapUiOptions

/** Platform-specific map view settings; Android uses TextureView for normal Compose composition. */
internal expect fun mapUiOptions(): MapUiOptions

internal expect fun supportsMapGlassSnapshots(): Boolean

/** Keeps the live map interactive while the UI kit samples a separate, camera-aligned image. */
@Composable
internal expect fun MapGlassHost(
    snapshot: ImageBitmap?,
    modifier: Modifier,
    map: @Composable BoxScope.() -> Unit,
    controls: @Composable BoxScope.() -> Unit,
)
