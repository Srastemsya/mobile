# Вход и регистрация

Проверяет существующую сессию, подтверждает email или телефон через OTP и собирает профиль нового пользователя. Для демо-жюри Android-клиент предлагает пять тестовых email и автоматически подтверждает известный dev OTP только для этих адресов.

## Бизнес-логика

- Жюри выбирает `person1@example.test`…`person5@example.test`; приложение запрашивает challenge, подставляет настроенный dev OTP и подтверждает его без ручного ввода.
- Остальные сценарии сохраняют обычный вход: email или телефон, ручной код и завершение профиля при необходимости.
- Автоматическое подтверждение ограничено точным списком синтетических email. Код не запрашивается у API и не отображается пользователю.

## Архитектура

### Domain Layer

- Use cases: `GetRegistrationCompleted`, `RequestRegistrationCode`, `VerifyRegistrationCode`, `CompleteRegistration`.
- Models: `RegistrationMethod`, `RegistrationProfile`, `RegistrationRepository`.

### Data Layer

- `BackendRegistrationRepository` использует `/v1/me`, `/v1/auth/challenges`, `/v1/auth/challenges/{id}/verify` и `/v1/me/registration`.
- Challenge ID и контакт хранятся локально до проверки; успешная проверка сохраняет сессию через `BackendApi`.

### Presentation Layer

- `RegistrationViewModel` хранит шаги `METHOD`, `CONTACT`, `CODE`, `PROFILE` и выполняет проверку OTP.
- `RegistrationScreen` использует `SrSelect` для демо-аккаунтов, `SrField` для телефона и `SrAuthCode` для ручного OTP.
- `JudgeDemoAccounts` содержит список адресов и dev OTP fixture.

## Зависимости

### Использует

- UI Kit: `SrSelect`, `SrField`, `SrAuthCode`, `SrButton`.
- `BackendApi` для challenge, сессии и профиля.

### Используется в

- `NavigationMain` показывает регистрацию до основной навигации и при `PROFILE_INCOMPLETE`.

## Особенности реализации

- Dev fixture OTP описан в [контракте Discovery v1](../../../../../../../../docs/contracts/discovery-v1.md) и [инструкции backend](../../../../../../../../docs/backend-integration.md). Автовход не расширять на произвольные адреса или обычные номера.
- UX описан в [дизайне входа](../../../../../../../../docs/design/auth.md).
