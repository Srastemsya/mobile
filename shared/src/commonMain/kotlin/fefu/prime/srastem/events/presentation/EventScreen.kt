package fefu.prime.srastem.events.presentation

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import fefu.prime.srastem.events.domain.EventAdmission
import fefu.prime.srastem.events.domain.EventCategory
import fefu.prime.srastem.events.domain.EventDraft
import fefu.prime.srastem.events.domain.EventParticipant
import fefu.prime.srastem.events.domain.SrEvent
import fefu.prime.srastem.map.presentation.MapScreen
import fefu.prime.srastem.platform.media.PlatformImagePicker
import ru.srastemsya.uikit.SrArtwork
import ru.srastemsya.uikit.SrButton
import ru.srastemsya.uikit.SrButtonStyle
import ru.srastemsya.uikit.SrCard
import ru.srastemsya.uikit.SrChip
import ru.srastemsya.uikit.SrColors
import ru.srastemsya.uikit.SrField
import ru.srastemsya.uikit.SrFieldKind
import ru.srastemsya.uikit.SrGlyph
import ru.srastemsya.uikit.SrIcon
import ru.srastemsya.uikit.SrIllustration
import ru.srastemsya.uikit.SrType
import ru.srastemsya.uikit.SrEventCover
import ru.srastemsya.uikit.SrPublishBar
import ru.srastemsya.uikit.SrPublishState

@Composable
fun EventScreen(
    viewModel: EventViewModel,
    modifier: Modifier = Modifier,
    onOpenMap: () -> Unit = {},
    onGoHome: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val event = state.events.firstOrNull { it.id == state.selectedEventId }
    Column(
        modifier.fillMaxSize().background(Color.White.copy(alpha = .66f))
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp)
            .padding(bottom = if (state.page == EventPage.LIST) 0.dp else 12.dp),
    ) {
        when (state.page) {
            EventPage.LIST -> EventListPage(state, viewModel)
            EventPage.DETAIL -> event?.let { EventDetailsPage(it, state, viewModel, onOpenMap, onGoHome) }
                ?: EventListPage(state, viewModel)

            EventPage.CREATE_LOCATION -> CreateLocationPage(viewModel)
            EventPage.CREATE_FORM -> CreateEventPage(state, viewModel)
            EventPage.CREATED -> EventCreatedPage(event, viewModel)
            EventPage.JOINED -> EventJoinedPage(event, viewModel)
            EventPage.ORGANIZER -> event?.let { OrganizerPage(it, state, viewModel) }
                ?: EventListPage(state, viewModel)

        }
    }
    if (state.error != null) {
        EventErrorMessage(state.error.orEmpty(), viewModel::clearError)
    }
    state.pendingParticipant?.let { participant ->
        ConfirmRemoveParticipant(
            participant,
            viewModel::cancelRemoveParticipant,
            viewModel::confirmRemoveParticipant
        )
    }
}

@Composable
private fun EventListPage(state: EventUiState, vm: EventViewModel) {
    var showCreateSoon by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().height(70.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("События", style = SrType.Display, color = SrColors.Text)
                Text("Владивосток", style = SrType.Caption, color = SrColors.Muted)
            }
            SrButton("+ Создать", { showCreateSoon = true }, Modifier.size(width = 112.dp, height = 48.dp))
        }
        Text(
            "Ближайшие события",
            Modifier.padding(top = 8.dp, bottom = 12.dp),
            style = SrType.Title,
            color = SrColors.Text
        )
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.events.filterNot { it.isPast }.forEach { event ->
                EventPosterCard(event, onClick = { vm.openEvent(event.id) })
            }
            if (state.events.isEmpty()) EmptyEvents()
        }
    }
    if (showCreateSoon) {
        AlertDialog(
            onDismissRequest = { showCreateSoon = false },
            title = { Text("Скоро") },
            text = { Text("Создание событий появится в одном из следующих обновлений.") },
            confirmButton = {
                TextButton(onClick = { showCreateSoon = false }) { Text("Понятно") }
            },
        )
    }
}

