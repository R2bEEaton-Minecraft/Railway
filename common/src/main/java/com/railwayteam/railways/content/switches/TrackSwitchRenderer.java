package com.railwayteam.railways.content.switches;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.railwayteam.railways.util.CustomTrackOverlayRendering;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class TrackSwitchRenderer
    extends SmartBlockEntityRenderer<TrackSwitchBlockEntity, TrackSwitchRenderer.SwitchRenderState> {

    public TrackSwitchRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public SwitchRenderState createRenderState() {
        return new SwitchRenderState();
    }

    @Override
    public void extractRenderState(TrackSwitchBlockEntity be, SwitchRenderState state, float tickProgress, Vec3 cameraPos,
                                   @Nullable ModelFeatureRenderer.CrumblingOverlay crumbling) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumbling);
        state.clear();

        if (be.isRemoved())
            return;

        BlockState blockState = be.getBlockState();
        state.yRot = AngleHelper.horizontalAngle(blockState.getValue(TrackSwitchBlock.FACING));
        state.automatic = be.isAutomatic();

        Level level = be.getLevel();

        if (state.automatic) {
            SuperByteBuffer flagBuf = CachedBuffers.partial(CRBlockPartials.BRASS_SWITCH_FLAG, blockState)
                .light(state.lightCoords);
            if (level != null) flagBuf.cardinalLighting(level);
            state.flag = flagBuf.extractRenderState();
            state.flagAngle = brassFlagAngle(be);
        } else {
            SuperByteBuffer flagBuf = CachedBuffers.partial(CRBlockPartials.ANDESITE_SWITCH_FLAG, blockState)
                .light(state.lightCoords);
            if (level != null) flagBuf.cardinalLighting(level);
            state.flag = flagBuf.extractRenderState();

            SuperByteBuffer handleBuf = CachedBuffers.partial(CRBlockPartials.ANDESITE_SWITCH_HANDLE, blockState)
                .light(state.lightCoords);
            if (level != null) handleBuf.cardinalLighting(level);
            state.handle = handleBuf.extractRenderState();

            state.flagAngle = andesiteFlagAngle(be);
        }

        TrackTargetingBehaviour<TrackSwitch> target = be.edgePoint;
        if (level == null || target == null)
            return;

        BlockPos targetPosition = target.getGlobalPosition();
        BlockState trackState = level.getBlockState(targetPosition);
        Block block = trackState.getBlock();

        if (!(block instanceof ITrackBlock))
            return;

        state.level = level;
        state.targetPosition = targetPosition;
        state.trackOffset = targetPosition.subtract(be.getBlockPos());
        state.trackState = trackState;
        state.targetDirection = target.getTargetDirection();
        state.targetBezier = target.getTargetBezier();
        PartialModel overlayModel = be.getOverlayModel();
        boolean offsetOverlayToSide = CustomTrackOverlayRendering.overlayWillOverlap(target);
        state.overlayState = CustomTrackOverlayRendering.extractOverlayRenderState(
            level, targetPosition, trackState, state.targetDirection, state.targetBezier, overlayModel, 1.0f, offsetOverlayToSide
        );
    }

    private static float brassFlagAngle(TrackSwitchBlockEntity be) {
        if (be.isReverseLeft() || (be.isNormal() && be.exitCount == 2 && be.hasExit(TrackSwitchBlock.SwitchState.REVERSE_RIGHT)))
            return -0.40f;
        if (be.isReverseRight() || (be.isNormal() && be.exitCount == 2 && be.hasExit(TrackSwitchBlock.SwitchState.REVERSE_LEFT)))
            return 0.40f;
        return 0.0f;
    }

    private static float andesiteFlagAngle(TrackSwitchBlockEntity be) {
        if (be.isReverseLeft())
            return 1.5708f;
        if (be.isReverseRight())
            return -1.5708f;
        return 0.0f;
    }

    @Override
    public void submit(SwitchRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);

        if (state.flag != null) {
            matrices.pushPose();
            matrices.translate(0.5, 0.5, 0.5);
            matrices.mulPose(Axis.YP.rotationDegrees(-state.yRot));
            if (state.automatic) {
                matrices.translate(0, -2.0 / 16, 0);
                matrices.mulPose(Axis.YP.rotation(1.5708f));
                matrices.translate(0.0, 8.5 / 16 - 0.5, 0.0);
                matrices.mulPose(Axis.ZP.rotation(-state.flagAngle));
                matrices.translate(0.0, - (8.5 / 16 - 0.5), 0.0);
                matrices.translate(0.0, 1.0 / 16, 0.0);
            } else {
                matrices.mulPose(Axis.YP.rotation(-state.flagAngle));
            }
            matrices.translate(-0.5, -0.5, -0.5);
            state.flag.submit(RenderTypes.cutoutMovingBlock(), matrices, queue);
            matrices.popPose();
        }

        if (state.handle != null) {
            matrices.pushPose();
            matrices.translate(0.5, 0.5, 0.5);
            matrices.mulPose(Axis.YP.rotationDegrees(-state.yRot));
            matrices.mulPose(Axis.YP.rotation(1.5708f));
            matrices.translate(-0.5, -0.5, -0.5);
            state.handle.submit(RenderTypes.cutoutMovingBlock(), matrices, queue);
            matrices.popPose();
        }

        if (state.overlayState != null && state.level != null && state.targetPosition != null && state.trackState != null) {
            matrices.pushPose();
            matrices.translate(state.trackOffset.getX(), state.trackOffset.getY(), state.trackOffset.getZ());
            if (CustomTrackOverlayRendering.prepareTrackOverlay(state.level, state.targetPosition, state.trackState,
                state.targetBezier, state.targetDirection, matrices)) {
                state.overlayState.submit(RenderTypes.cutoutMovingBlock(), matrices, queue);
            }
            matrices.popPose();
        }
    }

    public static class SwitchRenderState extends SmartBlockEntityRenderer.SmartRenderState {
        public float yRot;
        public boolean automatic;
        public float flagAngle;
        public @Nullable SuperByteBufferRenderState flag;
        public @Nullable SuperByteBufferRenderState handle;

        public @Nullable Level level;
        public @Nullable BlockPos targetPosition;
        public @Nullable BlockPos trackOffset;
        public @Nullable BlockState trackState;
        public @Nullable Direction.AxisDirection targetDirection;
        public @Nullable com.zurrtum.create.infrastructure.component.BezierTrackPointLocation targetBezier;
        public @Nullable SuperByteBufferRenderState overlayState;

        public void clear() {
            flag = null;
            handle = null;
            level = null;
            targetPosition = null;
            trackOffset = null;
            trackState = null;
            targetDirection = null;
            targetBezier = null;
            overlayState = null;
        }
    }
}
