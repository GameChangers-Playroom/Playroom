package io.github.flameyheart.playroom.tiltify.action;

import io.github.flameyheart.playroom.tiltify.Action;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;

public class GivePotionAction implements Action<GivePotionAction.PotionData> {
    @Override
    public boolean execute(ServerPlayerEntity target, PotionData potion) {
        target.addStatusEffect(new StatusEffectInstance(potion.effect, potion.time, potion.amplifier));
        return true;
    }

    public static class PotionData {
        private final StatusEffect effect;
        private final int time;
        private final int amplifier;

        public PotionData(StatusEffect effect, int time, int amplifier) {
            this.effect = effect;
            this.time = time;
            this.amplifier = amplifier;
        }

        public StatusEffect effect() {
            return effect;
        }

        public int time() {
            return time;
        }

        public int amplifier() {
            return amplifier;
        }
    }

    public static class PotionDataSeconds extends PotionData {
        public PotionDataSeconds(StatusEffect effect, int time, int amplifier) {
            super(effect, time * 20, amplifier);
        }
    }
}
