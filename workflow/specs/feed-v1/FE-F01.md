# FE-F01

Мобильный пакет (KMP, Android/iOS). Обрабатывается только раздел FE-F01 в [implementation.md](../../../docs/specs/feed-v1/implementation.md#fe-f01--фильтры-подборки) и общие требования этого документа (доменная модель, компоненты UI Kit, готовность пакета).

Порядок и зависимости описаны в [README ленты](../../../docs/specs/feed-v1/README.md). Пакет зависит от FE-F00. Критерии F01-01 … F01-02 берутся из [acceptance.md](../../../docs/specs/feed-v1/acceptance.md).

Серверное поведение не реализуется: границей служат [контракт Discovery v1](../../../docs/contracts/discovery-v1.md) и [мобильный HTTP-контракт](../../../docs/contracts/mobile-api.md). Применяются правила [mobile-architecture.md](../../rules/mobile-architecture.md) и [feature-docs.md](../../rules/feature-docs.md).
