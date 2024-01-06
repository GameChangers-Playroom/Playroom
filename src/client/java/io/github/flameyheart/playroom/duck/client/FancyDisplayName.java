package io.github.flameyheart.playroom.duck.client;

import net.minecraft.text.Text;

public interface FancyDisplayName {
    void playroom$setDisplayName(Text displayName);
    void playroom$setPrefix(Text prefix);

    Text playroom$getDisplayName();
    Text playroom$getPrefix();

    boolean playroom$hasDisplayName();
    boolean playroom$hasPrefix();
}
