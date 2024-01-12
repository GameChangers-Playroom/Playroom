package io.github.flameyheart.playroom.render.screen;

import io.github.flameyheart.playroom.tiltify.Donation;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

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

        var message = rootComponent.childById(LabelComponent.class, "message");
        message.text(Text.literal("donation.message()")).shadow(true);
        message.shadow(true);
    }
}
