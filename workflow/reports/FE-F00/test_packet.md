# FE-F00 · Тестовый пакет для Tester

Пакет содержит только публичные документы, команды сборки/запуска, доступ к фикстурам и идентичность ревизии/сборки. Ожидания приёмки — исключительно из `workflow/reports/FE-F00/acceptance.md`.

## Идентичность ревизии и сборки

- Репозиторий: worktree `.worktrees/fe-f00`, ветка `fe-f00`.
- Базовая ревизия: `3447a081ccd672a6e09790634b0984a57fa4a923` (HEAD не изменялся; изменения пакета — в рабочем дереве, коммит и push делегированы).
- Android-сборка: `./gradlew :androidApp:assembleDebug` → APK `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.
- iOS-сборка: `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' -derivedDataPath build/ios-derived build` → приложение `build/ios-derived/Build/Products/Debug-iphonesimulator/Srastem.app`.
- KMP-цели iOS: `./gradlew :shared:compileKotlinIosSimulatorArm64 :shared:compileKotlinIosArm64`.
- Unit-тесты (доказательства прогонов с метками времени): `tests/shared/run.sh`, отчёты в `tests/reports/` (финальный — `tests/reports/20261113-144852-shared.md`: exit 0, 19 passed на Android host JVM и 19 passed на iOS Simulator ARM64, 0 failed, 0 skipped).
- Окружение: JDK 17, Gradle 9.5.1, Kotlin 2.4.21, Xcode 27.2 (27B52), симулятор iOS 27.2.

## Публичные документы (источники ожиданий и поведения)

- Критерии приёмки (неизменяемые): `workflow/reports/FE-F00/acceptance.md`.
- Спецификация пакета: `docs/specs/feed-v1/implementation.md` (раздел «FE-F00 · Вход и выбор режима» и общие требования: доменная модель, компоненты UI Kit, готовность пакета); очередь и зависимости: `docs/specs/feed-v1/README.md`; критерий очереди: `docs/specs/feed-v1/acceptance.md` (F00-01).
- Спецификация пакета в очереди workflow: `workflow/specs/feed-v1/FE-F00.md`.
- Контракты (граница серверного поведения): `docs/contracts/discovery-v1.md`, `docs/contracts/mobile-api.md` (коды ошибок, включая `403 PROFILE_INCOMPLETE`).
- Дизайн: `docs/design/feed.md`, `docs/design/ui-kit.md`; макет экрана выбора режима — Figma `startup_design`, фрейм `280-10679` («01 · Выбор устройства»), https://www.figma.com/design/khewsPf7b4JVyBwJaCmO43/startup_design?node-id=280-10679 .
- Клиентская реализация и привязки: `docs/frontend-implementation.md`; интеграция со стендом: `docs/backend-integration.md`.
- Документация фичи (публичная в репозитории): `shared/src/commonMain/kotlin/fefu/prime/srastem/feed/FEATURE.md`.

## Команды сборки и запуска

- Unit-тесты фичи: `tests/shared/run.sh` (агрегатор всех suite: `tests/run-all.sh`). Отчёты прогонов пишутся скриптом в `tests/reports/<stamp>-shared.md` и не редактируются вручную.
- Android-приложение: `./gradlew :androidApp:assembleDebug`, установка на эмулятор/устройство: `adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk`.
- iOS-приложение (симулятор): команда xcodebuild из раздела идентичности; запуск в симуляторе: `xcrun simctl boot <UDID> && xcrun simctl install <UDID> build/ios-derived/Build/Products/Debug-iphonesimulator/Srastem.app && xcrun simctl launch <UDID> <bundle-id из iosApp/Configuration>`.
- Deeplink приглашения комнаты (унаследованное поведение): схема `srastem://party/<id>` (Android intent / `xcrun simctl openurl`).

## Фикстуры и доступ к стенду

- Dev-стенд backend: запуск по `docs/backend-integration.md` (Compose из каталога backend-dev); API: `127.0.0.1:8080` (iOS Simulator), `10.0.2.2:8080` (Android Emulator), физический Android — `adb reverse tcp:8080 tcp:8080`.
- Учётные записи dev: `person1@example.test`, `person2@example.test`; новый профиль — `new@example.test`; код dev OTP — `0427`.
- Незавершённый профиль для проверки AC-06: вход новой учёткой (`new@example.test`) без шага профиля; завершение профиля — имя и дата рождения (18+).
- До готовности стенда BE-D03 ручная проверка возможна без сервера: экран выбора режима, навигация вкладки «Лента», вход «Свайпать события», заглушки точек входа и гейт профиля не требуют серверных данных (проверка профиля fail-open при недоступности сети; авторитетная защита — серверный `403 PROFILE_INCOMPLETE`).

## Замечания для Tester

- Точки входа FE-F01/FE-F04 в этом пакете — явные документированные заглушки (экран поверх без таббара с «Назад»): это соответствует замечанию об объёме в `acceptance.md` и не является обрывом сценария.
- Инфраструктурные сбои окружения (сборочное окружение, сеть, стенд) фиксируются как BLOCKED, а не FAIL приложения.
