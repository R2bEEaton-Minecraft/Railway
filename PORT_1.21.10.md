# Fabric port to Minecraft 1.21.10

Base: `1.21/dev`, `4d5274c3b` (`gametest: add copycat headstock custom texture tests`).
Implementation branch: `1.21.10/dev` in the supplied linked worktree.
The main checkout, protected branches, and intentionally excluded legacy mixins/DFU implementations were left alone. No merge, cherry-pick, push, or PR was performed.

## Build inputs

| Input | Version / coordinate |
| --- | --- |
| Minecraft | `com.mojang:minecraft:1.21.10` |
| Mojang mappings | Official 1.21.10, layered with Parchment |
| Parchment | `org.parchmentmc.data:parchment-1.21.10:2025.10.12@zip` |
| Create Fly | `maven.modrinth:create-fly:1.21.10-6.0.9-2` |
| Fabric API | `net.fabricmc.fabric-api:fabric-api:0.138.4+1.21.10` |
| Fabric loader | `net.fabricmc:fabric-loader:0.18.4` (retained) |
| Architectury Loom | `1.17.487` (retained) |
| Java | Temurin `21.0.8+9`; Java 21 toolchain retained |
| Gradle wrapper | `9.6.1` (retained) |
| JEI, enabled in development | `maven.modrinth:jei:26.3.0.31` |
| REI, disabled | `21.9.813+fabric` |
| Sodium, disabled | `mc1.21.10-0.7.3-fabric` |
| Iris, disabled | `1.9.7+1.21.10-fabric` |
| Mixin Extras | `0.5.3` (retained) |

Target versions were checked against the publishers' metadata:

