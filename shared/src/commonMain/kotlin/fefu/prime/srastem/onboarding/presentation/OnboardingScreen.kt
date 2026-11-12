package fefu.prime.srastem.onboarding.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collect
import coil3.compose.AsyncImage
import srastem.shared.generated.resources.Res

private data class OnboardingSlide(
    val resourcePath: String,
    val description: String,
)

private val slides = listOf(
    OnboardingSlide(
        "drawable/onboarding_slide_1.svg",
        "Куда пойти сегодня? Находи события и места во Владивостоке.",
    ),
    OnboardingSlide(
        "drawable/onboarding_slide_2.svg",
        "Договоритесь без споров. Создайте комнату и свайпайте варианты.",
    ),
    OnboardingSlide(
        "drawable/onboarding_slide_3.svg",
        "Найди свою компанию и присоединяйся к открытым сборам.",
    ),
    OnboardingSlide(
        "drawable/onboarding_slide_4.svg",
        "Вместе проще и выгоднее. Делите депозиты и билеты в приложении.",
    ),
)

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onSlideChanged: (Int) -> Unit,
    onContinue: () -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = state.currentSlide,
        pageCount = { slides.size },
    )

    LaunchedEffect(state.currentSlide) {
        if (pagerState.currentPage != state.currentSlide) {
            pagerState.animateScrollToPage(state.currentSlide)
        }
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect(onSlideChanged)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8FC))
            .safeDrawingPadding()
            .padding(horizontal = 22.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ProgressSteps(
            currentSlide = state.currentSlide,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 10.dp),
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 4.dp),
            pageSpacing = 12.dp,
            verticalAlignment = Alignment.CenterVertically,
        ) { page ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = Res.getUri(slides[page].resourcePath),
                    contentDescription = slides[page].description,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(482f / 806f)
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp)
                .padding(top = 16.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF4149FF))
                .clickable(role = Role.Button, onClick = onContinue),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Срастёмся",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Text(
            text = "Нажимая кнопку «Срастёмся», вы соглашаетесь с\nполитикой конфиденциальности",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 4.dp),
            color = Color(0xFF9999A2),
            fontSize = 10.sp,
            lineHeight = 13.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ProgressSteps(
    currentSlide: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        slides.indices.forEach { index ->
            val color by animateColorAsState(
                targetValue = if (index <= currentSlide) Color(0xFF4149FF) else Color(0xFFD9DAE4),
                animationSpec = tween(durationMillis = 220),
                label = "onboardingProgress$index",
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}
