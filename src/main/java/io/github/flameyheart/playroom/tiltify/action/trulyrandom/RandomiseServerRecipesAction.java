package io.github.flameyheart.playroom.tiltify.action.trulyrandom;

import com.bawnorton.trulyrandom.api.TrulyRandomApi;
import com.bawnorton.trulyrandom.random.module.Module;

public class RandomiseServerRecipesAction implements ServerResetableTrulyRandomAction {
    @Override
    public TrulyRandomAction.Untargeted getUntargeted() {
        return TrulyRandomApi::randomiseServerRecipes;
    }

    @Override
    public Untargeted onResetUntargeted() {
        return TrulyRandomApi::resetServerRecipes;
    }

    @Override
    public int getTicksUntilReset() {
        return 20 * 60 * 10;
    }

    @Override
    public Module getModule() {
        return Module.RECIPES;
    }
}
