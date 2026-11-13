# FE-F00 · повторная проверка Developer

База: `2d1f5af3fc22fc9527e8dfdd87193ccc13b4abbf`, ветка `codex/FE-F00-recheck-20261113`, изменения без коммита. SHA-256 вывода `git diff` для проверенного дерева: `713790d61fffac9642e54d54a8373a0e881a806aed9bbcb08f60eae74b471f11`. APK `androidApp/build/outputs/apk/debug/androidApp-debug.apk`, SHA-256 `0e7a1aceebf6f60e5c333d845210b598ca1a571cc5441c42dc31de995e229066`.

## Изменения

- Проверка доступа через `CheckFeedAccessUseCase` и `BackendFeedAccessRepository` запрашивает актуальный `GET /v1/me`; ошибка соединения блокирует выбор режима и позволяет повторить проверку. Ответ `403 PROFILE_INCOMPLETE` при проверке трактуется как незавершённый профиль, автоматически открывается завершение регистрации. Возврат из регистрации повторно проверяет доступ, сохраняя выбранный тип.
- Карточки используют `SrChoiceSurface` из UI Kit с solid fallback. Обновлены unit-тесты ViewModel, [FEATURE.md](../../../../shared/src/commonMain/kotlin/fefu/prime/srastem/feed/FEATURE.md) и [frontend-implementation.md](../../../../docs/frontend-implementation.md).

## Проверки

- `ANDROID_HOME=/private/tmp/srastemsya-sdk.7DCrTs GRADLE_USER_HOME=/private/tmp/srastemsya-gradle ./gradlew :androidApp:assembleDebug :shared:testAndroid --no-configuration-cache --console=plain --info`: BUILD SUCCESSFUL, 95 задач, тесты Android host: 24, ошибок 0, пропусков 0. Последний offline прогон: [журнал](evidence/android-build-tests-artifact.log), BUILD SUCCESSFUL. Три новых теста репозитория проверяют успешный профиль, 403 и сетевую ошибку.
- Первый `tests/shared/run.sh` завершился до запуска тестов из-за недоступности lock в `~/.gradle`: [отчёт suite](../../../../tests/reports/20261113-222143-shared.md). Повышенный доступ позволил загрузить Gradle и зависимости; Android SDK найден и доустановлен в `/private/tmp`.
- `git diff --check`: успешно. Автоматический [скрипт](../../../../tests/shared/check-feed-architecture.py) проверяет поля доменных моделей, фазы и перечисления, интерфейсы и восемь use cases, domain imports, направление зависимостей presentation, DI, связь с party, API, UI Kit и размещение тестов: PASS ([журнал](evidence/architecture-check.log)). SHA-256 скрипта `97aeadc0912e7c25ea9be312cc894bdd4a3b0a1d889fe19c54ba2c73ba4cf3d0`. Это частичная проверка архитектуры, не доказательство всех семантических требований AC-08.
- Figma API для страницы `280:7618` вернул «nothing selected»; контекст и скриншот [фрейма 280:10679](https://www.figma.com/design/khewsPf7b4JVyBwJaCmO43/startap_design?node-id=280-10679) получены. Сверку работающего Android выполняет независимый Tester.
- Контракты Discovery и mobile-api из предоставленного архива прочитаны как данные. Серверная реализация вне объёма.

## Ограничения

FE-F01/FE-F04 остаются задокументированными заглушками; `kind=EVENT` сохраняется и отображается на точке входа. Обработчик `FeedModeViewModel.onError` пока не подключён к будущим сетевым сценариям FE-F01/FE-F04; проверка `403` реализована на существующей проверке доступа. Маршрутизация по состоянию неполного профиля подключена в `NavigationMain`; её UI-проверка ожидает Tester. Скрипт проверяет структуру, а не семантику всех операций и сетевого контракта. Соответствие визуальному макету, запуск на устройстве и пользовательская навигация ожидают независимой приёмки. iOS исключён пользователем.

Публичные документы: [спецификация](../../../../docs/specs/feed-v1/implementation.md), [Discovery](../../../../docs/contracts/discovery-v1.md), [mobile API](../../../../docs/contracts/mobile-api.md), [дизайн](../../../../docs/design/feed.md).

- Исправлены оформление карточек через `SrChoiceSurface` UI Kit и системный Back (заглушка → выбор → предыдущая вкладка). Добавлены тесты возврата. Отдельный fixture APK и loopback HTTP сервер доступны Tester через test_packet; production origin в исходниках восстановлен.
