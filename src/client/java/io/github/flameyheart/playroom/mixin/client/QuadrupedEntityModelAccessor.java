package io.github.flameyheart.playroom.mixin.client;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.QuadrupedEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(QuadrupedEntityModel.class)
public interface QuadrupedEntityModelAccessor {
    @Accessor
    ModelPart getBody();
}
