"""Explicit local runtime configuration shared by the standalone QA launchers."""
import hashlib
import json
import os
from pathlib import Path

_config = None
_dependencies = None


def configure(config_path):
    """Read launch argument arrays and dependency JARs; never execute shell text."""
    global _config, _dependencies
    config_path = Path(config_path).expanduser().resolve()
    config = json.loads(config_path.read_text())
    if not isinstance(config, dict):
        raise ValueError('Runtime configuration must be a JSON object')
    for key in ('server_command', 'client_command'):
        command = config.get(key)
        if not isinstance(command, list) or not command or any(not isinstance(part, str) for part in command):
            raise ValueError(f'{key} must be a nonempty array of command arguments')
        cp(command)
    dependencies = config.get('dependencies')
    if not isinstance(dependencies, list) or not dependencies or any(not isinstance(path, str) for path in dependencies):
        raise ValueError('dependencies must be a nonempty array of dependency JAR paths')
    resolved = []
    for value in dependencies:
        path = Path(value).expanduser()
        if not path.is_absolute():
            path = config_path.parent / path
        path = path.resolve()
        if not path.is_file():
            raise FileNotFoundError(path)
        resolved.append(path)
    _config = config
    _dependencies = resolved


def _configured():
    if _config is None:
        raise RuntimeError('Call configure(runtime_config_path) before using this provider')
    return _config


def audits():
    config = _configured()
    return {'command': list(config['server_command'])}, {'command': list(config['client_command'])}


def cp(command):
    """Extract Java's explicit classpath for fixture compilation and launch audits."""
    for index, argument in enumerate(command):
        if argument in ('-cp', '-classpath', '--class-path'):
            if index + 1 >= len(command) or not command[index + 1]:
                raise ValueError('Java classpath option must have a nonempty value')
            return command[index + 1].split(os.pathsep)
        if argument.startswith('--class-path='):
            value = argument.partition('=')[2]
            if not value:
                raise ValueError('Java classpath option must have a nonempty value')
            return value.split(os.pathsep)
    raise ValueError('Launch commands need an explicit -cp, -classpath, or --class-path argument')


def deps():
    _configured()
    # Historical providers list their own mod first; each fixture supplies Tool Pouch itself.
    return [None, *_dependencies]


def base_command(mode, run, port):
    config = _configured()
    key = {'server': 'server_command', 'native': 'client_command'}.get(mode)
    if key is None:
        raise ValueError(f'Unsupported runtime mode: {mode}')
    run = str(Path(run).resolve())
    return [argument.replace('{run_dir}', run).replace('{port}', str(port)) for argument in config[key]]


def sha(path):
    with Path(path).open('rb') as source:
        return hashlib.file_digest(source, 'sha256').hexdigest()