@Composable
private fun EventPosterCard(event: SrEvent, onClick: () -> Unit) {
    SrCard(Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick)) {
        EventHero(event, Modifier.fillMaxWidth())
        Row(
            Modifier.fillMaxWidth().padding(top = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(event.category.title, style = SrType.Caption, color = SrColors.Muted)
            Text(
                event.admission.title,
                style = SrType.Caption,
                color = if (event.admission == EventAdmission.BOOKING) SrColors.Primary else SrColors.Muted
            )
        }
        Text(
            event.title,
            Modifier.padding(top = 4.dp),
            style = SrType.Title,
            color = SrColors.Text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            event.startsAt + " · " + (if (event.priceRub == 0) "Бесплатно" else "≈ ${event.priceRub} ₽"),
            Modifier.padding(top = 4.dp), style = SrType.Label, color = SrColors.Text,
        )
        if (event.capacity != null) {
            Text(
                "Записалось ${event.confirmedCount} · осталось ${event.remainingPlaces}",
                Modifier.padding(top = 4.dp),
                style = SrType.Caption,
                color = SrColors.Muted
            )
        }
    }
}

@Composable
private fun EventHero(event: SrEvent, modifier: Modifier = Modifier) {
    SrEventCover(modifier) {
        if (event.coverUri != null) {
            AsyncImage(
                event.coverUri,
                contentDescription = event.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            SrArtwork(SrIllustration.EventThumbnail, Modifier.fillMaxSize(), event.title)
        }
    }
}

@Composable
private fun EventDetailsPage(
    event: SrEvent,
    state: EventUiState,
    vm: EventViewModel,
    onOpenMap: () -> Unit,
    onGoHome: () -> Unit,
) {
    val isOrganizer = event.organizerId == state.currentUserId
    val isParticipant = event.isAttending || event.participants.any { it.id == state.currentUserId }
    Column(Modifier.fillMaxSize()) {
        EventSubHeader("О событии", vm::back)
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(top = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            EventHero(event, Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SrChip(event.category.title)
                SrChip(
                    if (isOrganizer) "Ваше событие" else event.admission.title,
                    selected = isOrganizer
                )
            }
            Text(event.title, style = SrType.Display, color = SrColors.Text)
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White)
                    .border(1.dp, SrColors.Border, RoundedCornerShape(20.dp)).padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                EventMetadata(SrIcon.Events, event.startsAt)
                EventMetadata(SrIcon.Location, event.address)
            }
            Text("О событии", style = SrType.Title, color = SrColors.Text)
            Text(event.description, style = SrType.Body, color = SrColors.Text)
            Text(
                when {
                    event.capacity == null -> "Свободный вход · без ограничения мест"
                    else -> fefu.prime.srastem.events.domain.eventAttendanceSummary(
                        event.confirmedCount, event.capacity, event.remainingPlaces,
                    ).orEmpty()
                },
                style = SrType.Caption, color = SrColors.Muted,
            )
            if (isOrganizer) ParticipantSummary(event, vm)
            ActionRow("Показать на карте", event.address, onOpenMap)
            DisabledChatRow()
        }
        Spacer(Modifier.height(12.dp))
        when {
            isOrganizer -> SrButton(
                "Управлять событием",
                vm::openOrganizer,
                Modifier.fillMaxWidth()
            )

            isParticipant && event.canCancelRegistration -> SrButton(
                "Отменить запись",
                vm::leaveSelectedEvent,
                Modifier.fillMaxWidth(),
                style = SrButtonStyle.Secondary,
                enabled = !state.isBusy
            )

            isParticipant -> Text("Вы записаны", color = SrColors.Primary)

            event.admission == EventAdmission.FREE -> SrButton(
                "Показать на карте",
                onOpenMap,
                Modifier.fillMaxWidth()
            )

            else -> SrButton(
                if (event.admission == EventAdmission.BOOKING && event.priceRub > 0) "Записаться · ${event.priceRub} ₽" else "Записаться",
                vm::joinSelectedEvent,
                Modifier.fillMaxWidth(),
                enabled = !state.isBusy && event.canRegister && !event.isFull,
                loading = state.isBusy,
            )
        }
        if (state.returnHomeAfterSelection) {
            Spacer(Modifier.height(8.dp))
            SrButton("На главный экран", onGoHome, Modifier.fillMaxWidth(), style = SrButtonStyle.Secondary)
        }
    }
}

