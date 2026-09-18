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

package com.railwayteam.railways.content.conductor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class ConductorRemoteLayer extends RenderLayer<ConductorRenderState, ConductorRenderModel> {

    public ConductorRemoteLayer(RenderLayerParent<ConductorRenderState, ConductorRenderModel> pRenderer) {
        super(pRenderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitter, int packedLight,
                       ConductorRenderState state, float yRot, float xRot) {
        if (state.job == ConductorEntity.Job.REMOTE_CONTROL && state.antennaState != null) {
            poseStack.pushPose();
            getParentModel().getHead().translateAndRotate(poseStack);
            state.antennaState.submit(RenderTypes.cutoutMovingBlock(), poseStack, submitter);
            poseStack.popPose();
        } else if (state.job == ConductorEntity.Job.SPY && !state.secondaryHeadRenderState.isEmpty()) {
            poseStack.pushPose();
            getParentModel().getHead().translateAndRotate(poseStack);
            CustomHeadLayer.translateToHead(poseStack, CustomHeadLayer.Transforms.DEFAULT);
            state.secondaryHeadRenderState.submit(poseStack, submitter, packedLight, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }
    }
}
