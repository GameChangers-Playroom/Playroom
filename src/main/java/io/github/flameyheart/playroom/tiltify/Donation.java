package io.github.flameyheart.playroom.tiltify;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.flameyheart.playroom.util.CodecUtils;
import net.minecraft.util.Uuids;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Donation {
    public static final Codec<Donation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      Uuids.CODEC.fieldOf("id").forGetter(Donation::id),
      Codec.STRING.fieldOf("donorName").forGetter(Donation::donorName),
      Codec.list(Reward.CODEC).fieldOf("rewards").forGetter(Donation::rewards),
      Codec.FLOAT.fieldOf("amount").forGetter(Donation::amount),
      Codec.STRING.fieldOf("currency").forGetter(Donation::currency),
      Status.CODEC.optionalFieldOf("status", Status.NORMAL).forGetter(Donation::status)
    ).apply(instance, Donation::new));

    private final UUID id;
    private final String donorName;
    private final List<Reward> rewards;
    private final float amount;
    private final String currency;
    private Status status;

    public Donation(UUID id, String donorName, List<Reward> rewards, float amount, String currency, Status status) {
        this.id = id;
        this.donorName = donorName;
        this.rewards = rewards;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
    }

    public UUID id() {
        return id;
    }

    public String donorName() {
        return donorName;
    }

    public List<Reward> rewards() {
        return rewards;
    }

    public float amount() {
        return amount;
    }

    public String currency() {
        return currency;
    }

    public Status status() {
        return status;
    }

    public void updateStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Donation) obj;
        return Objects.equals(this.id, that.id) &&
          Objects.equals(this.donorName, that.donorName) &&
          Objects.equals(this.rewards, that.rewards) &&
          Float.floatToIntBits(this.amount) == Float.floatToIntBits(that.amount) &&
          Objects.equals(this.currency, that.currency) &&
          Objects.equals(this.status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, donorName, rewards, amount, currency, status);
    }

    @Override
    public String toString() {
        return "Donation[" +
          "id=" + id + ", " +
          "donorName=" + donorName + ", " +
          "rewards=" + rewards + ", " +
          "amount=" + amount + ", " +
          "currency=" + currency + ", " +
          "status=" + status + ']';
    }

    public static final class Reward {
        public static final Codec<Reward> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          Uuids.CODEC.fieldOf("id").forGetter(Reward::id),
          Codec.STRING.fieldOf("message").forGetter(Reward::message),
          Status.CODEC.fieldOf("status").forGetter(Reward::status)
        ).apply(instance, Reward::new));

        private final UUID id;
        private final String message;
        private Status status;

        public Reward(UUID id, String message, Status status) {
            this.id = id;
            this.message = message;
            this.status = status;
        }

        public UUID id() {
            return id;
        }

        public String message() {
            return message;
        }

        public Status status() {
            return status;
        }

        public void updateStatus(Status status) {
            this.status = status;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Reward) obj;
            return Objects.equals(this.id, that.id) &&
              Objects.equals(this.message, that.message) &&
              Objects.equals(this.status, that.status);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, message, status);
        }

        @Override
        public String toString() {
            return "Reward[" +
              "id=" + id + ", " +
              "message=" + message + ", " +
              "status=" + status + ']';
        }

        public enum Status {
            AUTO_APPROVED,
            MANUAL_APPROVED,
            PLAYER_NOT_FOUND,
            TASK_NOT_FOUND;

            public static final Codec<Status> CODEC = CodecUtils.enumCodec(Status.class);
        }
    }

    public enum Status {
        NORMAL,
        REWARD_ERROR;

        public static final Codec<Status> CODEC = CodecUtils.enumCodec(Status.class);
    }
}
