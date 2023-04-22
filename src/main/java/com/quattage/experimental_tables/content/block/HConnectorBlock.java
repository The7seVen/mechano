package com.quattage.experimental_tables.content.block;

import com.mrh0.createaddition.energy.IWireNode;
import com.mrh0.createaddition.shapes.CAShapes;
import com.quattage.experimental_tables.ExperimentalTables;
import com.quattage.experimental_tables.content.block.entity.HConnectorBlockEntity;
import com.quattage.experimental_tables.registry.ModBlockEntities;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.VoxelShaper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.state.StateManager.Builder;

public class HConnectorBlock extends Block implements ITE<HConnectorBlockEntity>, IWrenchable {
    public static final VoxelShaper CONNECTOR_SHAPE = CAShapes.shape(4, 0, 4, 12, 11, 12).forDirectional();
	public static final DirectionProperty FACING = Properties.FACING;
	private static final VoxelShape boxwe = Block.createCuboidShape(0,7,7,10,9,9);
	private static final VoxelShape boxsn = Block.createCuboidShape(7,7,0,9,9,10);
    
	public HConnectorBlock(Settings properties) {
		super(properties);
		this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH));
	}

	@Override
    public VoxelShape getOutlineShape(BlockState state, net.minecraft.world.BlockView world, BlockPos pos, ShapeContext context) {
        return CONNECTOR_SHAPE.get(state.get(FACING).getOpposite());
    }
	
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return ModBlockEntities.HIGH_VOLTAGE_CONNECTOR.create(pos, state);
	}

	@Override
	public Class<HConnectorBlockEntity> getTileEntityClass() {
		return HConnectorBlockEntity.class;
	}
	
	@Override
	protected void appendProperties(Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
	
	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		return this.getDefaultState().with(FACING, context.getSide().getOpposite());
	}

    @Override
	public void onBreak(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
		super.onBreak(worldIn, pos, state, player);
		if(player.isCreative())
			return;
		BlockEntity te = worldIn.getBlockEntity(pos);
		if(te == null)
			return;
		if(!(te instanceof IWireNode))
			return;
		IWireNode cte = (IWireNode) te;
		
		cte.dropWires(worldIn);
	}

	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		super.onStateReplaced(state, world, pos, newState, moved);
		BlockEntity te = world.getBlockEntity(pos);
		if(te == null)
			return;
		if(!(te instanceof IWireNode))
			return;
		IWireNode cte = (IWireNode) te;
		
		cte.dropWires(world);
	}
	
    @Override
	public ActionResult onSneakWrenched(BlockState state, ItemUsageContext c) {
		if(c.getPlayer().isCreative()) {
			ExperimentalTables.LOGGER.info("CREATIVE");
			return IWrenchable.super.onSneakWrenched(state, c);
		}
		BlockEntity te = c.getWorld().getBlockEntity(c.getBlockPos());
		if(te == null) {
			ExperimentalTables.LOGGER.info("BLOCKENTITY IS NULL");
			return IWrenchable.super.onSneakWrenched(state, c);
		}
		if(!(te instanceof IWireNode)) {
			ExperimentalTables.LOGGER.info("BLOCKENTITY IS NOT WIRENODE");
			return IWrenchable.super.onSneakWrenched(state, c);
		}
		IWireNode cte = (IWireNode) te;
		
		ExperimentalTables.LOGGER.info("WIRES DROPPED");
		cte.dropWires(c.getWorld(), c.getPlayer());
		return IWrenchable.super.onSneakWrenched(state, c);
	}

    @Override
	public void neighborUpdate(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		BlockEntity tileentity = state.hasBlockEntity() ? worldIn.getBlockEntity(pos) : null;
		if(tileentity != null) {
			if(tileentity instanceof HConnectorBlockEntity) {
				((HConnectorBlockEntity)tileentity).updateCache();
			}
		}
		if (!state.canPlaceAt(worldIn, pos)) {
			dropStacks(state, worldIn, pos, tileentity);
			
			if(tileentity instanceof IWireNode)
				((IWireNode) tileentity).dropWires(worldIn);
			
			worldIn.removeBlock(pos, false);

			for (Direction direction : Direction.values())
				worldIn.updateNeighborsAlways(pos.offset(direction), this);
		}
	}

    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		Direction dir = state.get(FACING);
		return
				!VoxelShapes.matchesAnywhere(world.getBlockState(pos.offset(dir)).getSidesShape(world,pos.offset(dir)).getFace(dir.getOpposite()), boxwe, BooleanBiFunction.ONLY_SECOND) ||
				!VoxelShapes.matchesAnywhere(world.getBlockState(pos.offset(dir)).getSidesShape(world,pos.offset(dir)).getFace(dir.getOpposite()), boxsn, BooleanBiFunction.ONLY_SECOND) ||
				world.getBlockState(pos.offset(dir)).isSideSolidFullSquare(world, pos, dir.getOpposite());
	}

    @Override
	public BlockEntityType<? extends HConnectorBlockEntity> getTileEntityType() {
		return ModBlockEntities.HIGH_VOLTAGE_CONNECTOR.get();
	}
	
	@Override
	public BlockState rotate(BlockState state, BlockRotation direction) {
		return state.with(FACING, direction.rotate(state.get(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror) {
		return state.with(FACING, mirror.apply(state.get(FACING)));
	}
}
