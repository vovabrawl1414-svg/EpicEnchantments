package net.vova.epicenchantments.timer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.vova.epicenchantments.EpicEnchantments;
import net.vova.epicenchantments.command.TimerData;
import net.vova.epicenchantments.command.TimerManager;

@Mod.EventBusSubscriber(modid = EpicEnchantments.MODID, value = Dist.CLIENT)
public class TimerSoundHandler {

    private static final long FINISH_DISPLAY_TIME = 5000;
    private static long finishStartTime = 0;
    private static int lastSecondPlayed = -1;

    // Для предстартового отсчёта
    private static int lastCountdownSecond = -1;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        TimerData timer = TimerManager.getTimer(mc.player);

        // 🎯 ПРЕДСТАРТОВЫЙ ОТСЧЁТ (для секундомера)
        if (timer.isCountdownActive()) {
            int second = timer.getCountdownSecond();

            // Играем звук на секундах 0, 1, 2, 3
            if (second >= 0 && second <= 3 && second != lastCountdownSecond) {
                float pitch = 1.0F + (second * 0.1F);
                mc.getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING.get(), pitch, 1.0F)
                );
                lastCountdownSecond = second;
            }

            // На 4-й секунде (последняя) играем громкий звук и ЗАПУСКАЕМ секундомер
            if (second == 4 && second != lastCountdownSecond) {
                mc.getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.TOTEM_USE, 1.0F, 1.0F)
                );
                lastCountdownSecond = second;

                // 🚀 ЗАПУСКАЕМ СЕКУНДОМЕР!
                timer.startStopwatchAfterCountdown();
            }

            // Сброс, если вдруг предстарт прервали
            if (second > 4) {
                lastCountdownSecond = -1;
            }
            return;
        }

        // ⏰ ФИНИШ ТАЙМЕРА (обратный отсчёт достиг 0)
        if (timer.isFinished()) {
            if (finishStartTime == 0) {
                finishStartTime = System.currentTimeMillis();
                lastSecondPlayed = -1;
            }

            long elapsedTime = System.currentTimeMillis() - finishStartTime;
            int currentSecond = (int)(elapsedTime / 1000);

            // Играем звук на каждой секунде (0-4)
            if (currentSecond <= 4 && currentSecond != lastSecondPlayed) {
                float pitch = 1.0F + (currentSecond / 0.1F);

                mc.getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING.get(), pitch, 1.0F)
                );

                lastSecondPlayed = currentSecond;
            }

            if (elapsedTime > FINISH_DISPLAY_TIME) {
                timer.stop();
                finishStartTime = 0;
                lastSecondPlayed = -1;
            }
        } else {
            finishStartTime = 0;
            lastSecondPlayed = -1;
            lastCountdownSecond = -1;
        }
    }
}