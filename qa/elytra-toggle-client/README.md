# Elytra toggle client/server regression (Fabric 26.3)

Build the production Fabric 26.3 JAR, prepare a runtime configuration as described
in [the QA setup](../README.md), then run:

```sh
python3 qa/elytra-toggle-client/run.py --runtime-config /path/to/runtime.json
```

Use `--jar /path/to/toolpouch.jar` to select a different production build and
`--port 25676` to change the localhost server port. The fixture needs Java/Javac
25, Minecraft 26.3 client and server runtimes, Fabric Loader, Fabric API, Fzzy
Config, Fabric Language Kotlin, and a working graphical display. Starting the
isolated test server accepts Minecraft's EULA for that profile. An existing
trusted Python runtime helper can instead be passed with `--runtime-reference`;
it must expose `audits`, `cp`, `deps`, `base_command`, and `sha` (the first entry
of `deps()` is skipped because it identifies that helper's original project).

The runner compiles separate QA mods and copies the production JAR unchanged
into isolated client/server profiles. It checks:

- The new mapping is registered in Controls and unbound by default.
- Binding it temporarily to F8 and calling vanilla `KeyMapping.click` exercises
  the registered tick callback, production C2S payload, authoritative server
  toggle, vanilla S2C entity data synchronization, and translated actionbar.
- Both inventory pouches and pouches attached to equipped leggings allow flight
  when enabled and deny pouch flight when disabled, on client and server.
- Disabled pouch wings remain available to the cosmetic lookup, while a
  normally equipped chest Elytra remains usable.
- The disabled preference survives player respawn, a clean dedicated-server
  shutdown/restart, and client reconnection; toggling on afterward restores flight.

Flight eligibility is queried through the real protected `canGlide()` method
using reflection. Each query temporarily sets the fixture player's grounded
flag to false and restores it afterward, holding vanilla flight preconditions
constant. The fixture does not call the production toggle handler directly or
write the preference. It does not simulate physical keyboard input, actual
midair flight, rocket boosting, rendering, or third-party accessory providers.

Each invocation resets only this harness's ignored `runs/` profiles and
`control/` evidence. It preserves the first server's world for the second stage.
Assertions are recorded in `control/assertions.txt`; a successful run writes
`control/result.txt`. Launch commands and mod SHA256 hashes are recorded in
`runs/*/launch-audit-*.json`; `integrity.json` confirms all source JARs remained
unchanged. Generated QA JARs, worlds, logs and cached classes are ignored and
are never included in the production build.

## Recorded result

The 2026-09-29 run passed all 68 assertions across both stages using the Fabric
26.3 production JAR. Both processes shut down afterward, and the recorded
integrity check confirmed unchanged production and dependency JAR hashes.
