# Лента · вход, фильтры и подборка (FE-F00/FE-F01)

## Краткое описание

Фича `feed` — общая доменная модель и путь фильтрации выдачи мест и событий. FE-F00 предоставляет выбор режима, FE-F01 фильтрует кандидатов, а FE-F03 ведёт одиночный пул до выбранного результата. Спецификация: [docs/specs/feed-v1/implementation.md](../../../../../../../../docs/specs/feed-v1/implementation.md#fe-f03--одно-устройство-пул-и-результат), дизайн: [docs/design/feed.md](../../../../../../../../docs/design/feed.md).

## Бизнес-логика

- Вкладка «Лента» таббара открывает экран «Выбираем вместе»; таббар виден только на нём, последующие шаги открываются поверх без таббара с кнопкой «Назад» в `SrTopBar`.
- Карточка «На одном устройстве» (`Icon/user` → `SrIcon.Profile`) открывает фильтры FE-F01; карточка комнаты (`Icon/users` → `SrIcon.People`) открывает создание комнаты FE-F04 на Android.
- Вход «Свайпать события» с Главной открывает тот же экран с предвыбранным `kind=EVENT` (`MainViewModel.navigateToFeedWithEventsKind`); предвыбранный тип хранится в `FeedModeState.pendingKind` и пробрасывается в FE-F01 при выборе режима.
- Сценарии ленты доступны только с завершённым профилем: при входе выполняется проверка `GET /v1/me profileComplete`; неполный профиль или `403 PROFILE_INCOMPLETE` автоматически открывает завершение регистрации (`RegistrationViewModel.openProfileCompletion`). Ошибка проверки блокирует выбор и предлагает повторить запрос.
- Бюджет в фильтрах общий (`budgetTotalMinor`, копейки); цена карточки — на человека; проверку `price * partySize <= budgetTotal` выполняет сервер.
- FE-F01 принимает один `PLACE` или `EVENT`, серверную категорию данного типа или «Все», дату только для `EVENT`, необязательный общий бюджет и число участников. Отрицательный/нецелый бюджет и число участников меньше 1 при заданном бюджете блокируют отправку с ошибкой у поля.
- После отправки фильтров вызывается `GET /v1/feed/candidates`; экран показывает карточки через Android swipe-интерфейс FE-F02 и предоставляет кнопку «Изменить фильтры», возвращающую к сохранённым значениям.
- В FE-F03 первый лайк завершает одиночный выбор и показывает результат; явное нажатие «К месту»/«К мероприятию» загружает полные сведения каталога. Пропуск последнего кандидата автоматически продолжает выдачу по `nextCursor`.
- Комната: фазы `LOBBY/VOTING/REFRESHING/MATCHED/EXHAUSTED/CLOSED`; snapshot хранит только свои голоса (`myVotes`), устаревшие версии (`version` меньше текущей) игнорируются; long polling — один открытый `GET /rooms/{id}?afterVersion=&waitSeconds=25`, `204` — немедленный повтор, сетевая ошибка — повтор с задержкой 1.5 c.

## Архитектура

### Domain Layer (`feed/domain`)

- `CheckFeedAccessUseCase` — проверка завершённости профиля перед выбором режима.
- Use cases (по одному на операцию контракта Discovery v1):
  - `GetFeedCandidatesUseCase(filters, cursor)` — порция выдачи до 20 карточек;
  - `GetCatalogDetailsUseCase(ref)` — полные сведения выбранной записи каталога;
  - `CreateRoomUseCase(name)` — создание комнаты;
  - `CreateInviteUseCase(roomId)` — ссылка-приглашение (30 минут, многоразовая);
  - `JoinRoomUseCase(token)` — вступление по токену;
  - `StartRoomUseCase(roomId, filters)` — атомарный старт раунда с фильтрами;
  - `SubmitVoteUseCase(roomId, roundId, candidate, decision)` — голос `LIKE`/`SKIP`;
  - `CompleteRoundUseCase(roomId, roundId)` — завершение своего прохода;
  - `ObserveRoomUseCase(roomId)` — `Flow<RoomSnapshot>` long polling.
- Models: `FeedKind{PLACE,EVENT}`, `FeedFilters(kind, categoryId?, localDate?, budgetTotalMinor?, partySize)`, `FeedCard` (проекция `CatalogCard`, цена в копейках + подпись), `CatalogDetails` (карточка полного каталога, описание, адрес, время, цена и допуск), `FeedRef`, `FeedPage`, `SwipeDecision{LIKE,SKIP}`, `RoomSnapshot` (зеркало room-v2 DTO: фаза, фиксированный состав, `roundId`, `roundNumber`, `expiresAt`, `memberCount`, `finishedCount`, пул до 10 `candidates`, только собственные `myVotes` и `myFinished`, `version`, итог `winner`, `resultBasis`, `resultLikes`, `resultMemberCount`), `RoomPhase`, `RoomMember`, `RoomInvite`, `FeedError` (коды мобильного контракта: `ProfileIncomplete`, `InviteExpired`, `RoomNotFound`, `InvalidState`, `CursorExpired`, `Transport`).
- Интерфейсы: `FeedRepository`, `RoomRepository`.
- FE-F01: `FeedCategoryRepository` и `GetFeedCategoriesUseCase` загружают доступные категории.

### Data Layer (`feed/data`)

- Repository: `BackendFeedAccessRepository` (`GET /v1/me`, проверка доступа); `BackendFeedRepository` (`GET /v1/feed/candidates`, `GET /v1/catalog/{kind}/{id}`), `BackendRoomRepository` (`POST /v1/rooms`, `POST /v1/rooms/{id}/invites`, `POST /v1/room-invites/join`, `POST /v1/rooms/{id}/start`, `PUT /v1/rooms/{id}/rounds/{roundId}/votes/{kind}/{candidateId}`, `PUT .../completion`, `GET /v1/rooms/{id}` + long polling) — обе через общий `BackendApi` (auth, refresh, idempotency keys).
- `FeedMappers` — DTO мобильного контракта → доменные модели и коды ошибок; транспортные типы наружу не утекают.
- `BackendFeedCategoryRepository` читает `GET /v1/catalog/categories`.
- `FeedContract` — единый источник формы query `candidates` и тела `start` (использует и унаследованный `party/data/BackendPartyRepository`).

### Presentation Layer (`feed/presentation`)

- ViewModel: `FeedModeViewModel(CheckFeedAccessUseCase)`; State: `FeedModeState` includes mode/filter/result data and `FeedSwipeState(cards,queuedCards,index,decisions,loadingNextPool)`; Events include mode/filter actions plus `SwipeDecided`, `UndoSwipe`, `SwipeFinished` and `SwipePoolReady`. While a pool is loading, decisions are ignored and the UI replaces the card/actions with loading feedback. `FeedSwipeState` exposes current card, undo availability and progress. Methods `onError(FeedError)` (reactive `403 PROFILE_INCOMPLETE`) and `onRegistrationCompleted()`.
- Экраны: `FeedModeScreen` (фрейм 280-10679: `SrTopBar` «Лента», заголовок, две стекло-карточки с иконками) и `FeedStepScreen` (последующий шаг без таббара, «Назад» в `SrTopBar`). Фон — градиент Главной (`FeedDesign.ScreenGradient` из токенов `SrColors.Base/Tint/GlassIris`), карточки используют `SrChoiceSurface` и `SrShapes.Card`; без backdrop применяется подложка `color/surface/solid`.
- FE-F01: `FeedModeViewModel` хранит черновик фильтров, загруженные `FeedCategoryOption`, ошибки и текущие результаты; `FeedModeScreen` рисует выбор типа и категории в переносящемся по строкам ряду чипов (включая «Все»), `SrDateField` для даты события (передаёт ISO `YYYY-MM-DD`), бюджет, число участников, результат загрузки и возврат к фильтрам. Экран фильтров использует заголовок `SrTopBar` «Фильтры»; основная кнопка подписана «Показать подборку».
- FE-F02 (Android): `FeedSwipeState` stores the active local pool (at most 10 cards), queued candidates, active index, local decisions and undo history in `FeedModeViewModel`; `FeedSwipeDecided` maps horizontal drags and `SrVoteActions` to `LIKE`/`SKIP`, `UndoSwipe` removes the previous decision. At each local ten-card boundary, the next up-to-ten queued candidates become active and index/decision history reset. This is a client-side split only: API pages may contain up to 20 and have no page-size query. `FeedModeScreen` displays `SrVoteCard` with project artwork fallback, layered decorative cards, position and progress. No detail navigation is attached to card taps. Room context rendering is reserved through `contextLabel`; FE-F04 does not yet supply it.
- FE-F03 (Android): `FeedSwipeState.nextCursor` tracks the server cursor after the current response; skipping the last candidate in a response calls `GetFeedCandidatesUseCase` with retained filters and cursor, shows both refresh messages, and resets the local counter on success. An empty response or absent cursor shows exhaustion and returns to filters with values retained. The first LIKE freezes the chosen `FeedCard` and displays a full-screen result; the explicit result action calls `GetCatalogDetailsUseCase` → `GET /v1/catalog/{kind}/{id}` and renders full catalog fields. Candidate and detail cover URLs are resolved with `BackendApi.resolveMediaUrl`; missing or failed image loads show the same `SrArtwork` fallback used by the swipe card. `FeedModeViewModel` retains the result, candidate, decisions and undo state across rotation/background restoration; process-death persistence is not promised.
- FE-F03 Domain/Data: `CatalogDetails` is the detail projection; `GetCatalogDetailsUseCase` delegates to `FeedRepository.getCatalogDetails`, implemented by `BackendFeedRepository` and mapped by `FeedMappers.details`.
- Навигация: `NavigationMain` рендерит `FeedModeScreen` для `Destination.FeedScreen`, таббар скрывается при `stub != null`; `MainViewModel.navigateToFeedWithEventsKind()` — вход с Главной; `RegistrationViewModel.openProfileCompletion()` — маршрутизация незавершённого профиля.

## Зависимости

### Использует

- UI Kit [`:uikit`](../../../../../../../../uikit/README.md): `SrTopBar`, `SrTabBar`, `SrButton`, `SrGlyph`/`SrIcon`, токены `Theme.kt` (`SrColors`, `SrShapes`, `SrType`), стекло `Glass.kt`.
- Общий HTTP-слой `backend/BackendApi` (auth, refresh, idempotency, таймауты long polling).
- Регистрация: `RegistrationViewModel.openProfileCompletion` для маршрутизации `PROFILE_INCOMPLETE`.

### Используется в

- Навигация приложения (`NavigationMain`, `BottomNavigationBar`, таб «Лента»).
- Главная: вход «Свайпать события» (`home/presentation/HomeScreen.onOpenFeed`).
- Будущие пакеты FE-F01…FE-F06: use cases и модели `feed/domain` — единственная доменная модель ленты; унаследованная фича комнаты (`party/`, без собственного FEATURE.md до переноса) работает через граничные отображения `PartyFilters.toFeedFilters()` и алиас `PartyVote = SwipeDecision` до переноса своих сценариев.

## Особенности реализации

- Рефакторинг, а не переписывание: канонические типы вынесены из `party` в `feed/domain`; `party/data/BackendPartyRepository` больше не парсит контракт сам — query/тела строит `FeedContract`, DTO разбирает `FeedMappers`, проекции комнаты строятся из `RoomSnapshot`. Android deeplink-приглашения открывают `Destination.FeedScreen` и вызывают `RoomLobbyViewModel.acceptInvite`; iOS продолжает использовать старый `PartyViewModel` до отдельного пакета.
- FE-F04 на Android использует `RoomLobbyViewModel` и `RoomLobbyScreen`: название, создание, автоматическая выдача/копирование ссылки во встроенной карточке SrRoomInvite, группа SrAvatarGroup, вход по token, live-состав, комнатные фильтры без поля числа участников и старт. `FeedModeStub` служит только маркером шага для навигации и скрытия таббара. В FE-F04 голосование ещё не было подключено; FE-F05 добавляет его в тот же Android-сценарий комнаты.
- Проверка профиля при входе блокирует выбор при ошибке сети до успешного повтора. FE-F01 отображает ошибку загрузки категорий или кандидатов в состоянии экрана.
- Платформенные отличия: liquid glass доступен на Android API 31+ и iOS с `UIVisualEffectView`; ниже — полупрозрачная заливка/`color/surface/solid` (см. `uikit/Glass.*`). Long polling использует socket timeout 35 c при `waitSeconds=25` контракта.
- Известные ограничения: Android swipe-интерфейс подключён; Комнатный экран FE-F05 использует собственный snapshot вместо `FeedSwipeState.contextLabel`; это поле остаётся для одиночного экрана. FE-F03 запрашивает следующую API-страницу одиночной выдачи по сохранённым фильтрам и `nextCursor`, когда текущие кандидаты пропущены; FE-F05 обрабатывает обновление раунда комнаты в `RoomLobbyViewModel`.

## Тестовые сценарии

- `shared/src/commonTest/kotlin/fefu/prime/srastem/feed/domain/FeedUseCasesTest.kt` — use cases: выдача с курсором и фильтрами, преобразование ошибок; делегирование операций комнаты.
- `shared/src/commonTest/kotlin/fefu/prime/srastem/feed/presentation/FeedModeViewModelTest.kt` — ViewModel: вход и гейт профиля, фильтры, первый лайк сразу открывает выбранный результат без запроса следующей страницы, пропуск десяти кандидатов автоматически запрашивает `nextCursor` и сбрасывает позицию.
- `shared/src/commonTest/kotlin/fefu/prime/srastem/feed/data/FeedMappersTest.kt` — маппинг DTO деталей каталога для обёртки места `{card, description}` и DTO события; маппинг комнаты, ошибок контракта, параметров query и тела `start`.

## FE-F04 Android

`RoomLobbyViewModel` использует `CreateRoomUseCase`, `CreateInviteUseCase`, `JoinRoomUseCase`, `ObserveRoomUseCase`, `StartRoomUseCase`. Состояние `RoomLobbyState` хранит snapshot, ссылку, признак владельца, фильтры, статус копирования и ошибки; действия `openCreate`, `create`, `acceptInvite`, `issueInvite`, `openFilters`, `start`, `leave` управляют сценарием. `BackendRoomRepository` обслуживает `POST /v1/rooms`, `POST /v1/rooms/{id}/invites`, `POST /v1/room-invites/join`, `POST /v1/rooms/{id}/start`, `GET /v1/rooms/{id}`. При уходе из лобби observation отменяется. Сервер ограничивает комнатный пул 10 карточками по `lobby-v2.md` из контрактного архива. Старт отображает число кандидатов; голосование и свайпы комнаты реализованы в FE-F05 на Android.

## FE-F05 Android

Комнатный раунд показывает серверный пул (до 10 карточек) всем участникам в одинаковом порядке. Каждый голос `LIKE`/`PASS` отправляется через `SubmitVoteUseCase`; до завершения пользователь может вернуться и заменить свой голос. После решения по каждой карточке `CompleteRoundUseCase` завершает проход. Экран ожидания показывает только свои голоса и число завершивших; матч, новый раунд, исчерпание и закрытие определяет только snapshot сервера. Каждый раунд имеет собственный `expiresAt` на 30 минут. После завершения всеми участниками сервер объявляет `THRESHOLD` при `likes * 10 >= memberCount * 7` от фиксированного состава. Если порог не достигнут, сервер открывает новый пул без ранее показанных карточек; после исчерпания при хотя бы одном лайке объявляет `TOP_SCORE`, иначе `EXHAUSTED`. `Room.result` содержит `{winner, basis: THRESHOLD|TOP_SCORE, likes, memberCount}`; при `TOP_SCORE` раунд может быть `null`. `RoomSnapshot` хранит `roundId`, `roundNumber`, `expiresAt`, `memberCount`, `finishedCount`, до 10 `candidates`, собственные `myVotes`/`myFinished` и результат `winner`/`resultBasis`/`resultLikes`/`resultMemberCount`; `FeedMappers` читает эти поля из room-v2 DTO. `RoomLobbyViewModel` отсеивает старые версии, восстанавливает индекс из `myVotes`, перечитывает snapshot при ошибках состояния и отменяет наблюдение при уходе. `SettingsActiveRoomStore` сохраняет только ID текущей комнаты и ID владельца сессии; после перезапуска Android открывает комнату и запрашивает свежий snapshot через live API. При явном выходе ID удаляется; чужие голоса локально не хранятся. Match action загружает `GetCatalogDetailsUseCase` для победителя.

Дизайн: [состояния голосования и результата](../../../../../../../../docs/design/feed.md), [Figma Page 2](https://www.figma.com/design/khewsPf7b4JVyBwJaCmO43/startap_design?node-id=280-7618). Контракт room-v2: архив frontend API от 2026-11-13. Android реализован в `RoomLobbyScreen`; iOS остаётся на прежнем потоке до отдельного пакета.

Android принимает приглашения `https://srastemsya.nixbi.ru/room-invites/join?token=…` и `srastemsya://room-invites/join?token=…`. Для автоматического открытия HTTPS-ссылки домен должен отдавать `/.well-known/assetlinks.json` с идентификатором пакета и SHA-256 сертификата сборки. Кнопка копирования использует URL из ответа сервера.
