package com.quattage.mechano.content.block.Alternator.Rotor;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class RotorBlockEntity extends KineticBlockEntity {

    public RotorBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
	protected AABB createRenderBoundingBox() {
		return new AABB(worldPosition).inflate(1);
	}
}
