package io.github.flameyheart.playroom.tiltify;

import io.github.flameyheart.playroom.tiltify.action.GivePotionAction;
import io.github.flameyheart.playroom.util.ItemStackBuilder;
import io.github.flameyheart.playroom.util.PredicateUtils;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Automation {
    private static final Map<String, Task<?>> ACTION_MAP = new HashMap<>();
    public static final TaskBuilder<@Nullable Object> NULL = player -> null;

    static {
        register("46f3a4da-3730-4fab-bee7-4ea944454d0f", "Give snowballs", "give you Snowballs", Actions.GIVE_ITEM, giveItem(Items.SNOWBALL, 16)); // [GIVE ITEM] 16 snowballs[PLAYER REQUIRED]
        register("1ee9d7f1-86a0-415a-bb22-0685dd263686", "Give golden carrots", "give you Golden Carrots", Actions.GIVE_ITEM, giveItem(Items.GOLDEN_CARROT, 5)); // [GIVE ITEM] 5 golden carrots[PLAYER REQUIRED]
        register("d0998b9d-467a-453b-b447-569d8c47a27e", "Give steak", "give you Steak", Actions.GIVE_ITEM, giveItem(Items.COOKED_BEEF, 10)); // [GIVE ITEM] 10 steak[PLAYER REQUIRED]
        register("fa746607-3e15-4fb9-bdee-09fcc9874abd", "Give iron ingots", "give you Iron Ingots", Actions.GIVE_ITEM, giveItem(Items.IRON_INGOT, 10)); // [GIVE ITEM] 10 iron ingots[PLAYER REQUIRED]
        register("0ec7625f-ff67-4f8e-8a9e-19dedd739b8a", "Give bottles of enchanting", "give you Experience Bottles", Actions.GIVE_ITEM, giveItem(Items.EXPERIENCE_BOTTLE, 5)); // [GIVE ITEM] 5 bottles of enchanting [PLAYER REQUIRED]
        register("d8cd53f3-6b1a-49f1-b05b-ff4370c44493", "Give a cat egg", "give you a Cat Egg", Actions.GIVE_ITEM, giveItem(Items.CAT_SPAWN_EGG, 1)); // [GIVE ITEM] a cute cat egg to spawn [PLAYER REQUIRED]
        register("4e258379-30fb-4e0f-b2a9-41bdd5b0c76d", "Give gold ingots", "give you Gold Ingots", Actions.GIVE_ITEM, giveItem(Items.GOLD_INGOT, 5)) ;// [GIVE ITEM] 5 gold ingots [PLAYER REQUIRED]
        register("0490aff2-f537-42a6-bb6d-adf0585028cf", "Give emeralds", "give you Emeralds", Actions.GIVE_ITEM, giveItem(Items.EMERALD, 5)); // [GIVE ITEM] 5 emeralds [PLAYER REQUIRED]
        register("afdb4564-2b4b-4316-b376-24ae2a5622cb", "Give swiftness 2", "give you Swiftness", Actions.GIVE_POTION, givePotionSeconds(StatusEffects.SPEED, 10, 2)); // [GIVE POTION] swiftness 2 for 10 seconds [PLAYER REQUIRED]
        register("8dd746bd-bbf3-4232-94f5-64558ea4fc46", "Give a fortune 4 book", "give you a Fortune 4 Book", Actions.GIVE_ITEM, giveEnchant(Enchantments.FORTUNE, 4)); // [GIVE ITEM] a fortune 4 book to enchant with [PLAYER REQUIRED]
        register("da4908f2-d1f2-4267-a39d-fd925f25836b", "Give a golden apple", "give you a Golden Apple", Actions.GIVE_ITEM, giveItem(Items.GOLDEN_APPLE, 2)); // [GIVE ITEM] 2 normal golden apples [PLAYER REQUIRED]
        register("25c22317-ff0c-4ff3-a7dc-9b9ded1bb587", "Give diamonds", "give you Diamonds", Actions.GIVE_ITEM, giveItem(Items.DIAMOND, 2)); // [GIVE ITEM] 2 diamonds [PLAYER REQUIRED]
        register("551c369c-ca29-4001-9eb9-8c9259abde71", "Give slowness 4", "give you Slowness", Actions.GIVE_POTION, givePotionSeconds(StatusEffects.SLOWNESS, 10, 4)); // [GIVE POTION] slowness 4 for 10 seconds [PLAYER REQUIRED]
        register("ba7c33c2-4dd3-48a5-80ba-b6b420e7aad0", "Give a power 3 book", "give you a Power 3 Book", Actions.GIVE_ITEM, giveEnchant(Enchantments.POWER, 3)); // [GIVE ITEM] a power 3 book to enchant with [PLAYER REQUIRED]
        register("77e24c9b-63e5-4b8d-924a-be87a9dba198", "Randomise a Player's Block Textures", "randomise your Block Textures", Actions.RANDOMISE_TARGET_BLOCK_MODELS, NULL); // Randomise the appearances of every Minecraft block for a specific player for 5 minutes![PLAYER REQUIRED]
        register("9cbd8a94-d5aa-4009-9c9a-55e990bf3248", "Randomise a Player's Item Textures", "randomise your Item Textures", Actions.RANDOMISE_TARGET_ITEM_MODELS, NULL); // Randomises what every item looks like for a specific player for 5 minutes![PLAYER REQUIRED]
        register("5c695b2a-1c19-47a3-b8e7-dba77b693848", "Give a sharpness 3 book", "give you a Sharpness 3 Book", Actions.GIVE_ITEM, giveEnchant(Enchantments.SHARPNESS, 3)); // [GIVE ITEM] a sharpness 3 book to enchant with [PLAYER REQUIRED]
        register("c3e8fd42-f54c-4809-8f3f-73c9ada59bc2", "Give a wolf egg", "give you a Wolf Egg", Actions.GIVE_ITEM, giveItem(Items.WOLF_SPAWN_EGG, 1)); // [GIVE ITEM] a spawn egg of a wolf to tame [PLAYER REQUIRED]
        register("a3bc450f-fe84-4be2-8e0c-493cc259ea9d", "Randomise Loot Tables", "randomise everyone's Loot Tables", Actions.RANDOMISE_SERVER_LOOT_TABLES, NULL); // Randomise the items dropped or generated from any broken block, mob slain, chest generated and more for every player for 5 minutes!
        register("6897064a-ef89-407d-82e7-33c002d4a4c8", "Give an enchanted golden apple", "give you an Enchanted Golden Apple", Actions.GIVE_ITEM, giveItem(Items.ENCHANTED_GOLDEN_APPLE, 1)); // [GIVE ITEM] 1 enchanted golden apple [PLAYER REQUIRED]
        register("14f61bdf-6abf-45d1-b60a-82913722769c", "Randomise Everyone's Item Textures", "randomise everyone's Item Textures", Actions.RANDOMISE_SERVER_ITEM_MODELS, NULL); // Randomises what every item looks like for every player for 5 minutes!
        register("c2018b0e-0010-4aca-a14f-16f00044eede", "Randomise Everyone's Block Textures", "randomise everyone's Block Textures", Actions.RANDOMISE_SERVER_BLOCK_MODELS, NULL); // Randomise the appearances of every Minecraft block for every player for 5 minutes!
        register("737c844b-c0e4-4050-a59b-bfe5b7c3804a", "Spawn a Wither", "spawn a Wither", Actions.SPAWN_ENTITY, player -> {
            var entity = EntityType.WITHER.create(player.getServerWorld());
            if (entity == null) return null;
            entity.setPos(player.getX(), player.getY(), player.getZ());
            return entity;
        }); // Spawn a Wither on a player of your choice![PLAYER REQUIRED]
        register("3873d908-1c27-47ba-9901-433598f20699", "Spawn a Warden", "spawn a Warden", Actions.SPAWN_ENTITY, player -> {
            var entity = EntityType.WARDEN.create(player.getServerWorld());
            if (entity == null) return null;
            entity.setPos(player.getX(), player.getY(), player.getZ());
            return entity;
        }); // Spawn a Warden on a player of your choice [PLAYER REQUIRED]
        register("b2cbee01-e45d-4d6c-936a-972d4160ada1", "Randomise Recipes", "randomise everyone's Recipes", Actions.RANDOMISE_SERVER_RECIPES, NULL); // Randomises the recipes for every item for every player for 10 minutes!
    }

    private static TaskBuilder<ItemStack> giveItem(Item item, int amount) {
        return player -> new ItemStackBuilder(item).amount(amount).build();
    }

    private static TaskBuilder<ItemStack> giveEnchant(Enchantment enchantment, int level) {
        return player -> EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(enchantment, level));
    }

    private static TaskBuilder<GivePotionAction.PotionData> givePotionSeconds(StatusEffect effect, int time, int amplifier) {
        return player -> new GivePotionAction.PotionDataSeconds(effect, time, amplifier - 1);
    }

    public static <T> void register(String id, String name, String onDisplay, Action<T> action, TaskBuilder<T> builder) {
        ACTION_MAP.put(id, new Task<>(UUID.fromString(id), name, onDisplay, action, builder));
    }

    public static Task<?> get(UUID id) {
        return get(id.toString());
    }

    public static Task<?> get(String id) {
        return ACTION_MAP.get(id);
    }

    @FunctionalInterface
    public interface TaskBuilder<T> {
        @Nullable
        T build(ServerPlayerEntity player);
    }

    public record Task<T>(UUID id, String name, String onDisplay, Action<T> action, TaskBuilder<T> builder) {
        public boolean requiresPlayer() {
            return action.requiresPlayer();
        }

        public String className() {
            return action.getClass().getCanonicalName();
        }

        public boolean execute(ServerPlayerEntity player) {
            if (action.requiresPlayer() && player == null) {
                throw new IllegalStateException(action.getClass().getSimpleName() + " requires a player!");
            }
            if (PredicateUtils.checkUnlessDev(player, "playroom.bypass-rewards", 4, false)) {
                player.sendMessage(Text.translatable("feedback.playroom.tiltify.bypassed_reward"), true);
                return true;
            }
            return action.execute(player, builder.build(player), id);
        }
    }
}
