package io.github.flameyheart.playroom.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public interface PlayroomItem {

    default NbtCompound getPlayroomTag(ItemStack stack) {
        NbtCompound tag = stack.getOrCreateNbt();
        if (!tag.contains("Playroom")) {
            tag.put("Playroom", new NbtCompound());
        }
        return tag.getCompound("Playroom");
    }

    default NbtCompound getCooldownTag(ItemStack stack) {
        NbtCompound tag = getPlayroomTag(stack);
        if (!tag.contains("Cooldown")) {
            tag.put("Cooldown", new NbtCompound());
        }
        return tag.getCompound("Cooldown");
    }
}
