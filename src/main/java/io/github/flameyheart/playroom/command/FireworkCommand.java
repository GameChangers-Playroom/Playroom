package io.github.flameyheart.playroom.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class FireworkCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
          literal("firework").requires(Permissions.require("playroom.command.firework", 2)).then(
            argument("duration", IntegerArgumentType.integer(0, 127)).then(
              argument("pos", Vec3ArgumentType.vec3()).executes(context -> {
                  return execute(context.getSource(), (byte) IntegerArgumentType.getInteger(context, "duration"), Vec3ArgumentType.getVec3(context, "pos"));
              })
            ).executes(context -> {
                return execute(context.getSource(), (byte) IntegerArgumentType.getInteger(context, "duration"), context.getSource().getPosition());
            })
          )
        );
    }

    private static int execute(ServerCommandSource source, byte duration, Vec3d pos) {
        ServerWorld world = source.getWorld();
        FireworkRocketEntity firework = new FireworkRocketEntity(world, pos.getX(), pos.getY(), pos.getZ(), randomizerImpl(world.getRandom(), duration));
        world.spawnEntity(firework);
        return 0;
    }

    private static ItemStack randomizerImpl(Random random, byte duration) {
        ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
        var topNbt = stack.getNbt();
        if (topNbt == null)
            topNbt = new NbtCompound();
        NbtCompound fireworks;
        if (topNbt.contains("Fireworks")) {
            fireworks = topNbt.getCompound("Fireworks");
        } else {
            fireworks = new NbtCompound();
        }
        fireworks.put("Explosions", generateRandomExplosions(random));
        fireworks.putByte("Flight", duration);
        topNbt.put("Fireworks", fireworks);
        stack.setNbt(topNbt);
        return stack;
    }

    private static NbtList generateRandomExplosions(Random random) {
        NbtCompound comp = new NbtCompound();
        // Randomize Colors
        comp.putIntArray("Colors", getRandomColors(random, 1));
        // Randomize FadeColors
        comp.putIntArray("FadeColors", getRandomColors(random, 5));
        // Randomize Type
        comp.putByte("Type", (byte) random.nextInt(5));
        // Randomize Flicker & Trail
        int randomFlickerTrail = random.nextInt(10);
        if (randomFlickerTrail == 7 || randomFlickerTrail == 9) {
            comp.putBoolean("Trail", true);
        }
        if (randomFlickerTrail == 8 || randomFlickerTrail == 9) {
            comp.putBoolean("Flicker", true);
        }
        NbtList list = new NbtList();
        list.add(comp);
        return list;
    }

    private static ArrayList<Integer> getRandomColors(Random random, int probChance) {
        ArrayList<Integer> colors = new ArrayList<>();
        if (random.nextInt(probChance) != 0) {
            return colors;
        }
        colors.add(getRandomDyeColor(random));
        if (random.nextInt(2) == 0) {
            return colors;
        }
        for (int i = 0; i < random.nextInt(3); i++) {
            colors.add(getRandomDyeColor(random));
        }
        return colors;
    }

    // Don't allow black-and-white colors in our colorful fireworks shows :)
    private static int getRandomDyeColor(Random random) {
        DyeColor c = DyeColor.BLACK;
        while (c.compareTo(DyeColor.BLACK) == 0 || c.compareTo(DyeColor.GRAY) == 0
          || c.compareTo(DyeColor.LIGHT_GRAY) == 0 || c.compareTo(DyeColor.WHITE) == 0) {
            c = DyeColor.byId(random.nextInt(16));
        }
        return c.getFireworkColor();
    }
}
