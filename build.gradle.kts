/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.architectury.plugin.ArchitectPluginExtension
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import me.modmuss50.mpp.ModPublishExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.VarInsnNode
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.Deflater
import dev.ithundxr.silk.ChangelogText
import me.modmuss50.mpp.ReleaseType

plugins {
    java
    `maven-publish`
    id("architectury-plugin") version "3.5.169"
    id("dev.architectury.loom") version "1.17.487" apply false
    id("me.modmuss50.mod-publish-plugin") version "0.7.4" apply false // https://github.com/modmuss50/mod-publish-plugin
    id("com.gradleup.shadow") version "9.4.3" apply false
    id("dev.ithundxr.silk") version "0.11.15" // https://github.com/IThundxr/silk
    id("net.kyori.blossom") version "2.1.0" apply false // https://github.com/KyoriPowered/blossom
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.8" // https://github.com/JetBrains/gradle-idea-ext-plugin
}

println("Steam 'n' Rails v${"mod_version"()}")

val isRelease = System.getenv("RELEASE_BUILD")?.toBoolean() ?: false
val buildNumber = System.getenv("GITHUB_RUN_NUMBER")?.toInt()
// whether dev mixins should be stripped, even if it's not a release build
val removeDevMixinAnyway = System.getenv("REMOVE_DEV_MIXIN_ANYWAY")?.toBoolean() ?: false
// whether the build should include dev commands, even in a non-dev environment
val includeDevCommands = !isRelease && System.getenv("INCLUDE_DEV_COMMANDS")?.toBoolean() ?: false
val gitHash = "\"${calculateGitHash() + (if (hasUnstaged()) "-modified" else "")}\""

repositories {
    maven("https://maven.ithundxr.dev/mirror")
    maven("https://mvn.devos.one/snapshots/")
    maven("https://api.modrinth.com/maven") {
        content {
            includeGroup("maven.modrinth")
        }
    }
}

val patchedCreateFlyJar = layout.projectDirectory.file(
    "local-maven/local/createfly/create-fly/${"create_fabric_version"()}/create-fly-${"create_fabric_version"()}.jar"
)

fun resolveCreateFlyDevJar(): File {
    return configurations.detachedConfiguration(
        dependencies.create("maven.modrinth:create-fly:${"create_fabric_version"()}")
    ).also {
        it.isTransitive = false
    }.singleFile
}

fun patchCreateFlyDevJar(sourceJar: File, outputFile: File) {
    outputFile.parentFile.mkdirs()

    JarFile(sourceJar).use { jar ->
        JarOutputStream(outputFile.outputStream()).use { out ->
            jar.entries().asIterator().forEach { entry ->
                if (entry.isDirectory)
                    return@forEach

                var data = jar.getInputStream(entry).readAllBytes()
                if (entry.name.endsWith(".class"))
                    data = patchCreateFlyMixinDescriptors(entry.name, data)
                if (entry.name == "com/zurrtum/create/client/model/obj/ObjGeometry\$ModelMesh.class")
                    data = patchCreateFlyObjTextureFallback(data)
                if (entry.name == "create.mixins.json")
                    data = data.toString(Charsets.UTF_8)
                        .replace(Regex("(?m)^\\s*\\\"UtilMixin\\\",?\\r?\\n"), "")
                        .toByteArray(Charsets.UTF_8)
                if (entry.name == "create.client.mixins.json")
                    data = data.toString(Charsets.UTF_8)
                        .replace(Regex("(?m)^\\s*\\\"MinecraftClientMixin\\\",?\\r?\\n"), "")
                        .toByteArray(Charsets.UTF_8)

                out.putNextEntry(JarEntry(entry.name))
                out.write(data)
                out.closeEntry()
            }
        }
    }
}

fun patchCreateFlyObjTextureFallback(bytes: ByteArray): ByteArray {
    val node = ClassNode()
    ClassReader(bytes).accept(node, 0)
    node.methods.filter { it.name == "addQuads" }.forEach { method ->
        method.instructions.toArray()
            .filterIsInstance<FieldInsnNode>()
            .filter {
                it.opcode == Opcodes.GETFIELD &&
                    it.owner == "com/zurrtum/create/client/model/obj/ObjMaterialLibrary\$Material" &&
                    it.name == "diffuseColorMap"
            }
            .forEach { field ->
                val present = LabelNode()
                method.instructions.insert(field, InsnList().apply {
                    add(InsnNode(Opcodes.DUP))
                    add(JumpInsnNode(Opcodes.IFNONNULL, present))
                    add(InsnNode(Opcodes.POP))
                    add(LdcInsnNode("#particle"))
                    add(present)
                })
            }
    }
    return ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS).also(node::accept).toByteArray()
}

