package fefu.prime.srastem.home.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.srastemsya.uikit.SrColors
import ru.srastemsya.uikit.SrHomeCover
import ru.srastemsya.uikit.SrHomeCoverImage
import ru.srastemsya.uikit.SrHomeVector
import ru.srastemsya.uikit.SrHomeVectorImage
import ru.srastemsya.uikit.SrType

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onOpenFeed: () -> Unit,
    onOpenEvents: () -> Unit,
    onOpenMap: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier.fillMaxSize()
            .background(SrColors.Base)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(top = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        HomeHeader()
        WelcomePanel()
        QuickActions(onOpenMap, onOpenFeed)
        PopularNearby(
            collections = state.content?.collections.orEmpty(),
            isLoading = state.isLoading,
            error = state.error,
            onRetry = viewModel::refresh,
            onOpenEvents = onOpenEvents,
            onOpenMap = onOpenMap,
        )
        Spacer(Modifier.height(180.dp))
    }
}

@Composable
private fun HomeHeader() {
    Row(
        Modifier.fillMaxWidth().height(59.dp).padding(start = 25.dp, end = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            Modifier.width(128.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            SrHomeVectorImage(
                SrHomeVector.Brand,
                Modifier.size(width = 53.dp, height = 24.dp),
                "Знак Срастёмся"
            )
            Text(
                "СРАСТЁМСЯ",
                style = SrType.Title.copy(
                    fontSize = 20.sp,
                    lineHeight = 25.sp,
                    letterSpacing = (-.35).sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = SrColors.Text,
                maxLines = 1,
            )
        }
        Box(
            Modifier.size(42.dp).shadow(
                16.dp,
                RoundedCornerShape(14.dp),
                ambientColor = SrColors.Shadow,
                spotColor = SrColors.Shadow
            )
                .clip(RoundedCornerShape(14.dp)).background(SrColors.Tint)
                .clickable(role = Role.Button, onClick = {}),
            contentAlignment = Alignment.Center,
        ) {
            SrHomeVectorImage(SrHomeVector.Bell, Modifier.size(21.dp), "Уведомления")
        }
    }
}

@Composable
private fun WelcomePanel() {
    Box(
        Modifier.fillMaxWidth().height(140.dp)
            .shadow(
                16.dp,
                RoundedCornerShape(24.dp),
                ambientColor = SrColors.Shadow,
                spotColor = SrColors.Shadow
            )
            .clip(RoundedCornerShape(24.dp))
            .background(SrColors.Tint),
    ) {
        SrHomeVectorImage(
            SrHomeVector.Bay,
            Modifier.align(Alignment.CenterEnd).size(width = 241.dp, height = 140.dp),
            "Бухта и маяк Владивостока",
        )
        Text(
            "Куда\nсрастёмся\nсегодня?",
            Modifier.align(Alignment.CenterStart).padding(start = 20.dp).width(156.dp),
            style = SrType.Display.copy(
                fontSize = 29.sp,
                lineHeight = 31.sp,
                letterSpacing = (-1.15).sp
            ),
            color = SrColors.Text,
        )
    }
}

@Composable
private fun QuickActions(onOpenMap: () -> Unit, onOpenFeed: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().height(175.dp).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        QuickMoodAction(onOpenMap)
        SwipeEventsAction(onOpenFeed)
    }
}

@Composable
private fun QuickMoodAction(onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(73.dp).clip(RoundedCornerShape(20.dp))
            .background(SrColors.Tint).border(1.dp, SrColors.Border, RoundedCornerShape(20.dp))
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Row(
            Modifier.align(Alignment.CenterStart).padding(start = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier.size(39.dp).clip(RoundedCornerShape(13.dp)).background(SrColors.Tint),
                contentAlignment = Alignment.Center
            ) {
                SrHomeVectorImage(SrHomeVector.Spark, Modifier.size(20.dp), "")
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Подобрать под вайб",
                    style = SrType.Label.copy(
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = SrColors.Text
                )
                Text(
                    "Расскажи, что хочется",
                    style = SrType.Caption.copy(
                        fontSize = 10.sp,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = SrColors.Primary
                )
            }
        }
        SrHomeVectorImage(
            SrHomeVector.Chevron,
            Modifier.align(Alignment.CenterEnd).padding(end = 10.dp).size(16.dp)
        )
        Row(
            Modifier.align(Alignment.TopEnd).offset(x = (-10).dp, y = 2.dp)
                .size(width = 84.dp, height = 70.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SrHomeVectorImage(
                SrHomeVector.Spark,
                Modifier.size(16.dp).offset(y = 14.dp).rotate(-9f).then(Modifier),
                ""
            )
            SrHomeVectorImage(
                SrHomeVector.Spark,
                Modifier.size(30.dp).offset(y = (-4).dp).rotate(10f).then(Modifier),
                ""
            )
            SrHomeVectorImage(
                SrHomeVector.Spark,
                Modifier.size(20.dp).offset(y = 16.dp).rotate(-7f).then(Modifier),
                ""
            )
        }
    }
}

@Composable
private fun SwipeEventsAction(onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(92.dp).clip(RoundedCornerShape(22.dp))
            .background(SrColors.Primary).clickable(role = Role.Button, onClick = onClick),
    ) {
        Column(
            Modifier.align(Alignment.CenterStart).padding(start = 19.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                "Свайпать события",
                style = SrType.Title.copy(
                    fontSize = 17.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = SrColors.OnAction
            )
            Text(
                "Найди своё идеальное",
                style = SrType.Label.copy(
                    fontSize = 13.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = SrColors.OnAction.copy(alpha = .84f)
            )
        }
        SwipeIllustration(Modifier.align(Alignment.TopStart).offset(x = 200.dp, y = 3.dp))
        Box(
            Modifier.align(Alignment.CenterEnd).padding(end = 13.dp).size(32.dp)
                .clip(RoundedCornerShape(50)).background(SrColors.OnAction),
            contentAlignment = Alignment.Center,
        ) {
            SrHomeVectorImage(SrHomeVector.Next, Modifier.size(20.dp), "Начать свайпать события")
        }
    }
}

@Composable
private fun SwipeIllustration(modifier: Modifier = Modifier) {
    Box(modifier.size(width = 124.dp, height = 110.dp)) {
        Box(
            Modifier.offset(x = 22.dp, y = 18.dp).size(width = 59.dp, height = 77.dp).rotate(21f)
                .clip(RoundedCornerShape(10.dp)).background(SrColors.OnAction.copy(alpha = .28f))
        )
        Box(
            Modifier.offset(x = 24.dp, y = 10.dp).size(width = 59.dp, height = 77.dp).rotate(10f)
                .clip(RoundedCornerShape(10.dp)).background(SrColors.OnAction.copy(alpha = .48f))
        )
        Box(
            Modifier.offset(x = 22.dp, y = 7.dp).size(width = 62.dp, height = 81.dp).rotate(-12f)
                .clip(RoundedCornerShape(10.dp)).background(SrColors.Tint)
                .border(2.dp, SrColors.OnAction, RoundedCornerShape(10.dp)),
        ) {
            SrHomeVectorImage(SrHomeVector.SwipeArt, Modifier.fillMaxSize(), "Вечерняя терраса")
        }
        SrHomeVectorImage(
            SrHomeVector.SwipeSpark,
            Modifier.offset(x = 2.dp, y = 9.dp).size(20.dp),
            ""
        )
    }
}

@Composable
private fun PopularNearby(
    collections: List<fefu.prime.srastem.home.domain.HomeCollection>,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onOpenEvents: () -> Unit,
    onOpenMap: () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().padding(start = 20.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(end = 20.dp).height(30.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Популярное рядом", style = SrType.Heading, color = SrColors.Text, maxLines = 1)
            Row(
                Modifier.height(30.dp).clip(RoundedCornerShape(12.dp))
                    .clickable(role = Role.Button, onClick = onOpenEvents),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Смотреть все",
                    style = SrType.Caption.copy(
                        fontSize = 12.sp,
                        lineHeight = 25.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = SrColors.Text
                )
                SrHomeVectorImage(SrHomeVector.Next, Modifier.size(21.dp))
            }
        }
        when {
            isLoading -> Text(
                "Загружаем подборки…",
                Modifier.padding(end = 20.dp),
                style = SrType.Body,
                color = SrColors.Muted
            )

            error != null -> Column(
                Modifier.padding(end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(error, style = SrType.Body, color = SrColors.Error)
                Text(
                    "Повторить",
                    Modifier.clickable(role = Role.Button, onClick = onRetry),
                    style = SrType.Label,
                    color = SrColors.Primary
                )
            }

            else -> Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(end = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                collections.forEachIndexed { index, item ->
                    HomeCollectionCard(
                        title = item.title,
                        subtitle = item.subtitle,
                        cover = when (index) {
                            0 -> SrHomeCover.SvetlanskayaBars
                            else -> SrHomeCover.RusskyWeekend
                        },
                        onOpen = onOpenMap,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeCollectionCard(
    title: String,
    subtitle: String,
    cover: SrHomeCover,
    onOpen: () -> Unit
) {
    var saved by remember(title) { mutableStateOf(false) }
    Column(
        Modifier.width(218.dp).height(223.dp).clip(RoundedCornerShape(20.dp))
            .background(SrColors.Solid)
            .clickable(role = Role.Button, onClick = onOpen),
    ) {
        Box(Modifier.width(218.dp).height(134.dp)) {
            SrHomeCoverImage(cover, Modifier.fillMaxSize(), title)
            Box(
                Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 9.dp).size(34.dp)
                    .clip(RoundedCornerShape(17.dp)).background(SrColors.Solid)
                    .clickable(role = Role.Button, onClick = { saved = !saved }),
                contentAlignment = Alignment.Center,
            ) {
                SrHomeVectorImage(
                    SrHomeVector.Heart,
                    Modifier.size(17.dp),
                    if (saved) "Убрать из сохранённого" else "Сохранить подборку"
                )
            }
        }
        Column(
            Modifier.fillMaxWidth().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                title,
                style = SrType.Title.copy(
                    fontSize = 16.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = SrColors.Text,
                maxLines = 2,
                overflow = TextOverflow.Clip,
            )
            Text(
                subtitle,
                style = SrType.Caption.copy(fontSize = 11.sp, lineHeight = 14.sp),
                color = SrColors.Muted,
                maxLines = 1
            )
        }
    }
}
