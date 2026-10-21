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

package com.railwayteam.railways.fabric_mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.railwayteam.railways.content.palettes.PalettesFlywheelBlock;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.zurrtum.create.client.content.kinetics.flywheel.FlywheelRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.kinetics.flywheel.FlywheelBlockEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Non-Flywheel counterpart of {@link MixinFlywheelVisual}.
 */
@Mixin(FlywheelRenderer.class)
public class MixinFlywheelRenderer {
    @WrapOperation(
        method = "extractRenderState(Lcom/zurrtum/create/content/kinetics/flywheel/FlywheelBlockEntity;Lcom/zurrtum/create/client/content/kinetics/flywheel/FlywheelRenderer$FlywheelRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
        at = @At(value = "FIELD", target = "Lcom/zurrtum/create/client/AllPartialModels;FLYWHEEL:Lcom/zurrtum/create/client/flywheel/lib/model/baked/PartialModel;", opcode = Opcodes.GETSTATIC)
    )
    private PartialModel railways$palettesFlywheel(Operation<PartialModel> original,
                                                   @Local(argsOnly = true) FlywheelBlockEntity blockEntity) {
        if (blockEntity.getBlockState().getBlock() instanceof PalettesFlywheelBlock pfb)
            return CRBlockPartials.FLYWHEELS.get(pfb.getColor());
        return original.call();
    }
}
