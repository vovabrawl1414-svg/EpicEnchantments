package net.vova.epicenchantments.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
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
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.vova.epicenchantments.EpicEnchantments;
import net.vova.epicenchantments.enchantments.ModEnchantments;
import net.vova.epicenchantments.item.ModItems;

import java.util.*;

@Mod.EventBusSubscriber(modid = EpicEnchantments.MODID)
public class ModEvents {

    // Максимальное количество блоков для добычи за раз (чтобы не ломать игру)
    private static final int MAX_VEIN_SIZE = 64;

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        Level level = (Level) event.getLevel();
        Block block = event.getState().getBlock();

        if (player == null || level.isClientSide()) return;

        ItemStack tool = player.getMainHandItem();

        // Проверяем все зачарования
        int excavatorLevel = EnchantmentHelper.getItemEnchantmentLevel(
                ModEnchantments.EXCAVATOR.get(), tool);
        int magnetLevel = EnchantmentHelper.getItemEnchantmentLevel(
                ModEnchantments.MAGNET.get(), tool);
        int lumberLevel = EnchantmentHelper.getItemEnchantmentLevel(
                ModEnchantments.LUMBERJACK.get(), tool);
        int autoLevel = EnchantmentHelper.getItemEnchantmentLevel(
                ModEnchantments.AUTOMELTING.get(), tool);
        int drillLevel = EnchantmentHelper.getItemEnchantmentLevel(
                ModEnchantments.DRILL.get(), tool);


        boolean hasExcavator = excavatorLevel > 0;
        boolean hasMagnet = magnetLevel > 0;
        boolean hasLumber = lumberLevel > 0;
        boolean hasAuto = autoLevel > 0;
        boolean hasDrill = drillLevel > 0;


        // Проверяем, можно ли сломать блок этим инструментом
        if (!tool.isCorrectToolForDrops(event.getState())) return;

        // Excavator — приоритет 1
        if (hasExcavator && isOreBlock(block)) {
            mineVein(level, player, event.getPos(), event.getState(), tool, hasMagnet, hasAuto);
            event.setCanceled(true);
            return;
        }

        // Lumberjack — приоритет 2
        if (hasLumber && isWoodBlock(block)) {
            mineVein(level, player, event.getPos(), event.getState(), tool, hasMagnet, hasAuto);
            event.setCanceled(true);
            return;
        }

        // DRILL — приоритет 3
        if (hasDrill) {
            handleDrill(event, player, tool, drillLevel, hasMagnet, hasAuto);
            return;
        }



        // AutoSmelt — для одиночных блоков
        if (hasAuto) {
            handleAutoSmelt(event, player, tool, hasMagnet);
            return;
        }

