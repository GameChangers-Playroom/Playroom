package io.github.flameyheart.playroom.mixin.client;

import io.github.flameyheart.playroom.duck.client.FancyDisplayName;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityMixin implements FancyDisplayName {
    private @Unique Text playroom$displayName;
    private @Unique Text playroom$prefix;

    @Override
    public void playroom$setDisplayName(Text displayName) {
        this.playroom$displayName = displayName;
    }

    @Override
    public void playroom$setPrefix(Text prefix) {
        this.playroom$prefix = prefix;
    }

    @Override
    public Text playroom$getDisplayName() {
        return playroom$displayName;
    }

    @Override
    public Text playroom$getPrefix() {
        return playroom$prefix;
    }

    @Override
    public boolean playroom$hasDisplayName() {
        return playroom$displayName != null;
    }

    @Override
    public boolean playroom$hasPrefix() {
        return playroom$prefix != null;
    }
}
