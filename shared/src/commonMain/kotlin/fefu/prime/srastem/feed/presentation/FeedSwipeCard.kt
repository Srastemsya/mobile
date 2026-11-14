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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import fefu.prime.srastem.feed.domain.FeedCard
import fefu.prime.srastem.feed.domain.SwipeDecision
import kotlinx.coroutines.launch
import ru.srastemsya.uikit.SrArtwork
import ru.srastemsya.uikit.SrColors
import ru.srastemsya.uikit.SrIllustration
import ru.srastemsya.uikit.SrIcon
import ru.srastemsya.uikit.SrGlyph
import ru.srastemsya.uikit.SrShapes
import ru.srastemsya.uikit.SrSwipeAcceptImage
import ru.srastemsya.uikit.SrType
import ru.srastemsya.uikit.SrVoteCard
import ru.srastemsya.uikit.SrVoteCardSize

/** Shared single/room swipe-card stack matching Figma's tall cover-first card. */
@Composable
internal fun FeedSwipeCardStack(
    card: FeedCard,
    metadata: List<String>,
    modifier: Modifier = Modifier,
    swipeEnabled: Boolean = true,
    onSwipe: (SwipeDecision) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val dragX = remember(card.ref) { Animatable(0f) }
    var settling by remember(card.ref) { mutableStateOf(false) }
    val swipeProgress = (abs(dragX.value) / 120f).coerceIn(0f, 1f)
    val swipeColor = if (dragX.value < 0f) SrColors.Error else SrColors.Primary
    val glowAlpha = swipeProgress * .72f
    val glowElevation = (abs(dragX.value) / 4f).coerceIn(0f, 24f).dp
    LaunchedEffect(card.ref, swipeEnabled) {
        if (swipeEnabled) {
            dragX.snapTo(0f)
            settling = false
        }
    }

    Box(modifier, contentAlignment = Alignment.Center) {
        SwipeCardBackplate(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                .offset(y = 12.dp).graphicsLayer(rotationZ = -3f),
        )
        SwipeCardBackplate(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                .offset(y = 6.dp).graphicsLayer(rotationZ = 3f),
        )
        SrVoteCard(
            title = card.title,
            metadata = metadata,
            cover = {
                Box(Modifier.fillMaxSize()) {
                    FeedCardCover(card.coverUrl, card.title, Modifier.fillMaxSize())
                    if (swipeProgress > .08f) {
                        SwipeDirectionBadge(
                            isLike = dragX.value > 0f,
                            alpha = swipeProgress,
                            modifier = Modifier.align(
                                if (dragX.value > 0f) Alignment.TopEnd else Alignment.TopStart
                            ).padding(16.dp),
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 7.dp)
                .shadow(
                    elevation = glowElevation,
                    shape = SrShapes.Card,
                    clip = false,
                    ambientColor = swipeColor.copy(alpha = glowAlpha),
                    spotColor = swipeColor.copy(alpha = glowAlpha),
                )
                .graphicsLayer(
                    translationX = dragX.value,
                    rotationZ = dragX.value / 24f,
                )
                .pointerInput(card.ref, swipeEnabled) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, amount ->
                            if (swipeEnabled && !settling) scope.launch { dragX.snapTo(dragX.value + amount) }
                        },
                        onDragCancel = {
                            if (!settling) scope.launch { dragX.animateTo(0f, tween(160)) }
                        },
                        onDragEnd = {
                            if (!swipeEnabled || settling) return@detectHorizontalDragGestures
                            val decision = when {
                                dragX.value > 80f -> SwipeDecision.LIKE
                                dragX.value < -80f -> SwipeDecision.SKIP
                                else -> null
                            }
                            settling = true
                            scope.launch {
                                if (decision == null) {
                                    dragX.animateTo(0f, tween(160))
                                    settling = false
                                } else {
                                    dragX.animateTo(
                                        if (decision == SwipeDecision.LIKE) 900f else -900f,
                                        tween(180),
                                    )
                                    onSwipe(decision)
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
private fun SwipeDirectionBadge(isLike: Boolean, alpha: Float, modifier: Modifier = Modifier) {
    val color = if (isLike) SrColors.Primary else SrColors.Error
    Row(
        modifier
            .graphicsLayer { this.alpha = alpha }
            .clip(SrShapes.Control)
            .background(SrColors.Solid.copy(alpha = .96f))
            .border(1.dp, color.copy(alpha = .55f), SrShapes.Control)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isLike) {
            SrSwipeAcceptImage(
                Modifier.size(26.dp).clip(RoundedCornerShape(7.dp)),
                "Принять событие",
            )
        } else {
            SrGlyph(
                SrIcon.Close,
                description = "Пропустить событие",
                tint = color,
                size = 24.dp,
            )
        }
        Text(
            if (isLike) "Да" else "Нет",
            style = SrType.Label,
            color = color,
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
