package fefu.prime.srastem.map.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import fefu.prime.srastem.map.domain.GeoPoint
import fefu.prime.srastem.map.domain.MapMarker
import fefu.prime.srastem.map.domain.MapMarkerIcon
import fefu.prime.srastem.map.domain.MapMarkerType
import fefu.prime.srastem.map.presentation.MapCatalogViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.runtime.snapshotFlow
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraUpdate
import org.maplibre.compose.map.DefaultMapRuntime
import org.maplibre.compose.map.MapRuntimeOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.resource.MapRequestInterceptor
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position
import ru.srastemsya.uikit.SrArtwork
import ru.srastemsya.uikit.SrButton
import ru.srastemsya.uikit.SrButtonStyle
import ru.srastemsya.uikit.SrChip
import ru.srastemsya.uikit.SrColors
import ru.srastemsya.uikit.SrGlyph
import ru.srastemsya.uikit.SrGlassMode
import ru.srastemsya.uikit.SrGlassSurface
import ru.srastemsya.uikit.SrIcon
import ru.srastemsya.uikit.SrIconButton
import ru.srastemsya.uikit.SrIllustration
import ru.srastemsya.uikit.SrShapes
import ru.srastemsya.uikit.SrType
import fefu.prime.srastem.BottomNavigationBar
import fefu.prime.srastem.Destination

private const val OPENFREEMAP_STYLE_URL = "https://tiles.openfreemap.org/styles/positron"

/** Configures a descriptive User-Agent before the first map is created. */
private object OSMMapRuntimeSetup {
    val configured: Unit by lazy {
        DefaultMapRuntime.configure(
            MapRuntimeOptions {
                requestInterceptor = object : MapRequestInterceptor {
                    override fun headers(request: org.maplibre.compose.resource.MapResourceRequest): Map<String, String> =
                        if (request.url.startsWith("https://tiles.openfreemap.org/")) {
                            mapOf("User-Agent" to "Srastem/1.0 (Compose Multiplatform app)")
                        } else {
                            emptyMap()
                        }
                }
            },
        )
    }
}

enum class MapCategory(val label: String) {
    Events("События"),
    Places("Заведения"),
    Activities("Активности"),
}

private val previewMarkers = listOf(
    MapMarker(
        id = "sup-tobizina",
        location = GeoPoint(latitude = 42.9923, longitude = 131.8465),
        type = MapMarkerType.Event,
        title = "Сапы на Тобизина",
        subtitle = "Сб, 14 ноя · 10:00",
        detail = "Записалось 4 · осталось 4 места",
        price = "≈ 2 500 ₽",
        icon = MapMarkerIcon.Activity,
    ),
    MapMarker(
        id = "coffee-center",
        location = GeoPoint(latitude = 43.1161, longitude = 131.8867),
        type = MapMarkerType.Place,
        title = "Кофейня у моря",
        subtitle = "Открыто · центр города",
        detail = "Кофейня и место для встреч",
        price = "Средний чек 450 ₽",
        icon = MapMarkerIcon.Place,
    ),
    MapMarker(
        id = "walk-center",
        location = GeoPoint(latitude = 43.1107, longitude = 131.8992),
        type = MapMarkerType.Activity,
        title = "Прогулка по набережной",
        subtitle = "Сегодня · 18:30",
        detail = "Небольшая компания · 3 участника",
        price = "Бесплатно",
        icon = MapMarkerIcon.Activity,
    ),
    MapMarker(
        id = "brunch",
        location = GeoPoint(latitude = 43.1232, longitude = 131.8990),
        type = MapMarkerType.Event,
        title = "Завтрак вместе",
        subtitle = "Вс, 15 ноя · 11:00",
        detail = "Записалось 2 · осталось 6 мест",
        price = "≈ 900 ₽",
        icon = MapMarkerIcon.Event,
    ),
    MapMarker(
        id = "quiz-vladivostok",
        location = GeoPoint(latitude = 43.1054, longitude = 131.8737),
        type = MapMarkerType.Event,
        title = "Квиз: Владивосток в мелочах",
        subtitle = "Пт, 13 ноя · 19:00",
        detail = "Записалось 6 · осталось 2 места",
        price = "≈ 700 ₽",
        icon = MapMarkerIcon.Quiz,
    ),
    MapMarker(
        id = "millionka",
        location = GeoPoint(latitude = 43.1152, longitude = 131.8858),
        type = MapMarkerType.Place,
        title = "Бар «Миллионка»",
        subtitle = "Светланская, 13",
        detail = "Бар и место для встреч",
        price = "Средний чек 1 500 ₽",
        icon = MapMarkerIcon.Place,
    ),
    MapMarker(
        id = "cape-tobizina",
        location = GeoPoint(latitude = 42.9929, longitude = 131.8428),
        type = MapMarkerType.Place,
        title = "Мыс Тобизина",
        subtitle = "Остров Русский",
        detail = "Живописная прогулочная локация",
        price = "Бесплатно",
        icon = MapMarkerIcon.Place,
    ),
    MapMarker(
        id = "lighthouse-walk",
        location = GeoPoint(latitude = 42.9979, longitude = 131.8654),
        type = MapMarkerType.Activity,
        title = "Активный отдых у моря",
        subtitle = "Остров Русский",
        detail = "Прогулка небольшой компанией",
        price = "Бесплатно",
        icon = MapMarkerIcon.Activity,
    ),
)

