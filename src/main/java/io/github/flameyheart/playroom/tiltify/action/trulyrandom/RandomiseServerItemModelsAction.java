package io.github.flameyheart.playroom.tiltify.action.trulyrandom;

import com.bawnorton.trulyrandom.api.TrulyRandomApi;
import com.bawnorton.trulyrandom.random.module.Module;

public class RandomiseServerItemModelsAction implements ServerResetableTrulyRandomAction {
    @Override
    public TrulyRandomAction.Untargeted getUntargeted() {
        return TrulyRandomApi::randomiseServerItemModels;
    }

    @Override
    public ResetableTrulyRandomAction.Untargeted onResetUntargeted() {
        return TrulyRandomApi::resetServerItemModels;
    }

    @Override
    public int getDuration() {
        return 20 * 60 * 5;
    }

    @Override
    public Module getModule() {
        return Module.ITEM_MODELS;
    }
}
