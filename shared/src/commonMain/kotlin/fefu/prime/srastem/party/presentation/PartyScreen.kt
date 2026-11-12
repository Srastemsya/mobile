package fefu.prime.srastem.party.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import fefu.prime.srastem.party.domain.PartyCategory
import fefu.prime.srastem.party.domain.PartyFeedType
import fefu.prime.srastem.party.domain.PartyItem
import fefu.prime.srastem.party.domain.PartyPhase
import fefu.prime.srastem.party.domain.PartyVote
import ru.srastemsya.uikit.SrButton
import ru.srastemsya.uikit.SrChip
import ru.srastemsya.uikit.SrColors
import ru.srastemsya.uikit.SrField
import ru.srastemsya.uikit.SrFieldKind
import ru.srastemsya.uikit.SrShapes
import ru.srastemsya.uikit.SrTopBar
import ru.srastemsya.uikit.SrType
import ru.srastemsya.uikit.SrVoteActions
import ru.srastemsya.uikit.SrVoteCard
import ru.srastemsya.uikit.SrVoteCardSize
import ru.srastemsya.uikit.SrIllustration
import ru.srastemsya.uikit.SrArtwork

@Composable
fun PartyScreen(viewModel: PartyViewModel, onOpenEvent: (String) -> Unit = {}, onOpenPlace: () -> Unit = {}) {
    val state by viewModel.state.collectAsState()
    Column(
        Modifier.fillMaxSize().background(
            Brush.horizontalGradient(listOf(Color(0xFFF8F8FD), Color(0xFFECECFF), Color(0xFFD8D8FF)))
        ).windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SrTopBar(
            title = when (state.page) {
                PartyPage.MODE -> "Лента"
                PartyPage.CREATE -> "Новая комната"
                PartyPage.JOIN -> "Войти в комнату"
                PartyPage.LOBBY -> "Комната"
                PartyPage.FILTERS -> "Фильтры"
                PartyPage.FEED -> if (state.filters.feedType == PartyFeedType.EVENTS) "Выбор события" else "Выбор места"
                PartyPage.WAITING -> "Лента"
                PartyPage.RESULT -> when {
                    state.isSingleDevice && state.selectedItem != null -> "Выбор сделан"
                    state.session?.room?.phase == PartyPhase.MATCH -> "Матч найден"
                    else -> "Лента"
                }
            },
            onBack = if (state.page == PartyPage.MODE) null else viewModel::back,
        )
        if (state.error != null) {
            Text(state.error.orEmpty(), color = SrColors.Error, style = SrType.Label)
            SrButton("Понятно", { viewModel.retry() }, Modifier.fillMaxWidth())
        } else when (state.page) {
            PartyPage.MODE -> ModePage(viewModel)
            PartyPage.CREATE -> CreatePage(state, viewModel)
            PartyPage.JOIN -> JoinPage(state, viewModel)
            PartyPage.LOBBY -> LobbyPage(state, viewModel)
            PartyPage.FILTERS -> FiltersPage(state, viewModel)
            PartyPage.FEED -> FeedPage(state, viewModel)
            PartyPage.WAITING -> WaitingPage(state, viewModel)
            PartyPage.RESULT -> ResultPage(state, viewModel, onOpenEvent, onOpenPlace)
        }
        if (state.busy) CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally), color = SrColors.Primary)
    }
}

@Composable
private fun ModePage(vm: PartyViewModel) {
    Text("Выбираем вместе", style = SrType.Display, color = SrColors.Text)
    Text("Найдите место или событие, которое понравится всей компании.", style = SrType.Body, color = SrColors.Muted)
    ChoiceCard("На одном устройстве", "Смотрите карточки вместе на одном телефоне и задайте общий бюджет.", vm::openSingleDevice)
    ChoiceCard("На нескольких устройствах", "Создайте комнату и пригласите друзей. У каждого будет своя лента.", vm::openCreate)
    SrButton("Присоединиться по ссылке", vm::openJoin, Modifier.fillMaxWidth(), style = ru.srastemsya.uikit.SrButtonStyle.Secondary)
}