private val cityCenter = Position(latitude = 43.1155, longitude = 131.8855)
private val initialMapCenter = Position(latitude = 43.065, longitude = 131.875)
private val defaultFilters = MapFilterSelection(onlyFreeSpots = true)

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    onNavigate: (Destination) -> Unit = {},
    onSaveEvent: (MapMarker) -> Unit = {},
    locationPickerMode: Boolean = false,
    onLocationSelected: (MapMarker) -> Unit = {},
    catalogViewModel: MapCatalogViewModel? = null,
    onOpenEvent: (String) -> Unit = {},
) {
    val catalogState by (catalogViewModel?.state ?: remember { kotlinx.coroutines.flow.MutableStateFlow(MapCatalogUiState()) }).collectAsState()
    val allMarkers = remember(catalogState.eventMarkers) {
        val eventIds = catalogState.eventMarkers.mapTo(mutableSetOf()) { it.id }
        previewMarkers.filterNot { it.id in eventIds } + catalogState.eventMarkers
    }
    OSMMapRuntimeSetup.configured
    val mapState = rememberMapState(
        baseStyle = BaseStyle.Uri(OPENFREEMAP_STYLE_URL),
        initialCameraPosition = CameraPosition(center = initialMapCenter, zoom = 10.6),
    )
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val snapshotter = remember {
        if (supportsMapGlassSnapshots()) {
            DefaultMapRuntime.instance.createSnapshotter(BaseStyle.Uri(OPENFREEMAP_STYLE_URL))
        } else {
            null
        }
    }
    var mapViewport by remember { mutableStateOf(IntSize.Zero) }
    var glassSnapshot by remember { mutableStateOf<ImageBitmap?>(null) }
    var snapshotViewport by remember { mutableStateOf(IntSize.Zero) }
    DisposableEffect(snapshotter) {
        onDispose {
            snapshotter?.close()
        }
    }
    LaunchedEffect(mapViewport) {
        if (mapViewport != snapshotViewport) {
            glassSnapshot = null
            snapshotViewport = mapViewport
        }
    }
    var selectedCategory by remember { mutableStateOf(MapCategory.Events) }
    var selectedMarkerId by remember { mutableStateOf<String?>(null) }
    var searchOpen by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var filtersExpanded by remember { mutableStateOf(false) }
    var filterSelection by remember { mutableStateOf(defaultFilters) }
    var appliedFilters by remember { mutableStateOf<MapFilterSelection?>(null) }

    val visibleMarkers = remember(selectedCategory, appliedFilters, allMarkers) {
        allMarkers.filter { marker ->
            categoryMatches(marker, selectedCategory) && filterMatches(marker, appliedFilters)
        }
    }
    val selectedMarker = visibleMarkers.firstOrNull { it.id == selectedMarkerId }
    val searchResults = remember(searchText, allMarkers) {
        if (searchText.isBlank()) emptyList()
        else allMarkers.filter { marker ->
            marker.title.contains(searchText, ignoreCase = true) ||
                    marker.subtitle.contains(searchText, ignoreCase = true) ||
                    (searchText.contains(
                        "квиз",
                        ignoreCase = true
                    ) && marker.icon == MapMarkerIcon.Quiz)
        }
    }

    LaunchedEffect(selectedCategory, appliedFilters) {
        if (selectedMarkerId !in visibleMarkers.map { it.id }) selectedMarkerId = null
    }

    LaunchedEffect(snapshotter, mapState, mapViewport, density, layoutDirection, searchOpen) {
        val activeSnapshotter = snapshotter
        if (activeSnapshotter == null || searchOpen || mapViewport.width <= 0 || mapViewport.height <= 0) {
            glassSnapshot = null
            return@LaunchedEffect
        }
        snapshotFlow { mapState.cameraPosition }
            .distinctUntilChanged()
            .collectLatest { camera ->
                // Keep the last valid image visible while the camera moves. The Compose
                // renderer doesn't expose a readable TextureView frame, so the snapshotter
                // refreshes after the camera settles instead of blanking the glass meanwhile.
                delay(120)
                val logicalSize = with(density) {
                    DpSize(mapViewport.width.toDp(), mapViewport.height.toDp())
                }

                suspend fun captureAt(scale: Float): Boolean {
                    val captureSize = DpSize(
                        width = (logicalSize.width / scale).coerceAtLeast(1.dp),
                        height = (logicalSize.height / scale).coerceAtLeast(1.dp),
                    )
                    return try {
                        val image = activeSnapshotter.capture(captureSize) {
                            cameraPosition = camera
                            this.density = density
                            this.layoutDirection = layoutDirection
                        }
                        glassSnapshot = image
                        true
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        // Retain the previous frame if a transient tile or render error occurs.
                        false
                    }
                }

                val captured = captureAt(scale = 3f)
                if (captured) {
                    delay(240)
                    captureAt(scale = 2f)
                } else if (mapState.cameraPosition == camera) {
                    delay(900)
                    if (mapState.cameraPosition == camera) captureAt(scale = 3f)
                }
            }
    }

    fun dismissSearch() {
        searchOpen = false
        searchText = ""
    }

    fun showSearchResult(marker: MapMarker) {
        showMarker(
            marker,
            ::dismissSearch,
            { selectedMarkerId = it },
            { selectedCategory = it },
            mapState,
            scope
        )
    }

    Box(modifier = modifier.background(MapDesign.ScreenGradient)) {
        if (searchOpen) {
            MapSearchScreen(
                query = searchText,
                onQueryChange = { searchText = it },
                results = searchResults,
                onBack = {
                    if (searchText.isNotEmpty()) searchText = "" else searchOpen = false
                },
                onChooseQuery = { searchText = it },
                onSelectResult = ::showSearchResult,
                onShowOnMap = {
                    searchResults.firstOrNull()?.let(::showSearchResult)
                },
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars),
            ) {
                MapHeader(
                    onSearchClick = {
                        searchText = ""
                        searchOpen = true
                    },
                    onFiltersClick = { filtersExpanded = true },
                )
                CategoryTabs(
                    selected = selectedCategory,
                    onSelected = {
                        selectedCategory = it
                        selectedMarkerId = null
                    },
                )

                MapGlassHost(
                    snapshot = glassSnapshot,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                        .onSizeChanged { mapViewport = it },
                    map = {
                        MaplibreMap(
                            state = mapState,
                            modifier = Modifier.fillMaxSize(),
                            uiOptions = mapUiOptions(),
                        ) {
                            visibleMarkers.forEach { marker ->
                                MapPin(
                                    marker = marker,
                                    selected = marker.id == selectedMarkerId,
                                    modifier = Modifier.placedAt(
                                        Position(
                                            latitude = marker.location.latitude,
                                            longitude = marker.location.longitude
                                        ),
                                        alignment = Alignment.BottomCenter,
                                    ),
                                    onClick = { selectedMarkerId = marker.id },
                                )
                            }
                            UserLocationPin(
                                modifier = Modifier.placedAt(
                                    cityCenter,
                                    alignment = Alignment.Center
                                )
                            )
                        }
                    },
                    controls = {
                        MapControls(
                            modifier = Modifier.align(Alignment.CenterEnd).offset(y = (-22).dp)
                                .padding(end = 16.dp),
                            onZoomIn = {
                                scope.launch { mapState.animateCamera(CameraUpdate(zoom = mapState.cameraPosition.zoom + 1.0)) }
                            },
                            onZoomOut = {
                                scope.launch { mapState.animateCamera(CameraUpdate(zoom = mapState.cameraPosition.zoom - 1.0)) }
                            },
                            onLocate = {
                                scope.launch {
                                    mapState.animateCamera(
                                        CameraUpdate(
                                            center = cityCenter,
                                            zoom = 12.4
                                        )
                                    )
                                }
                            },
                        )

                        MapAttribution(
                            modifier = Modifier.align(Alignment.BottomStart)
                                .padding(start = 12.dp, bottom = 8.dp),
                        )

                        selectedMarker?.let { marker ->
                            SelectedMarkerCard(
                                marker = marker,
                                onSave = { onSaveEvent(marker) },
                                onOpenDetails = { if (marker.type == MapMarkerType.Event) onOpenEvent(marker.id) },
                                locationPickerMode = locationPickerMode,
                                onChooseLocation = { onLocationSelected(marker) },
                                modifier = Modifier.align(Alignment.BottomCenter)
                                    .padding(start = 12.dp, end = 12.dp, bottom = 118.dp),
                            )
                        }

                        if (filtersExpanded) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF242640).copy(alpha = 0.13f))
                                    .clickable { filtersExpanded = false },
                            )
                            MapFilterSheet(
                                selection = filterSelection,
                                onSelectionChange = { filterSelection = it },
                                onApply = {
                                    appliedFilters = filterSelection
                                    selectedMarkerId = null
                                    filtersExpanded = false
                                },
                                onReset = {
                                    filterSelection = defaultFilters
                                    appliedFilters = null
                                    selectedMarkerId = null
                                },
                                onClose = { filtersExpanded = false },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .padding(start = 8.dp, end = 8.dp, bottom = 94.dp),
                            )
                        }

                        if (!locationPickerMode) {
                            BottomNavigationBar(
                                selectedDestination = Destination.MapScreen,
                                modifier = Modifier.align(Alignment.BottomCenter),
                                onNavigate = onNavigate,
                            )
                        }
                    },
                )
            }
        }
    }

}

