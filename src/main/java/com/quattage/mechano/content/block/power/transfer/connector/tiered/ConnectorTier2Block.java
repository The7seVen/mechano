package com.quattage.mechano.content.block.power.transfer.connector.tiered;

import java.util.ArrayList;
import java.util.List;

import com.quattage.mechano.MechanoBlockEntities;
import com.quattage.mechano.MechanoBlocks;
import com.quattage.mechano.MechanoClient;
import com.quattage.mechano.foundation.block.hitbox.Hitbox;
import com.quattage.mechano.foundation.block.orientation.SimpleOrientation;
import com.quattage.mechano.foundation.block.upgradable.BlockUpgradable;
import com.quattage.mechano.foundation.electricity.IBatteryBank;
import com.quattage.mechano.foundation.helper.CreativeTabExcludable;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ConnectorTier2Block extends AbstractConnectorBlock implements IBE<ConnectorTier2BlockEntity>, BlockUpgradable, CreativeTabExcludable {

    protected static Hitbox<SimpleOrientation> hitbox;

    public ConnectorTier2Block(Properties properties) {
        super(properties);
    }

    @Override
    public Class<ConnectorTier2BlockEntity> getBlockEntityClass() {
        return ConnectorTier2BlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ConnectorTier2BlockEntity> getBlockEntityType() {
        return MechanoBlockEntities.CONNECTOR_T2.get();
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return onUpgradeInitiated(world, pos, state, player, hand);
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        return onUpgradeInitiated(context.getLevel(), context.getClickedPos(), state, context.getPlayer(), context.getHand());
    }

    @Override
    VoxelShape getHitbox(BlockState state) {
        if(hitbox == null) hitbox = MechanoClient.HITBOXES.get(ORIENTATION, state.getValue(MODEL_TYPE), this);
        return hitbox.getRotated(state.getValue(ORIENTATION));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState pOldState, boolean pIsMoving) {
        super.onPlace(state, world, pos, pOldState, pIsMoving);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Enum<T> & StringRepresentable> List<EnumProperty<T>> getStatesToPreserve(
            ArrayList<EnumProperty<T>> out) {
        out.add((EnumProperty<T>) MODEL_TYPE);
        out.add((EnumProperty<T>) ORIENTATION);
        return out;
    }

    @Override
    public Builder defineDrops(Builder table, net.minecraft.world.level.storage.loot.LootPool.Builder pool) {
        pool.add(LootItem.lootTableItem(MechanoBlocks.CONNECTOR_T0.get()));
        table.withPool(pool.setRolls(ConstantValue.exactly(3)));
        return table;
    }
}

