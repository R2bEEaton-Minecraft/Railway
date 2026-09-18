package com.railwayteam.railways.content.conductor;

import com.railwayteam.railways.Railways;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ConductorRenderState extends LivingEntityRenderState {
    public DyeColor color = ConductorEntity.defaultColor();
    public ItemStack headStack = ItemStack.EMPTY;
    public ConductorEntity.Job job = ConductorEntity.Job.DEFAULT;
    public Identifier texture = Railways.asResource("textures/entity/conductor.png");

    // riding/sitting pose
    public boolean isRiding = false;

    // secondary head stack (e.g. goggles)
    public final ItemStackRenderState secondaryHeadRenderState = new ItemStackRenderState();

    // antenna (remote control)
    public @Nullable SuperByteBufferRenderState antennaState = null;

    // flag layer
    public boolean isHoldingSchedules = false;
    public @Nullable SuperByteBufferRenderState flagState = null;

    // toolbox layer
    public boolean isCarryingToolbox = false;
    public DyeColor toolboxColor = DyeColor.BROWN;
    public BlockState toolboxBlockState = null;
    public float toolboxLidAngle = 0f;
    public float toolboxDrawerOffset = 0f;
    public @Nullable SuperByteBufferRenderState toolboxBodyState = null;
    public @Nullable SuperByteBufferRenderState toolboxLidState = null;
    public @Nullable SuperByteBufferRenderState toolboxDrawer0State = null;
    public @Nullable SuperByteBufferRenderState toolboxDrawer1State = null;
}
