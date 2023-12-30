package io.github.flameyheart.playroom.mixin;

import io.github.flameyheart.playroom.duck.InventoryDuck;
import io.github.flameyheart.playroom.util.InventorySlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin implements InventoryDuck {
    @Shadow @Final public DefaultedList<ItemStack> main;

    @Shadow @Final private List<DefaultedList<ItemStack>> combinedInventory;

    @Override
    public InventorySlot playroom$removeFirstStack(Item item) {
        int slot = 0;
        for (DefaultedList<ItemStack> invSection : this.combinedInventory) {
            for (int i = 0; i < invSection.size(); ++i) {
                ItemStack stack = invSection.get(i);
                if (stack.isEmpty() || !(stack.isOf(item))) continue;
                invSection.set(i, ItemStack.EMPTY);
                return new InventorySlot(slot + i, stack);
            }
            slot += invSection.size();
        }
        return null;
    }
}
