package net.vova.epicenchantments.command;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.WeakHashMap;

public class TimerManager {
    private static final Map<Player, TimerData> PLAYER_TIMERS = new WeakHashMap<>();

    public static TimerData getTimer(Player player) {
        return PLAYER_TIMERS.computeIfAbsent(player, k -> new TimerData());
    }

    public static void removeTimer(Player player) {
        PLAYER_TIMERS.remove(player);
    }
}