@Composable
private fun ChoiceCard(title: String, description: String, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(SrShapes.Card).background(SrColors.Glass).border(1.dp, SrColors.GlassEdge, SrShapes.Card).clickable(onClick = onClick).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = SrType.Title, color = SrColors.Text)
        Text(description, style = SrType.Body, color = SrColors.Muted)
    }
}

@Composable
private fun CreatePage(state: PartyUiState, vm: PartyViewModel) {
    Text("Создайте комнату", style = SrType.Display, color = SrColors.Text)
    Text("Друзья смогут присоединиться по ссылке.", style = SrType.Body, color = SrColors.Muted)
    SrField(state.roomName, vm::setRoomName, "Название комнаты", placeholder = "Вечер пятницы")
    SrField(state.participantName, vm::setParticipantName, "Ваше имя", placeholder = "Как вас представить")
    Spacer(Modifier.height(2.dp))
    SrButton("Создать комнату", vm::createRoom, Modifier.fillMaxWidth(), loading = state.busy)
}

@Composable
private fun JoinPage(state: PartyUiState, vm: PartyViewModel) {
    Text("Присоединитесь к друзьям", style = SrType.Display, color = SrColors.Text)
    Text("Откройте ссылку-приглашение или вставьте код комнаты.", style = SrType.Body, color = SrColors.Muted)
    SrField(state.participantName, vm::setParticipantName, "Ваше имя")
    SrField(state.roomCode, vm::setRoomCode, "Код или ссылка", placeholder = "srastem://party/123456")
    SrButton("Войти в комнату", vm::joinRoom, Modifier.fillMaxWidth(), loading = state.busy)
}

@Composable
private fun LobbyPage(state: PartyUiState, vm: PartyViewModel) {
    val clipboard = LocalClipboardManager.current
    val session = state.session ?: return
    val room = session.room
    Text(room.name, style = SrType.Display, color = SrColors.Text)
    Text("Пригласите друзей, прежде чем начать выбор.", style = SrType.Body, color = SrColors.Muted)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        room.participants.take(4).forEach { Avatar(it.name) }
        if (room.participants.size > 4) Avatar("+${room.participants.size - 4}")
    }
    Text("${room.participants.size} участник(ов) подключено", style = SrType.Label, color = SrColors.Muted)
    Column(Modifier.clip(SrShapes.Panel).background(SrColors.Solid).border(1.dp, SrColors.Border, SrShapes.Panel).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(if (state.copied) "Ссылка готова" else "Пригласить в комнату", style = SrType.Title, color = SrColors.Text)
        Text(session.invitationUrl, style = SrType.Body, color = SrColors.Primary)
        Text("Поделитесь ссылкой с друзьями.", style = SrType.Body, color = SrColors.Muted)
        SrButton(if (state.copied) "Ссылка скопирована" else "Скопировать ссылку", {
            clipboard.setText(AnnotatedString(session.invitationUrl))
            vm.copyInvite()
        })
    }
    if (session.participantId == room.hostId) {
        SrButton("К фильтрам", vm::openFilters, Modifier.fillMaxWidth())
    } else {
        Text("Ждём, пока организатор запустит выбор", style = SrType.Body, color = SrColors.Muted)
    }
}

