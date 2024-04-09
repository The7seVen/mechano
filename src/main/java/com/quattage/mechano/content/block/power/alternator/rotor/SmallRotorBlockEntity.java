package com.quattage.mechano.content.block.power.alternator.rotor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SmallRotorBlockEntity extends AbstractRotorBlockEntity {

    public SmallRotorBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    protected int getStatorCircumference() {
        return 8;
    }

    @Override
    protected int getStatorRadius() {
        return 1;
    }

    
}
