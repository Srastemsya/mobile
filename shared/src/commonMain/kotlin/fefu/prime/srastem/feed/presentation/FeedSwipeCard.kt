package fefu.prime.srastem.feed.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import fefu.prime.srastem.feed.domain.FeedCard
import fefu.prime.srastem.feed.domain.SwipeDecision
import kotlinx.coroutines.launch
import ru.srastemsya.uikit.SrArtwork
import ru.srastemsya.uikit.SrColors
import ru.srastemsya.uikit.SrIllustration
import ru.srastemsya.uikit.SrShapes
import ru.srastemsya.uikit.SrVoteCard
import ru.srastemsya.uikit.SrVoteCardSize

/** Shared single/room swipe-card stack matching Figma's tall cover-first card. */
@Composable
internal fun FeedSwipeCardStack(
    card: FeedCard,
    metadata: List<String>,
    modifier: Modifier = Modifier,
    onSwipe: (SwipeDecision) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var dragX by remember(card.ref) { mutableFloatStateOf(0f) }
    var settling by remember(card.ref) { mutableStateOf(false) }
    val arrival = remember(card.ref) { Animatable(.96f) }
    androidx.compose.runtime.LaunchedEffect(card.ref) {
        arrival.animateTo(1f, tween(180))
    }

    Box(modifier, contentAlignment = Alignment.Center) {
        SwipeCardBackplate(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                .offset(x = (-9).dp, y = (-18).dp).graphicsLayer(rotationZ = -3f),
        )
        SwipeCardBackplate(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                .offset(x = 8.dp, y = (-10).dp).graphicsLayer(rotationZ = 3f),
        )
        SrVoteCard(
            title = card.title,
            metadata = metadata,
            cover = { FeedCardCover(card.coverUrl, card.title, Modifier.fillMaxSize()) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 7.dp)
                .graphicsLayer(
                    translationX = dragX,
                    rotationZ = dragX / 24f,
                    scaleX = arrival.value,
                    scaleY = arrival.value,
                )
                .pointerInput(card.ref) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, amount ->
                            if (!settling) dragX += amount
                        },
                        onDragEnd = {
                            if (settling) return@detectHorizontalDragGestures
                            val decision = when {
                                dragX > 80f -> SwipeDecision.LIKE
                                dragX < -80f -> SwipeDecision.SKIP
                                else -> null
                            }
                            settling = true
                            scope.launch {
                                val animation = Animatable(dragX)
                                if (decision == null) {
                                    animation.animateTo(0f, tween(160)) { dragX = value }
                                    settling = false
                                } else {
                                    animation.animateTo(
                                        if (decision == SwipeDecision.LIKE) 900f else -900f,
                                        tween(180),
                                    ) { dragX = value }
                                    onSwipe(decision)
                                }
                            }
                        },
                        onDragCancel = {
                            if (!settling) {
                                settling = true
                                scope.launch {
                                    Animatable(dragX).animateTo(0f, tween(160)) { dragX = value }
                                    settling = false
                                }
                            }
                        },
                    )
                },
            size = SrVoteCardSize.Full,
            description = card.description.takeIf(String::isNotBlank),
        )
    }
}

@Composable
private fun SwipeCardBackplate(modifier: Modifier = Modifier) {
    Box(
        modifier.aspectRatio(335f / 565f)
            .clip(SrShapes.Card)
            .background(SrColors.Glass)
            .border(1.dp, SrColors.GlassEdge, SrShapes.Card),
    )
}

@Composable
internal fun FeedCardCover(url: String?, title: String, modifier: Modifier = Modifier) {
    var imageFailed by remember(url) { mutableStateOf(url.isNullOrBlank()) }
    Box(modifier, contentAlignment = Alignment.Center) {
        SrArtwork(SrIllustration.VoteCompact, Modifier.fillMaxSize(), title)
        if (!imageFailed && !url.isNullOrBlank()) {
            AsyncImage(
                model = url,
                contentDescription = "$title — обложка",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onState = { state ->
                    if (state is AsyncImagePainter.State.Error) imageFailed = true
                },
            )
        }
    }
}

@Composable
internal fun VoteDirectionHint(modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth()) {
        androidx.compose.material3.Text("← Нет", color = SrColors.Muted)
        Spacer(Modifier.weight(1f))
        androidx.compose.material3.Text("Да →", color = SrColors.Primary)
    }
}