@Composable
private fun FiltersPage(state: PartyUiState, vm: PartyViewModel) {
    Text("Что ищем?", style = SrType.Display, color = SrColors.Text)
    Text(
        if (state.isSingleDevice) "Вы выбираете вместе на одном устройстве." else "Организатор задаёт фильтры для всей комнаты.",
        style = SrType.Body,
        color = SrColors.Muted,
    )
    Text("Тип подборки", style = SrType.Label, color = SrColors.Muted)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SrChip("Места", selected = state.filters.feedType == PartyFeedType.PLACES, onClick = { vm.updateFilters { it.copy(feedType = PartyFeedType.PLACES, category = PartyCategory.ALL) } })
        SrChip("События", selected = state.filters.feedType == PartyFeedType.EVENTS, onClick = { vm.updateFilters { it.copy(feedType = PartyFeedType.EVENTS, category = PartyCategory.ALL) } })
    }
    Text("Категории", style = SrType.Label, color = SrColors.Muted)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val categories = if (state.filters.feedType == PartyFeedType.PLACES) {
            listOf(PartyCategory.ALL to "Все", PartyCategory.FOOD to "Еда и бары", PartyCategory.ACTIVITIES to "Активности")
        } else {
            listOf(PartyCategory.ALL to "Все", PartyCategory.ACTIVITIES to "Активности")
        }
        categories.forEach { (category, label) ->
            SrChip(label, selected = state.filters.category == category, onClick = { vm.updateFilters { it.copy(category = category) } })
        }
    }
    SrField(state.filters.budgetPerPerson.toString(), { value -> vm.updateFilters { it.copy(budgetPerPerson = value.filter(Char::isDigit).toIntOrNull() ?: 0) } }, "Бюджет на человека, ₽", kind = SrFieldKind.Number)
    SrField(state.filters.whenLabel, { value -> vm.updateFilters { it.copy(whenLabel = value) } }, "Когда")
    Spacer(Modifier.height(2.dp))
    SrButton(
        "Запустить выбор",
        if (state.isSingleDevice) vm::startSingleDeviceFeed else vm::saveFiltersAndStart,
        Modifier.fillMaxWidth(),
        loading = state.busy,
    )
}

@Composable
private fun FeedPage(state: PartyUiState, vm: PartyViewModel) {
    val session = state.session
    val items = session?.room?.items ?: state.localItems
    if (state.isSingleDevice) {
        Text("Одно устройство · общий бюджет", style = SrType.Caption, color = SrColors.Muted, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    } else {
        val room = session?.room ?: return
        Text("Комната: ${room.name}", style = SrType.Caption, color = SrColors.Muted, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            room.participants.take(4).forEach { Avatar(it.name, size = 32.dp) }
            Spacer(Modifier.width(4.dp)); Text("${room.participants.count { it.completedFeed }}/${room.participants.size} завершили", style = SrType.Caption, color = SrColors.Muted)
        }
    }
    val item = items.getOrNull(state.itemIndex)
    if (item == null) {
        Text("Подборка обновляется…", style = SrType.Heading, color = SrColors.Text)
        Text("Ищем варианты по вашим фильтрам", style = SrType.Body, color = SrColors.Muted)
    } else {
        SwipeablePartyDeck(items, state.itemIndex, vm::vote)
        SrVoteActions(
            onPass = { vm.vote(PartyVote.SKIP) },
            onLike = { vm.vote(PartyVote.LIKE) },
            onUndo = if (state.isSingleDevice && state.itemIndex > 0) vm::undoVote else null,
            progress = if (items.isEmpty()) 0f else (state.itemIndex + 1).toFloat() / items.size,
            modifier = Modifier,
        )
    }
}

@Composable
private fun SwipeablePartyDeck(items: List<PartyItem>, itemIndex: Int, onVote: (PartyVote) -> Unit) {
    val current = items.getOrNull(itemIndex) ?: return
    val queuedItems = items.drop(itemIndex + 1).take(3)
    var observedItemCount by remember { mutableIntStateOf(items.size) }
    var appendedItemIds by remember { mutableStateOf(emptySet<String>()) }
    LaunchedEffect(items.size) {
        if (items.size > observedItemCount) {
            appendedItemIds = appendedItemIds + items.drop(observedItemCount).map { it.id }
        }
        observedItemCount = items.size
    }
    Box(Modifier.fillMaxWidth().padding(horizontal = 7.dp).height(565.dp), contentAlignment = Alignment.TopCenter) {
        queuedItems.asReversed().forEachIndexed { layer, queuedItem ->
            val depth = queuedItems.size - layer
            val rearOffset = depth - 1
            val cardModifier = Modifier.width(358.dp).offset(
                x = if (rearOffset == 0) 8.dp else (-10 * rearOffset).dp,
                y = if (rearOffset == 0) (-15).dp else (-20 * rearOffset).dp,
            ).rotate(if (rearOffset == 0) 2f else -2f).graphicsLayer {
                scaleX = 1f - .015f * rearOffset
                scaleY = 1f - .015f * rearOffset
                alpha = .82f - .2f * rearOffset
            }
            key(queuedItem.id) {
                if (queuedItem.id in appendedItemIds) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(260)) + scaleIn(initialScale = .9f) + slideInVertically { it / 3 },
                        label = "append-card-to-deck",
                    ) { PartyItemCard(item = queuedItem, modifier = cardModifier) }
                } else {
                    PartyItemCard(item = queuedItem, modifier = cardModifier)
                }
            }
        }
        PartyItemCard(item = current, onSwipe = onVote, isPromoted = itemIndex > 0)
    }
}

