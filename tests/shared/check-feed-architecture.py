#!/usr/bin/env python3
"""Source-level FE-F00 architecture checks; behavioral tests remain separate."""
from pathlib import Path
import re

root = Path('shared/src/commonMain/kotlin/fefu/prime/srastem')
feed = root / 'feed'
domain = feed / 'domain'

def read(path):
    return path.read_text()

def contains(path, *tokens):
    source = read(path)
    for token in tokens:
        assert token in source, f'{path}: missing {token}'

contains(domain/'FeedKind.kt', 'PLACE', 'EVENT')
contains(domain/'SwipeDecision.kt', 'LIKE', 'SKIP')
contains(domain/'FeedFilters.kt', 'kind:', 'categoryId:', 'localDate:', 'budgetTotalMinor:', 'partySize:')
contains(domain/'FeedCard.kt', 'ref:', 'title:', 'priceMinor:', 'coverUrl:')
contains(domain/'RoomPhase.kt', 'LOBBY', 'VOTING', 'REFRESHING', 'MATCHED', 'EXHAUSTED', 'CLOSED')
contains(domain/'RoomSnapshot.kt', 'phase:', 'version:', 'myVotes:', 'winner:')
contains(domain/'FeedRepository.kt', 'interface FeedRepository', 'getCandidates')
contains(domain/'RoomRepository.kt', 'interface RoomRepository', 'createRoom', 'createInvite', 'joinRoom', 'startRoom', 'submitVote', 'completeRound', 'observeRoom')
for path in domain.glob('*.kt'):
    for line in read(path).splitlines():
        if line.startswith('import '):
            assert line.startswith(('import kotlin.', 'import kotlinx.coroutines.')), (path, line)
for path in (feed/'presentation').glob('*.kt'):
    source = read(path)
    assert not re.search(r'import .*\.feed\.data\.', source), path
    assert not re.search(r'import .*\.backend\.', source), path
for cls in ('GetFeedCandidates', 'CreateRoom', 'CreateInvite', 'JoinRoom', 'StartRoom', 'SubmitVote', 'CompleteRound', 'ObserveRoom'):
    path = domain / f'{cls}UseCase.kt'
    contains(path, f'class {cls}UseCase', 'operator fun invoke')
    assert read(path).count('operator fun invoke') == 1, path
contains(feed/'data/BackendFeedRepository.kt', 'BackendApi', ': FeedRepository', 'FeedMappers')
contains(feed/'data/BackendRoomRepository.kt', 'BackendApi', ': RoomRepository', 'FeedMappers')
contains(root/'party/domain/PartyModels.kt', 'SwipeDecision', 'FeedFilters')
contains(root/'party/data/BackendPartyRepository.kt', 'FeedMappers', 'FeedContract')
contains(root/'di/AppContainer.kt', 'BackendFeedRepository', 'BackendRoomRepository', 'CheckFeedAccessUseCase')
contains(feed/'presentation/FeedModeViewModel.kt', 'StateFlow<FeedModeState>', 'CheckFeedAccessUseCase', 'FeedModeEvent')
contains(feed/'presentation/FeedModeScreen.kt', 'SrTopBar', 'SrChoiceSurface', 'SrColors', 'SrShapes')
contains(feed/'FEATURE.md', '## Краткое описание', '## Бизнес-логика', '## Архитектура', '## Зависимости', '## Особенности реализации')
for layer in ('domain', 'data', 'presentation'):
    assert list((Path('shared/src/commonTest/kotlin/fefu/prime/srastem/feed')/layer).glob('*Test.kt')), layer
print('PASS: FE-F00 source-level model, dependency direction, use-case, DI, UI Kit, feature documentation and test placement checks')