if (!patchedCreateFlyJar.asFile.isFile) {
    patchCreateFlyDevJar(resolveCreateFlyDevJar(), patchedCreateFlyJar.asFile)
}

val patchCreateFlyDevJarTask = tasks.register("patchCreateFlyDevJar") {
    outputs.file(patchedCreateFlyJar)

    doLast {
        patchCreateFlyDevJar(resolveCreateFlyDevJar(), patchedCreateFlyJar.asFile)
    }
}
val patchedCreateFlyFiles = files(patchedCreateFlyJar).also {
    it.builtBy(patchCreateFlyDevJarTask)
}
extra["patchedCreateFlyFiles"] = patchedCreateFlyFiles
extra["patchedCreateFlyDependency"] = "local.createfly:create-fly:${"create_fabric_version"()}"

val patchedRegistrateVersion = "MC1.20-1.3.11-fabric-dev"
val patchedRegistrateJar = layout.projectDirectory.file(
    "local-maven/local/registrate/Registrate/$patchedRegistrateVersion/Registrate-$patchedRegistrateVersion.jar"
)

fun resolveRegistrateJar(): File = configurations.detachedConfiguration(
    dependencies.create("com.tterrag.registrate_fabric:Registrate:1.3.79-MC1.20.1")
).also { it.isTransitive = false }.singleFile

fun patchRegistrateJar(sourceJar: File, outputFile: File) {
    outputFile.parentFile.mkdirs()
    JarFile(sourceJar).use { jar ->
        JarOutputStream(outputFile.outputStream()).use { out ->
            jar.entries().asIterator().forEach { entry ->
                if (!entry.isDirectory && entry.name != "fabric.mod.json") {
                    out.putNextEntry(JarEntry(entry.name))
                    var data = jar.getInputStream(entry).readAllBytes()
                    if (entry.name.endsWith(".class"))
                        data = patchRegistrateIntermediaryCalls(data)
                    if (entry.name == "registrate-fabric.mixins.json")
                        data = data.toString(Charsets.UTF_8)
                            .replace(Regex("(?m)^\\s*\\\"LootTableProviderMixin\\\",?\\r?\\n"), "")
                            .replace(Regex("(?m)^\\s*\\\"accessor\\.SpawnPlacementsAccessor\\\",?\\r?\\n"), "")
                            .toByteArray(Charsets.UTF_8)
                    if (entry.name == "com/tterrag/registrate/fabric/RegistryObject.class")
                        data = patchRegistrateRegistryObject(data)
                    if (entry.name == "com/tterrag/registrate/builders/BlockBuilder.class")
                        data = patchRegistrateBlockBuilderIds(patchRegistrateBlockBuilder(data))
                    if (entry.name == "com/tterrag/registrate/builders/ItemBuilder.class")
                        data = patchRegistrateItemBuilderIds(data)
                    if (entry.name == "com/tterrag/registrate/builders/EntityBuilder.class")
                        data = patchRegistrateEntityBuilderIds(data)
                    if (entry.name == "com/tterrag/registrate/builders/BlockEntityBuilder.class")
                        data = patchRegistrateBlockEntityBuilder(data)
                    if (entry.name == "com/tterrag/registrate/builders/MenuBuilder.class")
                        data = patchRegistrateMenuBuilder(data)
                    out.write(data)
                    out.closeEntry()
                }
            }
            out.putNextEntry(JarEntry("fabric.mod.json"))
            out.write(jar.getInputStream(jar.getJarEntry("fabric.mod.json")).readAllBytes())
            out.closeEntry()
        }
    }
}

fun patchRegistrateIntermediaryCalls(bytes: ByteArray): ByteArray {
    val node = ClassNode()
    ClassReader(bytes).accept(node, 0)
    node.methods.forEach { method ->
        method.instructions.iterator().forEachRemaining { insn ->
            if (insn is MethodInsnNode) {
                if (insn.name == "method_30517") insn.name = "key"
                if (insn.owner == "net/minecraft/class_2378" && insn.name == "method_10223") insn.name = "getValue"
            }
            if (insn is FieldInsnNode && insn.opcode == Opcodes.GETSTATIC &&
                insn.owner == "net/minecraft/class_1723" && insn.name == "field_21668") {
                insn.owner = "net/minecraft/client/renderer/texture/TextureAtlas"
                insn.name = "LOCATION_BLOCKS"
            }
        }
    }
    return ClassWriter(0).also { node.accept(it) }.toByteArray()
}