@Composable
private fun PartyItemCard(
    item: PartyItem,
    onSwipe: ((PartyVote) -> Unit)? = null,
    modifier: Modifier = Modifier,
    isPromoted: Boolean = false,
) {
    val voteAction = onSwipe
    val density = LocalDensity.current
    val promotionStartX = with(density) { if (isPromoted) 8.dp.toPx() else 0f }
    val promotionStartY = with(density) { if (isPromoted) (-15).dp.toPx() else 0f }
    val promotionX = remember(item.id) { Animatable(promotionStartX) }
    val promotionY = remember(item.id) { Animatable(promotionStartY) }
    val promotionRotation = remember(item.id) { Animatable(if (isPromoted) 2f else 0f) }
    val promotionScale = remember(item.id) { Animatable(if (isPromoted) .985f else 1f) }
    val dragX = androidx.compose.runtime.remember(item.id) { Animatable(0f) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(item.id) {
        dragX.snapTo(0f)
        if (isPromoted) {
            launch { promotionX.animateTo(0f, tween(230)) }
            launch { promotionY.animateTo(0f, tween(230)) }
            launch { promotionRotation.animateTo(0f, tween(230)) }
            launch { promotionScale.animateTo(1f, tween(230)) }
        }
    }
    val swipeModifier = if (voteAction == null) Modifier else Modifier.pointerInput(item.id, voteAction) {
        detectDragGestures(
            onDrag = { change, amount ->
                change.consume()
                scope.launch { dragX.snapTo(dragX.value + amount.x) }
            },
            onDragEnd = {
                val amount = dragX.value
                val threshold = size.width * 0.25f
                if (abs(amount) >= threshold) {
                    scope.launch {
                        dragX.animateTo(if (amount > 0) size.width * 1.25f else -size.width * 1.25f, tween(220))
                        voteAction(if (amount > 0) PartyVote.LIKE else PartyVote.SKIP)
                    }
                } else {
                    scope.launch { dragX.animateTo(0f, tween(180)) }
                }
            },
        )
    }
    Box(
        modifier.fillMaxWidth().height(565.dp)
            .offset { IntOffset((dragX.value + promotionX.value).roundToInt(), promotionY.value.roundToInt()) }
            .graphicsLayer {
                scaleX = promotionScale.value
                scaleY = promotionScale.value
                rotationZ = (dragX.value / 40f + promotionRotation.value).coerceIn(-12f, 12f)
            }
            .then(swipeModifier)
            .then(if (voteAction == null) Modifier else Modifier.semantics {
                customActions = listOf(
                    CustomAccessibilityAction("Пропустить карточку") { voteAction(PartyVote.SKIP); true },
                    CustomAccessibilityAction("Выбрать карточку") { voteAction(PartyVote.LIKE); true },
                )
            }),
    ) {
        SrVoteCard(
            title = item.title,
            metadata = listOfNotNull(
                item.category,
                item.price,
                item.location,
                fefu.prime.srastem.events.domain.eventAttendanceSummary(
                    item.confirmedCount, item.capacity, item.remainingCount,
                ),
            ),
            cover = {
                if (item.imageKey.startsWith("content://") || item.imageKey.startsWith("file://") || item.imageKey.startsWith("http")) {
                    AsyncImage(item.imageKey, contentDescription = item.title, modifier = Modifier.fillMaxSize())
                } else SrArtwork(SrIllustration.EventThumbnail, Modifier.fillMaxSize(), item.title)
            },
            modifier = Modifier.fillMaxSize(),
            size = SrVoteCardSize.Full,
            description = item.description,
        )
        if (voteAction != null && abs(dragX.value) > 12f) {
            val liking = dragX.value > 0
            Text(
                if (liking) "ДА" else "НЕТ",
                Modifier.align(if (liking) Alignment.TopStart else Alignment.TopEnd).padding(22.dp)
                    .rotate(if (liking) -12f else 12f),
                style = SrType.Display,
                color = if (liking) Color(0xFF16845B) else Color(0xFFD94B5B),
            )
        }
    }
}

@Composable
private fun WaitingPage(state: PartyUiState, vm: PartyViewModel) {
    val room = state.session?.room ?: return
    val complete = room.finishedCount.takeIf { it > 0 } ?: room.participants.count { it.completedFeed }
    Text("Ваш голос принят", style = SrType.Display, color = SrColors.Text)
    Text("Если большинство выберет один вариант, покажем его всем. Иначе обновим подборку.", style = SrType.Body, color = SrColors.Muted)
    Column(Modifier.clip(SrShapes.Panel).background(SrColors.Solid).border(1.dp, SrColors.Border, SrShapes.Panel).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Ждём участников", style = SrType.Title, color = SrColors.Text)
        Text("Голос отправлен · $complete из ${room.participants.size} завершили ленту", style = SrType.Body, color = SrColors.Muted)
    }
    SrButton("В начало ленты", vm::back, Modifier.fillMaxWidth(), style = ru.srastemsya.uikit.SrButtonStyle.Secondary)
}

@Composable
private fun ResultPage(state: PartyUiState, vm: PartyViewModel, onOpenEvent: (String) -> Unit, onOpenPlace: () -> Unit) {
    if (state.isSingleDevice) {
        val selected = state.selectedItem
        if (selected == null) {
            Text("Больше вариантов нет", style = SrType.Display, color = SrColors.Text)
            Text("Вы пролистали все варианты.", style = SrType.Body, color = SrColors.Muted)
        } else {
            Text("Ваш вариант", style = SrType.Display, color = SrColors.Text)
            SelectedPartyItem(selected)
        }
        SrButton(
            if (selected == null) "К фильтрам" else if (state.filters.feedType == PartyFeedType.EVENTS) "К мероприятию" else "К месту",
            onClick = when {
                selected == null -> vm::openFilters
                state.filters.feedType == PartyFeedType.EVENTS -> ({ onOpenEvent(selected.id) })
                else -> onOpenPlace
            },
            modifier = Modifier.fillMaxWidth(),
        )
        return
    }
    val room = state.session?.room ?: return
    val item = room.matchedItem
    if (item == null) {
        Text("Больше вариантов нет", style = SrType.Display, color = SrColors.Text)
        Text("Участники завершили ленту, но общий выбор не совпал.", style = SrType.Body, color = SrColors.Muted)
        SrButton("К фильтрам", vm::openFilters, Modifier.fillMaxWidth())
    } else {
        PartyItemCard(item)
        SrChip("Выбор большинства", selected = true)
        SrButton(
            if (state.filters.feedType == PartyFeedType.EVENTS) "К мероприятию" else "К месту",
            onClick = if (state.filters.feedType == PartyFeedType.EVENTS) ({ onOpenEvent(item.id) }) else onOpenPlace,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SelectedPartyItem(item: PartyItem) {
    Column(
        Modifier.fillMaxWidth().clip(SrShapes.Card).background(SrColors.Solid)
            .border(1.dp, SrColors.Border, SrShapes.Card).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier.fillMaxWidth().height(300.dp).clip(SrShapes.Control)
                .background(Brush.verticalGradient(listOf(Color(0xFF354878), Color(0xFFFFA36C), Color(0xFF182540)))),
            contentAlignment = Alignment.Center,
        ) {
            Text(item.location, style = SrType.Title, color = Color.White)
        }
        Text(item.title, style = SrType.Heading, color = SrColors.Text)
        Text("${item.category} · ${item.price} · ${item.location}", style = SrType.Label, color = SrColors.Text)
        Text(item.description, style = SrType.Body, color = SrColors.Muted)
    }
}

@Composable
private fun Avatar(name: String, size: androidx.compose.ui.unit.Dp = 40.dp) {
    Box(Modifier.size(size).clip(CircleShape).background(SrColors.Tint), contentAlignment = Alignment.Center) {
        Text(name.take(2).uppercase(), style = SrType.Label, color = SrColors.Text)
    }
}
