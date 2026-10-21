/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
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

package com.railwayteam.railways.content.fuel.tank;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class FuelTankRenderer implements BlockEntityRenderer<FuelTankBlockEntity, FuelTankRenderer.FuelTankRenderState> {
    protected final FluidStateModelSet fluidStateModelSet;

    public FuelTankRenderer(BlockEntityRendererProvider.Context context) {
        this.fluidStateModelSet = Minecraft.getInstance().getModelManager().getFluidStateModelSet();
    }

    @Override
    public FuelTankRenderState createRenderState() {
        return new FuelTankRenderState();
    }

    @Override
    public boolean shouldRender(FuelTankBlockEntity be, Vec3 cameraPosition) {
        return be.isController() && BlockEntityRenderer.super.shouldRender(be, cameraPosition);
    }

    @Override
    public void extractRenderState(FuelTankBlockEntity be, FuelTankRenderState state, float tickProgress, Vec3 cameraPos,
                                   @Nullable CrumblingOverlay crumblingOverlay) {
        state.fluid = null;
        if (!be.window)
            return;

        LerpedFloat fluidLevel = be.getFluidLevel();
        if (fluidLevel == null)
            return;

        float capHeight = 1 / 4f;
        float tankHullWidth = 1 / 16f + 1 / 128f;
        float minPuddleHeight = 1 / 16f;
        float totalHeight = be.getHeight() - 2 * capHeight - minPuddleHeight;

        float level = fluidLevel.getValue(tickProgress);
        if (level < 1 / (512f * totalHeight))
            return;

        FluidStack fluidStack = be.getTankInventory().getFluid();
        if (fluidStack.isEmpty())
            return;

        Level world = be.getLevel();
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.lightCoords = world != null ? LightCoordsUtil.getLightCoords(world, state.blockPos) : 15728880;

        float clampedLevel = Mth.clamp(level * totalHeight, 0, totalHeight);
        state.translate = clampedLevel - totalHeight;

        int width = be.getWidth();
        float xMin = tankHullWidth;
        float xMax = xMin + width - 2 * tankHullWidth;
        float yMin = totalHeight + capHeight + minPuddleHeight - clampedLevel;
        float yMax = yMin + clampedLevel;
        float zMin = tankHullWidth;
        float zMax = zMin + width - 2 * tankHullWidth;

        state.fluid = FluidRenderHelper.extractFluidRenderState(
            world instanceof BlockAndTintGetter getter ? getter : null,
            state.blockPos,
            fluidStateModelSet,
            fluidStack.getFluid(),
            fluidStack.getComponentChanges(),
            xMin, yMin, zMin,
            xMax, yMax, zMax,
            state.lightCoords,
            false,
            true
        );
    }

    @Override
    public void submit(FuelTankRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        if (state.fluid == null)
            return;
        matrices.translate(0.0F, state.translate, 0.0F);
        state.fluid.submit(matrices, queue);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    public static class FuelTankRenderState extends BlockEntityRenderState {
        public float translate;
        public FluidRenderHelper.@Nullable FluidRenderState fluid;
    }
}
