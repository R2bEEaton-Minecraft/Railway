package com.tterrag.registrate.providers;

import com.railwayteam.railways.base.data.CRTagGen;
import net.minecraft.tags.TagKey;

public class RegistrateTagsProvider<T> {
    public CRTagGen.TagAppender<T> addTag(TagKey<T> tag) {
        return new CRTagGen.TagAppender<>();
    }
}
