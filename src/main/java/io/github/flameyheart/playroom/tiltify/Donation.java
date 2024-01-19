package io.github.flameyheart.playroom.tiltify;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.flameyheart.playroom.util.CodecUtils;
import net.minecraft.util.Uuids;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Donation {
    public static final Codec<Donation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      Uuids.CODEC.fieldOf("id").forGetter(Donation::id),
      Codec.STRING.fieldOf("donorName").forGetter(Donation::donorName),
      Codec.STRING.fieldOf("message").forGetter(Donation::message),
      Codec.list(Reward.CODEC).fieldOf("rewards").forGetter(Donation::rewards),
      Codec.FLOAT.fieldOf("amount").forGetter(Donation::amount),
      Codec.STRING.fieldOf("currency").forGetter(Donation::currency),
      Status.CODEC.optionalFieldOf("status", Status.NORMAL).forGetter(Donation::status)
    ).apply(instance, Donation::new));

    private final UUID id;
    private final String donorName;
    private final String message;
    private final List<Reward> rewards;
    private final float amount;
    private final String currency;
    private Status status;

    public Donation(UUID id, String donorName, String message, List<Reward> rewards, float amount, String currency, Status status) {
        this.id = id;
        this.donorName = donorName;
        this.message = message;
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

    public String message() {
        return message;
    }

    public List<Reward> rewards() {
        return rewards;
    }

    public Reward reward(UUID id) {
        return rewards.stream().filter(reward -> reward.claimId().equals(id)).findFirst().orElse(null);
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
        StringBuilder rewards = new StringBuilder();
        for (Reward reward : this.rewards) {
            rewards.append(reward.toString()).append(",\n");
        }

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
          Uuids.CODEC.fieldOf("rewardId").forGetter(Reward::rewardId),
          Uuids.CODEC.fieldOf("claimId").forGetter(Reward::claimId),
          Codec.STRING.fieldOf("name").forGetter(Reward::name),
          Codec.STRING.fieldOf("message").forGetter(Reward::message),
          Status.CODEC.fieldOf("status").forGetter(Reward::status)
        ).apply(instance, Reward::new));

        private final UUID rewardId;
        private final UUID claimId;
        private final String name;
        private final String message;
        private Status status;

        public Reward(UUID rewardId, UUID claimId, String name, String message, Status status) {
            this.rewardId = rewardId;
            this.claimId = claimId;
            this.name = name;
            this.message = message;
            this.status = status;
        }

        public UUID rewardId() {
            return rewardId;
        }

        public UUID claimId() {
            return claimId;
        }

        public String name() {
            return name;
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
        public String toString() {
            return "Reward[" +
              "rewardId=" + rewardId + ", " +
              "claimId=" + claimId + ", " +
              "name=" + name + ", " +
              "message=" + message + ", " +
              "status=" + status + ']';
        }

        public enum Status {
            AUTO_APPROVED(false),
            MANUAL_APPROVED(false),
            PLAYER_NOT_FOUND(true),
            TASK_NOT_FOUND(true);

            public static final Codec<Status> CODEC = CodecUtils.enumCodec(Status.class);
            public final boolean error;

            Status(boolean error) {
                this.error = error;
            }
        }
    }

    public enum Status {
        NORMAL,
        REWARD_ERROR;

        public static final Codec<Status> CODEC = CodecUtils.enumCodec(Status.class);
    }
}
