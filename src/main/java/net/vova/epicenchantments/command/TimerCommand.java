package net.vova.epicenchantments.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;


public class TimerCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("timer")
                        .then(Commands.literal("set")
                                .then(Commands.argument("time", StringArgumentType.string())
                                        .executes(context -> {
                                            String timeStr = StringArgumentType.getString(context, "time");
                                            return setTimer(context.getSource(), timeStr);
                                        })
                                )
                        )
                        .then(Commands.literal("start")
                                .executes(context -> startTimer(context.getSource()))
                        )
                        .then(Commands.literal("pause")
                                .executes(context -> pauseTimer(context.getSource()))
                        )
                        .then(Commands.literal("resume")
                                .executes(context -> resumeTimer(context.getSource()))
                        )
                        .then(Commands.literal("stop")
                                .executes(context -> stopTimer(context.getSource()))
                        )
                        .then(Commands.literal("clear")
                                .executes(context -> clearTimer(context.getSource()))
                        )
        );
    }

    private static int setTimer(CommandSourceStack source, String timeStr) {
        if (source.getEntity() instanceof ServerPlayer player) {
            try {
                long milliseconds = parseTimeString(timeStr);
                TimerData timer = TimerManager.getTimer(player);
                timer.setCountdown(milliseconds);

                String formatted = formatTime(milliseconds);
                player.sendSystemMessage(Component.literal("§a✓ Таймер установлен на " + formatted));
                return 1;
            } catch (IllegalArgumentException e) {
                player.sendSystemMessage(Component.literal("§c✗ Неверный формат времени. Используй: 30s, 5m, 1h, 2m30s"));
                return 0;
            }
        }
        return 0;
    }

    private static int startTimer(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            TimerData timer = TimerManager.getTimer(player);
            timer.start();
            player.sendSystemMessage(Component.literal("§a✓ Таймер запущен"));
            return 1;
        }
        return 0;
    }

    private static int pauseTimer(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            TimerData timer = TimerManager.getTimer(player);
            timer.pause();
            player.sendSystemMessage(Component.literal("§e⏸ Таймер на паузе"));
            return 1;
        }
        return 0;
    }

    private static int resumeTimer(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            TimerData timer = TimerManager.getTimer(player);
            timer.resume();
            player.sendSystemMessage(Component.literal("§a▶ Таймер продолжен"));
            return 1;
        }
        return 0;
    }

    private static int stopTimer(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            TimerData timer = TimerManager.getTimer(player);
            timer.stop();
            player.sendSystemMessage(Component.literal("§c⏹ Таймер остановлен"));
            return 1;
        }
        return 0;
    }

    private static int clearTimer(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            TimerData timer = TimerManager.getTimer(player);
            timer.stop();
            player.sendSystemMessage(Component.literal("§a✓ Таймер очищен"));
            return 1;
        }
        return 0;
    }

    private static long parseTimeString(String str) {
        str = str.toLowerCase().replace(" ", "");
        long total = 0;
        String num = "";

        for (char c : str.toCharArray()) {
            if (Character.isDigit(c)) {
                num += c;
            } else {
                if (num.isEmpty()) continue;

                int value = Integer.parseInt(num);
                switch (c) {
                    case 's' -> total += value * 1000L;
                    case 'm' -> total += value * 60L * 1000L;
                    case 'h' -> total += value * 60L * 60L * 1000L;
                    default -> throw new IllegalArgumentException();
                }
                num = "";
            }
        }

        if (total == 0) throw new IllegalArgumentException();
        return total;
    }

    private static String formatTime(long ms) {
        long hours = ms / (60 * 60 * 1000);
        long minutes = (ms % (60 * 60 * 1000)) / (60 * 1000);
        long seconds = (ms % (60 * 1000)) / 1000;

        if (hours > 0) {
            return String.format("%dч %dм %dс", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dм %dс", minutes, seconds);
        } else {
            return String.format("%dс", seconds);
        }
    }
}