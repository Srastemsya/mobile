package fefu.prime.srastem.map.presentation

import android.os.Build
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import org.maplibre.compose.map.AndroidRenderMode
import org.maplibre.compose.map.MapUiOptions
import ru.srastemsya.uikit.SrAndroidNativeMapGlassHost

internal actual fun mapUiOptions(): MapUiOptions = MapUiOptions {
    renderMode = AndroidRenderMode.Texture
}

internal actual fun supportsMapGlassSnapshots(): Boolean = Build.VERSION.SDK_INT >= 31

@Composable
internal actual fun MapGlassHost(
    snapshot: ImageBitmap?,
    modifier: Modifier,
    map: @Composable BoxScope.() -> Unit,
    controls: @Composable BoxScope.() -> Unit,
) {
    SrAndroidNativeMapGlassHost(
        snapshot = snapshot,
        map = map,
        modifier = modifier,
        controls = controls,
    )
}
