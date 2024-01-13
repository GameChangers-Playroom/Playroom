package io.github.flameyheart.playroom.render.screen;

import io.github.flameyheart.playroom.PlayroomClient;
import io.github.flameyheart.playroom.tiltify.Donation;
import io.github.flameyheart.playroom.util.LinedStringBuilder;
import io.github.flameyheart.playroom.util.MapBuilder;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Surface;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class DonationScreen extends BaseScreen<FlowLayout> {
    private final Donation donation;

    public DonationScreen(Donation donation) {
        super(FlowLayout.class, "donation_screen");
        this.donation = donation;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        var closeButton = rootComponent.childById(ButtonComponent.class, "done");
        closeButton.onPress(button -> this.close());

        var title = rootComponent.childById(LabelComponent.class, "title");
        title.text(Text.translatable("playroom.donation_screen.title", donation.donorName(), donation.amount(), donation.currency()));
        title.shadow(true);

        var rewards = rootComponent.childById(FlowLayout.class, "rewards");
        rewards.child(Components.label(Text.literal(donation.message())));
        boolean hasEditPermission = client.player != null && Permissions.check(client.player, "playroom.admin.server.update_donations", 4);
        for (Donation.Reward reward : donation.rewards()) {
            var template = model.expandTemplate(FlowLayout.class, hasEditPermission ? "staff-reward" : "reward", new MapBuilder<String, String>().build());
            template.childById(LabelComponent.class, "text").text(Text.translatable("playroom.donation_screen.reward", reward.name(), reward.message()));

            template.mouseEnter().subscribe(() -> {
                template.surface(HOVER);
            });

            template.mouseLeave().subscribe(() -> {
                template.surface(Surface.BLANK);
            });

            if (hasEditPermission) {
                template.childById(LabelComponent.class, "status").text(Text.translatable("text.playroom.reward.status." + reward.status().name().toLowerCase()));

                LinedStringBuilder text = new LinedStringBuilder();

                text.append("Reward: ").append(reward.rewardId());
                text.appendLine("Claim id: ").append(reward.claimId());
                text.appendLine("Target: ").append(reward.message());
                text.appendLine("Task: ").append(reward.name());
                text.appendLine().appendLine("Click to get the reward UUID on the format:").appendLine("{donation_id} {reward_id}");

                List<TooltipComponent> tooltip = client.textRenderer.wrapLines(Text.literal(text.toString()), client.getWindow().getScaledWidth() - 16)
                  .stream().map(TooltipComponent::of).toList();
                template.tooltip(tooltip);

                template.mouseDown().subscribe((mouseX, mouseY, button) -> {
                    if (button == 0) {
                        this.playDownSound(MinecraftClient.getInstance().getSoundManager());
                        client.keyboard.setClipboard(donation.id() + " " + reward.claimId().toString());
                    }
                    return true;
                });
            }

            rewards.child(template);
        }
    }
}
