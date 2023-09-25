package io.github.flameyheart.playroom.mixin;

import com.google.gson.Gson;
import dev.isxander.yacl3.config.v2.impl.serializer.GsonConfigSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(value = GsonConfigSerializer.class, remap = false)
public interface GsonConfigSerializerAccessor {
    @Accessor Gson getGson();
}
