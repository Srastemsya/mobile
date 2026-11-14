package fefu.prime.srastem.platform.media

import androidx.compose.runtime.Composable

@Composable
expect fun PlatformImagePicker(
    onImageSelected: (String?) -> Unit,
    content: @Composable (openPicker: () -> Unit) -> Unit,
)
