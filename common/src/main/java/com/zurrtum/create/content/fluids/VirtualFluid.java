package com.zurrtum.create.content.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.tterrag.registrate.fabric.SimpleFlowableFluid;

public class VirtualFluid extends SimpleFlowableFluid {
    public VirtualFluid() {
        super(new Properties(() -> null, () -> null));
    }
    public Item getBucket() {
        return Items.AIR;
    }
    protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
        return false;
    }
    public Vec3 getFlow(BlockGetter level, BlockPos pos, FluidState state) {
        return Vec3.ZERO;
    }
    protected boolean canConvertToSource(ServerLevel level) { return false; }
    public int getTickDelay(LevelReader level) {
        return 0;
    }
    protected float getExplosionResistance() {
        return 0;
    }
    public float getHeight(FluidState state, BlockGetter level, BlockPos pos) {
        return 0;
    }
    public float getOwnHeight(FluidState state) {
        return 0;
    }
    protected BlockState createLegacyBlock(FluidState state) {
        return Blocks.AIR.defaultBlockState();
    }
    public boolean isSource(FluidState state) {
        return false;
    }
    public int getAmount(FluidState state) {
        return 0;
    }
    public VoxelShape getShape(FluidState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }
}
