package fefu.prime.srastem

import androidx.compose.runtime.Composable

@Composable
expect fun FeedBackHandler(enabled: Boolean, onBack: () -> Unit)
