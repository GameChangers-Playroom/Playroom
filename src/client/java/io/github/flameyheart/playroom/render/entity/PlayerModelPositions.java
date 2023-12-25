package io.github.flameyheart.playroom.render.entity;

public record PlayerModelPositions(ModelPosition head, ModelPosition hat, ModelPosition body, ModelPosition rightArm, ModelPosition leftArm, ModelPosition rightLeg, ModelPosition leftLeg) {
    public record ModelPosition(float pivotX, float pivotY, float pivotZ, float roll, float yaw, float pitch) {
    }
}
