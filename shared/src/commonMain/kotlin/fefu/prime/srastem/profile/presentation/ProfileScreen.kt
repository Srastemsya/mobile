package fefu.prime.srastem.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import fefu.prime.srastem.profile.domain.EventStatus
import fefu.prime.srastem.profile.domain.ProfileDetails
import fefu.prime.srastem.profile.domain.ProfileEvent
import fefu.prime.srastem.profile.domain.ProfileSavedKind
import fefu.prime.srastem.platform.media.PlatformImagePicker
import ru.srastemsya.uikit.SrArtwork
import ru.srastemsya.uikit.SrButton
import ru.srastemsya.uikit.SrColors
import ru.srastemsya.uikit.SrEventListCard
import ru.srastemsya.uikit.SrEventListState
import ru.srastemsya.uikit.SrField
import ru.srastemsya.uikit.SrFieldKind
import ru.srastemsya.uikit.SrGlyph
import ru.srastemsya.uikit.SrIcon
import ru.srastemsya.uikit.SrIllustration
import ru.srastemsya.uikit.SrHomeCover
import ru.srastemsya.uikit.SrHomeCoverImage
import ru.srastemsya.uikit.SrType

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier,
    onOpenEvent: (String) -> Unit = {},
    onOpenCollection: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.page) {
        if (state.page == ProfilePage.EVENTS || state.page == ProfilePage.SAVED) viewModel.refresh()
    }
    Column(
        modifier = modifier.fillMaxSize()
            .background(Color.White.copy(alpha = .66f))
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp)
            .padding(bottom = if (state.page == ProfilePage.HOME) 104.dp else 20.dp),
    ) {
        when (state.page) {
            ProfilePage.HOME -> HomePage(state.profile, viewModel)
            ProfilePage.DETAILS -> DetailsPage(state.profile, viewModel)
            ProfilePage.SAVED -> SavedPage(state.savedEvents, viewModel, onOpenEvent, onOpenCollection)
            ProfilePage.EVENTS -> EventsPage(state.myEvents, viewModel, onOpenEvent)
        }
    }
}

@Composable
private fun HomePage(profile: ProfileDetails, vm: ProfileViewModel) {
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.Center) {
            Text("Профиль", style = SrType.Heading, color = SrColors.Text)
            Box(
                Modifier.align(Alignment.CenterEnd).size(44.dp).clip(CircleShape)
                    .background(Color.White.copy(alpha = .86f))
                    .clickable(role = Role.Button) { vm.open(ProfilePage.DETAILS) },
                contentAlignment = Alignment.Center,
            ) { SettingsGlyph() }
        }
        Spacer(Modifier.height(14.dp))
        Row(
            Modifier.fillMaxWidth().height(104.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = SrColors.Shadow)
                .clip(RoundedCornerShape(24.dp)).background(Color.White)
                .border(1.dp, SrColors.Border, RoundedCornerShape(24.dp))
                .clickable(role = Role.Button) { vm.open(ProfilePage.DETAILS) }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ProfileAvatar(profile, Modifier.size(68.dp))
            Text(profile.name, Modifier.weight(1f), style = SrType.Heading, color = SrColors.Text)
            SrGlyph(SrIcon.Next)
        }
        Spacer(Modifier.height(18.dp))
        Column(
            Modifier.fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = SrColors.Shadow)
                .clip(RoundedCornerShape(24.dp)).background(Color.White)
                .border(1.dp, SrColors.Border, RoundedCornerShape(24.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            ProfileMenuRow("Сохранённое", SrIcon.Save) { vm.open(ProfilePage.SAVED) }
            ProfileMenuRow("Мои события", SrIcon.Events) { vm.open(ProfilePage.EVENTS) }
            ProfileMenuRow("Промокоды и скидки", SrIcon.Spark) { }
            ProfileMenuRow("Помощь и поддержка", SrIcon.Warning, isSupport = true) { }
        }
    }
}

