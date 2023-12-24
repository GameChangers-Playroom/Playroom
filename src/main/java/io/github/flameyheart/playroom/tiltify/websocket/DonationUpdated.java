package io.github.flameyheart.playroom.tiltify.websocket;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class DonationUpdated extends WebhookStructure.Webhook {
    public WebhookStructure.Money amount;
    public UUID campaignId;
    public UUID causeId;
    public String completedAt;
    public String createdAt;
    public String donorComment;
    public String donorName;
    @Nullable
    public String email;
    public UUID fundraisingEventId;
    public UUID id;
    @Nullable
    public UUID pollId;
    @Nullable
    public UUID pollOptionId;
    @Nullable
    public String rewardCustomQuestion;
    @Nullable
    public String rewardId;
    @Nullable
    public List<WebhookStructure.RewardClaim> rewardClaims;
    public boolean sustained;
    @Nullable
    public UUID targetId;
    public UUID teamEventId;
}