@Composable
private fun EventJoinedPage(event: SrEvent?, vm: EventViewModel) {
    Column(Modifier.fillMaxSize()) {
        EventSubHeader("Участие", vm::back)
        if (event != null) {
            Column(
                Modifier.padding(top = 40.dp).fillMaxWidth().clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.dp, SrColors.Border, RoundedCornerShape(24.dp)).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier.size(46.dp).clip(CircleShape).background(SrColors.Tint),
                    contentAlignment = Alignment.Center
                ) {
                    SrGlyph(SrIcon.Check, tint = SrColors.Primary)
                }
                Text("Вы записаны", style = SrType.Heading, color = SrColors.Text)
                Text(event.title, style = SrType.Title, color = SrColors.Text)
                Text(event.startsAt, style = SrType.Body, color = SrColors.Muted)
                Text(event.address, style = SrType.Body, color = SrColors.Muted)
                Text(
                    "Записались ${event.confirmedCount} · осталось ${event.remainingPlaces ?: "много"} мест",
                    style = SrType.Caption,
                    color = SrColors.Muted
                )
            }
            Spacer(Modifier.weight(1f))
            DisabledChatRow()
            Spacer(Modifier.height(12.dp))
            SrButton("В афишу", vm::back, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun CreateLocationPage(vm: EventViewModel) {
    Column(Modifier.fillMaxSize()) {
        EventSubHeader("Место события", vm::back)
        Box(
            Modifier.fillMaxWidth().weight(1f).padding(top = 14.dp).clip(RoundedCornerShape(24.dp))
        ) {
            MapScreen(
                modifier = Modifier.fillMaxSize(),
                onNavigate = {},
                locationPickerMode = true,
                onLocationSelected = { marker ->
                    vm.chooseLocation(
                        marker.title,
                        marker.location.latitude,
                        marker.location.longitude
                    )
                },
            )
        }
    }
}

@Composable
private fun CreateEventPage(state: EventUiState, vm: EventViewModel) {
    Column(Modifier.fillMaxSize()) {
        EventSubHeader("Новое событие", vm::back)
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PlatformImagePicker(
                onImageSelected = { uri -> if (uri != null) vm.updateDraft { it.copy(coverUri = uri) } },
            ) { openPicker ->
                SrEventCover(
                    modifier = Modifier.fillMaxWidth(),
                    onUpload = openPicker,
                    cover = if (state.draft.coverUri == null) null else {
                        {
                            AsyncImage(
                                state.draft.coverUri,
                                contentDescription = "Обложка события",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    },
                )
            }
            Row(
                Modifier.fillMaxWidth().height(62.dp).clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .clickable(role = Role.Button) { vm.back() }.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SrGlyph(SrIcon.Location, tint = SrColors.Primary)
                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                    Text(state.draft.address, style = SrType.Label, color = SrColors.Text)
                    Text("Изменить точку", style = SrType.Caption, color = SrColors.Muted)
                }
                SrGlyph(SrIcon.Next)
            }
            SrField(
                state.draft.title,
                { value -> vm.updateDraft { it.copy(title = value) } },
                "Название",
                placeholder = "Например, прогулка у моря"
            )
            CategoryPicker(state.draft, state.categories, vm)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SrField(
                    state.draft.startsAt.substringBefore(" · "),
                    { value ->
                        vm.updateDraft {
                            it.copy(
                                startsAt = "$value · ${
                                    it.startsAt.substringAfter(
                                        " · ",
                                        "16:00"
                                    )
                                }"
                            )
                        }
                    },
                    "Дата",
                    modifier = Modifier.weight(1f),
                    placeholder = "ДД.ММ.ГГГГ"
                )
                SrField(
                    state.draft.startsAt.substringAfter(" · ", "16:00"),
                    { value -> vm.updateDraft { it.copy(startsAt = "${it.startsAt.substringBefore(" · ")} · $value") } },
                    "Время",
                    modifier = Modifier.weight(1f),
                    placeholder = "16:00"
                )
            }
            SrField(
                state.draft.description,
                { value -> vm.updateDraft { it.copy(description = value) } },
                "Описание",
                kind = SrFieldKind.Multiline,
                placeholder = "Расскажите о событии"
            )
            Text("Участие", style = SrType.Label, color = SrColors.Text)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SrChip(
                    "Свободный вход",
                    selected = state.draft.admission == EventAdmission.FREE,
                    onClick = { vm.updateDraft { it.copy(admission = EventAdmission.FREE) } })
                SrChip(
                    "По записи",
                    selected = state.draft.admission == EventAdmission.BOOKING,
                    onClick = { vm.updateDraft { it.copy(admission = EventAdmission.BOOKING) } })
            }
            if (state.draft.admission == EventAdmission.BOOKING) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SrField(
                        state.draft.capacity,
                        { value -> vm.updateDraft { it.copy(capacity = value.filter(Char::isDigit)) } },
                        "Количество мест",
                        modifier = Modifier.weight(1f),
                        kind = SrFieldKind.Number
                    )
                    Text(
                        "Участие бесплатное",
                        Modifier.align(Alignment.CenterVertically).weight(1f),
                        style = SrType.Caption,
                        color = SrColors.Muted
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        SrPublishBar(
            state = when {
                state.isBusy -> SrPublishState.Submitting
                state.error != null -> SrPublishState.Error
                state.draft.title.isBlank() -> SrPublishState.Disabled
                else -> SrPublishState.Ready
            },
            onPublish = vm::publish,
            modifier = Modifier.fillMaxWidth(),
            errorMessage = state.error ?: "Не удалось опубликовать событие",
        )
    }
}

@Composable
private fun CategoryPicker(
    draft: EventDraft,
    categories: List<fefu.prime.srastem.events.domain.EventCatalogCategory>,
    vm: EventViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Категория", style = SrType.Label, color = SrColors.Text)
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val options = categories.ifEmpty {
                EventCategory.entries.map { category ->
                    fefu.prime.srastem.events.domain.EventCatalogCategory(
                        id = when (category) {
                            EventCategory.WALK, EventCategory.ACTIVE -> "outdoors"
                            EventCategory.GAMES, EventCategory.FOOD, EventCategory.OTHER -> "culture"
                        },
                        label = category.title,
                    )
                }.distinctBy { it.id }
            }
            options.forEach { category ->
                SrChip(
                    category.label,
                    selected = draft.categoryId == category.id,
                    onClick = {
                        vm.updateDraft {
                            it.copy(
                                categoryId = category.id,
                                category = when (category.id) {
                                    "outdoors" -> EventCategory.ACTIVE
                                    "food" -> EventCategory.FOOD
                                    "culture" -> EventCategory.OTHER
                                    else -> EventCategory.OTHER
                                },
                            )
                        }
                    })
            }
        }
    }
}

