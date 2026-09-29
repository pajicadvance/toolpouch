# Elytra toggle server regression

This development-only Fabric 26.3 fixture runs production classes and mixins in a
real dedicated server. It verifies flight eligibility, midflight disabling,
vanilla chest elytra durability, disabled pouch contents, inventory and leggings
pouches, inventory policy, persistence and legacy defaults, respawn/transfer
copying, broken elytra, and independent player preferences.

```sh
python3 qa/elytra-toggle/run.py \
  --runtime-config /path/to/local-runtime.json \
  --toolpouch versions/26.3-fabric/build/libs/toolpouch-fabric-1.1.10+26.3.jar
```

See [runtime setup](../README.md) for the checked-in JSON runtime provider.
Alternatively, `--runtime-reference /path/to/cached-runtime-provider.py` accepts
an existing local provider. Requires Python 3.11+, JDK 25 and a provider with `audits()`
(server and vanilla-client launch audit dictionaries), `cp(command)`, `deps()`
(the provider's main mod followed by shared runtime dependencies),
`base_command('server', run_directory, port)` and `sha(path)` functions.
The provider is explicitly supplied because Minecraft runtime caches are local
to each developer. No downloads or production world changes are performed.

Each run creates a new ignored `build/run-*` directory with its console log,
result, launch audit, and input-JAR integrity record. Fixture JARs stay outside
release packages. This fixture does not test physical keyboard input, GUI,
client networking, or optional Aileron compatibility.
