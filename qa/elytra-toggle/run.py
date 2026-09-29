#!/usr/bin/env python3
"""Run ElytraToggleQa against a built Fabric 26.3 JAR and an explicit cached runtime provider."""
import argparse
import importlib.util
import json
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import zipfile


def main():
    here = Path(__file__).resolve().parent
    parser = argparse.ArgumentParser(description=__doc__)
    source = parser.add_mutually_exclusive_group(required=True)
    source.add_argument('--runtime-config', type=Path, help='JSON runtime configuration (see qa/README.md)')
    source.add_argument('--runtime-reference', type=Path, help='Existing Python runtime-provider module')
    parser.add_argument('--toolpouch', type=Path, required=True)
    parser.add_argument('--port', type=int, default=25679)
    args = parser.parse_args()
    provider = args.runtime_reference.resolve() if args.runtime_reference else here.parent / 'runtime_provider.py'
    spec = importlib.util.spec_from_file_location('runtime', provider)
    runtime = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(runtime)
    if args.runtime_config:
        runtime.configure(args.runtime_config)
    build = here / 'build'
    build.mkdir(exist_ok=True)
    run = Path(tempfile.mkdtemp(prefix='run-', dir=build))
    classes = run / 'classes'
    classes.mkdir()
    dependencies = [args.toolpouch.resolve(), *map(Path, runtime.deps()[1:])]
    server, vanilla = runtime.audits()
    paths = runtime.cp(server['command']) + runtime.cp(vanilla['command']) + list(map(str, dependencies))
    for index, jar in enumerate(dependencies):
        with zipfile.ZipFile(jar) as archive:
            for member in archive.namelist():
                if member.endswith('.jar'):
                    dest = run / 'nested' / str(index) / Path(member).name
                    dest.parent.mkdir(parents=True, exist_ok=True)
                    dest.write_bytes(archive.read(member))
                    paths.append(str(dest))
    subprocess.run(['javac', '--release', '25', '-proc:none', '-cp', os.pathsep.join(dict.fromkeys(paths)),
                    '-d', str(classes), str(here / 'ElytraToggleQa.java')], check=True)
    fixture = run / 'elytra-toggle-qa.jar'
    with zipfile.ZipFile(fixture, 'w', zipfile.ZIP_DEFLATED) as archive:
        archive.writestr('fabric.mod.json', json.dumps({
            'schemaVersion': 1, 'id': 'elytra_toggle_qa', 'version': '1', 'environment': 'server',
            'entrypoints': {'main': ['qa.ElytraToggleQa']},
            'depends': {'toolpouch': '*', 'fabric-api': '*'}}))
        for path in classes.rglob('*.class'):
            archive.write(path, path.relative_to(classes))
    dependencies.append(fixture)
    mods = run / 'mods'
    mods.mkdir()
    for path in dependencies:
        shutil.copy2(path, mods / path.name)
    (run / 'eula.txt').write_text('eula=true\n')
    (run / 'server.properties').write_text(
        f'server-ip=127.0.0.1\nserver-port={args.port}\nonline-mode=false\nenforce-secure-profile=false\n'
        'view-distance=2\nsimulation-distance=2\nlevel-name=qa-world\nlevel-seed=927436\n'
        'level-type=minecraft:flat\ngenerator-settings={"layers":[{"block":"minecraft:bedrock","height":1},'
        '{"block":"minecraft:grass_block","height":1}],"biome":"minecraft:plains"}\n'
        'generate-structures=false\ndifficulty=peaceful\n')
    command = runtime.base_command('server', run, args.port)
    before = {str(path): runtime.sha(path) for path in dependencies}
    (run / 'launch-audit.json').write_text(json.dumps({'command': command, 'mods': before}, indent=2))
    print('QA artifacts:', run, flush=True)
    with (run / 'console.log').open('w') as log:
        result = subprocess.run(command, cwd=run, stdout=log, stderr=subprocess.STDOUT, timeout=180)
    after = {str(path): runtime.sha(path) for path in dependencies}
    (run / 'integrity.json').write_text(json.dumps({'before': before, 'after': after, 'unchanged': before == after}, indent=2))
    result_path = run / 'result.txt'
    message = result_path.read_text() if result_path.exists() else (run / 'console.log').read_text()[-8000:]
    print(message, flush=True)
    if before != after:
        raise RuntimeError('Input JAR changed during test')
    if result.returncode != 0 or not result_path.exists() or not message.startswith('PASS '):
        raise RuntimeError('Regression failed; inspect result and console.log')


if __name__ == '__main__':
    main()
