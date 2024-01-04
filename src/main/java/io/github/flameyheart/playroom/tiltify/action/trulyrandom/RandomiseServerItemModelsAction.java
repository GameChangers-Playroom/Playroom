package io.github.flameyheart.playroom.tiltify.action.trulyrandom;

import com.bawnorton.trulyrandom.api.TrulyRandomApi;
import com.bawnorton.trulyrandom.random.module.Module;

public class RandomiseServerItemModelsAction implements ResetableTrulyRandomAction {
    @Override
    public TrulyRandomAction.Untargeted getUntargeted() {
        return TrulyRandomApi::randomiseServerItemModels;
    }

    @Override
    public ResetableTrulyRandomAction.Untargeted onResetUntargeted() {
        return TrulyRandomApi::resetServerItemModels;
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
        return false;
    }
}