@Composable
private fun EventCreatedPage(event: SrEvent?, vm: EventViewModel) {
    Column(Modifier.fillMaxSize()) {
        EventSubHeader("Событие опубликовано", vm::back)
        Column(
            Modifier.fillMaxWidth().padding(top = 36.dp).clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .border(1.dp, SrColors.Border, RoundedCornerShape(24.dp)).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SrGlyph(SrIcon.Check, tint = SrColors.Primary, size = 28.dp)
            Text("Событие опубликовано", style = SrType.Heading, color = SrColors.Text)
            Text(event?.title.orEmpty(), style = SrType.Title, color = SrColors.Text)
            Text(event?.startsAt.orEmpty(), style = SrType.Body, color = SrColors.Muted)
            Text(event?.address.orEmpty(), style = SrType.Body, color = SrColors.Muted)
        }
        Spacer(Modifier.weight(1f))
        SrButton("Открыть событие", { event?.id?.let(vm::openEvent) }, Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        SrButton("В афишу", vm::back, Modifier.fillMaxWidth(), style = SrButtonStyle.Secondary)
    }
}

@Composable
private fun OrganizerPage(event: SrEvent, state: EventUiState, vm: EventViewModel) {
    Column(Modifier.fillMaxSize()) {
        EventSubHeader("Управление событием", vm::back)
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(top = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EventHero(event, Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)))
            Text(event.title, style = SrType.Heading, color = SrColors.Text)
            Text(
                "${event.confirmedCount} из ${event.capacity ?: "∞"} участников",
                style = SrType.Title,
                color = SrColors.Text
            )
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color.White)
                    .border(1.dp, SrColors.Border, RoundedCornerShape(22.dp)).padding(13.dp)
            ) {
                event.participants.forEach { participant ->
                    Row(
                        Modifier.fillMaxWidth().height(52.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(36.dp).clip(CircleShape).background(SrColors.Tint),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                participant.name.take(1),
                                style = SrType.Caption,
                                color = SrColors.Primary
                            )
                        }
                        Text(
                            participant.name,
                            Modifier.weight(1f).padding(start = 8.dp),
                            style = SrType.Label,
                            color = SrColors.Text
                        )
                        Text(
                            "Удалить",
                            Modifier.clip(RoundedCornerShape(10.dp))
                                .clickable(role = Role.Button) { vm.askRemoveParticipant(participant) }
                                .padding(8.dp),
                            style = SrType.Label,
                            color = SrColors.Error
                        )
                    }
                }
                if (event.participants.isEmpty()) Text(
                    "Пока никто не записался",
                    Modifier.padding(8.dp),
                    style = SrType.Body,
                    color = SrColors.Muted
                )
            }
            DisabledChatRow()
        }
        Spacer(Modifier.height(12.dp))
        if (event.canDelete) SrButton(
            "Удалить событие",
            vm::deleteSelectedEvent,
            Modifier.fillMaxWidth(),
            style = SrButtonStyle.Destructive,
            enabled = !state.isBusy
        )
    }
}

