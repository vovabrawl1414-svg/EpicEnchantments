package net.vova.epicenchantments.event;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.vova.epicenchantments.EpicEnchantments;
import net.vova.epicenchantments.enchantments.ModEnchantments;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = EpicEnchantments.MODID)
public class ReplanterEvents {

    // Хранилище защищённых позиций (ключ - позиция, значение - время окончания защиты)
    private static final Map<BlockPos, Long> protectedPositions = new ConcurrentHashMap<>();
    private static final long PROTECTION_TIME = 2000; // 2 секунды в миллисекундах (40 тиков)

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        Level level = (Level) event.getLevel();
        Block block = event.getState().getBlock();
        BlockState state = event.getState();
        BlockPos pos = event.getPos();

        if (player == null || level.isClientSide()) return;

        // ⚡ ПРОВЕРКА НА ЗАЩИТУ ⚡
        if (isProtected(pos)) {
            event.setCanceled(true); // Отменяем ломание защищённого блока
            player.displayClientMessage(Component.literal("§b🌱 Растение только посажено! Подожди 2 секунды"), true);
            return;
        }

        ItemStack tool = player.getMainHandItem();

        int replanterLevel = EnchantmentHelper.getItemEnchantmentLevel(
                ModEnchantments.REPLANTER.get(), tool);

        if (replanterLevel == 0) return;
        if (!(tool.getItem() instanceof HoeItem)) return;
        if (!isCrop(block)) return;
        if (!isFullyGrown(state)) return;

        double chance = getReplantChance(replanterLevel);

        List<ItemStack> drops = getBlockDrops(level, pos, player, tool, state);

        if (chance >= 1.0 || level.random.nextDouble() < chance) {
            ItemStack seeds = getSeedsForCrop(block);

            if (!seeds.isEmpty() && hasSeeds(player, seeds)) {
                takeOneSeed(player, seeds);

                level.removeBlock(pos, false);

                // 🌱 Сажаем новое растение
                level.setBlock(pos, block.defaultBlockState(), 3);

                // 🛡️ АКТИВИРУЕМ ЗАЩИТУ
                protectPosition(pos);

                // Эффекты
                level.playSound(null, pos, SoundEvents.CROP_PLANTED,
                        SoundSource.PLAYERS, 0.5f, 1.0f);

                if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            5,
                            0.3, 0.3, 0.3,
                            0.1
                    );

                    // ✨ Добавляем частицы защиты (зелёные звёздочки)
                    serverLevel.sendParticles(
                            net.minecraft.core.particles.ParticleTypes.WAX_ON,
                            pos.getX() + 0.5,
                            pos.getY() + 0.8,
                            pos.getZ() + 0.5,
                            3,
                            0.2, 0.2, 0.2,
                            0.05
                    );
                }

                event.setCanceled(true);
            }
        }

        // Возвращаем дроп
        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                ItemEntity item = new ItemEntity(level,
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        drop.copy()
                );
                item.setPickUpDelay(10);
                level.addFreshEntity(item);
            }
        }

        tool.hurtAndBreak(1, player, (p) ->
                p.broadcastBreakEvent(player.getUsedItemHand()));
    }

    // 🛡️ МЕТОДЫ ЗАЩИТЫ 🛡️

    private static void protectPosition(BlockPos pos) {
        protectedPositions.put(pos, System.currentTimeMillis() + PROTECTION_TIME);

        // Запускаем таймер для автоматической очистки через 2 секунды
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                protectedPositions.remove(pos);
            }
        }, PROTECTION_TIME);
    }

    private static boolean isProtected(BlockPos pos) {
        Long protectionEnd = protectedPositions.get(pos);
        if (protectionEnd == null) return false;

        if (System.currentTimeMillis() > protectionEnd) {
            protectedPositions.remove(pos);
            return false;
        }
        return true;
    }

    // Очистка старых защит (вызывать периодически)
    private static void cleanupOldProtections() {
        long now = System.currentTimeMillis();
        protectedPositions.entrySet().removeIf(entry -> now > entry.getValue());
    }

    // Все остальные методы (isCrop, isFullyGrown, getSeedsForCrop и т.д.) остаются без изменений
    private static boolean isCrop(Block block) {
        return block == Blocks.WHEAT ||
                block == Blocks.CARROTS ||
                block == Blocks.POTATOES ||
                block == Blocks.BEETROOTS ||
                block == Blocks.NETHER_WART ||
                block == Blocks.SWEET_BERRY_BUSH ||
                block == Blocks.COCOA ||
                block == Blocks.MELON_STEM;
    }

    private static boolean isFullyGrown(BlockState state) {
        Block block = state.getBlock();

        if (block == Blocks.WHEAT || block == Blocks.CARROTS || block == Blocks.POTATOES) {
            return state.getValue(net.minecraft.world.level.block.CropBlock.AGE) == 7;
        }
        if (block == Blocks.BEETROOTS) {
            return state.getValue(net.minecraft.world.level.block.BeetrootBlock.AGE) == 3;
        }
        if (block == Blocks.NETHER_WART) {
            return state.getValue(net.minecraft.world.level.block.NetherWartBlock.AGE) == 3;
        }
        if (block == Blocks.SWEET_BERRY_BUSH) {
            return state.getValue(net.minecraft.world.level.block.SweetBerryBushBlock.AGE) == 3;
        }
        if (block == Blocks.COCOA) {
            return state.getValue(net.minecraft.world.level.block.CocoaBlock.AGE) == 2;
        }
        if (block == Blocks.MELON_STEM) {
            return state.getValue(net.minecraft.world.level.block.StemBlock.AGE) == 7;
        }

        return false;
    }

    private static ItemStack getSeedsForCrop(Block block) {
        if (block == Blocks.WHEAT) return new ItemStack(Items.WHEAT_SEEDS);
        if (block == Blocks.CARROTS) return new ItemStack(Items.CARROT);
        if (block == Blocks.POTATOES) return new ItemStack(Items.POTATO);
        if (block == Blocks.BEETROOTS) return new ItemStack(Items.BEETROOT_SEEDS);
        if (block == Blocks.NETHER_WART) return new ItemStack(Items.NETHER_WART);
        if (block == Blocks.SWEET_BERRY_BUSH) return new ItemStack(Items.SWEET_BERRIES);
        if (block == Blocks.COCOA) return new ItemStack(Items.COCOA_BEANS);
        if (block == Blocks.MELON_STEM) return new ItemStack(Items.MELON_SEEDS);
        return ItemStack.EMPTY;
    }

    private static double getReplantChance(int level) {
        switch(level) {
            case 1: return 0.4;
            case 2: return 0.6;
            case 3: return 0.8;
            case 4: return 1.0;
            default: return 0.0;
        }
    }

    private static boolean hasSeeds(Player player, ItemStack seeds) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack invStack = player.getInventory().getItem(i);
            if (invStack.getItem() == seeds.getItem()) {
                return true;
            }
        }
        return false;
    }

    private static void takeOneSeed(Player player, ItemStack seeds) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack invStack = player.getInventory().getItem(i);
            if (invStack.getItem() == seeds.getItem()) {
                invStack.shrink(1);
                break;
            }
        }
    }

    private static List<ItemStack> getBlockDrops(Level level, BlockPos pos, Player player,
                                                 ItemStack tool, BlockState state) {
        BlockEntity blockEntity = level.getBlockEntity(pos);

        LootParams.Builder lootBuilder = new LootParams.Builder((net.minecraft.server.level.ServerLevel) level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, tool)
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity);

        return state.getDrops(lootBuilder);
    }
}