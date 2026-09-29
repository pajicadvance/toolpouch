#!/usr/bin/env python3
"""Run Fabric 26.3 client/server regression against an unchanged production JAR."""
import argparse
import importlib.util
import json
import os
from pathlib import Path
import shutil
import subprocess
import time
import zipfile

HERE = Path(__file__).resolve().parent
PROJECT = HERE.parents[1]


def build(launch, mods):
    classes = HERE / 'classes'
    if classes.exists():
        shutil.rmtree(classes)
    classes.mkdir()
    server, vanilla = launch.audits()
    paths = launch.cp(server['command']) + launch.cp(vanilla['command']) + list(map(str, mods))
    for jar in mods:
        with zipfile.ZipFile(jar) as archive:
            for member in archive.namelist():
                if member.endswith('.jar'):
                    path = HERE / 'nested' / Path(member).name
                    path.parent.mkdir(exist_ok=True)
                    path.write_bytes(archive.read(member))
                    paths.append(str(path))
    subprocess.run(['javac', '--release', '25', '-proc:none', '-cp', os.pathsep.join(dict.fromkeys(paths)),
                    '-d', str(classes), *map(str, HERE.glob('*.java'))], check=True)
    for side in ('server', 'client'):
        with zipfile.ZipFile(HERE / f'{side}-qa.jar', 'w') as archive:
            archive.writestr('fabric.mod.json', json.dumps({
                'schemaVersion': 1, 'id': 'elytra_qa_' + side, 'version': '1', 'environment': side,
                'entrypoints': {'main' if side == 'server' else 'client': ['elytraqa.Elytra' + side.title() + 'Qa']},
                'depends': {'toolpouch': '*', 'fabric-api': '*'}}))
            for path in classes.rglob('*' + side.title() + '*.class'):
                archive.write(path, path.relative_to(classes))


def stop(children):
    for child, log, side in reversed(children):
        if child.poll() is None:
            if side == 'server':
                child.stdin.write('stop\n')
                child.stdin.flush()
            else:
                child.terminate()
            try:
                child.wait(timeout=30)
            except subprocess.TimeoutExpired:
                child.kill()
                child.wait()
        log.close()
    children.clear()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    runtime_args = parser.add_mutually_exclusive_group(required=True)
    runtime_args.add_argument('--runtime-reference', type=Path,
                        help='Trusted external Python launcher exposing audits(), cp(), deps(), sha(), base_command(mode, run, port)')
    runtime_args.add_argument('--runtime-config', type=Path,
                              help='JSON launch commands and dependency paths; see ../runtime-config.example.json')
    parser.add_argument('--jar', type=Path, default=PROJECT / 'versions/26.3-fabric/build/libs/toolpouch-fabric-1.1.10+26.3.jar')
    parser.add_argument('--dependency', action='append', type=Path,
                        help='Repeat for Fabric API, Fzzy Config and Fabric Language Kotlin; defaults to reference deps()[1:]')
    parser.add_argument('--port', type=int, default=25676)
    args = parser.parse_args()
    provider = args.runtime_reference.resolve() if args.runtime_reference else HERE.parent / 'runtime_provider.py'
    spec = importlib.util.spec_from_file_location('runtime_reference', provider)
    launch = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(launch)
    if args.runtime_config:
        launch.configure(args.runtime_config)
    mods = [args.jar.resolve(), *[p.resolve() for p in (args.dependency or launch.deps()[1:])]]
    build(launch, mods)
    control = HERE / 'control'
    control.mkdir(exist_ok=True)
    for path in control.glob('*'):
        path.unlink()
    # Only this fixture's ignored profiles are discarded, so each run starts with a new player.
    if (HERE / 'runs').exists():
        shutil.rmtree(HERE / 'runs')
    children = []
    env = os.environ.copy()
    env.update(SDL_VIDEODRIVER='x11', SDL_VIDEO_X11_XINPUT2='0', LP_NUM_THREADS='4')
    hashes = {str(path): launch.sha(path) for path in mods}
    try:
        for stage in (1, 2):
            for mode in ('server', 'native'):
                run = HERE / 'runs' / mode
                run.mkdir(parents=True, exist_ok=True)
                mod_dir = run / 'mods'
                mod_dir.mkdir(exist_ok=True)
                side = 'server' if mode == 'server' else 'client'
                selected = mods + [HERE / f'{side}-qa.jar']
                for path in selected:
                    shutil.copy2(path, mod_dir / path.name)
                if mode == 'server':
                    (run / 'eula.txt').write_text('eula=true\n')
                    (run / 'server.properties').write_text(f'server-ip=127.0.0.1\nserver-port={args.port}\nonline-mode=false\nwhite-list=false\nenforce-secure-profile=false\nview-distance=2\nsimulation-distance=2\nlevel-name=qa-world\nlevel-seed=927436\nlevel-type=minecraft:flat\ngenerator-settings={{"layers":[{{"block":"minecraft:bedrock","height":1}},{{"block":"minecraft:dirt","height":2}},{{"block":"minecraft:grass_block","height":1}}],"biome":"minecraft:plains"}}\ngenerate-structures=false\ndifficulty=peaceful\n')
                else:
                    (run / 'options.txt').write_text('graphicsMode:0\nrenderDistance:3\nsimulationDistance:5\nmaxFps:30\nmaxFpsInactive:30\nsoundCategory_master:0.0\njoinedFirstServer:true\n')
                command = launch.base_command(mode, run, args.port)
                command.insert(1, '-Delytra.qa.control=' + str(control))
                if stage == 2:
                    command.insert(1, '-Delytra.qa.reconnect=true')
                (run / f'launch-audit-{stage}.json').write_text(json.dumps({'command': command, 'mods': [{'path': str(p), 'sha256': launch.sha(p)} for p in selected], 'qa_fixture': True}, indent=2))
                log_path = run / f'console-{stage}.log'
                log = log_path.open('w')
                child = subprocess.Popen(command, cwd=run, env=env, stdin=subprocess.PIPE, stdout=log, stderr=subprocess.STDOUT, text=True)
                children.append((child, log, side))
                if mode == 'server':
                    for _ in range(120):
                        if child.poll() is not None:
                            raise RuntimeError('Server exited: ' + str(log_path))
                        if 'Done (' in log_path.read_text():
                            break
                        time.sleep(1)
                    else:
                        raise TimeoutError('Server startup')
            for _ in range(180):
                result = control / 'result.txt'
                if result.exists():
                    message = result.read_text()
                    print(message, flush=True)
                    if not message.startswith('RECONNECT' if stage == 1 else 'PASS'):
                        raise RuntimeError('Client regression failed')
                    break
                if any(child.poll() is not None for child, _, _ in children):
                    raise RuntimeError('Process exited; inspect console logs')
                time.sleep(1)
            else:
                raise TimeoutError('Client result')
            stop(children)
            if stage == 1:
                for name in ('result.txt', 'command', 'ack'):
                    (control / name).unlink(missing_ok=True)
                # Stage 2 restarts the dedicated server as well as the client, exercising disk persistence.
    finally:
        stop(children)
        after = {str(path): launch.sha(path) for path in mods}
        (HERE / 'integrity.json').write_text(json.dumps({'before': hashes, 'after': after, 'unchanged': hashes == after}, indent=2))
        assert hashes == after, 'Production JAR modified during run'


if __name__ == '__main__':
    main()
