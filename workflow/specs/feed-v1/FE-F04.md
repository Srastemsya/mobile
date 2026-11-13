# FE-F04

Мобильный пакет (KMP, Android/iOS). Обрабатывается только раздел FE-F04 в [implementation.md](../../../docs/specs/feed-v1/implementation.md#fe-f04--комната-создание-приглашение-запуск) и общие требования этого документа (доменная модель, компоненты UI Kit, готовность пакета).

Порядок и зависимости описаны в [README ленты](../../../docs/specs/feed-v1/README.md). Пакет зависит от FE-F01 и готового стенда BE-D03. Критерии F04-01 … F04-02 берутся из [acceptance.md](../../../docs/specs/feed-v1/acceptance.md).

Серверное поведение не реализуется: границей служат [контракт Discovery v1](../../../docs/contracts/discovery-v1.md) и [мобильный HTTP-контракт](../../../docs/contracts/mobile-api.md). Применяются правила [mobile-architecture.md](../../rules/mobile-architecture.md) и [feature-docs.md](../../rules/feature-docs.md).
