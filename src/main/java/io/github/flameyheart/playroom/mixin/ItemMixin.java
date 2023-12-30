package io.github.flameyheart.playroom.mixin;

import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.registry.Tags;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {
    @Unique private FoodComponent playroom$foodComponent;
    @Shadow @Final private RegistryEntry.Reference<Item> registryEntry;

    @Inject(method = "isFood", at = @At(value = "HEAD"), cancellable = true)
    private void crunchyCrystals0(CallbackInfoReturnable<Boolean> cir) {
        if (!Playroom.isExperimentEnabled("crunchy_crystals")) return;
        TagKey<Item> itemTag = Tags.CRUNCHY_CRYSTALS;
        if (registryEntry.isIn(itemTag)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getFoodComponent", at = @At(value = "HEAD"), cancellable = true)
    private void crunchyCrystals1(CallbackInfoReturnable<FoodComponent> cir) {
        if (!Playroom.isExperimentEnabled("crunchy_crystals")) return;
        TagKey<Item> itemTag = Tags.CRUNCHY_CRYSTALS;
        if (registryEntry.isIn(itemTag)) {
            if (playroom$foodComponent == null) {
                playroom$foodComponent = new FoodComponent.Builder().hunger(1).saturationModifier(0.1f)
                  .statusEffect(new StatusEffectInstance(StatusEffects.INSTANT_DAMAGE, 1, 0), 1.0f)
                  .statusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 300, 1), 1.0f)
                  .statusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 300, 1), 1.0f)
                  .build();
            }
            cir.setReturnValue(playroom$foodComponent);
        }
    }
}