@Composable
private fun DetailsPage(profile: ProfileDetails, vm: ProfileViewModel) {
    var pendingPhotoUri by remember { mutableStateOf<String?>(null) }
    var cropScale by remember { mutableFloatStateOf(profile.photoScale) }
    var cropOffsetX by remember { mutableFloatStateOf(profile.photoOffsetX) }
    var cropOffsetY by remember { mutableFloatStateOf(profile.photoOffsetY) }
    Column(Modifier.fillMaxSize()) {
        SubHeader("Личные данные", vm::back)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                Modifier.fillMaxWidth().height(108.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = SrColors.Shadow)
                    .clip(RoundedCornerShape(24.dp)).background(Color.White)
                    .border(1.dp, SrColors.Border, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                PlatformImagePicker(onImageSelected = { uri ->
                    if (uri != null) {
                        pendingPhotoUri = uri
                        cropScale = 1.24f
                        cropOffsetX = 0f
                        cropOffsetY = 0f
                    }
                }) { openPicker ->
                    Box(
                        Modifier.size(68.dp).clip(CircleShape)
                            .background(SrColors.Tint)
                            .clickable(role = Role.Button, onClick = openPicker),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (profile.photoUri != null) ProfileAvatar(profile, Modifier.fillMaxSize())
                        else SrGlyph(SrIcon.Photo, tint = SrColors.Primary, size = 24.dp)
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text("Имя", style = SrType.Caption, color = SrColors.Muted)
                    Spacer(Modifier.height(2.dp))
                    SrField(
                        profile.name,
                        { value -> vm.updateProfile { it.copy(name = value) } },
                        label = "",
                        placeholder = "Ваше имя",
                    )
                }
            }
            SrField(profile.city, { value -> vm.updateProfile { it.copy(city = value) } }, "Город")
            SrField(
                profile.birthDate,
                {},
                "Дата рождения",
                enabled = false,
                leading = SrIcon.Events
            )
            SrField(
                profile.about,
                { value -> vm.updateProfile { it.copy(about = value) } },
                "О себе · необязательно",
                kind = SrFieldKind.Multiline,
                placeholder = "Расскажите немного о себе",
            )
            Text(
                "Изменения сохраняются автоматически",
                Modifier.fillMaxWidth().padding(top = 4.dp),
                style = SrType.Caption,
                color = SrColors.Muted,
            )
        }
    }
    pendingPhotoUri?.let { uri ->
        AvatarCropDialog(
            uri = uri,
            scale = cropScale,
            offsetX = cropOffsetX,
            offsetY = cropOffsetY,
            onScaleChanged = { cropScale = it },
            onOffsetChanged = { x, y -> cropOffsetX = x; cropOffsetY = y },
            onDismiss = { pendingPhotoUri = null },
            onApply = {
                vm.updateProfile {
                    it.copy(
                        photoUri = uri,
                        photoScale = cropScale,
                        photoOffsetX = cropOffsetX,
                        photoOffsetY = cropOffsetY,
                    )
                }
                pendingPhotoUri = null
            },
        )
    }
}

@Composable
private fun SavedPage(
    events: List<ProfileEvent>,
    vm: ProfileViewModel,
    onOpenEvent: (String) -> Unit,
    onOpenCollection: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        SubHeader("Сохранённое", vm::back)
        Text(
            "Подборки и события, которые вам понравились",
            Modifier.padding(top = 20.dp, bottom = 12.dp),
            style = SrType.Body,
            color = SrColors.Muted,
        )
        if (events.isEmpty()) {
            EmptyProfileList("Здесь пока ничего нет в избранном")
        } else {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                events.forEach { event ->
                    SavedEventCard(
                        event,
                        onOpen = {
                            if (event.kind == ProfileSavedKind.HOME_COLLECTION) onOpenCollection()
                            else onOpenEvent(event.id)
                        },
                        onRemove = { vm.removeSaved(event.id) })
                }
            }
        }
    }
}

@Composable
private fun EventsPage(
    events: List<ProfileEvent>,
    vm: ProfileViewModel,
    onOpenEvent: (String) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        SubHeader("Мои события", onBack = vm::back)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Предстоящие", style = SrType.Title, color = SrColors.Text)
            val upcoming = events.filter { it.status == EventStatus.UPCOMING }
            upcoming.forEach { event -> EventCard(event, onOpen = { onOpenEvent(event.id) }) }
            if (upcoming.isEmpty()) EmptyProfileList("Предстоящих событий пока нет")
            Spacer(Modifier.height(8.dp))
            Text("Прошедшие", style = SrType.Title, color = SrColors.Text)
            val completed = events.filter { it.status == EventStatus.COMPLETED }
            completed.forEach { event ->
                EventCard(
                    event,
                    onOpen = { onOpenEvent(event.id) },
                    onReview = { })
            }
            if (completed.isEmpty()) EmptyProfileList("Завершённых событий пока нет")
        }
    }
}

@Composable
private fun SubHeader(title: String, onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp).height(52.dp)
            .shadow(5.dp, RoundedCornerShape(16.dp), ambientColor = SrColors.Shadow)
            .clip(RoundedCornerShape(16.dp)).background(Color.White)
            .border(1.dp, SrColors.Border, RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(44.dp).clip(CircleShape).clickable(role = Role.Button, onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            SrGlyph(SrIcon.Back, description = "Назад", tint = SrColors.Text)
        }
        Text(title, Modifier.weight(1f), style = SrType.Heading, color = SrColors.Text)
        Spacer(Modifier.size(44.dp))
    }
}

