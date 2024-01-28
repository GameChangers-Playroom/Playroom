package io.github.flameyheart.playroom.mixin;

import com.google.common.collect.Iterators;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.flameyheart.playroom.entity.attribute.DynamicEntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;
import java.util.Iterator;

@Mixin(EntityAttributeInstance.class)
public abstract class EntityAttributeInstanceMixin {
    @Shadow @Final private EntityAttribute type;

    @Shadow protected abstract Collection<EntityAttributeModifier> getModifiersByOperation(EntityAttributeModifier.Operation operation);

    @ModifyReturnValue(method = "getValue", at = @At("RETURN"))
    private double handleDynamic(double original) {
        return playroom$computeValue(original);
    }

    @WrapOperation(method = "computeValue", at = @At(value = "INVOKE", target = "Ljava/util/Collection;iterator()Ljava/util/Iterator;"))
    private <T> Iterator<T> skipDynamic(Collection<T> instance, Operation<Iterator<T>> original) {
        return Iterators.filter(instance.iterator(), t -> !(t instanceof DynamicEntityAttributeModifier));
    }

    @Unique
    private double playroom$computeValue(double base) {
        double d = base;
        for (Iterator<EntityAttributeModifier> iterator = playroom$getDynamicModifiersByOperation(EntityAttributeModifier.Operation.ADDITION); iterator.hasNext(); ) {
            EntityAttributeModifier entityAttributeModifier = iterator.next();
            d += entityAttributeModifier.getValue();
        }
        double e = d;
        for (Iterator<EntityAttributeModifier> iterator = playroom$getDynamicModifiersByOperation(EntityAttributeModifier.Operation.MULTIPLY_BASE); iterator.hasNext(); ) {
            EntityAttributeModifier entityAttributeModifier = iterator.next();
            e += d * entityAttributeModifier.getValue();
        }
        for (Iterator<EntityAttributeModifier> iterator = playroom$getDynamicModifiersByOperation(EntityAttributeModifier.Operation.MULTIPLY_TOTAL); iterator.hasNext(); ) {
            EntityAttributeModifier entityAttributeModifier = iterator.next();
            e *= 1.0 + entityAttributeModifier.getValue();
        }
        return this.type.clamp(e);
    }

    @Unique
    private Iterator<EntityAttributeModifier> playroom$getDynamicModifiersByOperation(EntityAttributeModifier.Operation operation) {
        return Iterators.filter(this.getModifiersByOperation(operation).iterator(), t -> t instanceof DynamicEntityAttributeModifier);
    }
}