fun patchRegistrateRegistryObject(bytes: ByteArray): ByteArray {
    val node = ClassNode()
    ClassReader(bytes).accept(node, 0)
    node.methods.forEach { method ->
        method.instructions.iterator().forEachRemaining { insn ->
            if (insn is MethodInsnNode && insn.owner == "net/minecraft/class_2378" && insn.name == "method_10223")
                insn.name = "getValue"
        }
    }
    return ClassWriter(0).also { node.accept(it) }.toByteArray()
}

fun patchRegistrateBlockBuilder(bytes: ByteArray): ByteArray {
    val node = ClassNode()
    ClassReader(bytes).accept(node, 0)
    node.methods.forEach { method ->
        method.instructions.iterator().forEachRemaining { insn ->
            if (insn is MethodInsnNode && insn.owner == "net/minecraft/class_2248" && insn.name == "method_9539")
                insn.name = "getDescriptionId"
            if (insn is InvokeDynamicInsnNode) {
                insn.bsmArgs = insn.bsmArgs.map { arg ->
                    if (arg is Handle && arg.name == "method_9539")
                        Handle(arg.tag, arg.owner, "getDescriptionId", arg.desc, arg.isInterface)
                    else arg
                }.toTypedArray()
            }
        }
    }
    return ClassWriter(0).also { node.accept(it) }.toByteArray()
}

