package net.vova.epicenchantments.entity.ai;

import net.vova.epicenchantments.entity.custom.AirEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;

import java.util.EnumSet;

public class AirAttackGoal extends Goal {
    private final AirEntity entity;
    private final double speedModifier;
    private final boolean followingTargetEvenIfNotSeen;

    private LivingEntity target;
    private int attackDelay = 10;
    private int ticksUntilNextAttack = 0;
    private int retreatDelay = 0;
    private boolean isRetreating = false;

    private static final int RETREAT_TIME = 15;
    private static final double ATTACK_RANGE = 3.5D;
    private static final double RETREAT_DISTANCE = 6.0D;

    public AirAttackGoal(AirEntity entity, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        this.entity = entity;
        this.speedModifier = speedModifier;
        this.followingTargetEvenIfNotSeen = followingTargetEvenIfNotSeen;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        this.target = this.entity.getTarget();

        // 🔥 ПРАВИЛЬНАЯ проверка на Creative/Spectator
        if (this.target instanceof Player player) {
            if (player.isCreative() || player.isSpectator()) {
                return false;
            }
        }

        return this.target != null && this.target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        // 🔥 ПРАВИЛЬНАЯ проверка на Creative/Spectator
        if (this.target instanceof Player player) {
            if (player.isCreative() || player.isSpectator()) {
                return false;
            }
        }

        return this.target != null && this.target.isAlive() &&
                (this.followingTargetEvenIfNotSeen || this.entity.getSensing().hasLineOfSight(this.target));
    }

    @Override
    public void start() {
        this.entity.setAggressive(true);
        this.ticksUntilNextAttack = 0;
        this.retreatDelay = 0;
        this.isRetreating = false;
    }

    @Override
    public void stop() {
        this.entity.setAggressive(false);
        this.entity.setAttacking(false);
        this.entity.getNavigation().stop();
        this.isRetreating = false;
    }

    @Override
    public void tick() {
        if (this.target == null) return;

        double distanceToTarget = this.entity.distanceToSqr(this.target);
        boolean canSeeTarget = this.entity.getSensing().hasLineOfSight(this.target);

        this.entity.getLookControl().setLookAt(this.target, 30.0F, 30.0F);

        if (this.retreatDelay > 0) {
            this.retreatDelay--;
            this.entity.setAttacking(false);

            if (!this.isRetreating) {
                this.isRetreating = true;
                Vec3 retreatDirection = this.entity.position().subtract(this.target.position()).normalize();
                Vec3 retreatPos = this.entity.position().add(retreatDirection.scale(RETREAT_DISTANCE));

                if (retreatPos.y < this.target.getY() + 2) {
                    retreatPos = new Vec3(retreatPos.x, this.target.getY() + 2 + this.entity.getRandom().nextDouble() * 3, retreatPos.z);
                }

                this.entity.getNavigation().moveTo(retreatPos.x, retreatPos.y, retreatPos.z, this.speedModifier * 1.2);
            }
            return;
        }

        this.isRetreating = false;

        if (distanceToTarget <= ATTACK_RANGE * ATTACK_RANGE && canSeeTarget) {
            this.entity.getNavigation().stop();

            if (this.ticksUntilNextAttack <= 0) {
                this.entity.setAttacking(true);
                this.performAttack(this.target);
                this.ticksUntilNextAttack = attackDelay;
                this.retreatDelay = RETREAT_TIME;
            } else {
                this.ticksUntilNextAttack--;

                if (this.ticksUntilNextAttack <= 5) {
                    this.entity.setAttacking(true);
                }
            }
        } else {
            this.entity.setAttacking(false);
            this.entity.getNavigation().moveTo(this.target, this.speedModifier);
            this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
        }

        if (this.entity.getNavigation().isInProgress()) {
            BlockPos pathPos = this.entity.getNavigation().getTargetPos();
            if (pathPos != null && pathPos.getY() < this.target.getY() - 1) {
                this.entity.getNavigation().moveTo(pathPos.getX(), this.target.getY() + 2, pathPos.getZ(), this.speedModifier);
            }
        }
    }

    private void performAttack(LivingEntity enemy) {
        this.entity.swing(InteractionHand.MAIN_HAND);
        this.entity.doHurtTarget(enemy);

        Vec3 attackDirection = enemy.position().subtract(this.entity.position()).normalize();
        this.entity.setDeltaMovement(this.entity.getDeltaMovement().add(attackDirection.scale(0.5)));
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}