package io.github.flameyheart.playroom.registry;

import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.item.LaserGun;
import io.github.flameyheart.playroom.item.OldLaserGun;
import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.gui.ItemGroupTab;
import io.wispforest.owo.registration.annotations.IterationIgnored;
import io.wispforest.owo.registration.reflect.ItemRegistryContainer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;

public class Items implements ItemRegistryContainer {
    public static final LaserGun LASER_GUN = new LaserGun(new FabricItemSettings().rarity(Rarity.RARE).maxCount(1));
    public static final OldLaserGun LASER_GUN_OLD = new OldLaserGun(new FabricItemSettings().rarity(Rarity.RARE).maxCount(1));
    public static final Item ICE_BLOCKS  = new Item(new FabricItemSettings().rarity(Rarity.RARE).maxCount(1));

    @IterationIgnored
    public static final OwoItemGroup ITEM_GROUP = OwoItemGroup.builder(Playroom.id("items"), () -> Icon.of(ICE_BLOCKS))
        .initializer(itemGroup -> itemGroup.tabs.add(new ItemGroupTab(Icon.of(LASER_GUN), Text.translatable("itemGroup.playroom.items"), (context, entries) -> {
            entries.add(LASER_GUN);
            entries.add(LASER_GUN_OLD);
            entries.add(ICE_BLOCKS);
        }, ItemGroupTab.DEFAULT_TEXTURE, true))).build();
}
