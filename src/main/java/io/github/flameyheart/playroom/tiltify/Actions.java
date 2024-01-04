package io.github.flameyheart.playroom.tiltify;

import io.github.flameyheart.playroom.tiltify.action.ExecuteCommandAction;
import io.github.flameyheart.playroom.tiltify.action.GiveItemAction;
import io.github.flameyheart.playroom.tiltify.action.trulyrandom.*;

public class Actions {
    public static final Action EXECUTE_COMMAND = new ExecuteCommandAction();
    public static final Action GIVE_ITEM = new GiveItemAction();
    public static final Action RANDOMISE_SERVER_RECIPES = new RandomiseServerRecipesAction();
    public static final Action RANDOMISE_SERVER_LOOT_TABLES = new RandomiseServerLootTablesAction();
    public static final Action RANDOMISE_SERVER_ITEM_MODELS = new RandomiseServerItemModelsAction();
    public static final Action RANDOMISE_TARGET_ITEM_MODELS = new RandomiseTargetItemModelsAction();
    public static final Action RANDOMISE_SERVER_BLOCK_MODELS = new RandomiseServerBlockModelsAction();
    public static final Action RANDOMISE_TARGET_BLOCK_MODELS = new RandomiseTargetBlockModelsAction();

}
