package io.github.flameyheart.playroom.dontation;

import io.github.flameyheart.playroom.config.ClientConfig;
import io.github.flameyheart.playroom.tiltify.Automation;
import io.github.flameyheart.playroom.tiltify.Donation;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RewardDisplayer {
    private static final List<DonationDisplay> donations = new ArrayList<>();

    static {
        HudRenderCallback.EVENT.register((context, delta) -> {
            int offset = donations.size();
            for(DonationDisplay donationDisplay: donations) {
                donationDisplay.render(context, --offset);
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            donations.forEach(DonationDisplay::tick);
            donations.removeIf(DonationDisplay::isDone);
        });
    }

    public void displayDonation(Donation donation, int duration) {
        MinecraftClient client = MinecraftClient.getInstance();
        String name = donation.donorName();
        String amount = donation.amount() + " " + donation.currency();
        InGameHud hud = client.inGameHud;
        ChatHud chatHud = hud.getChatHud();
        List<Text> messages = new ArrayList<>();
        if(donation.rewards().isEmpty()) {
            messages.add(Text.translatable("playroom.donation.receive.none", name, amount));
        } else if(donation.rewards().size() == 1) {
            Donation.Reward reward = donation.rewards().get(0);
            Automation.Task<?> task = Automation.get(reward.rewardId());
            messages.add(Text.translatable("playroom.donation.receive.single", name, amount, task.onDisplay()));
        } else {
            List<Text> multiple = new ArrayList<>();
            multiple.add(Text.translatable("playroom.donation.receive.multiple", name, amount));
            donation.rewards().forEach(reward -> {
                Automation.Task<?> task = Automation.get(reward.rewardId());
                multiple.add(Text.of(" - " + capitalize(task.onDisplay())));
            });
            if(ClientConfig.instance().dontationLocation != DonationLocation.CHAT) {
                Collections.reverse(multiple);
            }
            messages.addAll(multiple);
        }
        if(ClientConfig.instance().dontationLocation == DonationLocation.CHAT) {
            messages.forEach(chatHud::addMessage);
        } else {
            if(ClientConfig.instance().dontationLocation == DonationLocation.BOTTOM_RIGHT) {
                Collections.reverse(messages);
            }
            for (Text message : messages) {
                donations.add(new DonationDisplay(message, duration++));
            }
        }
    }

    private static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    static class DonationDisplay {
        Text message;
        int duration;
        int ticks = 0;

        DonationDisplay(Text message, int duration) {
            this.message = message;
            this.duration = duration;
        }

        void render(DrawContext context, int offset) {
            MinecraftClient client = MinecraftClient.getInstance();
            TextRenderer textRenderer = client.textRenderer;
            offset *= textRenderer.fontHeight;
            int colour = 0xBBFFFFFF;
            switch(ClientConfig.instance().dontationLocation) {
                case TOP_LEFT -> context.drawText(textRenderer, message, 4, 4 + offset, colour, true);
                case TOP_RIGHT -> context.drawText(textRenderer, message, client.getWindow().getScaledWidth() - textRenderer.getWidth(message) - 4, 4 + offset, colour, true);
                case BOTTOM_RIGHT -> context.drawText(textRenderer, message, client.getWindow().getScaledWidth() - textRenderer.getWidth(message) - 4, client.getWindow().getScaledHeight() - 40 - offset, colour, true);
                default -> {}
            }
        }

        void tick() {
            ticks++;
        }

        boolean isDone() {
            return ticks >= duration;
        }
    }
}
