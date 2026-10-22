package com.railwayteam.railways.content.smokestack.block.diesel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class DieselSmokeStackRenderer
    extends SmartBlockEntityRenderer<DieselSmokeStackBlockEntity, DieselSmokeStackRenderer.FanRenderState> {
    public DieselSmokeStackRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public FanRenderState createRenderState() {
        return new FanRenderState();
    }

    @Override
    public void extractRenderState(DieselSmokeStackBlockEntity be, FanRenderState state, float tickProgress,
                                   Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumbling) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumbling);
        state.fan = null;
        if (be.isRemoved())
            return;

        Direction facing = state.blockState.getValue(DieselSmokeStackBlock.FACING);
        state.xRot = facing == Direction.DOWN ? 180 : facing.getAxis().isHorizontal() ? 90 : 0;
        state.zRot = facing.getAxis().isVertical() ? 0 : ((int) facing.toYRot()) % 360;
        state.fanRotation = (float) be.getFanRotation(be.getRpm(tickProgress));
        state.fan = CachedBuffers.partial(CRBlockPartials.DIESEL_STACK_FAN, state.blockState);
    }

    @Override
    public void submit(FanRenderState state, PoseStack matrices, SubmitNodeCollector queue,
                       CameraRenderState cameraState) {
        if (state.fan == null)
            return;

        queue.submitCustomGeometry(matrices, RenderType.cutout(), (pose, consumer) -> state.fan
            .light(state.lightCoords)
            .translate(0.5, 0.5, 0.5)
            .rotateXDegrees(state.xRot)
            .rotateZDegrees(state.zRot)
            .rotateYDegrees(state.fanRotation)
            .translate(-0.5, -0.5, -0.5)
            .renderInto(pose, consumer));
    }

    public static class FanRenderState extends SmartBlockEntityRenderer.SmartRenderState {
        public @Nullable SuperByteBuffer fan;
        public float xRot;
        public float zRot;
        public float fanRotation;
    }
}
