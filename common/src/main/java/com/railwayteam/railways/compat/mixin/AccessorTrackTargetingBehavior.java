package com.railwayteam.railways.mixincompat;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.zurrtum.create.content.trains.signal.TrackEdgePoint;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

/** Access to the targeting state needed to keep paired track couplers synchronized. */
@Mixin(value = TrackTargetingBehaviour.class, remap = false)
public interface AccessorTrackTargetingBehavior {
	@Accessor UUID getId();
	@Accessor void setId(UUID id);
	@Accessor BlockPos getTargetTrack();
	@Accessor void setTargetTrack(BlockPos targetTrack);
	@Accessor boolean isOrthogonal();
	@Accessor void setOrthogonal(boolean orthogonal);
	@Accessor Direction.AxisDirection getTargetDirection();
	@Accessor void setTargetDirection(Direction.AxisDirection targetDirection);
	@Accessor Vec3 getRotatedDirection();
	@Accessor void setRotatedDirection(Vec3 rotatedDirection);
	@Accessor Vec3 getPrevDirection();
	@Accessor void setPrevDirection(Vec3 prevDirection);
	@Accessor CompoundTag getMigrationData();
	@Accessor void setMigrationData(CompoundTag migrationData);
	@Accessor BezierTrackPointLocation getTargetBezier();
	@Accessor void setTargetBezier(BezierTrackPointLocation targetBezier);
	@Accessor void setEdgePoint(TrackEdgePoint edgePoint);
}
