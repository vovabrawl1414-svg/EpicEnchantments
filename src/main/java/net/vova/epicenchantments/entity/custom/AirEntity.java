package net.vova.epicenchantments.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import net.vova.epicenchantments.EpicEnchantments;
import net.vova.epicenchantments.entity.ai.AirAttackGoal;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class AirEntity extends Monster {
    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(AirEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;

    public final AnimationState attackAnimationState = new AnimationState();
    public int attackAnimationTimeout = 0;

    public AirEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);

        // ✅ ПЛАВНОЕ управление полётом (уменьшена резкость)
        this.moveControl = new FlyingMoveControl(this, 15, false); // 10 вместо 20, false для плавности
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 16.0F);
        this.setPathfindingMalus(BlockPathTypes.COCOA, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.FENCE, -1.0F);
    }

    @Override
    protected ResourceLocation getDefaultLootTable(){
        return new ResourceLocation(EpicEnchantments.MODID,"entities/air");
    }

    @Override
    protected PathNavigation createNavigation(Level pLevel) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, pLevel) {
            @Override
            public void tick() {
                super.tick();
                // ✅ Плавное движение без резких остановок
                if (this.isDone() && this.mob.getTarget() == null) {
                    this.mob.setDeltaMovement(this.mob.getDeltaMovement().scale(0.98));
                }
            }
        };
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    @Override
    public void tick() {
        super.tick();

        this.setNoGravity(true);

        if(this.level().isClientSide()) {
            setupAnimationStates();
        }
    }

    private void setupAnimationStates() {
        if(this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = this.random.nextInt(40) + 80;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }

        if(this.isAttacking() && attackAnimationTimeout <= 0) {
            attackAnimationTimeout = 15; // Быстрая анимация
            attackAnimationState.start(this.tickCount);
        } else {
            --this.attackAnimationTimeout;
        }

        if(!this.isAttacking()) {
            attackAnimationState.stop();
        }
    }

    @Override
    protected void updateWalkAnimation(float pPartialTick) {
        float f = Math.min(pPartialTick * 6F, 1f);
        this.walkAnimation.update(f, 0.2f);
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
    }

    @Override
    public void travel(Vec3 pTravelVector) {
        if (this.isEffectiveAi() || this.isControlledByLocalInstance()) {
            // ✅ ПЛАВНОЕ движение без рывков
            float speed = this.getSpeed();

            if (this.isInWater()) {
                this.moveRelative(0.02F, pTravelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.8F));
            } else if (this.isInLava()) {
                this.moveRelative(0.02F, pTravelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5F));
            } else {
                // Плавный полёт в воздухе
                this.moveRelative(speed, pTravelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                // ✅ Плавное замедление вместо резкой остановки
                this.setDeltaMovement(this.getDeltaMovement().scale(0.91F));
            }
        }
        this.calculateEntityAnimation(false);
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACKING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));


        this.goalSelector.addGoal(1, new AirAttackGoal(this, 1.8D, true));

        // ✅ Полёт к цели
        this.goalSelector.addGoal(2, new FlyingTowardsTargetGoal(this, 1.6D, 20f));

        // ✅ Случайный полёт (увеличен радиус для высоты)
        this.goalSelector.addGoal(3, new RandomFlyingGoal(this, 1.2D));

        // ✅ Смотреть на игрока
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 12.0F));

        // ✅ Смотреть по сторонам
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        // Цели
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                LivingEntity target = this.mob.getLastHurtByMob();
                // 🔥 ПРАВИЛЬНАЯ проверка
                if (target instanceof Player player) {
                    if (player.isCreative() || player.isSpectator()) {
                        return false;
                    }
                }
                return super.canUse();
            }
        });
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (target) -> {
                    // 🔥 ПРАВИЛЬНАЯ проверка в предикате
                    if (target instanceof Player player) {
                        return !player.isCreative() && !player.isSpectator();
                    }
                    return false;
                }
        ));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)         // Увеличена дальность преследования
                .add(Attributes.MOVEMENT_SPEED, 0.09D)       // Увеличена скорость
                .add(Attributes.FLYING_SPEED, 0.09D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)         // Увеличен урон
                .add(Attributes.ATTACK_KNOCKBACK, 1.0D)
                .add(Attributes.ATTACK_SPEED, 3.0D)          // Быстрая атака
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 1.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.2D);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PHANTOM_AMBIENT;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.PHANTOM_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PHANTOM_DEATH;
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 2;
    }

    @Override
    public boolean isMaxGroupSizeReached(int pSize) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
        return false;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return true;
    }

    // ✅ Цель для полёта вперёд
    static class FlyingTowardsTargetGoal extends Goal {
        private final AirEntity mob;
        private final double speedModifier;
        private final float withinDistance;

        public FlyingTowardsTargetGoal(AirEntity mob, double speedModifier, float withinDistance) {
            this.mob = mob;
            this.speedModifier = speedModifier;
            this.withinDistance = withinDistance;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.mob.getTarget();
            return target != null && target.isAlive() &&
                    this.mob.distanceToSqr(target) > this.withinDistance * this.withinDistance;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.mob.getTarget();
            return target != null && target.isAlive() && !this.mob.navigation.isDone();
        }

        @Override
        public void start() {
            LivingEntity target = this.mob.getTarget();
            if (target != null) {
                this.mob.navigation.moveTo(target, this.speedModifier);
            }
        }

        @Override
        public void stop() {
            this.mob.navigation.stop();
        }
    }

    // ✅ Улучшенный случайный полёт с поддержанием высоты
    static class RandomFlyingGoal extends Goal {
        private final AirEntity mob;
        private final double speedModifier;
        private Vec3 targetPos;

        public RandomFlyingGoal(AirEntity mob, double speedModifier) {
            this.mob = mob;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return this.mob.getTarget() == null &&
                    (this.mob.navigation.isDone() || this.mob.random.nextInt(20) == 0);
        }

        @Override
        public boolean canContinueToUse() {
            return this.mob.getTarget() == null &&
                    !this.mob.navigation.isDone() &&
                    this.targetPos != null;
        }

        @Override
        public void start() {
            this.targetPos = this.findRandomFlyingPos();
            if (this.targetPos != null) {
                this.mob.navigation.moveTo(this.targetPos.x, this.targetPos.y, this.targetPos.z, this.speedModifier);
            }
        }

        @Nullable
        private Vec3 findRandomFlyingPos() {
            // ✅ Поддержание высоты: минимум 3 блока над землёй, максимум 15
            LivingEntity target = this.mob.getTarget();

            if (target != null) {
                // Кружим вокруг цели
                double angle = this.mob.random.nextDouble() * Math.PI * 2;
                double distance = 4.0 + this.mob.random.nextDouble() * 4.0;
                double x = target.getX() + Math.cos(angle) * distance;
                double z = target.getZ() + Math.sin(angle) * distance;
                double y = target.getY() + 2.0 + this.mob.random.nextDouble() * 3.0;
                return new Vec3(x, y, z);
            }

            // Случайный полёт с удержанием высоты
            Vec3 viewVector = this.mob.getViewVector(0.0F);
            Vec3 hoverPos = HoverRandomPos.getPos(this.mob, 10, 7,
                    viewVector.x, viewVector.z, (float)Math.PI / 2F, 5, 2);

            if (hoverPos != null) {
                // ✅ Принудительно поднимаем высоту если слишком низко
                BlockPos groundPos = this.mob.getOnPos();
                if (hoverPos.y < groundPos.getY() + 3) {
                    hoverPos = new Vec3(hoverPos.x, groundPos.getY() + 3 + this.mob.random.nextDouble() * 5, hoverPos.z);
                }
                return hoverPos;
            }

            return AirAndWaterRandomPos.getPos(this.mob, 8, 5, 2, viewVector.x, viewVector.z, (float)Math.PI / 2F);
        }
    }
}