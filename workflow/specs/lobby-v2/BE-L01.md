# BE-L01

Обрабатывается только раздел BE-L01 в [implementation.md](../../../docs/specs/lobby-v2/implementation.md) и общие требования этого документа.

Порядок, зависимости, ветка и база PR описаны в [README комнаты v2](../../../docs/specs/lobby-v2/README.md). Критерии L01-01 … L01-06 берутся из [acceptance.md](../../../docs/specs/lobby-v2/acceptance.md).

Публичное поведение определяется [контрактом комнаты v2](../../../docs/contracts/lobby-v2.md) поверх [Discovery v1](../../../docs/contracts/discovery-v1.md) и [мобильного HTTP-контракта](../../../docs/contracts/mobile-api.md). При расхождении для комнат действует контракт комнаты v2. Правила следующего пула по весам и итога при исчерпании в этот пакет не входят.
