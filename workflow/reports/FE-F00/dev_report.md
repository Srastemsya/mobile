# FE-F00 · Отчёт Developer

## Идентичность ревизии и сборки

- Ветка: `fe-f00`, базовая ревизия `dev @ 3447a081ccd672a6e09790634b0984a57fa4a923` (HEAD не изменялся: коммиты и push делегированы, работа в рабочем дереве).
- Состояние дерева на момент отчёта: изменённые/новые файлы перечислены ниже; `workflow/reports/FE-F00/acceptance.md` и `test_report.md` не тронуты.
- Сборки и прогоны выполнены на этой ревизии рабочего дерева; метки времени — в файлах доказательств.

## Изменения

Новые файлы (фича `feed`, слои domain/data/presentation + документация + тесты):

- `shared/src/commonMain/kotlin/fefu/prime/srastem/feed/domain/`: `FeedKind`, `FeedFilters`, `FeedCard`, `FeedRef`, `FeedPage`, `SwipeDecision`, `RoomSnapshot`, `RoomPhase`, `RoomMember`, `RoomInvite` (в `RoomRepository.kt`), `FeedError`, интерфейсы `FeedRepository`/`RoomRepository`, use cases по одному на операцию: `GetFeedCandidatesUseCase`, `CreateRoomUseCase`, `CreateInviteUseCase`, `JoinRoomUseCase`, `StartRoomUseCase`, `SubmitVoteUseCase`, `CompleteRoundUseCase`, `ObserveRoomUseCase`.
- `shared/src/commonMain/kotlin/fefu/prime/srastem/feed/data/`: `BackendFeedRepository`, `BackendRoomRepository` (через общий `BackendApi`), `FeedMappers` (DTO → домен, коды ошибок), `FeedContract` (единая форма query `candidates` и тела `start`).
- `shared/src/commonMain/kotlin/fefu/prime/srastem/feed/presentation/`: `FeedModeViewModel`, `FeedModeState`, `FeedModeEvent`, `FeedModeScreen` (экран «Выбираем вместе» по фрейму 280-10679 и последующий шаг `FeedStepScreen` без таббара с «Назад» в `SrTopBar`).
- `shared/src/commonMain/kotlin/fefu/prime/srastem/feed/FEATURE.md` — документация фичи по шаблону `workflow/rules/feature-docs.md`.
- `shared/src/commonTest/kotlin/fefu/prime/srastem/feed/{domain,data,presentation}/` — unit-тесты use cases, мапперов и ViewModel зеркально слоям.

Изменённые файлы:

- Рефакторинг `party` под общую модель (AC-08): `party/domain/PartyModels.kt` (алиас `PartyVote = SwipeDecision`, граничные отображения `PartyFeedType↔FeedKind`, `PartyFilters.toFeedFilters(partySize)`), `party/data/BackendPartyRepository.kt` (больше не парсит контракт сам: query/тела из `FeedContract`, DTO через `FeedMappers`, проекции комнаты из `RoomSnapshot`; deeplink-поведение `acceptInvite` сохранено).
- Навигация: `NavigationMain.kt` (вкладка «Лента» → `FeedModeScreen`; таббар только на экране выбора режима; маршрутизация `CompleteProfileSelected`), `BottomNavigationBar.kt` (в таббаре появилась вкладка «Лента», `FeedScreen → SrTab.Feed`), `MainViewModel.kt` (`navigateToFeedWithEventsKind` для входа с Главной), `MainActivity.kt`/`MainViewController.kt` (deeplink приглашения открывает `Destination.RoomScreen`).
- Доступ с завершённым профилем (AC-06): `registration/presentation/RegistrationViewModel.kt` (`openProfileCompletion`), `di/AppContainer.kt` (`createFeedModeViewModel` с проверкой `GET /v1/me profileComplete`, use cases ленты как привязки DI, тестовые швы `configuredFeedRepository`/`configuredRoomRepository`).
- `docs/frontend-implementation.md` — обновлён раздел привязок DI/репозиториев ленты.
- `gradle/libs.versions.toml`, `shared/build.gradle.kts` — зависимости commonTest (`kotlinx-coroutines-test`), исправлено случайное дублирование библиотек в каталоге версий.

