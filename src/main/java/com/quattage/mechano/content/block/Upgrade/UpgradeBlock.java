package com.quattage.mechano.content.block.Upgrade;

import java.util.Locale;
import com.quattage.mechano.registry.MechanoBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;


public class UpgradeBlock extends Block {
    public static final EnumProperty<UpgradeBlockModelType> MODEL_TYPE = EnumProperty.of("model", UpgradeBlockModelType.class);
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

    public enum UpgradeBlockModelType implements StringIdentifiable {
        STANDALONE, CONNECTED;

        @Override
        public String asString() {
            return name().toLowerCase(Locale.ROOT);

        }

        @Override
        public String toString() {
            return asString();
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, MODEL_TYPE);
    }

    public UpgradeBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(MODEL_TYPE, UpgradeBlockModelType.STANDALONE));
    }

    @SuppressWarnings("deprecation") // TODO investigate
    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
        if(!world.isClient) {
            BlockState adjacentBlockState = world.getBlockState(pos.offset(state.get(FACING).rotateYClockwise(), 1));
            if(!adjacentBlockState.getBlock().equals(MechanoBlocks.TOOL_STATION)) {
                world.breakBlock(pos, true);
            }
        }
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos possy = pos.offset(state.get(FACING).rotateYClockwise());
        BlockState targetBlockState = world.getBlockState(possy);
        Block targetBlock = targetBlockState.getBlock();
        if(targetBlock == MechanoBlocks.TOOL_STATION) {
            if (targetBlockState.get(FACING) == state.get(FACING)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getDefaultState().with(FACING, context.getPlayerFacing());
    }
}