@Composable
private fun ParticipantSummary(event: SrEvent, vm: EventViewModel) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color.White)
            .border(1.dp, SrColors.Border, RoundedCornerShape(22.dp)).padding(13.dp)
    ) {
        Text(
            "Участники · ${event.confirmedCount} из ${event.capacity ?: "∞"}",
            style = SrType.Title,
            color = SrColors.Text
        )
        event.participants.take(2).forEach { participant ->
            Row(
                Modifier.fillMaxWidth().height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    participant.name,
                    Modifier.weight(1f),
                    style = SrType.Label,
                    color = SrColors.Text
                )
                Text(
                    "Удалить",
                    Modifier.clickable(role = Role.Button) { vm.askRemoveParticipant(participant) }
                        .padding(8.dp),
                    style = SrType.Caption,
                    color = SrColors.Error
                )
            }
        }
    }
}

@Composable
private fun EventMetadata(icon: SrIcon, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SrGlyph(icon, tint = SrColors.Muted)
        Text(text, style = SrType.Label, color = SrColors.Text)
    }
}

@Composable
private fun ActionRow(title: String, trailing: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(1.dp, SrColors.Border, RoundedCornerShape(18.dp))
            .clickable(role = Role.Button, onClick = onClick).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SrGlyph(SrIcon.Location, tint = SrColors.Primary)
        Text(title, Modifier.padding(start = 10.dp), style = SrType.Label, color = SrColors.Primary)
        Spacer(Modifier.weight(1f))
        Text(trailing, style = SrType.Caption, color = SrColors.Muted)
        SrGlyph(SrIcon.Next, tint = SrColors.Muted, size = 16.dp)
    }
}

@Composable
private fun DisabledChatRow() {
    Row(
        Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = .6f)).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EventChatGlyph()
        Text(
            "Чат событий появится позже",
            Modifier.padding(start = 10.dp),
            style = SrType.Caption,
            color = SrColors.Muted
        )
    }
}

@Composable
private fun EventChatGlyph() {
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
            SrColors.Muted,
            style = androidx.compose.ui.graphics.drawscope.Stroke(size.minDimension * .09f)
        )
    }
}

@Composable
private fun EventSubHeader(title: String, onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp).height(52.dp)
            .shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = SrColors.Shadow)
            .clip(RoundedCornerShape(18.dp)).background(Color.White)
            .border(1.dp, SrColors.Border, RoundedCornerShape(18.dp)).padding(horizontal = 8.dp),
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
private fun ConfirmRemoveParticipant(
    participant: EventParticipant,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onCancel) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color.White)
                .padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Удалить участника?", style = SrType.Heading, color = SrColors.Text)
            Text(
                "${participant.name} будет удалён из списка участников события.",
                style = SrType.Body,
                color = SrColors.Muted
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SrButton("Отмена", onCancel, Modifier.weight(1f), style = SrButtonStyle.Secondary)
                SrButton(
                    "Удалить",
                    onConfirm,
                    Modifier.weight(1f),
                    style = SrButtonStyle.Destructive
                )
            }
        }
    }
}

@Composable
private fun EventErrorMessage(message: String, onDismiss: () -> Unit) {
    androidx.compose.material3.Snackbar(
        Modifier.padding(horizontal = 16.dp).clickable(onClick = onDismiss),
        action = {
            Text(
                "Закрыть",
                Modifier.clickable(onClick = onDismiss),
                style = SrType.Caption,
                color = SrColors.OnAction
            )
        },
    ) { Text(message, style = SrType.Body, color = SrColors.OnAction) }
}

@Composable
private fun EmptyEvents() {
    Column(
        Modifier.fillMaxWidth().padding(top = 70.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Пока нет событий", style = SrType.Title, color = SrColors.Text)
        Text("Создайте первое событие в афише", style = SrType.Body, color = SrColors.Muted)
    }
}
