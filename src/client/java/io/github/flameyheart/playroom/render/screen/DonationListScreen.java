package io.github.flameyheart.playroom.render.screen;

import io.github.flameyheart.playroom.PlayroomClient;
import io.github.flameyheart.playroom.tiltify.Donation;
import io.github.flameyheart.playroom.util.MapBuilder;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Surface;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;

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
        boolean hasEditPermission = client.player != null && Permissions.check(client.player, "playroom.admin.server.update_donations", 4);
        var template = model.expandTemplate(FlowLayout.class,  hasEditPermission ? "staff-donation" : "donation", new MapBuilder<String, String>()
          .put("id", donation.id().toString())
          .build());

        template.childById(LabelComponent.class, "message").text(Text.translatable("playroom.donation_screen.title", donation.donorName(), donation.amount(), donation.currency()));

        template.mouseEnter().subscribe(() -> {
            template.surface(HOVER);
        });

        template.mouseLeave().subscribe(() -> {
            template.surface(Surface.BLANK);
        });

        if (hasEditPermission) {
            template.childById(LabelComponent.class, "status").text(Text.translatable("text.playroom.donation.status." + donation.status().name().toLowerCase()));
        }

        List<TooltipComponent> tooltip = new ArrayList<>();
        if (hasEditPermission) {
            tooltip.add(TooltipComponent.of(Text.literal(donation.id().toString()).asOrderedText()));
            tooltip.add(TooltipComponent.of(Text.literal("").asOrderedText()));
        }
        if (StringUtils.isNotBlank(donation.message())) {
            tooltip.add(TooltipComponent.of(Text.literal("Message:").setStyle(Style.EMPTY.withUnderline(true)).asOrderedText()));
            tooltip.add(TooltipComponent.of(Text.literal(donation.message()).asOrderedText()));
        }
        if (!donation.rewards().isEmpty()) {
            tooltip.add(TooltipComponent.of(Text.literal("").asOrderedText()));
            tooltip.add(TooltipComponent.of(Text.literal("Reward:").setStyle(Style.EMPTY.withUnderline(true)).asOrderedText()));
        }
        for (Donation.Reward reward : donation.rewards()) {
            tooltip.add(TooltipComponent.of(Text.literal(reward.name()).asOrderedText()));
        }

        if (hasEditPermission) {
            tooltip.add(TooltipComponent.of(Text.literal("").asOrderedText()));
            tooltip.add(TooltipComponent.of(Text.literal("Right-click to get the donation UUID").asOrderedText()));
        }
        template.tooltip(tooltip);

        template.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if (button == 0) {
                this.playDownSound(MinecraftClient.getInstance().getSoundManager());
                client.setScreen(new DonationScreen(donation));
            } else if (hasEditPermission && button == 1) {
                this.playDownSound(MinecraftClient.getInstance().getSoundManager());
                client.keyboard.setClipboard(donation.id().toString());
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
