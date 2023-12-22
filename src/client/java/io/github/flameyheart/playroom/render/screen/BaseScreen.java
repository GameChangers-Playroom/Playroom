package io.github.flameyheart.playroom.render.screen;

import io.github.flameyheart.playroom.Playroom;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundEvents;
import org.apache.http.util.Asserts;
import org.jetbrains.annotations.NotNull;

public abstract class BaseScreen<R extends ParentComponent> extends BaseUIModelScreen<R> {
    protected final Screen parent;

    public BaseScreen(Class<R> rootComponentClass, String source) {
        super(rootComponentClass, DataSource.asset(Playroom.id(source)));
        this.parent = MinecraftClient.getInstance().currentScreen;
    }

    public BaseScreen(Class<R> rootComponentClass, String source, Screen parent) {
        super(rootComponentClass, DataSource.asset(Playroom.id(source)));
        this.parent = parent;
    }

    public BaseScreen(Class<R> rootComponentClass, DataSource source) {
        super(rootComponentClass, source);
        this.parent = MinecraftClient.getInstance().currentScreen;
    }

    public BaseScreen(Class<R> rootComponentClass, DataSource source, Screen parent) {
        super(rootComponentClass, source);
        this.parent = parent;
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    public static void drawSurface(FlowLayout rootComponent, MinecraftClient client) {
        if (client.world == null) {
            rootComponent.surface(Surface.OPTIONS_BACKGROUND);
        }
    }

    public void playDownSound(SoundManager soundManager) {
        soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    public <T extends Component> T getComponent(@NotNull ParentComponent parent, @NotNull Class<T> expectedClass, @NotNull String id) {
        var component = parent.childById(expectedClass, id);
        Asserts.notNull(component, id);

        return component;
    }

    public <T extends Component> T getComponent(@NotNull Class<T> expectedClass, @NotNull String id) {
        var component = rootComponent().childById(expectedClass, id);
        Asserts.notNull(component, id);

        return component;
    }

    public R rootComponent() {
        return uiAdapter.rootComponent;
    }

    public static final Surface HOVER = (context, component) -> {
        context.drawGradientRect(
          component.x(), component.y(), component.width(), component.height(),
          0xC0101010, 0x00101010, 0x00101010, 0xC0101010
        );
    };
}