@Composable
private fun ProfileMenuRow(
    title: String,
    icon: SrIcon,
    isSupport: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(SrColors.Tint),
            contentAlignment = Alignment.Center
        ) {
            if (isSupport) SupportGlyph() else SrGlyph(icon, tint = SrColors.Primary, size = 20.dp)
        }
        Text(
            title,
            Modifier.weight(1f),
            style = SrType.Body.copy(fontWeight = FontWeight.SemiBold),
            color = SrColors.Text
        )
        SrGlyph(SrIcon.Next, tint = SrColors.Muted, size = 16.dp)
    }
}

@Composable
private fun ProfileAvatar(profile: ProfileDetails, modifier: Modifier = Modifier) {
    if (profile.photoUri != null) {
        Box(modifier.clip(CircleShape)) {
            AsyncImage(
                model = profile.photoUri,
                contentDescription = "Фото профиля",
                modifier = Modifier.fillMaxSize().graphicsLayer {
                    val cropToAvatar = 318f / 238f
                    scaleX = profile.photoScale * cropToAvatar
                    scaleY = profile.photoScale * cropToAvatar
                    translationX = profile.photoOffsetX * size.width
                    translationY = profile.photoOffsetY * size.height
                },
                contentScale = ContentScale.Crop,
            )
        }
    } else {
        Box(
            modifier.clip(CircleShape).background(SrColors.Tint),
            contentAlignment = Alignment.Center
        ) {
            Text("РК", style = SrType.Title, color = SrColors.Primary)
        }
    }
}

