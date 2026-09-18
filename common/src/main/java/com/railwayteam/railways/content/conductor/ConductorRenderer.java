package com.railwayteam.railways.content.conductor;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class ConductorRenderer extends MobRenderer<ConductorEntity, ConductorRenderState, ConductorRenderModel> {
    public static final Identifier TEXTURE = Railways.asResource("textures/entity/conductor.png");
    private final ItemModelResolver itemModelResolver;

    public ConductorRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new ConductorRenderModel(ctx.bakeLayer(ConductorEntityModel.LAYER_LOCATION)), 0.2f);
        this.itemModelResolver = ctx.getItemModelResolver();
        addLayer(new ConductorCapLayer(this, ctx.getModelSet()));
        addLayer(new ConductorRemoteLayer(this));
        addLayer(new ConductorFlagLayer(this));
        addLayer(new ConductorToolboxLayer(this));
    }

    @Override
    public ConductorRenderState createRenderState() {
        return new ConductorRenderState();
    }

    @Override
    public void extractRenderState(ConductorEntity conductor, ConductorRenderState state, float partialTick) {
        super.extractRenderState(conductor, state, partialTick);
        state.color = conductor.getColor();
        state.headStack = conductor.getItemBySlot(EquipmentSlot.HEAD);
        state.job = conductor.getJob();
        state.texture = textureFor(conductor, state.headStack);

        state.isRiding = conductor.isPassenger();
        state.isHoldingSchedules = conductor.isHoldingSchedulesClient();

        // Secondary head item (e.g. goggles)
        ItemStack secondaryHeadStack = conductor.getSecondaryHeadStack();
        if (!secondaryHeadStack.isEmpty()) {
            this.itemModelResolver.updateForLiving(state.secondaryHeadRenderState, secondaryHeadStack, ItemDisplayContext.HEAD, conductor);
        } else {
            state.secondaryHeadRenderState.clear();
        }

        // Antenna
        if (state.job == ConductorEntity.Job.REMOTE_CONTROL) {
            state.antennaState = CachedBuffers.partial(CRBlockPartials.CONDUCTOR_ANTENNA, Blocks.AIR.defaultBlockState())
                    .rotateXDegrees(180)
                    .translate(3 / 16.0, 3.5 / 16.0, 0 / 16.0)
                    .rotateZDegrees(-30)
                    .light(state.lightCoords)
                    .extractRenderState();
        } else {
            state.antennaState = null;
        }

        // Flag
        if (state.isHoldingSchedules) {
            state.flagState = CachedBuffers.partial(
                    CRBlockPartials.CONDUCTOR_WHISTLE_FLAGS.get(state.color),
                    Blocks.AIR.defaultBlockState())
                    .translate(-0.78125, 0.15, -0.688)
                    .light(state.lightCoords)
                    .extractRenderState();
        } else {
            state.flagState = null;
        }

        // Toolbox
        state.isCarryingToolbox = conductor.isCarryingToolbox();
        if (state.isCarryingToolbox) {
            var toolbox = conductor.getToolbox();
            ItemStack displayStack = conductor.getToolboxDisplayStack();
            state.toolboxColor = toolbox.getColor();
            state.toolboxBlockState = displayStack.getItem() instanceof BlockItem bi
                    ? bi.getBlock().defaultBlockState() : null;
            state.toolboxLidAngle = toolbox.lid.getValue(partialTick);
            state.toolboxDrawerOffset = toolbox.drawers.getValue(partialTick);

            if (state.toolboxBlockState != null) {
                state.toolboxBodyState = CachedBuffers.partial(
                        CRBlockPartials.TOOLBOX_BODIES.get(state.toolboxColor), state.toolboxBlockState)
                        .light(state.lightCoords)
                        .extractRenderState();

                state.toolboxLidState = CachedBuffers.partial(
                        AllPartialModels.TOOLBOX_LIDS.get(state.toolboxColor), state.toolboxBlockState)
                        .translate(0, 6 / 16f, 12 / 16f)
                        .rotateXDegrees(60 * state.toolboxLidAngle)
                        .translate(0, -6 / 16f, -12 / 16f)
                        .light(state.lightCoords)
                        .extractRenderState();

                state.toolboxDrawer0State = CachedBuffers.partial(
                        AllPartialModels.TOOLBOX_DRAWER, state.toolboxBlockState)
                        .translate(0, 0, -state.toolboxDrawerOffset * .175f * 2)
                        .light(state.lightCoords)
                        .extractRenderState();

                state.toolboxDrawer1State = CachedBuffers.partial(
                        AllPartialModels.TOOLBOX_DRAWER, state.toolboxBlockState)
                        .translate(0, 1 / 8f, -state.toolboxDrawerOffset * .175f * 1)
                        .light(state.lightCoords)
                        .extractRenderState();
            } else {
                state.toolboxBodyState = null;
                state.toolboxLidState = null;
                state.toolboxDrawer0State = null;
                state.toolboxDrawer1State = null;
            }
        } else {
            state.toolboxBlockState = null;
            state.toolboxBodyState = null;
            state.toolboxLidState = null;
            state.toolboxDrawer0State = null;
            state.toolboxDrawer1State = null;
        }
    }

    @Override
    public Identifier getTextureLocation(ConductorRenderState state) {
        return state.texture;
    }

    private static Identifier textureFor(ConductorEntity conductor, ItemStack headItem) {
        String name = headItem.getHoverName().getString();
        if (name.startsWith("[sus]"))
            name = name.substring(5);

        if (!headItem.isEmpty()
            && headItem.getItem() instanceof ConductorCapItem
            && CRBlockPartials.CUSTOM_CONDUCTOR_SKINS.containsKey(name)) {
            return ensurePng(CRBlockPartials.CUSTOM_CONDUCTOR_SKINS.get(name));
        }

        if (conductor.getCustomName() != null) {
            Identifier texture = CRBlockPartials.CUSTOM_CONDUCTOR_SKINS_FOR_NAME.get(conductor.getCustomName().getString());
            if (texture != null)
                return ensurePng(texture);
        }

        return TEXTURE;
    }

    private static Identifier ensurePng(Identifier id) {
        if (id.getPath().endsWith(".png"))
            return id;
        return Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath() + ".png");
    }
}
