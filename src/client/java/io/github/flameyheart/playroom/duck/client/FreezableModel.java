package io.github.flameyheart.playroom.duck.client;

import io.github.flameyheart.playroom.PlayroomClient;
import io.github.flameyheart.playroom.duck.ExpandedEntityData;
import io.github.flameyheart.playroom.mixin.client.freeze.render.ModelPartAccessor;
import io.github.flameyheart.playroom.render.entity.ModelPosition;
import net.minecraft.client.model.ModelPart;

import java.util.HashMap;
import java.util.Map;

public interface FreezableModel {
    ModelPart playroom$getRoot();

    default void playroom$stopAnimation(ExpandedEntityData entityData) {
        if (playroom$getRoot() == null) return;
        if (entityData.playroom$showIce()) {
            Map<String, ModelPart> children = ((ModelPartAccessor) playroom$getRoot()).getChildren();
            Map<String, ModelPosition> positions = PlayroomClient.FROZEN_MODEL.computeIfAbsent(entityData, v -> new HashMap<>());
            if (!positions.isEmpty()) {
                positions.forEach(this::playroom$resetPositions);
            } else {
                children.forEach((name, modelPart) -> playroom$storePositions(name, modelPart, playroom$getRoot(), positions));
                PlayroomClient.FROZEN_MODEL.put(entityData, positions);
            }
        }
    }

    default void playroom$resetPositions(String name, ModelPosition position) {
        ModelPart modelPart = position.parentPart().getChild(name);
        modelPart.pivotX = position.pivotX();
        modelPart.pivotY = position.pivotY();
        modelPart.pivotZ = position.pivotZ();
        modelPart.roll = position.roll();
        modelPart.yaw = position.yaw();
        modelPart.pitch = position.pitch();
        position.children().forEach(this::playroom$resetPositions);
    }

    default void playroom$storePositions(String name, ModelPart modelPart, ModelPart parent, Map<String, ModelPosition> positions) {
        Map<String, ModelPosition> _children = new HashMap<>();
        ModelPosition position = new ModelPosition(modelPart.pivotX, modelPart.pivotY, modelPart.pivotZ, modelPart.roll, modelPart.yaw, modelPart.pitch, parent, _children);
        ((ModelPartAccessor) modelPart).getChildren()
                .forEach((childName, childModelPart) -> playroom$storePositions(childName, childModelPart, modelPart, _children));
        positions.put(name, position);
    }
}
