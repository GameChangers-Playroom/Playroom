package io.github.flameyheart.playroom.item;

import io.github.flameyheart.playroom.Constants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;

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

    default void playSound(World world, PlayerEntity player, SoundEvent sound) {
        if (!world.isClient) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(), sound, Constants.PLAYROOM_SOUND_CATEGORY, 0.5F, 1.0F);
        }
    }
}
