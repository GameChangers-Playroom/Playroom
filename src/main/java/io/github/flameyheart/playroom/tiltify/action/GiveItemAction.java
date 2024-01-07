package io.github.flameyheart.playroom.tiltify.action;

import io.github.flameyheart.playroom.tiltify.Action;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public class GiveItemAction implements Action<ItemStack> {
    @Override
    public boolean execute(ServerPlayerEntity target, ItemStack stack) {
        ItemEntity itemEntity;
        boolean bl = target.getInventory().insertStack(stack);
        if (!bl && !stack.isEmpty()) {
            itemEntity = target.dropItem(stack, false);
            if (itemEntity == null) return false;
            itemEntity.resetPickupDelay();
            itemEntity.setOwner(target.getUuid());
            return true;
        } else {
            stack.setCount(1);
            itemEntity = target.dropItem(stack, false);
            if (itemEntity != null) {
                itemEntity.setDespawnImmediately();
            }
            target.getWorld().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2f, ((target.getRandom().nextFloat() - target.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f);
            target.currentScreenHandler.sendContentUpdates();
        }
        return true;
    }
}