private fun showMarker(
    marker: MapMarker,
    dismissSearch: () -> Unit,
    selectMarker: (String) -> Unit,
    selectCategory: (MapCategory) -> Unit,
    mapState: org.maplibre.compose.map.MapState,
    scope: kotlinx.coroutines.CoroutineScope,
) {
    selectMarker(marker.id)
    selectCategory(categoryFor(marker))
    dismissSearch()
    scope.launch {
        mapState.animateCamera(
            CameraUpdate(
                center = Position(
                    latitude = marker.location.latitude,
                    longitude = marker.location.longitude
                ),
                zoom = 13.0,
            ),
        )
    }
}

private fun categoryFor(marker: MapMarker) = when (marker.type) {
    MapMarkerType.Event -> MapCategory.Events
    MapMarkerType.Place -> MapCategory.Places
    MapMarkerType.Activity -> MapCategory.Activities
}

private fun categoryMatches(marker: MapMarker, category: MapCategory): Boolean = when (category) {
    MapCategory.Events -> marker.type == MapMarkerType.Event
    MapCategory.Places -> marker.type == MapMarkerType.Place
    MapCategory.Activities -> marker.type == MapMarkerType.Activity
}

private fun filterMatches(marker: MapMarker, filter: MapFilterSelection?): Boolean {
    if (filter == null) return true
    if (filter.onlyFreeSpots && !marker.detail.contains("осталось", ignoreCase = true)) return false
    if (filter.category == "Квизы" && marker.icon != MapMarkerIcon.Quiz) return false
    if (filter.category == "Бары" && !marker.title.contains("бар", ignoreCase = true)) return false
    if (filter.category == "Активности" && marker.type != MapMarkerType.Activity) return false
    val budgetLimit = when (filter.budget) {
        "До 1 000 ₽" -> 1_000
        "До 3 000 ₽" -> 3_000
        else -> null
    }
    if (budgetLimit != null) {
        val price = marker.price.filter(Char::isDigit).toIntOrNull()
        if (price != null && price > budgetLimit) return false
    }
    return true
}

