package com.quattage.experimental_tables.content.block;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.quattage.experimental_tables.ExperimentalTables;
import com.quattage.experimental_tables.content.block.entity.ToolStationBlockEntity;
import com.quattage.experimental_tables.registry.ModBlockEntities;

import blue.endless.jankson.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;


public class WideTableBlock extends BlockWithEntity {
    public static final EnumProperty<WideBlockModelType> MODEL_TYPE = EnumProperty.of("model", WideBlockModelType.class);
    protected static final VoxelShape BLOCK_NORTH = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape BLOCK_SOUTH = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape BLOCK_EAST = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape BLOCK_WEST = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    private final ContainerManager container;
    private final Supplier<Text> containerName;
    
    public WideTableBlock(ContainerManager container, Settings settings) {
        super(settings);
        this.container = container;
        containerName = Suppliers.memoize(() -> Text.translatable("container.tables." + Registry.BLOCK.getKey(WideTableBlock.this).toString()));
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(MODEL_TYPE, WideBlockModelType.MAIN));
    }

    public enum WideBlockModelType implements StringIdentifiable {
        MAIN, SIDE, CONNECTED;

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
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getDefaultState().with(FACING, context.getPlayerFacing());
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient) {
            WideBlockModelType wideBlockModel = state.get(MODEL_TYPE);
            if (wideBlockModel == WideBlockModelType.MAIN || wideBlockModel == WideBlockModelType.CONNECTED) {
                BlockPos otherpos = pos.offset(state.get(FACING).rotateYClockwise());
                BlockState otherstate = world.getBlockState(otherpos);
                if (otherstate.getBlock() == this) {
                    world.setBlockState(otherpos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                    world.syncWorldEvent(player, WorldEvents.BLOCK_BROKEN, otherpos, Block.getRawIdFromState(otherstate));
                }
            }
            if (wideBlockModel == WideBlockModelType.SIDE) {
                BlockPos otherpos = pos.offset(state.get(FACING).rotateYCounterclockwise());
                BlockState otherstate = world.getBlockState(otherpos);
                if (otherstate.getBlock() == this) {
                    world.setBlockState(otherpos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                    world.syncWorldEvent(player, WorldEvents.BLOCK_BROKEN, otherpos, Block.getRawIdFromState(otherstate));
                }
            }
        }
        super.onBreak(world, pos, state, player);
    }

    @Nullable
    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(@NotNull BlockState state, @NotNull World world, @NotNull BlockPos pos) {
        return new SimpleNamedScreenHandlerFactory(
            (id, inv, player) -> container.create(id, inv, ScreenHandlerContext.create(world, pos)),
            containerName.get()
        );
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity player, ItemStack stack) {
        super.onPlaced(world, pos, state, player, stack);
        if(!world.isClient) {
            BlockPos possy = pos.offset(state.get(FACING).rotateYClockwise());
            world.setBlockState(possy, state.with(MODEL_TYPE, WideBlockModelType.SIDE), Block.NOTIFY_ALL);
            world.updateNeighbors(pos, Blocks.AIR);
            state.updateNeighbors(world, pos, Block.NOTIFY_ALL);
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
        if(!world.isClient) {
            WideBlockModelType wideBlockModel = state.get(MODEL_TYPE);
            if(wideBlockModel == WideBlockModelType.SIDE) {
                BlockPos adjacentPos = pos.offset(state.get(FACING).rotateYClockwise());
                BlockState adjacentBlockState = world.getBlockState(adjacentPos);
                if (adjacentBlockState.getBlock() == Blocks.DIRT) {
                    BlockPos rootBlockPos = pos.offset(state.get(FACING).rotateYCounterclockwise());
                    BlockState rootBlockState = world.getBlockState(rootBlockPos);
                    if (rootBlockState.get(MODEL_TYPE) == WideBlockModelType.MAIN) {
                        world.setBlockState(rootBlockPos, state.with(MODEL_TYPE, WideBlockModelType.CONNECTED), Block.NOTIFY_ALL);
                    }
                } else if (adjacentBlockState.getBlock() == Blocks.AIR) {
                    BlockPos rootBlockPos = pos.offset(state.get(FACING).rotateYCounterclockwise());
                    BlockState rootBlockState = world.getBlockState(rootBlockPos);
                    if (rootBlockState.get(MODEL_TYPE) == WideBlockModelType.CONNECTED) {
                        world.setBlockState(rootBlockPos, state.with(MODEL_TYPE, WideBlockModelType.MAIN), Block.NOTIFY_ALL);
                    }
                }
            } 
        }
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, @NotNull BlockView view, @NotNull BlockPos pos, @NotNull ShapeContext context) {
        switch (state.get(FACING)) {
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
    public PistonBehavior getPistonBehavior(BlockState state) {
        return PistonBehavior.BLOCK;
    }

    @Override
    public VoxelShape getSidesShape(BlockState state, BlockView view, BlockPos pos) {
        return VoxelShapes.empty();
    }

    @Override
    public boolean hasSidedTransparency(BlockState state) {
        return true;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, MODEL_TYPE);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView view, BlockPos pos) {
        BlockPos otherpos = pos.offset(state.get(FACING).rotateYClockwise());
        return view.getBlockState(otherpos).getMaterial().isReplaceable();
    }

    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView view, BlockPos pos) {
        return 1;
    }
    
    @FunctionalInterface
    public interface ContainerManager {
        ScreenHandler create(int windowId, Inventory inventory, ScreenHandlerContext position);
    }



    //* BLOCK ENTITY *//

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ToolStationBlockEntity) {
                ItemScatterer.spawn(world, pos, (ToolStationBlockEntity)blockEntity);
                world.updateComparators(pos, this);
            }
            //super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public @NotNull ActionResult onUse(@NotNull BlockState state, World world, @NotNull BlockPos pos, @NotNull PlayerEntity player, @NotNull Hand hand, @NotNull BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
            return ActionResult.CONSUME;
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ToolStationBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, ModBlockEntities.TOOL_STATION, ToolStationBlockEntity::tick);
    }
}