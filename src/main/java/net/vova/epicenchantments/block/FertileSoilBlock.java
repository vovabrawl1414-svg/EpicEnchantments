package net.vova.epicenchantments.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.IPlantable;

public class FertileSoilBlock extends FarmBlock {

    public static final IntegerProperty MOISTURE = BlockStateProperties.MOISTURE;
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final int GROWTH_SPEED_MULTIPLIER = 3;
    private static final int TICKS_BETWEEN_GROWTH = 20; // Каждую секунду

    public FertileSoilBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(MOISTURE, 7));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Всегда максимальная влажность
        level.setBlock(pos, state.setValue(MOISTURE, 7), 3);

        // Проверяем блок сверху
        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);
        Block aboveBlock = aboveState.getBlock();

        // Проверяем, является ли верхний блок растением
        if (isPlant(aboveBlock)) {
            // Ускоряем рост в 4 раза
            for (int i = 0; i < GROWTH_SPEED_MULTIPLIER; i++) {
                forceGrowPlant(level, abovePos, aboveState, random);
            }
            spawnGrowthParticles(level, abovePos, random);
        }
    }

    private void forceGrowPlant(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        Block block = state.getBlock();

        // Пшеница, морковь, картофель
        if (block instanceof CropBlock crop) {
            int age = state.getValue(CropBlock.AGE);
            if (age < crop.getMaxAge()) {
                level.setBlock(pos, state.setValue(CropBlock.AGE, age + 1), 3);
            }
        }
        // Свёкла
        else if (block == Blocks.BEETROOTS) {
            int age = state.getValue(net.minecraft.world.level.block.BeetrootBlock.AGE);
            if (age < 3) {
                level.setBlock(pos, state.setValue(net.minecraft.world.level.block.BeetrootBlock.AGE, age + 1), 3);
            }
        }
        // Незерский нарост
        else if (block instanceof NetherWartBlock) {
            int age = state.getValue(NetherWartBlock.AGE);
            if (age < 3) {
                level.setBlock(pos, state.setValue(NetherWartBlock.AGE, age + 1), 3);
            }
        }
        // Ягодный куст
        else if (block instanceof SweetBerryBushBlock) {
            int age = state.getValue(SweetBerryBushBlock.AGE);
            if (age < 3) {
                level.setBlock(pos, state.setValue(SweetBerryBushBlock.AGE, age + 1), 3);
            }
        }
        // Стебли тыквы/арбуза
        else if (block instanceof StemBlock stem) {
            int age = state.getValue(StemBlock.AGE);
            if (age < 7) {
                level.setBlock(pos, state.setValue(StemBlock.AGE, age + 1), 3);
            }
        }
        // Какао
        else if (block instanceof CocoaBlock) {
            int age = state.getValue(CocoaBlock.AGE);
            if (age < 2) {
                level.setBlock(pos, state.setValue(CocoaBlock.AGE, age + 1), 3);
            }
        }
        // Тростник
        else if (block instanceof SugarCaneBlock) {
            growSugarCane(level, pos, random);
        }
        // Бамбук
        else if (block instanceof BambooStalkBlock) {
            growBamboo(level, pos, random);
        }
    }

    private void growSugarCane(ServerLevel level, BlockPos pos, RandomSource random) {
        // Находим верхушку тростника
        BlockPos topPos = pos;
        int height = 1;

        while (level.getBlockState(topPos.above()).getBlock() instanceof SugarCaneBlock) {
            topPos = topPos.above();
            height++;
        }

        // Если можно вырастить ещё
        if (height < 3 && level.getBlockState(topPos.above()).isAir()) {
            level.setBlock(topPos.above(), Blocks.SUGAR_CANE.defaultBlockState(), 3);
        }
    }

    private void growBamboo(ServerLevel level, BlockPos pos, RandomSource random) {
        // Находим верхушку бамбука
        BlockPos topPos = pos;
        int height = 1;

        while (level.getBlockState(topPos.above()).getBlock() instanceof BambooStalkBlock) {
            topPos = topPos.above();
            height++;
        }

        // Если можно вырастить ещё (макс 12)
        if (height < 12 && level.getBlockState(topPos.above()).isAir()) {
            level.setBlock(topPos.above(), Blocks.BAMBOO.defaultBlockState(), 3);
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        // Не даём превращаться в грязь
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        // Не даём превращаться в грязь
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState aboveState = level.getBlockState(pos.above());
        return !aboveState.isSolid();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MOISTURE);
    }

    @Override
    public boolean isFertile(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter level, BlockPos pos, Direction direction, IPlantable plantable) {
        return true;
    }

    private boolean isPlant(Block block) {
        return block instanceof CropBlock ||
                block == Blocks.BEETROOTS ||
                block instanceof NetherWartBlock ||
                block instanceof SweetBerryBushBlock ||
                block instanceof StemBlock ||
                block instanceof CocoaBlock ||
                block instanceof SugarCaneBlock ||
                block instanceof BambooStalkBlock ||
                block == Blocks.BAMBOO_SAPLING;
    }

    private void spawnGrowthParticles(ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() < 0.3f) {
            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    2,
                    0.3, 0.3, 0.3,
                    0.05
            );
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }
}