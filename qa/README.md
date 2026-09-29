# Standalone Minecraft runtime checks

The elytra toggle fixtures use a built Tool Pouch Fabric 26.3 JAR, JDK 25,
Python 3.11 or newer, and locally installed Minecraft/Fabric runtime files.
They create disposable worlds in ignored fixture directories.
They do not download a game runtime or use an existing gameplay world.

Copy `runtime-config.example.json` to a local configuration file and replace
its illustrative paths with your installed runtime's paths. Supply the complete
Java launch argument arrays from a working Fabric server and client installation,
including **all** runtime libraries, client assets, and native library settings.
The short example classpaths are placeholders, not a complete Minecraft launcher.
The client entry point must be Fabric's `KnotClient`, and the server entry point
must be `KnotServer`. Use matching Minecraft 26.3 and Fabric runtime versions.

- `server_command` and `client_command` are arrays of separate process arguments.
  No shell expansion, environment substitution, or shell evaluation occurs.
  Use absolute paths for command arguments and classpath entries; `java` can be
  resolved from `PATH`. Both commands must expose an explicit Java classpath
  (`-cp`, `-classpath`, or `--class-path`) so the fixture can compile against it.
  Use your operating system's classpath separator (`:` on Linux/macOS, `;` on Windows).
- `{run_dir}` and `{port}` in command arguments are replaced with the disposable
  run directory and selected localhost port. Keep these placeholders in client
  `--gameDir` and `--quickPlayMultiplayer` arguments. The server's port is written
  into its disposable `server.properties`.
- `dependencies` lists the required **mod** JARs: Fabric API, Fzzy Config, and
  Fabric Language Kotlin, at versions compatible with Tool Pouch. These are
  copied into the disposable run's `mods/` directory. Do not include Tool Pouch
  itself: pass its freshly built JAR using `--toolpouch`. Dependency paths may be
  absolute or relative to the configuration file. Java runtime libraries belong
  in the command classpaths instead.

The fixture compilation uses the combined server/client classpaths even for a
server-only test, so both command arrays are required. Do not put session tokens
or other credentials in a checked-in runtime configuration; the disposable
server uses offline mode on localhost.

Run the server regression from the repository root:

```sh
python3 qa/elytra-toggle/run.py \
  --runtime-config /path/to/local-runtime.json \
  --toolpouch versions/26.3-fabric/build/libs/toolpouch-fabric-1.1.10+26.3.jar
```

Each harness writes its exact launch command, console log, assertion result, and
input-JAR integrity audit in its ignored fixture directories. See the individual
fixture README for covered behavior and limitations. Existing local launch
providers remain supported through `--runtime-reference`; this is an alternative
to `--runtime-config`, not an additional requirement.
