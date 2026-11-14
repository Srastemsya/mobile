# Android: локальная сборка и тестирование

Проверено 13 ноября 2026 года на этой машине. Каталоги в `/private/tmp` временные: перед использованием проверьте их наличие; после очистки системы SDK и кэш может потребоваться установить заново.

- Android SDK: `/private/tmp/srastemsya-sdk.7DCrTs` (также путь записан в `/private/tmp/srastemsya-sdk-path`).
- ADB: `/private/tmp/srastemsya-sdk.7DCrTs/platform-tools/adb`.
- Эмулятор: `/private/tmp/srastemsya-sdk.7DCrTs/emulator/emulator`; AVD `Srastemsya35` (Android API 35), `ANDROID_AVD_HOME=/private/tmp/srastemsya-sdk.7DCrTs/avd`.
- Кэш Gradle с загруженными зависимостями: `/private/tmp/srastemsya-gradle`.

Пример сборки и host-тестов из корня рабочей копии:

```sh
ANDROID_HOME=/private/tmp/srastemsya-sdk.7DCrTs \
GRADLE_USER_HOME=/private/tmp/srastemsya-gradle \
./gradlew :androidApp:assembleDebug :shared:testAndroid --no-configuration-cache --console=plain
```

APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`. Для поиска устройства используйте `/private/tmp/srastemsya-sdk.7DCrTs/platform-tools/adb devices`, для списка эмуляторов — `ANDROID_AVD_HOME=/private/tmp/srastemsya-sdk.7DCrTs/avd /private/tmp/srastemsya-sdk.7DCrTs/emulator/emulator -list-avds`. Если сеть, запуск ADB или доступ к кэшу ограничены песочницей, проверьте доступные разрешения среды; не считайте отсутствие `adb` в `PATH` отсутствием SDK. Физическое устройство может содержать приложение с другой подписью: не удаляйте его и данные без разрешения пользователя.

При проверке FE-F00 сборка и 24 host-теста прошли; независимая UI-проверка выполнена на эмуляторе API 35. Наличие этих файлов и работа эмулятора не гарантируются в будущих сессиях.
