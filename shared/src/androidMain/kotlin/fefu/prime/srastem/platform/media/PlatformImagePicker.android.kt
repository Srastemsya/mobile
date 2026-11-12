package fefu.prime.srastem.platform.media

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun PlatformImagePicker(
    onImageSelected: (String?) -> Unit,
    content: @Composable (openPicker: () -> Unit) -> Unit,
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            onImageSelected(uri.toString())
        }
    }
    content { launcher.launch(arrayOf("image/*")) }
}