        // Magnet — для всех остальных
        if (hasMagnet) {
            handleMagnet(event, player, tool);
            return;
        }
    }
    private static void handleDrill(BlockEvent.BreakEvent event, Player player,
                                    ItemStack tool, int drillLevel,
                                    boolean hasMagnet, boolean hasAuto) {
        Level level = (Level) event.getLevel();
        BlockPos startPos = event.getPos();
        BlockState targetState = event.getState();
        Block targetBlock = targetState.getBlock();

        int depth = drillLevel; // 1, 2, 3
        List<BlockPos> positions = new ArrayList<>();

        // Получаем вектор взгляда игрока
        Vec3 lookVec = player.getLookAngle();

        // Определяем основное направление (по максимальной компоненте)
        double absX = Math.abs(lookVec.x);
        double absY = Math.abs(lookVec.y);
        double absZ = Math.abs(lookVec.z);

        // Собираем блоки: 3x3 перпендикулярно взгляду, depth вдоль взгляда
        for (int d = 0; d < depth; d++) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    BlockPos offset;

                    if (absX > absY && absX > absZ) {
                        // Смотрим по X (восток/запад)
                        int xOffset = lookVec.x > 0 ? d : -d;
                        offset = startPos.offset(xOffset, i, j);
                    } else if (absZ > absY && absZ > absX) {
                        // Смотрим по Z (север/юг)
                        int zOffset = lookVec.z > 0 ? d : -d;
                        offset = startPos.offset(i, j, zOffset);
                    } else {
                        // Смотрим по Y (вверх/вниз) - для кирок это основной случай
                        int yOffset = lookVec.y > 0 ? d : -d;
                        offset = startPos.offset(i, yOffset, j);
                    }

                    positions.add(offset);
                }
            }
        }

        List<ItemStack> allDrops = new ArrayList<>();

        // Ломаем блоки
        for (BlockPos pos : positions) {
            // Проверяем, что позиция в пределах мира и не совпадает с начальной несколько раз
            if (!level.isInWorldBounds(pos)) continue;

            BlockState state = level.getBlockState(pos);

            // Проверяем, что блок можно сломать этим инструментом
            if (tool.isCorrectToolForDrops(state) &&
                    state.getDestroySpeed(level, pos) >= 0) {

                // Звук и визуал
                level.playSound(null, pos, SoundEvents.STONE_BREAK,
                        SoundSource.PLAYERS, 0.3f, 1.0f);

                // Получаем дроп
                List<ItemStack> drops;
                if (hasAuto) {
                    drops = getSmeltedDrops(level, pos, player, tool, state, state.getBlock());
                } else {
                    drops = getBlockDrops(level, pos, player, tool, state);
                }
                allDrops.addAll(drops);

                tool.hurtAndBreak(1, player, (p) ->
                        p.broadcastBreakEvent(player.getUsedItemHand()));

                level.removeBlock(pos, false);
            }
        }

        // Раздача дропа
        if (hasMagnet) {
            for (ItemStack drop : allDrops) {
                if (!drop.isEmpty()) addToPlayerInventory(player, drop);
            }
        } else {
            for (ItemStack drop : allDrops) {
                if (!drop.isEmpty()) {
                    ItemEntity item = new ItemEntity(level,
                            startPos.getX() + 0.5,
                            startPos.getY() + 0.5,
                            startPos.getZ() + 0.5,
                            drop.copy()
                    );
                    item.setPickUpDelay(10);
                    level.addFreshEntity(item);
                }
            }
        }

        event.setCanceled(true);
    }

    private static void handleAutoSmelt(BlockEvent.BreakEvent event, Player player, ItemStack tool, boolean hasMagnet) {
        Level level = (Level) event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        Block block = state.getBlock();

        if (!tool.isCorrectToolForDrops(state)) return;

        // Отменяем стандартное выпадение предметов
        event.setCanceled(true);

        // Получаем дроп от блока с автоплавкой
        List<ItemStack> drops = getSmeltedDrops(level, pos, player, tool, state, block);

        // ИСПРАВЛЕНО: Правильная логика для магнита
        if (hasMagnet) {
            // Если есть магнит - предметы в инвентарь
            for (ItemStack drop : drops) {
                if (!drop.isEmpty()) {
                    addToPlayerInventory(player, drop);
                }
            }
        } else {
            // Если нет магнита - предметы на землю
            for (ItemStack drop : drops) {
                if (!drop.isEmpty()) {
                    ItemEntity itemEntity = new ItemEntity(
                            level,
                            pos.getX() + 0.5,  // Используем позицию блока, а не игрока
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            drop.copy()
                    );
                    itemEntity.setPickUpDelay(10);
                    level.addFreshEntity(itemEntity);
                }
            }
        }

        // Наносим урон инструменту
        tool.hurtAndBreak(1, player, (p) -> {
            p.broadcastBreakEvent(player.getUsedItemHand());
        });

        // Удаляем блок
        level.removeBlock(pos, false);
    }

    // Обновленный метод добычи жилы с поддержкой магнита и автоплавки
    private static void mineVein(Level level, Player player, BlockPos startPos,
                                 BlockState targetState, ItemStack tool,
                                 boolean hasMagnet, boolean hasAuto) {
        Set<BlockPos> minedPositions = new HashSet<>();
        Queue<BlockPos> toCheck = new LinkedList<>();

        toCheck.add(startPos);
        minedPositions.add(startPos);

        var targetBlock = targetState.getBlock();

        // Поиск в ширину для нахождения смежных блоков того же типа
        while (!toCheck.isEmpty() && minedPositions.size() < MAX_VEIN_SIZE) {
            BlockPos currentPos = toCheck.poll();

            for (BlockPos neighborPos : getNeighborPositions(currentPos)) {
                if (minedPositions.contains(neighborPos)) continue;

                BlockState neighborState = level.getBlockState(neighborPos);

                if (neighborState.getBlock() == targetBlock &&
                        tool.isCorrectToolForDrops(neighborState) &&
                        neighborState.getDestroySpeed(level, neighborPos) >= 0) {

                    toCheck.add(neighborPos);
                    minedPositions.add(neighborPos);

                    if (minedPositions.size() >= MAX_VEIN_SIZE) break;
                }
            }
        }

        // Собираем все дропы
        List<ItemStack> allDrops = new ArrayList<>();

        // Ломаем все найденные блоки
        for (BlockPos pos : minedPositions) {
            // Воспроизводим звук на позиции каждого блока
            level.playSound(
                    null,
                    pos.getX(), pos.getY(), pos.getZ(),
                    SoundEvents.STONE_BREAK,
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    0.3f,
                    1.0f
            );

            BlockState state = level.getBlockState(pos);

            if (state.getBlock() == targetBlock &&
                    tool.isCorrectToolForDrops(state) &&
                    state.getDestroySpeed(level, pos) >= 0) {

                // Получаем дроп от блока (с автоплавкой если есть)
                List<ItemStack> drops;
                if (hasAuto) {
                    drops = getSmeltedDrops(level, pos, player, tool, state, targetBlock);
                } else {
                    drops = getBlockDrops(level, pos, player, tool, state);
                }
                allDrops.addAll(drops);

                // Наносим урон инструменту за каждый блок
                tool.hurtAndBreak(1, player, (p) -> {
                    p.broadcastBreakEvent(player.getUsedItemHand());
                });

                // Удаляем блок
                level.removeBlock(pos, false);
            }
        }

        // ИСПРАВЛЕНО: Правильная логика для магнита
        if (hasMagnet) {
            // Если есть магнит - ВСЕ предметы в инвентарь
            for (ItemStack drop : allDrops) {
                if (!drop.isEmpty()) {
                    addToPlayerInventory(player, drop);
                }
            }
        } else {
            // Если нет магнита - ВСЕ предметы на землю
            for (ItemStack drop : allDrops) {
                if (!drop.isEmpty()) {
                    ItemEntity itemEntity = new ItemEntity(
                            level,
                            startPos.getX() + 0.5,  // Центрируем в первом блоке жилы
                            startPos.getY() + 0.5,
                            startPos.getZ() + 0.5,
                            drop.copy()
                    );
                    itemEntity.setPickUpDelay(10);
                    level.addFreshEntity(itemEntity);
                }
            }
        }
    }

    private static List<ItemStack> getSmeltedDrops(Level level, BlockPos pos, Player player,
                                                   ItemStack tool, BlockState state, Block block) {
        // Стандартный дроп (без плавки)
        List<ItemStack> originalDrops = getBlockDrops(level, pos, player, tool, state);
        List<ItemStack> smeltedDrops = new ArrayList<>();

        for (ItemStack originalDrop : originalDrops) {
            // Пытаемся получить результат плавки
            ItemStack smeltedResult = getSmeltingResult(originalDrop);

            if (!smeltedResult.isEmpty()) {
                // Если есть результат плавки, используем его
                smeltedDrops.add(smeltedResult);
            } else {
                // Если нет результата плавки, оставляем оригинальный дроп
                smeltedDrops.add(originalDrop);
            }
        }

        return smeltedDrops;
    }

    // Более надежный метод для получения результата плавки
    private static ItemStack getSmeltingResult(ItemStack input) {
        if (input.isEmpty()) return ItemStack.EMPTY;

        // Используем ручное сопоставление (надежнее)
        return manualSmeltingMap(input);
    }

    // Ручное сопоставление руд и результатов плавки
    private static ItemStack manualSmeltingMap(ItemStack input) {
        // Если это блок руды
        if (input.getItem() == net.minecraft.world.item.Items.RAW_IRON) {
            return new ItemStack(Items.IRON_INGOT, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.RAW_COPPER) {
            return new ItemStack(Items.COPPER_INGOT, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.RAW_GOLD) {
            return new ItemStack(Items.GOLD_INGOT, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.IRON_ORE ||
                input.getItem() == net.minecraft.world.item.Items.DEEPSLATE_IRON_ORE) {
            return new ItemStack(Items.IRON_INGOT, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.GOLD_ORE ||
                input.getItem() == net.minecraft.world.item.Items.DEEPSLATE_GOLD_ORE) {
            return new ItemStack(Items.GOLD_INGOT, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.COPPER_ORE ||
                input.getItem() == net.minecraft.world.item.Items.DEEPSLATE_COPPER_ORE) {
            return new ItemStack(Items.COPPER_INGOT, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.ANCIENT_DEBRIS) {
            return new ItemStack(Items.NETHERITE_SCRAP, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.NETHER_GOLD_ORE) {
            return new ItemStack(Items.GOLD_NUGGET, input.getCount() * 2); // Незерская золотая руда
        } else if (input.getItem() == net.minecraft.world.item.Items.SAND ||
                input.getItem() == net.minecraft.world.item.Items.RED_SAND) {
            return new ItemStack(Items.GLASS, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.CLAY_BALL) {
            return new ItemStack(Items.BRICK, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.CLAY) {
            return new ItemStack(Items.TERRACOTTA, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.NETHERRACK) {
            return new ItemStack(Items.NETHER_BRICK, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.CACTUS) {
            return new ItemStack(Items.GREEN_DYE, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.WET_SPONGE) {
            return new ItemStack(Items.SPONGE, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.SEA_PICKLE) {
            return new ItemStack(Items.LIME_DYE, input.getCount() * 2);
        } else if (input.getItem() == net.minecraft.world.item.Items.KELP) {
            return new ItemStack(Items.DRIED_KELP, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.BEEF) {
            return new ItemStack(Items.COOKED_BEEF, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.PORKCHOP) {
            return new ItemStack(Items.COOKED_PORKCHOP, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.CHICKEN) {
            return new ItemStack(Items.COOKED_CHICKEN, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.MUTTON) {
            return new ItemStack(Items.COOKED_MUTTON, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.RABBIT) {
            return new ItemStack(Items.COOKED_RABBIT, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.COD) {
            return new ItemStack(Items.COOKED_COD, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.SALMON) {
            return new ItemStack(Items.COOKED_SALMON, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.POTATO) {
            return new ItemStack(Items.BAKED_POTATO, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.CHORUS_FRUIT) {
            return new ItemStack(Items.POPPED_CHORUS_FRUIT, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.STONE) {
            return new ItemStack(Items.SMOOTH_STONE, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.COBBLESTONE) {
            return new ItemStack(Items.STONE, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.SANDSTONE) {
            return new ItemStack(Items.SMOOTH_SANDSTONE, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.RED_SANDSTONE) {
            return new ItemStack(Items.SMOOTH_RED_SANDSTONE, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.QUARTZ_BLOCK) {
            return new ItemStack(Items.SMOOTH_QUARTZ, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.STONE_BRICKS) {
            return new ItemStack(Items.CRACKED_STONE_BRICKS, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.NETHER_QUARTZ_ORE) {
            return new ItemStack(Items.QUARTZ, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.RAW_IRON_BLOCK) {
            return new ItemStack(Items.IRON_BLOCK, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.RAW_COPPER_BLOCK) {
            return new ItemStack(Items.COPPER_BLOCK, input.getCount());
        } else if (input.getItem() == net.minecraft.world.item.Items.RAW_GOLD_BLOCK) {
            return new ItemStack(Items.GOLD_BLOCK, input.getCount());
        }

        return ItemStack.EMPTY; // Если нет плавки, возвращаем пустой стак
    }

    private static void handleMagnet(BlockEvent.BreakEvent event, Player player, ItemStack tool) {
        Level level = (Level) event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = event.getState();

        if (!tool.isCorrectToolForDrops(state)) return;

        // Отменяем стандартное выпадение предметов
        event.setCanceled(true);

        // Получаем дроп от блока
        List<ItemStack> drops = getBlockDrops(level, pos, player, tool, state);

        // Добавляем предметы прямо в инвентарь
        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                addToPlayerInventory(player, drop);
            }
        }

        // Наносим урон инструменту
        tool.hurtAndBreak(1, player, (p) -> {
            p.broadcastBreakEvent(player.getUsedItemHand());
        });

        // Удаляем блок
        level.removeBlock(pos, false);
    }

    // Метод для получения дропа от блока
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

    // Метод для добавления предметов в инвентарь игрока
    private static void addToPlayerInventory(Player player, ItemStack stack) {
        if (player.level().isClientSide()) return;

        ServerPlayer serverPlayer = (ServerPlayer) player;

        // Пытаемся добавить в инвентарь
        if (!serverPlayer.getInventory().add(stack)) {
            // Если не поместилось, выкидываем на землю рядом с игроком
            ItemEntity itemEntity = new ItemEntity(
                    player.level(),
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    stack.copy()
            );
            player.level().addFreshEntity(itemEntity);
        } else {
            // Обновляем инвентарь
            serverPlayer.containerMenu.broadcastChanges();

            player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.ITEM_PICKUP,
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    0.2f,
                    1.0f
            );
        }
    }

    // Метод для проверки рудного блока
    private static boolean isOreBlock(Block block) {
        return block == Blocks.COAL_ORE ||
                block == Blocks.DEEPSLATE_COAL_ORE ||
                block == Blocks.COPPER_ORE ||
                block == Blocks.DEEPSLATE_COPPER_ORE ||
                block == Blocks.IRON_ORE ||
                block == Blocks.DEEPSLATE_IRON_ORE ||
                block == Blocks.GOLD_ORE ||
                block == Blocks.DEEPSLATE_GOLD_ORE ||
                block == Blocks.DIAMOND_ORE ||
                block == Blocks.DEEPSLATE_DIAMOND_ORE ||
                block == Blocks.EMERALD_ORE ||
                block == Blocks.DEEPSLATE_EMERALD_ORE ||
                block == Blocks.LAPIS_ORE ||
                block == Blocks.DEEPSLATE_LAPIS_ORE ||
                block == Blocks.REDSTONE_ORE ||
                block == Blocks.DEEPSLATE_REDSTONE_ORE ||
                block == Blocks.NETHER_QUARTZ_ORE ||
                block == Blocks.NETHER_GOLD_ORE ||
                block == Blocks.ANCIENT_DEBRIS ||
                // Руды 1.17+
                block == Blocks.RAW_COPPER_BLOCK ||
                block == Blocks.RAW_IRON_BLOCK ||
                block == Blocks.RAW_GOLD_BLOCK ||
                // Аметист
                block == Blocks.AMETHYST_CLUSTER ||
                block == Blocks.LARGE_AMETHYST_BUD ||
                block == Blocks.MEDIUM_AMETHYST_BUD ||
                block == Blocks.SMALL_AMETHYST_BUD ||
                // Дополнительные
                block == Blocks.GILDED_BLACKSTONE ||
                block == Blocks.NETHERITE_BLOCK;
    }

    private static boolean isWoodBlock(Block block) {
        return block == Blocks.ACACIA_LOG ||
                block == Blocks.BIRCH_LOG ||
                block == Blocks.CHERRY_LOG ||
                block == Blocks.DARK_OAK_LOG ||
                block == Blocks.JUNGLE_LOG ||
                block == Blocks.MANGROVE_LOG ||
                block == Blocks.OAK_LOG ||
                block == Blocks.SPRUCE_LOG ||
                block == Blocks.STRIPPED_ACACIA_LOG ||
                block == Blocks.STRIPPED_BIRCH_LOG ||
                block == Blocks.STRIPPED_CHERRY_LOG ||
                block == Blocks.STRIPPED_DARK_OAK_LOG ||
                block == Blocks.STRIPPED_JUNGLE_LOG ||
                block == Blocks.STRIPPED_MANGROVE_LOG ||
                block == Blocks.STRIPPED_OAK_LOG ||
                block == Blocks.STRIPPED_SPRUCE_LOG ||
                block == Blocks.WARPED_STEM ||
                block == Blocks.CRIMSON_STEM;
    }

    private static List<BlockPos> getNeighborPositions(BlockPos pos) {
        List<BlockPos> neighbors = new ArrayList<>();

        // Все 6 направлений
        neighbors.add(pos.above());
        neighbors.add(pos.below());
        neighbors.add(pos.north());
        neighbors.add(pos.south());
        neighbors.add(pos.west());
        neighbors.add(pos.east());

        return neighbors;
    }
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack mainHandItem = player.getMainHandItem();

            if (mainHandItem.getItem() == ModItems.OBSIDIAN_SWORD.get()) {

                Collection<ItemEntity> drops = event.getDrops();
                List<ItemEntity> extraDrops = new ArrayList<>();

                for (ItemEntity drop : drops) {
                    ItemStack dropStack = drop.getItem().copy();
                    dropStack.setCount(dropStack.getCount());
                    ItemEntity extraDrop = new ItemEntity(
                            event.getEntity().level(),
                            event.getEntity().getX(),
                            event.getEntity().getY(),
                            event.getEntity().getZ(),
                            dropStack
                    );
                    extraDrop.setDefaultPickUpDelay();
                    extraDrops.add(extraDrop);
                }

                for (ItemEntity extraDrop : extraDrops) {
                    event.getEntity().level().addFreshEntity(extraDrop);
                }
            }
        }
    }
}