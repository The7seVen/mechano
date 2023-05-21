package com.quattage.mechano.content.block.ToolStation;

import java.util.Locale;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.quattage.mechano.Mechano;
import com.quattage.mechano.registry.MechanoBlocks;
import com.quattage.mechano.registry.MechanoBlockEntities;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.item.ItemHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;


public class ToolStationBlock extends HorizontalDirectionalBlock implements ITE<ToolStationBlockEntity> {
    public static final EnumProperty<WideBlockModelType> MODEL_TYPE = EnumProperty.create("model", WideBlockModelType.class);
    protected static final VoxelShape BLOCK_NORTH = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape BLOCK_SOUTH = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape BLOCK_EAST = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape BLOCK_WEST = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    
    public ToolStationBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(MODEL_TYPE, WideBlockModelType.BASE));
    }

    public enum WideBlockModelType implements StringRepresentable {
        BASE, FORGED, HEATED, MAXIMIZED, DUMMY;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        @Override
        public String toString() {
            return getSerializedName();
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            if (state.getValue(MODEL_TYPE) != WideBlockModelType.DUMMY) {
                BlockPos otherpos = pos.relative(state.getValue(FACING).getClockWise());
                BlockState otherstate = level.getBlockState(otherpos);
                if (otherstate.getBlock() == this) {
                    level.setBlock(otherpos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    level.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, otherpos, Block.getId(otherstate));
                }
            }
            else {
                BlockPos otherpos = pos.relative(state.getValue(FACING).getCounterClockWise());
                BlockState otherstate = level.getBlockState(otherpos);
                if (otherstate.getBlock() == this) {
                    level.setBlock(otherpos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    level.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, otherpos, Block.getId(otherstate));
                }
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity player, ItemStack stack) {
        super.setPlacedBy(world, pos, state, player, stack);
        if(!world.isClientSide) {
            BlockPos possy = pos.relative(state.getValue(FACING).getClockWise());
            world.setBlock(possy, state.setValue(MODEL_TYPE, WideBlockModelType.DUMMY), Block.UPDATE_ALL);
            world.updateNeighborsAt(pos, Blocks.AIR);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        super.neighborChanged(state, world, pos, sourceBlock, sourcePos, notify);
        if(!world.isClientSide) {
            if(state.getValue(MODEL_TYPE) == WideBlockModelType.DUMMY) {
                referToMainBlockForUpdate(state, world, pos, sourceBlock, sourcePos, notify);
            } else {
                mainBlockUpdate(state, world, pos, sourceBlock, sourcePos, notify);
            }
        }
    }

    private void referToMainBlockForUpdate(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        BlockPos mainBlockPos = pos.relative(state.getValue(FACING).getCounterClockWise());
        BlockState mainBlockState = world.getBlockState(mainBlockPos);
        this.mainBlockUpdate(mainBlockState, world, mainBlockPos, sourceBlock, sourcePos, notify);
    }

    private void mainBlockUpdate(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        BlockPos left = pos.relative(state.getValue(FACING).getCounterClockWise());
        BlockPos right = pos.relative(state.getValue(FACING).getClockWise(), 2);
        if(sourcePos.equals(left) || sourcePos.equals(right)) {
            BlockState leftBlockState = world.getBlockState(left);
            BlockState rightBlockState = world.getBlockState(right);
            if(leftBlockState.getBlock().equals(MechanoBlocks.FORGE_UPGRADE.get()) && rightBlockState.getBlock().equals(MechanoBlocks.INDUCTOR.get())) {
                world.setBlock(pos, state.setValue(MODEL_TYPE, WideBlockModelType.MAXIMIZED), Block.UPDATE_ALL);
            } else if(leftBlockState.getBlock().equals(MechanoBlocks.FORGE_UPGRADE.get())) {
                world.setBlock(pos, state.setValue(MODEL_TYPE, WideBlockModelType.FORGED), Block.UPDATE_ALL);
            } else if(rightBlockState.getBlock().equals(MechanoBlocks.INDUCTOR.get())) {
                world.setBlock(pos, state.setValue(MODEL_TYPE, WideBlockModelType.HEATED), Block.UPDATE_ALL);
            } else {
                world.setBlock(pos, state.setValue(MODEL_TYPE, WideBlockModelType.BASE), Block.UPDATE_ALL);
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, @NotNull BlockGetter view, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        switch (state.getValue(FACING)) {
            case NORTH:
                return BLOCK_NORTH;
            case SOUTH:
                return BLOCK_SOUTH;
            case WEST:
                return BLOCK_WEST;
            default:
                return BLOCK_EAST;
        }
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter view, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, MODEL_TYPE);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader view, BlockPos pos) {
        BlockPos otherpos = pos.relative(state.getValue(FACING).getClockWise());
        return view.getBlockState(otherpos).getMaterial().isReplaceable();
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter view, BlockPos pos) {
        return 1;
    }
    
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.hasBlockEntity() || state.getBlock() == newState.getBlock())
			return;

		withTileEntityDo(level, pos, te -> ItemHelper.dropContents(level, pos, te.INVENTORY));
		level.removeBlockEntity(pos);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            withTileEntityDo(level, pos, entity -> NetworkHooks.openScreen((ServerPlayer) player, entity, entity::sendToContainer));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
	public BlockEntityType<? extends ToolStationBlockEntity> getTileEntityType() {
		return MechanoBlockEntities.TOOL_STATION.get();
	}

    @Override
    public Class<ToolStationBlockEntity> getTileEntityClass() {
        return ToolStationBlockEntity.class;
    }
}