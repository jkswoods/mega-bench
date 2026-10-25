#!/usr/bin/env python3
"""Build Mega Bench from source and separately supplied, pinned native dependencies."""
from pathlib import Path
import argparse
import hashlib
import json
import os
import shutil
import subprocess
import zipfile
from release_pipeline import config, filename

ROOT = Path(__file__).resolve().parents[1]


def digest(data):
    return hashlib.sha256(data).hexdigest()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--server-jar', type=Path, default=os.environ.get('MB_SERVER_JAR'))
    parser.add_argument('--java-home', type=Path, default=os.environ.get('MB_JAVA_HOME') or os.environ.get('JAVA_HOME'))
    args = parser.parse_args()
    if not args.server_jar:
        parser.error('Supply --server-jar from an official Hytale installation.')
    pin = json.loads((ROOT / 'config/target.json').read_text())
    if digest(args.server_jar.read_bytes()) != pin['server_sha256']:
        parser.error('Server JAR does not match the pinned Hytale build in config/target.json.')
    javac = str(args.java_home / 'bin/javac') if args.java_home else shutil.which('javac')
    java = str(args.java_home / 'bin/java') if args.java_home else shutil.which('java')
    if not javac or not java:
        parser.error('JDK 25 is required; set --java-home, MB_JAVA_HOME or JAVA_HOME.')
    version = subprocess.check_output([javac, '-version'], text=True).strip()
    if not version.startswith('javac 25'):
        parser.error('This build requires JDK 25.')
    classes = ROOT / 'build/classes'
    if classes.exists():
        shutil.rmtree(classes)
    classes.mkdir(parents=True)
    sources = sorted((ROOT / 'src/main/java').rglob('*.java'))
    subprocess.run([javac, '--release', '25', '-Xlint:all', '-cp', str(args.server_jar),
                    '-d', str(classes), *map(str, sources)], check=True)
    test_classes = ROOT / 'build/test-classes'
    test_classes.mkdir(parents=True, exist_ok=True)
    classpath = os.pathsep.join([str(classes), str(args.server_jar)])
    tests = sorted((ROOT / 'src/test/java').rglob('*.java'))
    subprocess.run([javac, '--release', '25', '-cp', classpath, '-d', str(test_classes),
                    *map(str, tests)], check=True)
    for test in ['BridgeStateTest', 'CompatibilityTest']:
        subprocess.run([java, '-ea', '-cp', os.pathsep.join([str(test_classes), classpath]),
                        'dev.megabench.' + test], check=True)

    entries = {}
    for folder in [classes, ROOT / 'src/main/resources']:
        for path in sorted(folder.rglob('*')):
            if path.is_file():
                entries[path.relative_to(folder).as_posix()] = path.read_bytes()
    mod = json.loads(entries['manifest.json'])
    output = ROOT / 'build/libs' / filename(config(ROOT/'release.json'))
    output.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(output, 'w', zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
        for name, data in sorted(entries.items()):
            info = zipfile.ZipInfo(name, (2026, 10, 23, 0, 0, 0))
            info.compress_type = zipfile.ZIP_DEFLATED
            info.external_attr = 0o100644 << 16
            archive.writestr(info, data)
    print(output.relative_to(ROOT))
    print('SHA256 ' + digest(output.read_bytes()))


if __name__ == '__main__':
    main()
