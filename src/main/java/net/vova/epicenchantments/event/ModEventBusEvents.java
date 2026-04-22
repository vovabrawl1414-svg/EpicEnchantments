package net.vova.epicenchantments.event;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.vova.epicenchantments.EpicEnchantments;
import net.vova.epicenchantments.command.FilterCommand;
import net.vova.epicenchantments.entity.ModEntities;
import net.vova.epicenchantments.entity.custom.AirEntity;

@Mod.EventBusSubscriber(modid = EpicEnchantments.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEventBusEvents {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.AIR.get(), AirEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        FilterCommand.register(event.getDispatcher());
        System.out.println("Filter command registered!");
    }
}