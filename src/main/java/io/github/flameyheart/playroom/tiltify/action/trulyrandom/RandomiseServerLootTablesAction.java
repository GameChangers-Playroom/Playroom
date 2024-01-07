package io.github.flameyheart.playroom.tiltify.action.trulyrandom;

import com.bawnorton.trulyrandom.api.TrulyRandomApi;
import com.bawnorton.trulyrandom.random.module.Module;

public class RandomiseServerLootTablesAction implements ServerResetableTrulyRandomAction {
    @Override
    public TrulyRandomAction.Untargeted getUntargeted() {
        return TrulyRandomApi::randomiseServerLootTables;
    }

    @Override
    public ResetableTrulyRandomAction.Untargeted onResetUntargeted() {
        return TrulyRandomApi::resetServerLootTables;
    }

    @Override
    public int getTicksUntilReset() {
        return 20 * 60 * 5;
    }

    @Override
    public Module getModule() {
        return Module.LOOT_TABLES;
    }
}
