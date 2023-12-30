package io.github.flameyheart.playroom.tiltify.webhook;

public abstract class WebhookEvent<T extends WebhookStructure.Webhook> {
    public T data;
    public WebhookStructure.Meta meta;

    public static class DonationUpdatedEvent extends WebhookEvent<DonationUpdated> {

    }
}
