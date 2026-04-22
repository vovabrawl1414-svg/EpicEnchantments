package net.vova.epicenchantments.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.joml.Random;

import java.util.List;

public class SackItem extends Item {
    private static final int MAX_USES = 2;
    private static final int MAX_DAMAGE = MAX_USES - 1; // 0 = новый, 1 = одно использование, 2 = сломано
    private static final String TAG_ENTITY_DATA = "CapturedEntity";

    public SackItem(Properties properties) {
        super(properties.durability(MAX_DAMAGE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        // Если в мешке уже есть моб
        if (hasCapturedEntity(stack)) {
            // ПКМ с зажатым Shift для выпускания
            if (player.isShiftKeyDown()) {
                releaseEntity(level, player, stack);
                return InteractionResultHolder.success(stack);
            }
        } else {
            // Если мешок пуст - ищем моба перед игроком
            captureEntity(level, player, stack);
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        // Если в мешке есть моб - выпустить на блок
        if (hasCapturedEntity(stack) && player != null) {
            releaseEntityAt(level, pos.offset(context.getClickedFace().getNormal()), stack);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private void captureEntity(Level level, Player player, ItemStack stack) {
        // Проверяем, не сломан ли мешок
        if (stack.getDamageValue() >= stack.getMaxDamage()) {
            player.sendSystemMessage(Component.translatable("item.epicenchantments.sack.broken"));
            return;
        }

        // Поиск ближайшего живого существа в радиусе 3 блоков
        AABB searchBox = player.getBoundingBox().inflate(3.0);
        List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                e -> e != player && e.isAlive() && !(e instanceof Player)
        );

        if (!entities.isEmpty()) {
            LivingEntity target = entities.get(0);

            // Сохраняем данные моба
            CompoundTag entityData = new CompoundTag();
            target.save(entityData);

            stack.getOrCreateTag().put(TAG_ENTITY_DATA, entityData);

            // Удаляем моба из мира
            target.remove(Entity.RemovalReason.DISCARDED);

            player.sendSystemMessage(Component.translatable(
                    "item.epicenchantments.sack.captured",
                    target.getName().getString()
            ));
        } else {
            player.sendSystemMessage(Component.translatable("item.epicenchantments.sack.no_entity"));
        }
    }

    private void releaseEntity(Level level, Player player, ItemStack stack) {
        if (!hasCapturedEntity(stack)) return;
        releaseEntityAt(level, player.blockPosition(), stack);
    }

    private void releaseEntityAt(Level level, BlockPos pos, ItemStack stack) {
        if (!hasCapturedEntity(stack)) return;

        CompoundTag entityData = stack.getTag().getCompound(TAG_ENTITY_DATA);

        // Создаём моба из сохранённых данных
        EntityType.create(entityData, level).ifPresent(entity -> {
            entity.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            level.addFreshEntity(entity);

            // Наносим урон вручную (надёжнее всего)
            int newDamage = stack.getDamageValue() + 1;

            if (newDamage >= stack.getMaxDamage()) {
                // Предмет ломается
                stack.shrink(1);
            } else {
                // Просто увеличиваем damage
                stack.setDamageValue(newDamage);
            }

            // Очищаем данные моба (мешок пуст)
            stack.getTag().remove(TAG_ENTITY_DATA);
        });
    }

    private boolean hasCapturedEntity(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(TAG_ENTITY_DATA);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // Показываем прочность в процентах
        int damage = stack.getDamageValue();
        int maxDamage = stack.getMaxDamage();
        int remainingUses = maxDamage - damage + 1; // +1 потому что damage=0 это 2 использования

        if (hasCapturedEntity(stack)) {
            CompoundTag entityData = stack.getTag().getCompound(TAG_ENTITY_DATA);
            String entityName = entityData.getString("id");

            tooltip.add(Component.translatable(
                    "item.epicenchantments.sack.contains",
                    entityName.replace("minecraft:", "")
            ));
        } else {
            tooltip.add(Component.translatable("item.epicenchantments.sack.empty"));
        }
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return hasCapturedEntity(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true; // Всегда показываем полоску прочности
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        // Рассчитываем ширину полоски (0-13 пикселей)
        float damage = stack.getDamageValue();
        float maxDamage = stack.getMaxDamage();
        return Math.round(13.0f - (damage / maxDamage) * 13.0f);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        // Цвет полоски: зелёный -> жёлтый -> красный
        float damage = stack.getDamageValue();
        float maxDamage = stack.getMaxDamage();
        float health = (maxDamage - damage) / maxDamage;

        // RGB: от зелёного (0x00FF00) до красного (0xFF0000)
        int red = (int) (255 - health * 255);
        int green = (int) (health * 255);
        return (red << 16) | (green << 8) | 0;
    }
}