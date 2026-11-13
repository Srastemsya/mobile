package fefu.prime.srastem

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun FeedBackHandler(enabled: Boolean, onBack: () -> Unit) = BackHandler(enabled, onBack)
