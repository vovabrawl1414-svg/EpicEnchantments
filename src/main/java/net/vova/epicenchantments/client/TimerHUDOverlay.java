package net.vova.epicenchantments.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.vova.epicenchantments.EpicEnchantments;
import net.vova.epicenchantments.command.TimerData;
import net.vova.epicenchantments.command.TimerManager;

@Mod.EventBusSubscriber(modid = EpicEnchantments.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TimerHUDOverlay {

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "timer", new IGuiOverlay() {
            @Override
            public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
                Minecraft mc = Minecraft.getInstance();
                Player player = mc.player;

                if (player == null) return;

                TimerData timer = TimerManager.getTimer(player);
                if (timer.getMode() == TimerData.TimerMode.STOPPED) return;

                long currentTime = timer.getCurrentTime();
                boolean isFinished = timer.isFinished();

                // Форматируем время
                String timeString = formatTime(currentTime, timer.getMode());

                // Выбираем иконку
                String icon;
                int color;

                if (timer.isPaused()) {
                    icon = "⏸️";
                    color = 0xFFAAAA; // серый
                } else if (timer.getMode() == TimerData.TimerMode.COUNTDOWN) {
                    icon = "⏳";
                    if (isFinished) {
                        color = 0xFF5555; // красный
                    } else if (currentTime < 10000) { // меньше 10 секунд
                        color = 0xFFAA00; // оранжевый
                    } else if (currentTime < 60000) { // меньше минуты
                        color = 0xFFFF55; // жёлтый
                    } else {
                        color = 0xFFFFFF; // белый
                    }
                } else {
                    icon = "⏱️";
                    color = 0xFFFFFF; // белый
                }

                // Рендерим в правом верхнем углу
                int x = screenWidth - 70;
                int y = 10;

                Component text = Component.literal(icon + " " + timeString);
                graphics.drawString(mc.font, text, x, y, color, true);
            }
        });
    }

    private static String formatTime(long ms, TimerData.TimerMode mode) {
        if (mode == TimerData.TimerMode.STOPPED) return "00:00";

        boolean negative = ms < 0;
        if (negative) ms = -ms;

        long hours = ms / (60 * 60 * 1000);
        long minutes = (ms % (60 * 60 * 1000)) / (60 * 1000);
        long seconds = (ms % (60 * 1000)) / 1000;

        String sign = negative ? "-" : "";

        if (hours > 0) {
            return String.format("%s%02d:%02d:%02d", sign, hours, minutes, seconds);
        } else {
            return String.format("%s%02d:%02d", sign, minutes, seconds);
        }
    }
}