@Composable
private fun MapHeader(
    onSearchClick: () -> Unit,
    onFiltersClick: () -> Unit,
) {
    SrGlassSurface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(52.dp),
        shape = SrShapes.Nav,
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Карта",
                modifier = Modifier.weight(1f),
                style = SrType.Display.copy(fontSize = 27.sp, lineHeight = 34.sp),
                color = SrColors.Text,
                maxLines = 1
            )
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                MapHeaderIconButton(SrIcon.Search, "Поиск", onSearchClick)
                MapHeaderIconButton(SrIcon.Filter, "Фильтры", onFiltersClick)
            }
        }
    }
}

@Composable
private fun MapHeaderIconButton(icon: SrIcon, description: String, onClick: () -> Unit) {
    SrGlassSurface(
        modifier = Modifier.size(42.dp).clickable(role = Role.Button, onClick = onClick),
        shape = SrShapes.Control,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            SrGlyph(
                icon,
                description = description,
                tint = if (icon == SrIcon.Filter) SrColors.Primary else SrColors.Muted,
                size = 21.dp,
            )
        }
    }
}

@Composable
private fun CategoryTabs(selected: MapCategory, onSelected: (MapCategory) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MapCategory.entries.forEach { category ->
            val isSelected = selected == category
            val shape = RoundedCornerShape(17.dp)
            Box(
                modifier = Modifier.weight(1f).height(34.dp)
                    .then(
                        if (isSelected) {
                            Modifier.shadow(
                                4.dp,
                                shape,
                                ambientColor = SrColors.Primary.copy(alpha = .22f),
                                spotColor = SrColors.Primary.copy(alpha = .22f),
                            )
                        } else {
                            Modifier.shadow(6.dp, shape, ambientColor = SrColors.Shadow, spotColor = SrColors.Shadow)
                        },
                    )
                    .clip(shape)
                    .background(if (isSelected) SrColors.Primary else SrColors.Tint)
                    .then(if (isSelected) Modifier else Modifier.border(1.dp, Color.White, shape))
                    .selectable(isSelected, role = Role.Tab) { onSelected(category) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    category.label,
                    style = SrType.Caption.copy(fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.Bold),
                    color = if (isSelected) SrColors.OnAction else SrColors.Text,
                )
            }
        }
    }
}

