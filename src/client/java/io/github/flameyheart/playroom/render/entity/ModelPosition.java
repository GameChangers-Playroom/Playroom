package io.github.flameyheart.playroom.render.entity;

import net.minecraft.client.model.ModelPart;

import java.util.Map;

public record ModelPosition(float pivotX, float pivotY, float pivotZ, float roll, float yaw, float pitch, ModelPart parentPart, Map<String, ModelPosition> children) {
    }
