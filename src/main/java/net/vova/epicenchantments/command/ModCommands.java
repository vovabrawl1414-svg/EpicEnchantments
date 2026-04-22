package net.vova.epicenchantments.command;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.vova.epicenchantments.EpicEnchantments;

@Mod.EventBusSubscriber(modid = EpicEnchantments.MODID)
public class ModCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        FilterCommand.register(event.getDispatcher());
        TimerCommand.register(event.getDispatcher());
    }
}