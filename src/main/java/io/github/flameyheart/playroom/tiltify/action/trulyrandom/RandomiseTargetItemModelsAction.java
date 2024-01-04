package io.github.flameyheart.playroom.tiltify.action.trulyrandom;

import com.bawnorton.trulyrandom.api.TrulyRandomApi;
import com.bawnorton.trulyrandom.random.module.Module;

public class RandomiseTargetItemModelsAction implements ResetableTrulyRandomAction {
    @Override
    public TrulyRandomAction.Targeted getTargeted() {
        return TrulyRandomApi::randomiseItemModels;
    }

    @Override
    public ResetableTrulyRandomAction.Targeted onResetTargeted() {
        return TrulyRandomApi::resetItemModels;
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
