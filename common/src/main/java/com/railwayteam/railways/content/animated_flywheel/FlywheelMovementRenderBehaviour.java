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

package com.railwayteam.railways.content.animated_flywheel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderState;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.contraptions.render.ClientContraption;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.kinetics.flywheel.FlywheelBlockEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Client half of {@link FlywheelMovementBehaviour}: pushes the computed wheel angle into the contraption's flywheel
 * block entity, which both the Flywheel visual and the fallback block entity renderer read every frame.
 */
public class FlywheelMovementRenderBehaviour implements MovementRenderBehaviour {
    public static void register() {
        FlywheelMovementBehaviour.INSTANCE.attachRender = new FlywheelMovementRenderBehaviour();
    }

    @Override
    public MovementRenderState getRenderState(Vec3 cameraPos, Font font, MovementContext context,
                                              VirtualRenderWorld renderWorld, PoseStack.Pose contraptionPose,
                                              Matrix4f projectionMatrix) {
        if (!(context.temporaryData instanceof FlywheelMovementBehaviour.TemporaryData temporaryData)) return null;
        if (!(ClientContraption.getBlockEntityClientSide(context.contraption, context.localPos) instanceof FlywheelBlockEntity flywheelBlockEntity)) return null;

        if (Float.isNaN(temporaryData.baseAngle))
            temporaryData.baseAngle = flywheelBlockEntity.angle;

        flywheelBlockEntity.angle = (temporaryData.baseAngle + Mth.lerp(
            AnimationTickHolder.getPartialTicks(),
            temporaryData.wheelAngle0,
            temporaryData.wheelAngle1
        )) % 360;
        return null;
    }
}
