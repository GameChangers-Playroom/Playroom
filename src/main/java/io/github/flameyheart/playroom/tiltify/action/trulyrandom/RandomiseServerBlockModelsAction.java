package io.github.flameyheart.playroom.tiltify.action.trulyrandom;

import com.bawnorton.trulyrandom.api.TrulyRandomApi;
import com.bawnorton.trulyrandom.random.module.Module;

public class RandomiseServerBlockModelsAction implements ServerResetableTrulyRandomAction {
    @Override
    public TrulyRandomAction.Untargeted getUntargeted() {
        return TrulyRandomApi::randomiseServerBlockModels;
    }

    @Override
    public ResetableTrulyRandomAction.Untargeted onResetUntargeted() {
        return TrulyRandomApi::resetServerBlockModels;
    }

    @Override
    public int getDuration() {
        return 20 * 60 * 5;
    }

    @Override
    public Module getModule() {
        return Module.BLOCK_MODELS;
    }
}
