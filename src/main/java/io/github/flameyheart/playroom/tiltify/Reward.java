package io.github.flameyheart.playroom.tiltify;

import net.minecraft.util.StringIdentifiable;

public enum Reward implements StringIdentifiable {
    GIVE_SNOWBALLS("Give snowballs", "46f3a4da-3730-4fab-bee7-4ea944454d0f"),
    GIVE_GOLDEN_CARROTS("Give golden carrots", "1ee9d7f1-86a0-415a-bb22-0685dd263686"),
    GIVE_STEAK("Give steak", "d0998b9d-467a-453b-b447-569d8c47a27e"),
    GIVE_IRON_INGOTS("Give iron ingots", "fa746607-3e15-4fb9-bdee-09fcc9874abd"),
    GIVE_BOTTLES_OF_ENCHANTING("Give bottles of enchanting", "0ec7625f-ff67-4f8e-8a9e-19dedd739b8a"),
    GIVE_A_CAT_EGG("Give a cat egg", "d8cd53f3-6b1a-49f1-b05b-ff4370c44493"),
    GIVE_GOLD_INGOTS("Give gold ingots", "4e258379-30fb-4e0f-b2a9-41bdd5b0c76d"),
    GIVE_EMERALDS("Give emeralds", "0490aff2-f537-42a6-bb6d-adf0585028cf"),
    GIVE_SWIFTNESS_2("Give swiftness 2", "afdb4564-2b4b-4316-b376-24ae2a5622cb"),
    GIVE_A_FORTUNE_4_BOOK("Give a fortune 4 book", "8dd746bd-bbf3-4232-94f5-64558ea4fc46"),
    GIVE_A_GOLDEN_APPLE("Give a golden apple", "da4908f2-d1f2-4267-a39d-fd925f25836b"),
    GIVE_DIAMONDS("Give diamonds", "25c22317-ff0c-4ff3-a7dc-9b9ded1bb587"),
    GIVE_SLOWNESS_4("Give slowness 4", "551c369c-ca29-4001-9eb9-8c9259abde71"),
    GIVE_A_POWER_3_BOOK("Give a power 3 book", "ba7c33c2-4dd3-48a5-80ba-b6b420e7aad0"),
    RANDOMISE_PLAYERS_BLOCK_TEXTURES("Randomise a Player's Block Textures", "77e24c9b-63e5-4b8d-924a-be87a9dba198"),
    RANDOMISE_PLAYERS_ITEM_TEXTURES("Randomise a Player's Item Textures", "9cbd8a94-d5aa-4009-9c9a-55e990bf3248"),
    GIVE_A_SHARPNESS_3_BOOK("Give a sharpness 3 book", "5c695b2a-1c19-47a3-b8e7-dba77b693848"),
    GIVE_A_WOLF_EGG("Give a wolf egg", "c3e8fd42-f54c-4809-8f3f-73c9ada59bc2"),
    RANDOMISE_LOOT_TABLES("Randomise Loot Tables", "a3bc450f-fe84-4be2-8e0c-493cc259ea9d"),
    GIVE_AN_ENCHANTED_GOLDEN_APPLE("Give an enchanted golden apple", "6897064a-ef89-407d-82e7-33c002d4a4c8"),
    RANDOMISE_ITEM_TEXTURES("Randomise Everyone's Item Textures", "14f61bdf-6abf-45d1-b60a-82913722769c"),
    RANDOMISE_BLOCK_TEXTURES("Randomise Everyone's Block Textures", "c2018b0e-0010-4aca-a14f-16f00044eede"),
    SPAWN_A_WITHER("Spawn a Wither", "737c844b-c0e4-4050-a59b-bfe5b7c3804a"),
    SPAWN_A_WARDEN("Spawn a Warden", "3873d908-1c27-47ba-9901-433598f20699"),
    RANDOMISE_RECIPES("Randomise Recipes", "b2cbee01-e45d-4d6c-936a-972d4160ada1"),
    ;

    public static final com.mojang.serialization.Codec<Reward> CODEC = StringIdentifiable.createCodec(Reward::values);

    private final String displayName;
    private final String uuid;

    Reward(String displayName, String uuid) {
        this.displayName = displayName;
        this.uuid = uuid;
    }

    public String displayName() {
        return displayName;
    }

    public String uuid() {
        return uuid;
    }

    @Override
    public String asString() {
        return name();
    }
}
