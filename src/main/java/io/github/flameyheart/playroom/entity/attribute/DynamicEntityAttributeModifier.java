package io.github.flameyheart.playroom.entity.attribute;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.nbt.NbtCompound;

import java.util.UUID;
import java.util.function.Supplier;

public class DynamicEntityAttributeModifier extends EntityAttributeModifier {
    private final Supplier<Double> value;
    public DynamicEntityAttributeModifier(UUID uuid, String name, Supplier<Double> value, Operation operation) {
        super(uuid, name, 0, operation);
        this.value = value;
    }

    @Override
    public double getValue() {
        return value.get();
    }

    public String toString() {
        return "DynamicEntityAttributeModifier{amount=" + this.value.get() + ", operation=" + this.getOperation() + ", name='" + this.getName() + "', id=" + this.getId() + "}";
    }

    @Override
    public NbtCompound toNbt() {
        NbtCompound nbt = super.toNbt();
        nbt.remove("Amount");
        nbt.putDouble("Amount", this.value.get());
        return nbt;
    }
}
