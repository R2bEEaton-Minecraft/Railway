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

package com.railwayteam.railways.base.data;

import com.google.gson.JsonObject;
import com.railwayteam.railways.content.palettes.PalettesColor;
import net.minecraft.core.Direction;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Hand-written replacement for the blockstate half of the old Forge-only
 * {@code BuilderTransformers#locometalHingedDoorBlockState}/{@code locometalSlidingDoorBlockState}/
 * {@code locometalFoldingDoorBlockState} datagen (which relied on Registrate's
 * {@code RegistrateBlockstateProvider}, itself a subclass of Forge's {@code BlockStateProvider} -
 * a class that no longer exists on this Fabric-only toolchain now that Porting Lib is gone).
 * The per-color model JSON these blockstates reference already exists and is correct;
 * only the blockstate "variants" permutation needed regenerating against the live
 * property set (no more "visible" - that property was removed from SlidingDoorBlock
 * upstream between the 1.21.11 and 26.2-rc-2 create-fly releases).
 * <p>
 * Also generates the combined full-height model used by the sliding door's
 * Flywheel animation visual (see {@code MixinSlidingDoorVisual}), which needs
 * a single rigid mesh spanning both blocks rather than the two separate
 * bottom/top models the static blockstate uses.
 */
public class LocometalDoorBlockStateGen implements DataProvider {
    private static final Direction[] FACINGS = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    private static final DoubleBlockHalf[] HALVES = DoubleBlockHalf.values();
    private static final DoorHingeSide[] HINGES = DoorHingeSide.values();
    private static final boolean[] BOOLEANS = {false, true};

    private final PackOutput packOutput;

    public LocometalDoorBlockStateGen(PackOutput packOutput) {
        this.packOutput = packOutput;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (PalettesColor color : PalettesColor.values()) {
            futures.add(writeHingedDoor(output, color));
            futures.add(writeSlidingLikeDoor(output, color, "sliding"));
            futures.add(writeSlidingLikeDoor(output, color, "folding"));
            futures.add(writeSlidingDoorFlywheelModel(output, color, false));
            futures.add(writeSlidingDoorFlywheelModel(output, color, true));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private Path blockstatePath(String blockId) {
        return packOutput.getOutputFolder().resolve("assets/railways/blockstates/" + blockId + ".json");
    }

    private Path modelPath(String path) {
        return packOutput.getOutputFolder().resolve("assets/railways/models/" + path + ".json");
    }

    /**
     * The Flywheel visual that drives the sliding door's open/close animation
     * ({@code SlidingDoorVisual.SlidingVisual}) renders the door as a single rigid
     * instanced mesh spanning both blocks, unlike the two separate bottom/top models
     * used for the static blockstate. This generates that combined mesh per color,
     * reusing the existing bottom/top/side textures against the hand-authored
     * "doors/sliding_block" template.
     */
    private CompletableFuture<?> writeSlidingDoorFlywheelModel(CachedOutput output, PalettesColor color, boolean windowed) {
        String colorName = color.getSerializedName();
        String windowedName = windowed ? "_windowed" : "";
        String windowedTexPrefix = windowed ? "sliding_windowed_door_" : "sliding_door_";

        JsonObject textures = new JsonObject();
        textures.addProperty("block_particle", "railways:block/palettes/" + colorName + "/annexed_slashed");
        textures.addProperty("side", "railways:block/palettes/" + colorName + "/" + windowedTexPrefix + "side");
        textures.addProperty("top", "railways:block/palettes/" + colorName + "/" + windowedTexPrefix + "top");
        textures.addProperty("bottom", "railways:block/palettes/" + colorName + "/" + windowedTexPrefix + "bottom");

        JsonObject root = new JsonObject();
        root.addProperty("parent", "railways:block/palettes/doors/sliding_block");
        root.add("textures", textures);

        return DataProvider.saveStable(output, root, modelPath("block/palettes/" + colorName + "/sliding_door/block" + windowedName));
    }

    private static int yRotFor(Direction facing, boolean open, boolean right) {
        int yRot = (int) facing.toYRot() + 90;
        if (open)
            yRot += 90;
        if (right && open)
            yRot += 180;
        return ((yRot % 360) + 360) % 360;
    }

    /**
     * Sliding/folding doors translate rather than swing open on a hinge, so unlike
     * {@link #yRotFor}, their static blockstate model must keep the same rotation
     * regardless of the open/hinge state - swinging it would make the static "open"
     * model (visible whenever Flywheel isn't covering it) look like a regular
     * hinge-swung door instead of a translated one.
     */
    private static int yRotForSliding(Direction facing) {
        return (((int) facing.toYRot() + 90) % 360 + 360) % 360;
    }

    private static JsonObject modelEntry(String model, int yRot) {
        JsonObject entry = new JsonObject();
        entry.addProperty("model", model);
        if (yRot != 0)
            entry.addProperty("y", yRot);
        return entry;
    }

    private CompletableFuture<?> writeHingedDoor(CachedOutput output, PalettesColor color) {
        String colorName = color.getSerializedName();
        String blockId = colorName + "_hinged_locometal_door";
        JsonObject variants = new JsonObject();

        for (Direction facing : FACINGS) {
            for (DoubleBlockHalf half : HALVES) {
                for (DoorHingeSide hinge : HINGES) {
                    for (boolean open : BOOLEANS) {
                        for (boolean windowed : BOOLEANS) {
                            boolean right = hinge == DoorHingeSide.RIGHT;
                            boolean lower = half == DoubleBlockHalf.LOWER;
                            String texName = lower ? "bottom" : "top";
                            String modelSuffix = texName + "_" + ((right ^ open) ? "right" : "left");
                            String windowedName = windowed ? "_windowed" : "";
                            String model = "railways:block/palettes/" + colorName + "/hinged_door/block_" + modelSuffix + windowedName;

                            String key = "facing=" + facing.getSerializedName()
                                + ",half=" + half.getSerializedName()
                                + ",hinge=" + hinge.getSerializedName()
                                + ",open=" + open
                                + ",windowed=" + windowed;

                            variants.add(key, modelEntry(model, yRotFor(facing, open, right)));
                        }
                    }
                }
            }
        }

        JsonObject root = new JsonObject();
        root.add("variants", variants);
        return DataProvider.saveStable(output, root, blockstatePath(blockId));
    }

    private CompletableFuture<?> writeSlidingLikeDoor(CachedOutput output, PalettesColor color, String type) {
        String colorName = color.getSerializedName();
        String blockId = colorName + "_" + type + "_locometal_door";
        JsonObject variants = new JsonObject();

        for (Direction facing : FACINGS) {
            for (DoubleBlockHalf half : HALVES) {
                for (DoorHingeSide hinge : HINGES) {
                    for (boolean open : BOOLEANS) {
                        for (boolean windowed : BOOLEANS) {
                            boolean lower = half == DoubleBlockHalf.LOWER;
                            String texName = lower ? "bottom" : "top";
                            String windowedName = windowed ? "_windowed" : "";
                            String model = "railways:block/palettes/" + colorName + "/" + type + "_door/block_" + texName + windowedName;

                            String key = "facing=" + facing.getSerializedName()
                                + ",half=" + half.getSerializedName()
                                + ",hinge=" + hinge.getSerializedName()
                                + ",open=" + open
                                + ",windowed=" + windowed;

                            variants.add(key, modelEntry(model, yRotForSliding(facing)));
                        }
                    }
                }
            }
        }

        JsonObject root = new JsonObject();
        root.add("variants", variants);
        return DataProvider.saveStable(output, root, blockstatePath(blockId));
    }

    public String getName() {
        return "Steam 'n' Rails Locometal Door Blockstates";
    }
}