@Composable
private fun AvatarCropDialog(
    uri: String,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    onScaleChanged: (Float) -> Unit,
    onOffsetChanged: (Float, Float) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            Modifier.fillMaxSize().background(Color(0x8F141425)).clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(548.dp)
                    .shadow(16.dp, RoundedCornerShape(28.dp), ambientColor = SrColors.Shadow)
                    .clip(RoundedCornerShape(28.dp)).background(Color.White)
                    .border(1.dp, SrColors.Border, RoundedCornerShape(28.dp)).padding(20.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth().height(26.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Фото профиля",
                        Modifier.weight(1f),
                        style = SrType.Heading,
                        color = SrColors.Text
                    )
                    Box(
                        Modifier.size(36.dp).clip(CircleShape).clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center,
                    ) {
                        SrGlyph(
                            SrIcon.Close,
                            description = "Закрыть",
                            tint = SrColors.Text,
                            size = 20.dp
                        )
                    }
                }
                Spacer(Modifier.height(7.dp))
                Text(
                    "Переместите фото и настройте масштаб",
                    style = SrType.Caption,
                    color = SrColors.Muted
                )
                Spacer(Modifier.height(14.dp))
                Box(
                    Modifier.fillMaxWidth().height(318.dp)
                        .clip(RoundedCornerShape(20.dp)).background(Color(0xFF555D78))
                        .pointerInput(uri) {
                            var dragOffsetX = offsetX
                            var dragOffsetY = offsetY
                            detectDragGestures { change, drag ->
                                change.consume()
                                val apertureDiameterPx = size.width * (238f / 318f)
                                dragOffsetX =
                                    (dragOffsetX + drag.x / apertureDiameterPx).coerceIn(-.5f, .5f)
                                dragOffsetY =
                                    (dragOffsetY + drag.y / apertureDiameterPx).coerceIn(-.5f, .5f)
                                onOffsetChanged(dragOffsetX, dragOffsetY)
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = "Предпросмотр кадрирования фото",
                        modifier = Modifier.fillMaxSize().graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offsetX * size.width * (238f / 318f)
                            translationY = offsetY * size.height * (238f / 318f)
                        },
                        contentScale = ContentScale.Crop,
                    )
                    Canvas(Modifier.fillMaxSize()) {
                        val aperture = size.minDimension * (238f / 318f)
                        val mask = Path().apply {
                            fillType = PathFillType.EvenOdd
                            addRect(
                                androidx.compose.ui.geometry.Rect(
                                    0f,
                                    0f,
                                    size.width,
                                    size.height
                                )
                            )
                            addOval(
                                androidx.compose.ui.geometry.Rect(
                                    (size.width - aperture) / 2,
                                    (size.height - aperture) / 2,
                                    (size.width + aperture) / 2,
                                    (size.height + aperture) / 2,
                                )
                            )
                        }
                        drawPath(mask, Color.Black.copy(alpha = .38f))
                        drawCircle(
                            Color.White,
                            radius = aperture / 2,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth().height(44.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(44.dp).clip(CircleShape)
                            .clickable { onScaleChanged((scale - .1f).coerceAtLeast(1f)) },
                        contentAlignment = Alignment.Center,
                    ) { CropMinusGlyph() }
                    Slider(
                        value = scale,
                        onValueChange = onScaleChanged,
                        valueRange = 1f..2.5f,
                        modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
                    )
                    Box(
                        Modifier.size(44.dp).clip(CircleShape)
                            .clickable { onScaleChanged((scale + .1f).coerceAtMost(2.5f)) },
                        contentAlignment = Alignment.Center,
                    ) { SrGlyph(SrIcon.Plus, description = "Увеличить", tint = SrColors.Text) }
                }
                Spacer(Modifier.height(19.dp))
                SrButton("Применить", onApply, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun CropMinusGlyph() {
    Canvas(Modifier.size(20.dp)) {
        drawLine(
            SrColors.Text,
            androidx.compose.ui.geometry.Offset(size.width * .2f, size.height / 2),
            androidx.compose.ui.geometry.Offset(size.width * .8f, size.height / 2),
            strokeWidth = 1.8.dp.toPx(),
        )
    }
}

@Composable
private fun SavedEventCard(event: ProfileEvent, onOpen: () -> Unit, onRemove: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SrEventListCard(
            title = event.title,
            subtitle = event.subtitle,
            state = SrEventListState.Saved,
            onOpen = onOpen,
            cover = {
                if (event.kind == ProfileSavedKind.HOME_COLLECTION) {
                    val cover = event.coverKey?.let { key ->
                        runCatching { SrHomeCover.valueOf(key) }.getOrNull()
                    } ?: SrHomeCover.SvetlanskayaBars
                    SrHomeCoverImage(cover, Modifier.fillMaxSize(), event.title)
                } else {
                    SrArtwork(
                        SrIllustration.EventThumbnail,
                        Modifier.fillMaxSize(),
                        event.title
                    )
                }
            },
        )
        SrButton(
            "Убрать из сохранённого",
            onRemove,
            Modifier.fillMaxWidth(),
            style = ru.srastemsya.uikit.SrButtonStyle.Secondary
        )
    }
}

@Composable
private fun EventCard(event: ProfileEvent, onOpen: () -> Unit, onReview: (() -> Unit)? = null) {
    SrEventListCard(
        title = event.title,
        subtitle = event.subtitle,
        state = if (event.status == EventStatus.COMPLETED) SrEventListState.Completed else SrEventListState.Registered,
        onOpen = onOpen,
        cover = { SrArtwork(SrIllustration.EventThumbnail, Modifier.fillMaxSize(), event.title) },
        onReview = onReview,
    )
}

@Composable
private fun EmptyProfileList(message: String) {
    Column(
        Modifier.fillMaxWidth().padding(top = 90.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, style = SrType.Body, color = SrColors.Muted)
    }
}

@Composable
private fun SettingsGlyph() {
    androidx.compose.foundation.Canvas(Modifier.size(22.dp)) {
        val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
        drawCircle(
            SrColors.Muted,
            radius = size.minDimension * .36f,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(size.minDimension * .11f)
        )
        drawCircle(SrColors.Muted, radius = size.minDimension * .11f, center = center)
        repeat(8) { index ->
            val angle = index * (kotlin.math.PI / 4.0)
            val inner = size.minDimension * .35f
            val outer = size.minDimension * .48f
            val x = kotlin.math.cos(angle).toFloat()
            val y = kotlin.math.sin(angle).toFloat()
            drawLine(
                SrColors.Muted,
                androidx.compose.ui.geometry.Offset(center.x + x * inner, center.y + y * inner),
                androidx.compose.ui.geometry.Offset(center.x + x * outer, center.y + y * outer),
                size.minDimension * .11f
            )
        }
    }
}

@Composable
private fun SupportGlyph() {
    androidx.compose.foundation.Canvas(Modifier.size(20.dp)) {
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width * .18f, size.height * .16f)
            quadraticTo(
                size.width * .08f,
                size.height * .16f,
                size.width * .08f,
                size.height * .28f
            )
            lineTo(size.width * .08f, size.height * .68f)
            quadraticTo(
                size.width * .08f,
                size.height * .80f,
                size.width * .20f,
                size.height * .80f
            )
            lineTo(size.width * .34f, size.height * .80f)
            lineTo(size.width * .23f, size.height * .96f)
            lineTo(size.width * .54f, size.height * .80f)
            lineTo(size.width * .80f, size.height * .80f)
            quadraticTo(
                size.width * .92f,
                size.height * .80f,
                size.width * .92f,
                size.height * .68f
            )
            lineTo(size.width * .92f, size.height * .28f)
            quadraticTo(
                size.width * .92f,
                size.height * .16f,
                size.width * .80f,
                size.height * .16f
            )
            close()
        }
        drawPath(
            path,
            SrColors.Primary,
            style = androidx.compose.ui.graphics.drawscope.Stroke(size.minDimension * .09f)
        )
        repeat(3) { index ->
            drawCircle(
                SrColors.Primary,
                size.minDimension * .045f,
                androidx.compose.ui.geometry.Offset(
                    size.width * (.36f + index * .14f),
                    size.height * .48f
                )
            )
        }
    }
}
