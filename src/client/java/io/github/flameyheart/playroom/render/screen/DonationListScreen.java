package io.github.flameyheart.playroom.render.screen;

import io.github.flameyheart.playroom.PlayroomClient;
import io.github.flameyheart.playroom.tiltify.Donation;
import io.github.flameyheart.playroom.util.MapBuilder;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.Text;

import java.util.*;

public class DonationListScreen extends BaseScreen<FlowLayout> {
    private final Map<UUID, Donation> donations = new HashMap<>(PlayroomClient.DONATIONS);

    public DonationListScreen() {
        super(FlowLayout.class, "donation_list_screen");
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        FlowLayout items = rootComponent.childById(FlowLayout.class, "donations");
        Objects.requireNonNull(items, "Donations block is required!");

        for (Donation donation : PlayroomClient.DONATIONS.values()) {
            addDonation(donation, items);
        }

        var closeButton = rootComponent.childById(ButtonComponent.class, "done");
        closeButton.onPress(button -> this.close());
    }

    private void addDonation(Donation donation, FlowLayout items) {
        var template = model.expandTemplate(FlowLayout.class, "donation", new MapBuilder<String, String>()
          .put("id", donation.id().toString())
          .put("message", Text.translatable("playroom.donation_screen.title", donation.donorName(), donation.amount(), donation.currency()).getString())
          .build());

        template.mouseEnter().subscribe(() -> {
            template.surface(HOVER);
        });

        template.mouseLeave().subscribe(() -> {
            template.surface(Surface.BLANK);
        });

        List<TooltipComponent> tooltip = client.textRenderer.wrapLines(Text.literal("donation.message()"), client.getWindow().getScaledWidth() - 16)
          .stream().map(TooltipComponent::of).toList();
        template.tooltip(tooltip);

        template.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if (button == 0) {
                this.playDownSound(MinecraftClient.getInstance().getSoundManager());
                client.setScreen(new DonationScreen(donation));
            }
            return true;
        });

        items.child(template);
    }

    @Override
    public void tick() {
        if (client == null || client.currentScreen != this) return;
        if (donations.size() < PlayroomClient.DONATIONS.size()) {
            FlowLayout items = rootComponent().childById(FlowLayout.class, "donations");
            Objects.requireNonNull(items, "Donations block is required!");

            PlayroomClient.DONATIONS.forEach((uuid, donation) -> {
                donations.computeIfAbsent(uuid, uuid1 -> {
                    addDonation(donation, items);
                    return donation;
                });
            });
        }

        super.tick();
    }
}