## Соответствие критериям приёмки

| AC | Статус | Доказательство |
|---|---|---|
| AC-01 | готово | `BottomNavigationBar` включает `SrTab.Feed` → `Destination.FeedScreen` → `FeedModeScreen`; unit-тесты ViewModel (`FeedModeViewModelTest.tabEntryResetsPendingKindToPlaces`) |
| AC-02 | готово | `FeedModeScreen`: заголовок «Выбираем вместе», подзаголовок и тексты карточек один в один из фрейма 280-10679; иконки `SrIcon.Profile` (`Icon/user`) и `SrIcon.People` (`Icon/users`); стекло `SrColors.Glass/GlassEdge` + `SrShapes.Card`, padding 16/gap 8 |
| AC-03 | готово | Карточки открывают точки входа FE-F01/FE-F04 (явные заглушки `FeedStepScreen`); `showBottomNavigation` скрывает таббар при `stub != null`; «Назад» в `SrTopBar` (`FeedModeEvent.DismissNotice`) |
| AC-04 | готово | «Назад» со шага возвращает на экран выбора без сброса `pendingKind`/состояния вкладки (destination-based навигация, состояние вне экрана); тест `modeCardsOpenDocumentedStubsAndBackReturnsToChoice` |
| AC-05 | готово | `HomeScreen.onOpenFeed` → `MainViewModel.navigateToFeedWithEventsKind` → `OpenedWithEventsKind` → `pendingKind = EVENT`, проброс в FE-F01 через `FeedModeState.pendingKind` (текст заглушки фиксирует kind); тесты `homeSwipeEntryPreselectsEventsKind`, `modeCardsOpenDocumentedStubsAndBackReturnsToChoice` |
| AC-06 | готово | Проверка профиля при входе (`profileGate` → `GetRegistrationCompleted`), `FeedError.ProfileIncomplete` из `403 PROFILE_INCOMPLETE` (`FeedMappers.errorOf`) ведёт на `RegistrationViewModel.openProfileCompletion`; тесты `incompleteProfileBlocksModeCards`, `profileIncompleteErrorFromScenariosOpensCompletionGate`, `unavailableGateFailsOpenAndReactive403StillProtects` |
| AC-07 | готово | Экран собран только из `:uikit` (`SrTopBar`, `SrTabBar`, `SrButton`, `SrGlyph`, токены `Theme.kt`); новых цветов/радиусов нет: градиент Главной из существующих токенов `Base/Tint/GlassIris` (`FeedDesign.ScreenGradient`), стекло с fallback `color/surface/solid` через панели и platform-actuals `Glass.*` |
| AC-08 | готово | Модель в `feed/domain` как рефакторинг `party` (алиасы/отображения, общий парсинг в `feed/data`); все 8 use cases по одному на операцию; реализации в data через `BackendApi`; unit-тесты `FeedUseCasesTest`, `FeedMappersTest` |
| AC-09 | готово | `feed/FEATURE.md` по обязательным секциям; тесты в `commonTest` зеркально слоям; `docs/frontend-implementation.md` обновлён |
| AC-10 | готово | Android: `:androidApp:assembleDebug` успешен; iOS: `:shared:compileKotlinIosArm64/IosSimulatorArm64` + полный `xcodebuild` схемы `iosApp` (simulator) успешны; unit-тесты прошли на Android host JVM и iOS Simulator ARM64 |

## Команды и результаты (фактические прогоны)

Доказательства с метками времени (не редактировались вручную):

