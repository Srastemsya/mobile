# Лента · вход и выбор режима (FE-F00)

## Краткое описание

Фича `feed` — общая доменная модель и точка входа сценариев подборки мест и событий для компании. Пакет FE-F00 даёт вкладку «Лента» с экраном «Выбираем вместе» (две карточки режимов), вход «Свайпать события» с Главной с предвыбранным `kind=EVENT` и доступ только с завершённым профилем. Спецификация: [docs/specs/feed-v1/implementation.md](../../../../../../../../docs/specs/feed-v1/implementation.md#fe-f00--вход-и-выбор-режима), дизайн: [docs/design/feed.md](../../../../../../../../docs/design/feed.md), фрейм выбора режима: [280-10679](https://www.figma.com/design/khewsPf7b4JVyBwJaCmO43/startup_design?node-id=280-10679).

## Бизнес-логика

- Вкладка «Лента» таббара открывает экран «Выбираем вместе»; таббар виден только на нём, последующие шаги открываются поверх без таббара с кнопкой «Назад» в `SrTopBar`.
- Карточка «На одном устройстве» (`Icon/user` → `SrIcon.Profile`) ведёт в сценарий FE-F01 в одиночном режиме; карточка «На нескольких устройствах» (`Icon/users` → `SrIcon.People`) — в сценарий FE-F04. До реализации FE-F01/FE-F04 обе карточки открывают явные заглушки-точки входа (`FeedModeState.stub` → `FeedStepScreen`), сценарий не обрывается молча.
- Вход «Свайпать события» с Главной открывает тот же экран с предвыбранным `kind=EVENT` (`MainViewModel.navigateToFeedWithEventsKind`); предвыбранный тип хранится в `FeedModeState.pendingKind` и пробрасывается в FE-F01 при выборе режима.
- Сценарии ленты доступны только с завершённым профилем: при входе выполняется проверка `GET /v1/me profileComplete`, а `403 PROFILE_INCOMPLETE` маппится в `FeedError.ProfileIncomplete` и ведёт на шаг профиля регистрации (`RegistrationViewModel.openProfileCompletion`).
- Бюджет в фильтрах общий (`budgetTotalMinor`, копейки); цена карточки — на человека; проверку `price * partySize <= budgetTotal` выполняет сервер.
- Комната: фазы `LOBBY/VOTING/REFRESHING/MATCHED/EXHAUSTED/CLOSED`; snapshot хранит только свои голоса (`myVotes`), устаревшие версии (`version` меньше текущей) игнорируются; long polling — один открытый `GET /rooms/{id}?afterVersion=&waitSeconds=25`, `204` — немедленный повтор, сетевая ошибка — повтор с задержкой 1.5 c.

## Архитектура

### Domain Layer (`feed/domain`)

- Use cases (по одному на операцию контракта Discovery v1):
  - `GetFeedCandidatesUseCase(filters, cursor)` — порция выдачи до 20 карточек;
  - `CreateRoomUseCase(name)` — создание комнаты;
  - `CreateInviteUseCase(roomId)` — ссылка-приглашение (30 минут, многоразовая);
  - `JoinRoomUseCase(token)` — вступление по токену;
  - `StartRoomUseCase(roomId, filters)` — атомарный старт раунда с фильтрами;
  - `SubmitVoteUseCase(roomId, roundId, candidate, decision)` — голос `LIKE`/`SKIP`;
  - `CompleteRoundUseCase(roomId, roundId)` — завершение своего прохода;
  - `ObserveRoomUseCase(roomId)` — `Flow<RoomSnapshot>` long polling.
- Models: `FeedKind{PLACE,EVENT}`, `FeedFilters(kind, categoryId?, localDate?, budgetTotalMinor?, partySize)`, `FeedCard` (проекция `CatalogCard`, цена в копейках + подпись), `FeedRef`, `FeedPage`, `SwipeDecision{LIKE,SKIP}`, `RoomSnapshot` (зеркало DTO `Room`: фаза, состав, раунд, только свои голоса, `version`, `result.winner?`), `RoomPhase`, `RoomMember`, `RoomInvite`, `FeedError` (коды мобильного контракта: `ProfileIncomplete`, `InviteExpired`, `RoomNotFound`, `InvalidState`, `CursorExpired`, `Transport`).
- Интерфейсы: `FeedRepository`, `RoomRepository`.

### Data Layer (`feed/data`)

- Repository: `BackendFeedRepository` (`GET /v1/feed/candidates`), `BackendRoomRepository` (`POST /v1/rooms`, `POST /v1/rooms/{id}/invites`, `POST /v1/room-invites/join`, `POST /v1/rooms/{id}/start`, `PUT /v1/rooms/{id}/rounds/{roundId}/votes/{kind}/{candidateId}`, `PUT .../completion`, `GET /v1/rooms/{id}` + long polling) — обе через общий `BackendApi` (auth, refresh, idempotency keys).
- `FeedMappers` — DTO мобильного контракта → доменные модели и коды ошибок; транспортные типы наружу не утекают.
- `FeedContract` — единый источник формы query `candidates` и тела `start` (использует и унаследованный `party/data/BackendPartyRepository`).

### Presentation Layer (`feed/presentation`)

- ViewModel: `FeedModeViewModel(profileGate)`; State: `FeedModeState(pendingKind, busy, profileIncomplete, error, stub)`; Events: `FeedModeEvent` — `OpenedFromTab`, `OpenedWithEventsKind`, `SingleDeviceSelected`, `MultiDeviceSelected`, `DismissNotice`, `CompleteProfileSelected`. Методы `onError(FeedError)` (реактивный `403 PROFILE_INCOMPLETE`) и `onRegistrationCompleted()`.
- Экраны: `FeedModeScreen` (фрейм 280-10679: `SrTopBar` «Лента», заголовок, две стекло-карточки с иконками) и `FeedStepScreen` (последующий шаг без таббара, «Назад» в `SrTopBar`). Фон — градиент Главной (`FeedDesign.ScreenGradient` из токенов `SrColors.Base/Tint/GlassIris`), стекло — `SrColors.Glass/GlassEdge` + `SrShapes.Card`; платформы без эффекта получают подложку `color/surface/solid` через панели уведомлений.
- Навигация: `NavigationMain` рендерит `FeedModeScreen` для `Destination.FeedScreen`, таббар скрывается при `stub != null`; `MainViewModel.navigateToFeedWithEventsKind()` — вход с Главной; `RegistrationViewModel.openProfileCompletion()` — маршрутизация незавершённого профиля.

## Зависимости

### Использует

- UI Kit [`:uikit`](../../../../../../../../uikit/README.md): `SrTopBar`, `SrTabBar`, `SrButton`, `SrGlyph`/`SrIcon`, токены `Theme.kt` (`SrColors`, `SrShapes`, `SrType`), стекло `Glass.kt`.
- Общий HTTP-слой `backend/BackendApi` (auth, refresh, idempotency, таймауты long polling).
- Регистрация: `registration/domain/GetRegistrationCompleted` для проверки профиля и `RegistrationViewModel.openProfileCompletion` для маршрутизации `PROFILE_INCOMPLETE`.

### Используется в

- Навигация приложения (`NavigationMain`, `BottomNavigationBar`, таб «Лента»).
- Главная: вход «Свайпать события» (`home/presentation/HomeScreen.onOpenFeed`).
- Будущие пакеты FE-F01…FE-F06: use cases и модели `feed/domain` — единственная доменная модель ленты; унаследованная фича комнаты (`party/`, без собственного FEATURE.md до переноса) работает через граничные отображения `PartyFilters.toFeedFilters()` и алиас `PartyVote = SwipeDecision` до переноса своих сценариев.

## Особенности реализации

- Рефакторинг, а не переписывание: канонические типы вынесены из `party` в `feed/domain`; `party/data/BackendPartyRepository` больше не парсит контракт сам — query/тела строит `FeedContract`, DTO разбирает `FeedMappers`, проекции комнаты строятся из `RoomSnapshot`. Deeplink-приглашения (`srastem://…token=…`) сохранены: платформы открывают `Destination.RoomScreen` и вызывают `PartyViewModel.acceptInvite`.
- Точки входа FE-F01/FE-F04 — явные заглушки (`FeedModeStub`): видимый шаг с названием пакета, пояснением и возвратом; молчаливых обрывов нет. Заглушки убираются по мере реализации пакетов.
- Проверка профиля при входе fail-open при сетевой ошибке (офлайн не блокирует UI): авторитетная защита — `403 PROFILE_INCOMPLETE` от сервера, который маппится в `FeedError.ProfileIncomplete`.
- Платформенные отличия: liquid glass доступен на Android API 31+ и iOS с `UIVisualEffectView`; ниже — полупрозрачная заливка/`color/surface/solid` (см. `uikit/Glass.*`). Long polling использует socket timeout 35 c при `waitSeconds=25` контракта.
- Известные ограничения: FE-F01…FE-F06 не реализованы (свайп, фильтры, комната — следующие пакеты); серверное поведение (курсоры, majority) не проверяется клиентом.

## Тестовые сценарии

- `shared/src/commonTest/kotlin/fefu/prime/srastem/feed/domain/FeedUseCasesTest.kt` — use cases: выдача и курсор, проброс фильтров, оборачивание ошибок, по одной операции на use case.
- `shared/src/commonTest/kotlin/fefu/prime/srastem/feed/presentation/FeedModeViewModelTest.kt` — ViewModel: предвыбранный `kind=EVENT` с Главной и его проброс в FE-F01, сброс при входе вкладкой, заглушки точек входа, блокировка и маршрутизация незавершённого профиля, обработка `ProfileIncomplete`/транспортных ошибок.
- `shared/src/commonTest/kotlin/fefu/prime/srastem/feed/data/FeedMappersTest.kt` — маппинг DTO комнаты (фазы, свои голоса, `version`, `result.winner`), коды ошибок контракта, форма query и тела `start`.