- [Create Fly 1.21.10 releases](https://api.modrinth.com/v2/project/create-fly/version?game_versions=%5B%221.21.10%22%5D&loaders=%5B%22fabric%22%5D)
- [Fabric API artifact](https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/0.138.4+1.21.10/fabric-api-0.138.4+1.21.10.pom)
- [Parchment 1.21.10 metadata](https://maven.parchmentmc.org/org/parchmentmc/data/parchment-1.21.10/maven-metadata.xml)
- [JEI 1.21.10 releases](https://api.modrinth.com/v2/project/jei/version?game_versions=%5B%221.21.10%22%5D&loaders=%5B%22fabric%22%5D)

Create resolves from the existing Modrinth repository; Fabric API resolves from Fabric Maven. The existing development-only Create descriptor patch is retained with the correct target signature. No additional `local-maven` content or copy from the main checkout was needed. Old disabled compatibility pins and the existing voice-chat compile-only inputs remain inherited; those optional integrations were not certified for 1.21.10.

`mod_version` remains `0.1.5j-beta`. The existing archive convention produces `0.1.5j-beta+fabric-mc1.21.10`. Expanded metadata requires Minecraft `>=1.21.10 <1.21.11` and Create `>=6.0.9-2`.

## Source changes

The mechanical change reverses `Identifier` to `ResourceLocation`, including Java types/imports and string descriptors. Packet buffer methods return to `readResourceLocation`/`writeResourceLocation`; JEI's recipe key uses `location()`.

Other API changes were checked against the actual 1.21.10 Loom jars:

- Restore Minecraft's `critereon` package and the earlier `AbstractGolem`, `AbstractMinecart`, `Arrow`, and `GameRules` package locations.
- Remove six compatibility shims whose fully qualified names are supplied by 1.21.10: `ResourceLocation`, `ResourceLocationArgument`, `ResourceLocationException`, `RenderType`, `Util`, and `MethodsReturnNonnullByDefault`. The Registrate shim now uses the actual resource ID directly; callers no longer convert its ID through `toIdentifier()`.
- Restore `MinecartJukebox.activateMinecart(int, int, int, boolean)` and the `getBoolean(GameRules.RULE_DOENTITYDROPS)` gamerule query.
- Replace 1.21.11's `RenderSetup`/separate `RenderTypes` factories with 1.21.10's `RenderType` factories. The old block cutout pipelines use `RenderType.cutout()`. Create's factory class is `RenderTypes`, with its translucent/additive semaphore passes preserved.
- Remove calls to abstract `super.submit` in the four block-entity renderers. Their render-state extraction and own geometry submissions remain.
- Rebuild casing quads from cloned packed `int[]` vertex data. Remap each vertex's UV floats without modifying the cached source quad, preserving its tint, face, shading, and light emission.
- Adapt copycat headstock cropping to Create's `cropAndMove(int[], sprite, bounds, offset)` and wrap its result in a quad with the original metadata, including Create's custom-normal marker. A diagnostic client test reproduced marker loss on two cropped quads before the correction.
- Use Create's `PonderScene.getWorld()` API.
- Correct Create's development `MinecraftClientMixin` descriptor to `disconnect(Screen, boolean)` (`method_18096`), rather than 1.21.11's additional boolean parameter. The Entity riding descriptor already matches 1.21.10 and is retained.
- Remove the unused access-widener entry for the absent 1.21.11 `RenderType.create(String, RenderSetup)` method. All other access-widener entries pass validation.
- Pass tint and outline color explicitly in both cap renderers' full `submitModel` call. The short overload's integer is an outline color; `-1` made the vanilla outline-capable render type cover every cap with a white silhouette. Controlled before/after captures reproduced and verified this fix for conductor caps and player-worn caps.

Mixin changes:

- `BlockElementDeserializerMixin`: intercept instance `getFrom` and `getTo`, retaining extended model bounds while preserving vector parsing validation.
- `MixinRadialToolboxMenu`: use an instance handler for the non-static disposal lambda.
- `MixinSlidingDoorRenderer`: wrap `updateRenderState` in Create Fly 6.0.9-2.
- `MixinToolboxHandlerClient`: adjust the overlay's conductor position at the existing `ToolboxHandler.getMaxRange` invocation; remove the ineffective optional injection into the absent old NBT reader.
- `NbtUtilsMixin`: explicitly select the `CompoundTag` overload of `addDataVersion`.

The registered mixin lists and all Gradle source exclusion lists are unchanged. No previously excluded implementation was restored. The 1.21.8 branch was consulted as an API/NBT reference only.

## Resource and test changes

- Add particle slots/default texture bindings to 98 model JSONs, including two shared bogey texture parents. This resolves 105 missing model texture-reference warnings and two unresolved template texture warnings without changing faces or geometry.
- Supply the shared conductor-cap item templates with a default `cap` texture; colored child overrides remain effective.
- Update the three built-in resource packs from legacy format 15 to Minecraft 1.21.10's resource format 69 using the mandatory `min_format`/`max_format` fields. All three metadata sections were decoded successfully by Minecraft's own `PackMetadataSection.CLIENT_TYPE` codec. The target jar reports resource 69.0 and data 88.0; 1.21.11 reports 75.0/94.1.
- The existing singular `loot_table`, recipe, and tag directory conventions remain valid on 1.21.10. No resource-folder migration or bulk datagen overwrite was needed.
- Adapt gametest assertion messages to the `Component` API while retaining the original 49 tests.
- Add a coupler save/reload test covering independent primary/secondary IDs and target positions, secondary orientation and curve segment/location, and edge spacing. This exercises the Create 6.0.9-2 NBT path rather than relying on compilation alone.
- Remote-lens behavior is unchanged: equip goggles to make the conductor a SPY before binding the lens. The existing binding test passes.

## Verification evidence

Commands used `gradlew.bat` on Windows, equivalent to the repository's `./gradlew` entry point. Java 21 was active throughout. Offline mode initially failed because target Create was not cached; online mode was used to fetch required dependencies and subsequent runs used offline mode.

| Check | Observed result |
| --- | --- |
| `--offline compileJava` | Passed independently and in the final combined verification command |
| `--offline :fabric:runGametest` | Original 49/49 passed; final suite including the new test: **50/50 required passed**, 1.087 s test execution |
| `--offline :fabric:runGametestClient` | Normal client reached the world and exited successfully; follow-up isolated render-smoke runs verified the final cap and model corrections |
| Final client render smoke | World joined, conductor and player screenshots captured, clean shutdown; **0 missing/unresolved model textures, 0 mixin application failures, 0 InjectionErrors** |
| Adapted mixin checker | **69 registered mixins, 0 problems**, covering both actual configs on this baseline |
| `--offline build` | **Passed**, 22 tasks, including both access-widener validations and Fabric remapping |
| `--offline :fabric:runDatagen` | Passed; also passed on a separate clean `4d5274c3b` checkout before running on the port |
| Final combined compile/gametest/datagen/build | **Passed in 1m 50s**, 31 tasks; datagen wrote 0 changed files on its last run |
| Packaged `fabric.mod.json` | Inspected inside the remapped Fabric jar; correct MC/Create/API/loader ranges and MC archive suffix |

Datagen output stays under `fabric/build/generated/datagen`; it did not replace checked-in resources. The checker was copied/adapted under `F:/tmp/rr12110/checker` without editing `F:/tmp/rr/checker`. It uses the target remapped Minecraft/Create/Fabric classpath and Java 25 only to run the standalone checker, not to compile the mod.

Local evidence is retained under `F:/tmp/rr12110`: `verified-release.log`, `verification-final.log`, `build-2.log`, `client-normals-green.log`, `mixins-release.log`, `pack-metadata-check.log`, `baseline-datagen-2.log`, dependency metadata, checker sources, `client-normals-red.log`, and `render-smoke/screenshots/{caps,player-cap}.png`. The diagnostic mod is external to the repository and absent from the shipped jar. The custom-normal regression was observed failing (`COPYCAT_NORMALS_PRESERVED=false quads=2`) before the fix and passing (`true quads=2`) afterward.

## Review decisions

An independent read-only review found no Critical issues. Both reported issues were addressed: mandatory resource-pack format bounds, and loss of custom-normal metadata when cropping headstock quads. The latter was treated as a required backport parity correction and verified with a failing/passing client check. No review findings remain deferred.

The abstract superclass `submit` calls stay removed: these four block entities have no filter/link behaviours requiring an extra submission pass. Adding such behaviours later would require rendering them explicitly. Existing casing-normal handling and intentionally excluded functionality were retained; their broader visual/feature coverage remains a manual limit. The requested compile/runtime suite was used as port regression coverage rather than adding tests of mechanical renames.

## Limits and manual checks

Startup and server verification are green. Optional REI/Sodium/Iris/other compatibility integrations were not run. A standalone production launcher was not exercised; the release jar was remapped, validated, and its metadata inspected.

The development logs still include inherited mod-version SemVer advice, Fabric legacy/untranslated-tag warnings, and JEI's resource-reloader registration warning. During the earlier manual Ponder session, an old deployer structure reported legacy `Inventory`/air-`Filter` decode warnings; those unchanged templates were not part of the final startup smoke or baseline runtime comparison. No DFU implementation was restored to address them.

Manual follow-up:

1. Wear several cap colors and inspect conductors' caps while turning/looking up and down; also check glowing outlines.
2. Give a conductor goggles, bind and use the remote lens, move/interact, then exit possession.
3. Test gauge/bogey selection, train assembly, switches, coupling/decoupling, and a save/reload of targeted couplers.
4. Inspect curved track casings, textured copycat headstocks, semaphores, folding doors, and the optional built-in resource packs; exercise Ponder scenes separately.