@Composable
private fun MapPin(
    marker: MapMarker,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val type = when (marker.icon) {
        MapMarkerIcon.Activity -> MapPinType.Activity
        MapMarkerIcon.Place -> MapPinType.Place
        MapMarkerIcon.Event -> MapPinType.Event
        MapMarkerIcon.Quiz -> MapPinType.Quiz
    }
    Box(
        modifier = modifier.size(width = 54.dp, height = 64.dp)
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        SrMapPin(
            type = type,
            selected = selected,
            modifier = Modifier.fillMaxSize(),
            description = marker.title,
        )
    }
}

@Composable
private fun UserLocationPin(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(17.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(SrColors.Primary)
            .border(2.dp, Color.White, RoundedCornerShape(9.dp)),
    )
}

@Composable
private fun MapControls(
    modifier: Modifier = Modifier,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onLocate: () -> Unit,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MapActionIconButton(SrIcon.ZoomIn, "Увеличить масштаб", onZoomIn)
        MapActionIconButton(SrIcon.ZoomOut, "Уменьшить масштаб", onZoomOut)
        MapActionIconButton(SrIcon.Location, "Моё местоположение", onLocate)
    }
}

@Composable
private fun MapActionIconButton(icon: SrIcon, description: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier.size(42.dp)
            .shadow(8.dp, shape, ambientColor = SrColors.Shadow, spotColor = SrColors.Shadow)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, Color.White, shape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        SrGlyph(
            icon,
            description = description,
            tint = if (icon == SrIcon.Filter || icon == SrIcon.Location) SrColors.Primary else SrColors.Muted,
            size = 21.dp,
        )
    }
}

@Composable
private fun MapAttribution(modifier: Modifier = Modifier) {
    SrGlassSurface(modifier = modifier, shape = SrShapes.Small, mode = SrGlassMode.Liquid) {
        Column(Modifier.padding(horizontal = 7.dp, vertical = 4.dp)) {
            Text("© OpenStreetMap contributors", style = SrType.Caption, color = SrColors.Muted)
            Text("© OpenFreeMap · © OpenMapTiles", style = SrType.Caption, color = SrColors.Muted)
        }
    }
}

@Composable
private fun SelectedMarkerCard(
    marker: MapMarker,
    modifier: Modifier = Modifier,
    onSave: () -> Unit,
    onOpenDetails: () -> Unit,
    locationPickerMode: Boolean,
    onChooseLocation: () -> Unit,
) {
    SrGlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = SrShapes.Card,
        mode = SrGlassMode.Liquid
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(136.dp).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SrArtwork(
                SrIllustration.EventThumbnail,
                modifier = Modifier.size(width = 76.dp, height = 104.dp).clip(SrShapes.Small),
                description = "Иллюстрация события ${marker.title}",
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    marker.title, style = SrType.Title, color = SrColors.Text,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SrGlyph(SrIcon.Events, size = 14.dp)
                    Spacer(Modifier.size(4.dp))
                    Text(
                        marker.subtitle, style = SrType.Caption, color = SrColors.Text,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    marker.detail, style = SrType.Caption, color = SrColors.Muted,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SrChip(label = marker.price)
                    SrChip(
                        label = marker.detail.substringAfter("Записалось ", "2")
                            .substringBefore(" ") + " / 8"
                    )
                }
            }
            if (locationPickerMode) {
                SrButton("Выбрать", onChooseLocation, style = SrButtonStyle.Secondary)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (marker.type == MapMarkerType.Event) {
                        SrIconButton(SrIcon.Next, "Открыть событие", onOpenDetails, modifier = Modifier.size(40.dp))
                    }
                    SrIconButton(SrIcon.Save, "Сохранить событие", onSave, modifier = Modifier.size(40.dp))
                }
            }
        }
    }
}
