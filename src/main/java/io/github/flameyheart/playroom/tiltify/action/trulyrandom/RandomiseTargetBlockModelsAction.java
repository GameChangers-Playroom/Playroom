package io.github.flameyheart.playroom.tiltify.action.trulyrandom;

import com.bawnorton.trulyrandom.api.TrulyRandomApi;
import com.bawnorton.trulyrandom.random.module.Module;

public class RandomiseTargetBlockModelsAction implements ResetableTrulyRandomAction {
    @Override
    public TrulyRandomAction.Targeted getTargeted() {
        return TrulyRandomApi::randomiseBlockModels;
    }

    @Override
    public ResetableTrulyRandomAction.Targeted onResetTargeted() {
        return TrulyRandomApi::resetBlockModels;
    }

    @Override
    public int getTicksUntilReset() {
        return 20 * 60 * 5;
    }

    @Override
    public Module getModule() {
        return Module.ITEM_MODELS;
    }

    @Override
    public boolean isTargeted() {
        return true;
    }
}
