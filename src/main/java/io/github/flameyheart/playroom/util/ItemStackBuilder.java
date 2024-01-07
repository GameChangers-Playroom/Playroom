package io.github.flameyheart.playroom.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class ItemStackBuilder {
    private final ItemStack stack;

    public ItemStackBuilder(Item item) {
        stack = item.getDefaultStack();
    }

    public ItemStackBuilder amount(int amount) {
        stack.setCount(amount);
        return this;
    }

    public ItemStackBuilder name(Text name) {
        stack.setCustomName(name);
        return this;
    }

    public ItemStackBuilder nbt(Consumer<NbtCompound> consumer) {
        NbtCompound tag = stack.getOrCreateNbt();
        consumer.accept(tag);
        return this;
    }

    public ItemStack build() {
        return stack;
    }
}
