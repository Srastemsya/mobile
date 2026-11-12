#!/usr/bin/env python3
"""Install workflow templates without overwriting project files."""
import argparse
import json
from pathlib import Path


def bootstrap(project, profile=None):
    root = Path(project).resolve(strict=True)
    if not root.is_dir():
        raise ValueError('Project must be a directory')
    assets = Path(__file__).resolve().parent.parent / 'assets'
    sources = [(assets / 'base', root)]
    if profile:
        sources.append((assets / 'profiles' / profile, root / 'workflow' / 'rules' / profile))
    for source, dest in sources:
        for item in sorted(source.rglob('*')):
            if not item.is_file():
                continue
            target = dest / item.relative_to(source)
            target.parent.mkdir(parents=True, exist_ok=True)
            try:
                with target.open('x', encoding='utf-8') as out:
                    out.write(item.read_text(encoding='utf-8'))
                print('Created:', target.relative_to(root))
            except FileExistsError:
                print('Kept:', target.relative_to(root))
    marker = '<!-- spec-pipeline-routing-v1 -->'
    agents = root / 'AGENTS.md'
    text = agents.read_text(encoding='utf-8') if agents.exists() else ''
    if marker not in text:
        with agents.open('a', encoding='utf-8') as out:
            out.write('\n\n' + marker + '\n## Specification pipeline\n'
                      'When explicitly asked to process the specification queue, read workflow/QUEUE.md. '
                      'Setup alone does not start development. Read workflow/rules/index.md for '
                      'applicable project rules; load only the current role and relevant rules. '
                      'Do not load source, diffs or Developer history into PM or Tester contexts.\n')
    if profile:
        index = root / 'workflow/rules/index.md'
        reference = f'{profile}/INDEX.md'
        if reference not in index.read_text(encoding='utf-8'):
            with index.open('a', encoding='utf-8') as out:
                out.write(f'\n- Profile: [{profile}]({reference}); read its routing table, then only applicable files.\n')
    json.loads((root / 'workflow/config.json').read_text(encoding='utf-8'))


if __name__ == '__main__':
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('project')
    p.add_argument('--profile', choices=['matchwear'])
    args = p.parse_args()
    bootstrap(args.project, args.profile)
