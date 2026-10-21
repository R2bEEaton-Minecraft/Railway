package com.zurrtum.create.content.trains.track;

import com.railwayteam.railways.registry.CRTrackMaterials;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

public class TrackMaterialFactory {
    private final Identifier id;
    private Supplier<? extends TrackBlock> block;
    private TrackBlockFactory blockFactory = TrackBlock::new;
    private Identifier particle;
    private String langName;
    private Identifier trackType = CRTrackMaterials.CRTrackType.STANDARD;
    private Ingredient sleeperIngredient;
    private Ingredient railsIngredient;
    private boolean noRecipeGen = false;

    private TrackMaterialFactory(Identifier id) {
        this.id = id;
        this.particle = id.withPrefix("block/track/");
        this.langName = id.getPath();
    }

    public static TrackMaterialFactory make(Identifier id) {
        return new TrackMaterialFactory(id);
    }

    public TrackMaterialFactory block(Supplier<? extends Supplier<? extends TrackBlock>> block) {
        this.block = () -> block.get().get();
        return this;
    }

    public TrackMaterialFactory customBlockFactory(TrackBlockFactory blockFactory) {
        this.blockFactory = blockFactory;
        return this;
    }

    public TrackMaterialFactory customModels(Supplier<?> tie, Supplier<?> left, Supplier<?> right) {
        return this;
    }

    public TrackMaterialFactory lang(String langName) {
        this.langName = langName;
        return this;
    }

    public TrackMaterialFactory noRecipeGen() {
        this.noRecipeGen = true;
        return this;
    }

    public TrackMaterialFactory particle(Identifier particle) {
        this.particle = particle;
        return this;
    }

    public TrackMaterialFactory rails(ItemLike rails) {
        this.railsIngredient = Ingredient.of(rails);
        return this;
    }

    public TrackMaterialFactory rails(TagKey<Item> rails) {
        this.railsIngredient = Ingredient.of(HolderSet.emptyNamed(BuiltInRegistries.ITEM, rails));
        return this;
    }

    public TrackMaterialFactory rails(Ingredient rails) {
        this.railsIngredient = rails;
        return this;
    }

    public TrackMaterialFactory sleeper(Block sleeper) {
        this.sleeperIngredient = Ingredient.of(sleeper);
        return this;
    }

    public TrackMaterialFactory sleeper(Item sleeper) {
        this.sleeperIngredient = Ingredient.of(sleeper);
        return this;
    }

    public TrackMaterialFactory sleeper(Object sleeper) {
        if (sleeper instanceof Ingredient ing) {
            this.sleeperIngredient = ing;
        } else if (sleeper instanceof ItemLike itemLike) {
            this.sleeperIngredient = Ingredient.of(itemLike);
        } else if (sleeper instanceof TagKey<?> tagKey) {
            //noinspection unchecked
            this.sleeperIngredient = Ingredient.of(HolderSet.emptyNamed(BuiltInRegistries.ITEM, (TagKey<Item>) tagKey));
        }
        return this;
    }

    public TrackMaterialFactory standardModels() {
        return this;
    }

    public TrackMaterialFactory trackType(Identifier trackType) {
        this.trackType = trackType;
        return this;
    }

    public TrackMaterial build() {
        final TrackMaterial[] material = new TrackMaterial[1];
        Supplier<TrackBlock> blockSupplier = () -> block != null
            ? block.get()
            : blockFactory.create(BlockBehaviour.Properties.of(), material[0]);
        material[0] = new TrackMaterial(id, blockSupplier);
        CRTrackMaterials.registerMeta(material[0], trackType, particle, langName);
        CRTrackMaterials.registerIngredients(material[0], sleeperIngredient, railsIngredient, noRecipeGen);
        return material[0];
    }

    public Identifier getId() {
        return id;
    }

    @FunctionalInterface
    public interface TrackBlockFactory {
        TrackBlock create(BlockBehaviour.Properties properties, TrackMaterial material);
    }
}