fun patchRegistrateBlockBuilderIds(bytes: ByteArray): ByteArray {
    val node = ClassNode()
    ClassReader(bytes).accept(node, 0)
    val method = node.methods.firstOrNull { it.name == "createEntry" && it.desc == "()Lnet/minecraft/class_2248;" }
        ?: return bytes
    val target = method.instructions.iterator().asSequence().filterIsInstance<MethodInsnNode>()
        .firstOrNull { it.owner == "com/tterrag/registrate/util/nullness/NonNullFunction" && it.name == "apply" }
        ?: return bytes
    val code = InsnList().apply {
        add(VarInsnNode(Opcodes.ALOAD, 1))
        add(FieldInsnNode(Opcodes.GETSTATIC, "net/minecraft/core/registries/Registries", "BLOCK", "Lnet/minecraft/resources/ResourceKey;"))
        add(VarInsnNode(Opcodes.ALOAD, 0))
        add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, node.name, "getOwner", "()Lcom/tterrag/registrate/AbstractRegistrate;", false))
        add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/tterrag/registrate/AbstractRegistrate", "getModid", "()Ljava/lang/String;", false))
        add(VarInsnNode(Opcodes.ALOAD, 0))
        add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, node.name, "getName", "()Ljava/lang/String;", false))
        add(MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/resources/ResourceLocation", "fromNamespaceAndPath", "(Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;", false))
        add(MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/resources/ResourceKey", "create", "(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/resources/ResourceKey;", false))
        add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/level/block/state/BlockBehaviour\$Properties", "setId", "(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/world/level/block/state/BlockBehaviour\$Properties;", false))
        add(InsnNode(Opcodes.POP))
    }
    method.instructions.insertBefore(target, code)
    return ClassWriter(ClassWriter.COMPUTE_MAXS).also { node.accept(it) }.toByteArray()
}

fun patchRegistrateItemBuilderIds(bytes: ByteArray): ByteArray {
    val node = ClassNode()
    ClassReader(bytes).accept(node, 0)
    val method = node.methods.firstOrNull { it.name == "createEntry" && it.desc == "()Lnet/minecraft/class_1792;" }
        ?: return bytes
    val target = method.instructions.iterator().asSequence().filterIsInstance<MethodInsnNode>()
        .firstOrNull { it.owner == "com/tterrag/registrate/util/nullness/NonNullFunction" && it.name == "apply" }
        ?: return bytes
    val code = InsnList().apply {
        add(VarInsnNode(Opcodes.ALOAD, 1))
        add(FieldInsnNode(Opcodes.GETSTATIC, "net/minecraft/core/registries/Registries", "ITEM", "Lnet/minecraft/resources/ResourceKey;"))
        add(VarInsnNode(Opcodes.ALOAD, 0))
        add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, node.name, "getOwner", "()Lcom/tterrag/registrate/AbstractRegistrate;", false))
        add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/tterrag/registrate/AbstractRegistrate", "getModid", "()Ljava/lang/String;", false))
        add(VarInsnNode(Opcodes.ALOAD, 0))
        add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, node.name, "getName", "()Ljava/lang/String;", false))
        add(MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/resources/ResourceLocation", "fromNamespaceAndPath", "(Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;", false))
        add(MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/resources/ResourceKey", "create", "(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/resources/ResourceKey;", false))
        add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/item/Item\$Properties", "setId", "(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/world/item/Item\$Properties;", false))
        add(InsnNode(Opcodes.POP))
    }
    method.instructions.insertBefore(target, code)
    return ClassWriter(ClassWriter.COMPUTE_MAXS).also { node.accept(it) }.toByteArray()
}

fun patchRegistrateEntityBuilderIds(bytes: ByteArray): ByteArray {
    val node = ClassNode()
    ClassReader(bytes).accept(node, 0)
    val method = node.methods.firstOrNull { it.name == "createEntry" } ?: return bytes
    val target = method.instructions.iterator().asSequence().filterIsInstance<MethodInsnNode>()
        .firstOrNull { it.owner == "net/fabricmc/fabric/api/object/builder/v1/entity/FabricEntityTypeBuilder" && it.name == "build" && it.desc == "()Lnet/minecraft/class_1299;" }
        ?: return bytes
    val code = InsnList().apply {
        add(FieldInsnNode(Opcodes.GETSTATIC, "net/minecraft/core/registries/Registries", "ENTITY_TYPE", "Lnet/minecraft/resources/ResourceKey;"))
        add(VarInsnNode(Opcodes.ALOAD, 0))
        add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, node.name, "getOwner", "()Lcom/tterrag/registrate/AbstractRegistrate;", false))
        add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/tterrag/registrate/AbstractRegistrate", "getModid", "()Ljava/lang/String;", false))
        add(VarInsnNode(Opcodes.ALOAD, 0))
        add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, node.name, "getName", "()Ljava/lang/String;", false))
        add(MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/resources/ResourceLocation", "fromNamespaceAndPath", "(Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;", false))
        add(MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/resources/ResourceKey", "create", "(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/resources/ResourceKey;", false))
    }
    method.instructions.insertBefore(target, code)
    target.desc = "(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/class_1299;"
    return ClassWriter(ClassWriter.COMPUTE_MAXS).also { node.accept(it) }.toByteArray()
}

fun patchRegistrateBlockEntityBuilder(bytes: ByteArray): ByteArray {
    val source = ClassReader(bytes)
    val node = ClassNode()
    source.accept(ClassRemapper(node, object : Remapper() {
        override fun map(internalName: String): String = when (internalName) {
            "net/minecraft/class_2591\$class_2592" -> "net/fabricmc/fabric/api/object/builder/v1/block/entity/FabricBlockEntityTypeBuilder"
            "net/minecraft/class_2591\$class_5559" -> "net/fabricmc/fabric/api/object/builder/v1/block/entity/FabricBlockEntityTypeBuilder\$Factory"
            else -> internalName
        }
        override fun mapMethodName(owner: String, name: String, descriptor: String): String = when {
            owner == "net/minecraft/class_2591\$class_2592" && name == "method_20528" -> "create"
            owner == "net/minecraft/class_2591\$class_2592" && name == "method_11034" -> "build"
            else -> name
        }
    }), 0)
    return ClassWriter(0).also { node.accept(it) }.toByteArray()
}

fun patchRegistrateMenuBuilder(bytes: ByteArray): ByteArray {
    val node = ClassNode()
    ClassReader(bytes).accept(node, 0)
    node.methods.forEach { method ->
        val calls = method.instructions.iterator().asSequence().filterIsInstance<MethodInsnNode>().toList()
        calls.filter { it.owner == "net/fabricmc/fabric/api/screenhandler/v1/ExtendedScreenHandlerType" && it.name == "<init>" && !it.desc.contains("StreamCodec") }
            .forEach { call ->
                val code = InsnList().apply {
                    add(InsnNode(Opcodes.ACONST_NULL))
                    add(MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/network/codec/StreamCodec", "unit", "(Ljava/lang/Object;)Lnet/minecraft/network/codec/StreamCodec;", true))
                }
                method.instructions.insertBefore(call, code)
                call.desc = "(Lnet/fabricmc/fabric/api/screenhandler/v1/ExtendedScreenHandlerType\$ExtendedFactory;Lnet/minecraft/network/codec/StreamCodec;)V"
            }
    }
    return ClassWriter(ClassWriter.COMPUTE_MAXS).also { node.accept(it) }.toByteArray()
}

fun patchRegistrateMinecraftNames(bytes: ByteArray): ByteArray {
    val node = ClassNode()
    ClassReader(bytes).accept(node, 0)
    node.methods.forEach { method ->
        method.instructions.iterator().forEachRemaining { insn ->
            if (insn is FieldInsnNode && insn.owner == "net/minecraft/world/item/CreativeModeTabs" && insn.name == "f_256750_")
                insn.name = "SEARCH"
        }
    }
    return ClassWriter(0).also { node.accept(it) }.toByteArray()
}

if (!patchedRegistrateJar.asFile.isFile)
    patchRegistrateJar(resolveRegistrateJar(), patchedRegistrateJar.asFile)

extra["patchedRegistrateDependency"] = "local.registrate:Registrate:$patchedRegistrateVersion"

if (!isRelease && removeDevMixinAnyway) {
    println("Removing dev mixins, even though it's not a release build")
}

if (includeDevCommands) {
    println("Including dev commands in build")
}

extra["gitHash"] = gitHash
extra["includeDevCommands"] = includeDevCommands

architectury {
    minecraft = "minecraft_version"()
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "architectury-plugin")
    apply(plugin = "maven-publish")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    base.archivesName.set("archives_base_name"())
    group = "maven_group"()

    // Formats the mod version to include the loader and Minecraft version.
    // example: 1.0.0+fabric-mc1.19.2

    var gitBranchLabel = "";
    if (!isRelease && "mod_version"().endsWith("-alpha")) {
        // gitBranchLabel should be "-" + the current git branch (replacing any slashes with underscores)
        gitBranchLabel = "-" + calculateGitBranch().replace("/", "_")
    }

    version = "${"mod_version"()}${gitBranchLabel}+${project.name}-mc${"minecraft_version"()}"

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    java {
        withSourcesJar()
    }
}

subprojects {
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "net.kyori.blossom")

    setupRepositories()

    val capitalizedName =
        project.name.replaceFirstChar { it.uppercase() }

    val loom = project.extensions.getByType<LoomGradleExtensionAPI>()
    loom.apply {
        silentMojangMappingsLicense()
        runs.configureEach {
            vmArg("-XX:+AllowEnhancedClassRedefinition")
            vmArg("-XX:+IgnoreUnrecognizedVMOptions")
            vmArg("-Dmixin.debug.export=true")
            vmArg("-Dmixin.env.remapRefMap=true")
            vmArg("-Dmixin.env.refMapRemappingFile=${projectDir}/build/createSrgToMcp/output.srg")
            if (providers.gradleProperty("railways.debugCycleMenu").map { it.toBoolean() }.orElse(false).get()) {
                vmArg("-Drailways.debugCycleMenu=true")
            }
        }
    }

    configurations.configureEach {
        resolutionStrategy {
            force("net.fabricmc:fabric-loader:${"fabric_loader_version"()}")
        }
    }

    @Suppress("UnstableApiUsage")
    dependencies {
        "minecraft"("com.mojang:minecraft:${"minecraft_version"()}")
        // layered mappings - Mojmap names, parchment docs and parameters
        "mappings"(loom.layered {
            officialMojangMappings { nameSyntheticMembers = false }
            parchment("org.parchmentmc.data:parchment-${"minecraft_version"()}:${"parchment_version"()}@zip")
        })

        // Used to decompile mixin dumps, needs to be on the classpath
        // Uncomment if you want it to decompile mixin exports, beware it has very verbose logging.
        //implementation("org.vineflower:vineflower:1.10.0")
    }

    publishing {
        publications {
            create<MavenPublication>("maven${capitalizedName}") {
                artifactId = "${"archives_base_name"()}-${project.name}-${"minecraft_version"()}"
                from(components["java"])
            }
        }

        repositories {
            val mavenToken = System.getenv("MAVEN_TOKEN")
            val maven = if (isRelease) "releases" else "snapshots"
            if (mavenToken != null && mavenToken.isNotEmpty()) {
                maven {
                    url = uri("https://maven.ithundxr.dev/${maven}")
                    credentials {
                        username = "railways-github"
                        password = mavenToken
                    }
                }
            }
        }
    }

    // from here down is platform configuration
    if(project.path == ":common") {
        afterEvaluate {
            tasks.named<Jar>("jar") {
                archiveClassifier.set("")
                destinationDirectory = layout.buildDirectory.dir("libs").get()
            }
        }
        return@subprojects
    }

    tasks.withType<org.gradle.api.tasks.bundling.AbstractArchiveTask>().configureEach {
        archiveFileName.set(provider {
            val appendix = archiveAppendix.orNull?.takeIf { it.isNotEmpty() }?.let { "-$it" } ?: ""
            val version = archiveVersion.orNull?.takeIf { it.isNotEmpty() }?.let { "-$it" } ?: ""
            val classifier = archiveClassifier.orNull?.takeIf { it.isNotEmpty() }?.let { "-$it" } ?: ""
            "${archiveBaseName.get()}$appendix$version$classifier.${archiveExtension.get()}"
        })
    }

    apply(plugin = "com.gradleup.shadow")
    apply(plugin = "me.modmuss50.mod-publish-plugin")

    architectury {
        platformSetupLoomIde()
    }

    val remapJar = tasks.named<RemapJarTask>("remapJar") {
        from("${rootProject.projectDir}/LICENSE")
        val shadowJar = project.tasks.named<ShadowJar>("shadowJar").get()
        inputFile.set(shadowJar.archiveFile)
        injectAccessWidener = true
        dependsOn(shadowJar)
        archiveClassifier = null
        doLast {
            transformJar(outputs.files.singleFile)
        }
    }

    val common: Configuration by configurations.creating
    val shadowCommon: Configuration by configurations.creating
    val development = configurations.maybeCreate("development${capitalizedName}")

    configurations {
        compileOnly.get().extendsFrom(common)
        runtimeOnly.get().extendsFrom(common)
        development.extendsFrom(common)
    }

    dependencies {
        common(project(":common", "namedElements")) { isTransitive = false }
        shadowCommon(project(":common", "transformProduction${capitalizedName}")) { isTransitive = false }
    }

    tasks.named<ShadowJar>("shadowJar") {
        archiveClassifier = "dev-shadow"
        configurations = listOf(shadowCommon)
        exclude("architectury.common.json")
        destinationDirectory = layout.buildDirectory.dir("devlibs").get()
    }

    tasks.processResources {
        // include packs
        from(project(":common").file("src/main/resources")) {
            include("resourcepacks/")
        }

        // set up properties for filling into metadata
        val properties = mapOf(
                "version" to version,
                "minecraft_version" to "minecraft_version"(),
                "fabric_api_version" to "fabric_api_version"(),
                "fabric_loader_version" to "fabric_loader_version"(),
                "voicechat_api_version" to "voicechat_api_version"(),
                "create_fabric_version" to "create_fabric_version"(),
                "create_fabric_version_range" to "create_fabric_version_range"(),
        )

        inputs.properties(properties)

        filesMatching("fabric.mod.json") {
            expand(properties)
        }
    }

    tasks.jar {
        archiveClassifier = "dev"

        manifest {
            attributes(mapOf("Git-Hash" to gitHash))
        }
    }

    tasks.named<Jar>("sourcesJar") {
        val commonSources = project(":common").tasks.getByName<Jar>("sourcesJar")
        dependsOn(commonSources)
        from(commonSources.archiveFile.map { zipTree(it) })

        manifest {
            attributes(mapOf("Git-Hash" to gitHash))
        }
    }

    components.getByName<AdhocComponentWithVariants>("java") {
        withVariantsFromConfiguration(project.configurations["shadowRuntimeElements"]) {
            skip()
        }
    }

    val releaseType =
        if (version.toString().contains("alpha")) {
            ReleaseType.ALPHA;
        } else if (version.toString().contains("beta")) {
            ReleaseType.BETA;
        } else {
            ReleaseType.STABLE;
        }
    configure<ModPublishExtension> {
        file.set(remapJar.get().archiveFile)
        version.set(project.version.toString())
        changelog = ChangelogText.getChangelogText(rootProject).toString()
        type = releaseType
        displayName = "Steam 'n' Rails ${"mod_version"()} $capitalizedName ${"minecraft_version"()} C${"create_display_version"()}"
        modLoaders.add("fabric")
        modLoaders.add("quilt")

        curseforge {
            projectId = "curseforge_id"()
            accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
            minecraftVersions.add("minecraft_version"())

            requires {
                slug = "create-fly"
            }

            requires("fabric-api")
        }

        modrinth {
            projectId = "modrinth_id"()
            accessToken = providers.environmentVariable("MODRINTH_TOKEN")
            minecraftVersions.add("minecraft_version"())

            requires {
                slug = "create-fly"
            }

            requires("fabric-api")
        }
    }
}

fun transformJar(jar: File) {
    val contents = linkedMapOf<String, ByteArray>()
    JarFile(jar).use {
        it.entries().asIterator().forEach { entry ->
            if (!entry.isDirectory) {
                contents[entry.name] = it.getInputStream(entry).readAllBytes()
            }
        }
    }

    jar.delete()

    JarOutputStream(jar.outputStream()).use { out ->
        out.setLevel(Deflater.BEST_COMPRESSION)
        contents.forEach { var (name, data) = it
            if(name.startsWith("architectury_inject_${project.name}_common"))
                return@forEach

            if (name.endsWith(".json") || name.endsWith(".mcmeta")) {
                data = (JsonOutput.toJson(JsonSlurper().parse(data)).toByteArray())
            } else if (name.endsWith(".class")) {
                data = transformClass(data)
            }

            out.putNextEntry(JarEntry(name))
            out.write(data)
            out.closeEntry()
        }
        out.finish()
        out.close()
    }
}

fun transformClass(bytes: ByteArray): ByteArray {
    val node = ClassNode()
    ClassReader(bytes).accept(node, 0)

    // Remove Methods & Field Annotated with @DevEnvMixin
    node.methods.removeIf { methodNode: MethodNode -> removeIfDevMixin(node.name, methodNode.visibleAnnotations) }
    // Disabled as I don't feel ok with people being able to remove these
    //node.fields.removeIf { fieldNode: FieldNode -> removeIfDevMixin(fieldNode.visibleAnnotations) }

    return ClassWriter(0).also { node.accept(it) }.toByteArray()
}

fun patchCreateFlyMixinDescriptors(entryName: String, bytes: ByteArray): ByteArray {
    val replacements = when (entryName) {
        "com/zurrtum/create/mixin/EntityMixin.class",
        "com/zurrtum/create/client/mixin/EntityMixin.class" -> mapOf(
            "method_5873" to "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z",
            "method_5873(Lnet/minecraft/class_1297;ZZ)Z" to "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z"
        )
        "com/zurrtum/create/client/mixin/MinecraftClientMixin.class" -> mapOf(
            "method_18096" to "method_18096(Lnet/minecraft/class_437;ZZ)V"
        )
        else -> return bytes
    }

    val node = ClassNode()
    ClassReader(bytes).accept(node, 0)
    var changed = false

    for (method in node.methods) {
        changed = patchMixinAnnotationMethods(method.visibleAnnotations, replacements) || changed
        changed = patchMixinAnnotationMethods(method.invisibleAnnotations, replacements) || changed
    }

    if (!changed)
        return bytes

    return ClassWriter(0).also { node.accept(it) }.toByteArray()
}

fun patchMixinAnnotationMethods(annotations: List<AnnotationNode>?, replacements: Map<String, String>): Boolean {
    if (annotations == null)
        return false

    var changed = false
    for (annotation in annotations) {
        val values = annotation.values ?: continue
        var i = 0
        while (i < values.size - 1) {
            if (values[i] == "method") {
                @Suppress("UNCHECKED_CAST")
                val methods = values[i + 1] as? MutableList<Any>
                if (methods != null) {
                    for (j in methods.indices) {
                        val replacement = replacements[methods[j] as? String]
                        if (replacement != null) {
                            methods[j] = replacement
                            changed = true
                        }
                    }
                }
            }
            i += 2
        }
    }
    return changed
}

fun removeIfDevMixin(nodeName: String, visibleAnnotations: List<AnnotationNode>?): Boolean {
    // Don't remove methods if it's not a GHA build/Release build
    if (!removeDevMixinAnyway && buildNumber == null && !nodeName.lowercase(Locale.ROOT).matches(Regex(".*\\/mixin\\/.*Mixin")))
        return false

    if (visibleAnnotations != null) {
        for (annotationNode in visibleAnnotations) {
            if (annotationNode.desc == "Lcom/railwayteam/railways/annotation/mixin/DevEnvMixin;")
                return true
        }
    }

    return false
}

fun <T> getValueFromAnnotation(annotation: AnnotationNode?, key: String): T? {
    var getNextValue = false

    if (annotation?.values == null) {
        return null
    }

    // Keys and value are stored in successive pairs, search for the key and if found return the following entry
    for (value in annotation.values) {
        if (getNextValue) {
            @Suppress("UNCHECKED_CAST")
            return value as T
        }
        if (value == key) {
            getNextValue = true
        }
    }

    return null
}

tasks.register("railwaysPublish") {
    when (val platform = System.getenv("PLATFORM")) {
        null, "", "fabric" -> {
            dependsOn(":fabric:build", ":fabric:publish", ":fabric:publishMods")
        }
        else -> {
            throw GradleException("Unsupported PLATFORM '$platform'; this project only publishes Fabric builds.")
        }
    }
}

fun Project.setupRepositories() {
    repositories {
        mavenCentral()
        maven("https://maven.createmod.net") // Create, Ponder, Flywheel
        maven("https://modmaven.dev/") // flywheel fabric
        maven("https://maven.shedaniel.me/") // Cloth Config, REI
        maven("https://maven.blamejared.com/") // JEI, Hex Casting
        exclusiveMaven("https://maven.parchmentmc.org", "org.parchmentmc.data") // Parchment mappings
        exclusiveMaven("https://maven.quiltmc.org/repository/release", "org.quiltmc") // Quilt Mappings
        exclusiveMaven("https://api.modrinth.com/maven", "maven.modrinth") // LazyDFU
        exclusiveMaven("https://cursemaven.com", "curse.maven")
        maven("https://maven.theillusivec4.top/") // Curios
        maven("https://maven.ithundxr.dev/mirror") { // Registrate
            content {
                includeGroup("com.tterrag.registrate")
            }
        }
        maven("https://maven.maxhenkel.de/repository/public") // Simple Voice Chat
        maven("https://maven.jamieswhiteshirt.com/libs-release") // Reach Entity Attributes
        maven("https://maven.terraformersmc.com/releases/") // Mod Menu, EMI
        maven("https://mvn.devos.one/snapshots/") // Create Fabric, Porting Lib, Milk Lib, Registrate Fabric
        maven("https://mvn.devos.one/releases/") // Porting Lib
        maven("https://maven.cafeteria.dev/releases") // Fake Player API
        exclusiveMaven("https://maven.ladysnake.org/releases", "dev.onyxstudios.cardinal-components-api") // Cardinal Components (Hex Casting dependency)
        maven("https://jitpack.io/") { // Mixin Extras, Fabric ASM
            content {
                includeGroupByRegex("com.github.*")
            }
        }
        maven("$rootDir/local-maven")
    }
}

fun calculateGitHash(): String {
    try {
        val output = providers.exec {
            commandLine("git", "rev-parse", "HEAD")
        }
        return output.standardOutput.asText.get().trim()
    } catch(_: Throwable) {
        return "unknown"
    }
}

fun calculateGitBranch(): String {
    try {
        val output = providers.exec {
            commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
        }
        return output.standardOutput.asText.get().trim()
    } catch(_: Throwable) {
        return "unknown"
    }
}

fun hasUnstaged(): Boolean {
    try {
        val output = providers.exec {
            commandLine("git", "status", "--porcelain")
        }
        val result = output.standardOutput.asText.get().replace(Regex("M gradlew(\\.bat)?"), "").trimEnd()
        if (result.isNotEmpty())
            println("Found stageable results:\n${result}\n")
        return result.isNotEmpty()
    }  catch(_: Throwable) {
        return false
    }
}

fun Project.architectury(action: Action<ArchitectPluginExtension>) {
    action.execute(this.extensions.getByType<ArchitectPluginExtension>())
}

fun RepositoryHandler.exclusiveMaven(url: String, vararg groups: String) {
    exclusiveContent {
        forRepository { maven(url) }
        filter {
            groups.forEach {
                includeGroup(it)
            }
        }
    }
}

operator fun String.invoke(): String {
    return rootProject.ext[this] as? String
        ?: throw IllegalStateException("Property $this is not defined")
}

