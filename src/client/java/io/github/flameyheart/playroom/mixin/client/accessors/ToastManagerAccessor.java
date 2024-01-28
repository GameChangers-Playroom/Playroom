package io.github.flameyheart.playroom.mixin.client.accessors;

import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ToastManager.class)
public interface ToastManagerAccessor {
    @Accessor
    List<ToastManager.Entry<? extends Toast>> getVisibleEntries();
}
