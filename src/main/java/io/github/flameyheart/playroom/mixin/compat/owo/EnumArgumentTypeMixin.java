package io.github.flameyheart.playroom.mixin.compat.owo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wispforest.owo.command.EnumArgumentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Locale;

@Mixin(value = EnumArgumentType.class, remap = false)
public class EnumArgumentTypeMixin {
    @WrapOperation(method = "create*", at = @At(value = "INVOKE", target = "Ljava/lang/String;toLowerCase(Ljava/util/Locale;)Ljava/lang/String;"))
    private static String fixClassName(String instance, Locale locale, Operation<String> original) {
        return instance.replace("\\.", "/").replaceAll("\\$", ".");
    }
}
