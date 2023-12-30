package io.github.flameyheart.playroom.duck;

import io.github.flameyheart.playroom.util.InventorySlot;
import net.minecraft.item.Item;

public interface InventoryDuck {
    InventorySlot playroom$removeFirstStack(Item item);
}