- `tests/reports/20261113-144852-shared.md` — suite shared: exit 0, passed 38, failed 0, skipped 0 (Android host JVM 19 + iOS Simulator ARM64 19). Более ранние прогоны того же suite: `20261113-144412`, `20261113-144527`, `20261113-144554`, `20261113-144724`, `20261113-144756` (два последних — до финальных правок, один с exit 1 из-за сбоя инфраструктуры линковки, повторён).
- `workflow/reports/FE-F00/evidence/20261113-144602-shared-tests.log` — `./gradlew :shared:allTests --rerun-tasks`: BUILD SUCCESSFUL.
- `workflow/reports/FE-F00/evidence/20261113-144635-android-build.log` — `./gradlew :androidApp:assembleDebug`: BUILD SUCCESSFUL, APK `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.
- `workflow/reports/FE-F00/evidence/20261113-144645-ios-build.log` — klib-цели iOS + `xcodebuild -project iosApp.xcodeproj -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 17' -configuration Debug build`: ** BUILD SUCCEEDED **.
- `workflow/reports/FE-F00/evidence/20261113-144922-final-verification.log` — финальная сверка всех трёх команд на итоговом дереве: все BUILD SUCCESSFUL.

Окружение: Java 17 (JBR 17.0.14), Gradle 9.5.1, Kotlin 2.4.21, AGP 9.1.1, Xcode 27.2 (27B52), симулятор iOS 27.2.

## Соответствие правилам

- `workflow/rules/mobile-architecture.md`: слои domain/data/presentation внутри фичи; DI через `di/AppContainer` (use cases — поля контейнера, ViewModel — фабрики); транспортные DTO не выходят из data; ошибки домена (`FeedError`) вместо HTTP-статусов в presentation.
- `workflow/rules/design.md` и `docs/design/ui-kit.md`: только токены и компоненты `:uikit`; градиент собран из существующих токенов палитры; стекло с читаемой подложкой `color/surface/solid` при ограниченной поддержке.
- `workflow/rules/feature-docs.md`: `FEATURE.md` создан вместе с фичей, секции и ссылки актуальны.
- `workflow/rules/testing.md`: suite `tests/shared/run.sh` пишет уникальные отчёты с метками времени; отчёты не редактировались; агрегатор `tests/run-all.sh` не подменяет suite-отчёты.

## Ограничения и замечания

1. FE-F01/FE-F04 не входят в пакет: точки входа — явные документированные заглушки (`FeedModeStub`/`FeedStepScreen`) с названием пакета и возвратом; молчаливых обрывов нет. Убраны будут по мере реализации пакетов.
2. Серверное поведение (курсоры, majority, тайм-ауты комнат) не реализуется и не проверяется клиентом; интеграция с стендом BE-D03 — объём следующих пакетов/приёмки. Ручная проверка UI выполнена против контрактов; dev-стенд во время разработки не поднимался (доступность стенда фиксирует Tester).
3. Проверка профиля при входе fail-open при сетевой ошибке (офлайн не должен блокировать UI); авторитетная защита — серверный `403 PROFILE_INCOMPLETE`, обработанный и протестированный.
4. Унаследованный UI комнаты (`party/presentation`) остаётся до переноса сценариев FE-F02…FE-F06; он уже работает через общую модель и общий парсинг контракта.
5. Предупреждение компилятора о deprecated `LocalClipboardManager` в `PartyScreen.kt` унаследовано от базовой ревизии и вне объёма FE-F00.

## Публичные документы

- Спецификация пакета: `docs/specs/feed-v1/implementation.md` (раздел FE-F00), очередь `docs/specs/feed-v1/README.md`, приёмка `docs/specs/feed-v1/acceptance.md`.
- Контракты: `docs/contracts/discovery-v1.md`, `docs/contracts/mobile-api.md`.
- Дизайн: `docs/design/feed.md`, `docs/design/ui-kit.md`; фрейм выбора режима 280-10679.
- Интеграция и стенд: `docs/backend-integration.md`, `docs/frontend-implementation.md`.
- Документация фичи: `shared/src/commonMain/kotlin/fefu/prime/srastem/feed/FEATURE.md`.
