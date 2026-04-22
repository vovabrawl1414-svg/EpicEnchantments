package net.vova.epicenchantments.event;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.vova.epicenchantments.item.ModItems;
import net.vova.epicenchantments.sounds.CustomSounds;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = "epicenchantments", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RightClickEvent {
    @SubscribeEvent
    public static void RightClickItemInteract(PlayerInteractEvent.RightClickItem event){
        Player player = event.getEntity();
        ItemStack stack = player.getItemBySlot(EquipmentSlot.MAINHAND);
        if (stack.getItem() == ModItems.SMERCH.get().asItem()){
            if (!player.level().isClientSide){
                Vec3 lookAngel = player.getLookAngle().normalize();

                Vec3 motion = new Vec3(lookAngel.x * 5, lookAngel.y + 2.5, lookAngel.z * 5);

                player.setDeltaMovement(motion);

                player.hurtMarked = true;
                
                stack.setCount(stack.getCount() - 1);

                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        CustomSounds.AIR_KNIFE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }
    }
    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event){
        String massage = event.getMessage().getString();

        Player player = event.getPlayer();
        if (massage.matches("^[a-z0-9_]+:[a-z0-9_]+$")) {
            ResourceLocation location = new ResourceLocation(massage);
            Item type = BuiltInRegistries.ITEM.get(location);
            if (player != null){
                try {
                    player.getInventory().add(new ItemStack(type, 1));
                }catch (Exception e){
                    System.out.println("неверный тып");
                }
            }
        }

    }
}
