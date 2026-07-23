package com.railwayteam.railways.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.content.trains.track.TrackRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.signal.TrackEdgePoint;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.content.trains.track.TrackBlock;

import java.util.HashMap;
import java.util.Map;

public class CustomTrackOverlayRendering {
    public static final Map<EdgePointType<?>, PartialModel> CUSTOM_OVERLAYS = new HashMap<>();

    public static void register(EdgePointType<?> edgePointType, PartialModel model) {
        CUSTOM_OVERLAYS.put(edgePointType, model);
    }

    public static void renderOverlay(LevelAccessor level, BlockPos pos, Direction.AxisDirection direction,
                                     BezierTrackPointLocation bezier, PoseStack ms, MultiBufferSource buffer, int light, int overlay,
                                     EdgePointType<?> type, float scale) {
        renderOverlay(level, pos, direction, bezier, ms, buffer, light, overlay, CUSTOM_OVERLAYS.get(type), scale, false);
    }

    public static void renderOverlay(LevelAccessor level, BlockPos pos, Direction.AxisDirection direction,
                                     BezierTrackPointLocation bezier, PoseStack ms, MultiBufferSource buffer, int light, int overlay,
                                     PartialModel model, float scale) {
        renderOverlay(level, pos, direction, bezier, ms, buffer, light, overlay, model, scale, false);
    }

    public static void renderOverlay(LevelAccessor level, BlockPos pos, Direction.AxisDirection direction,
                                     BezierTrackPointLocation bezier, PoseStack ms, MultiBufferSource buffer, int light, int overlay,
                                     PartialModel model, float scale, boolean offsetToSide) {
        if (model == null || bezier != null)
            return;
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof ITrackBlock track) || !state.hasProperty(TrackBlock.SHAPE))
            return;

        Vec3 axis = state.getValue(TrackBlock.SHAPE).getAxes().getFirst();
        Vec3 diff = axis.scale(direction.getStep()).normalize();
        Vec3 normal = track.getUpNormal(level, pos, state);
        Vec3 angles = TrackRenderer.getModelAngles(normal, diff);

        ms.pushPose();
        TransformStack.of(ms).center()
            .rotateY((float) angles.y)
            .rotateX((float) angles.x)
            .uncenter()
            .translate(0, axis.y != 0 ? 7 / 16f : 0,
                axis.y != 0 ? direction.getStep() * 2.5f / 16f : 0);
        CachedBuffers.partial(model, state)
            .translate(.5, 0, .5)
            .scale(scale)
            .translate(offsetToSide ? .5 : -.5, 0, -.5)
            .light(LevelRenderer.getLightColor(level, pos))
            .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
        ms.popPose();
    }

    public static boolean overlayWillOverlap(TrackTargetingBehaviour<? extends TrackEdgePoint> target) {
        return false;
    }
}
