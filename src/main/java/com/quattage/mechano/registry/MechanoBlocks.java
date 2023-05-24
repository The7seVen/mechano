package com.quattage.mechano.registry;

import com.quattage.mechano.Mechano;
import com.quattage.mechano.content.block.Alternator.Rotor.RotorBlock;
import com.quattage.mechano.content.block.Inductor.InductorBlock;
import com.quattage.mechano.content.block.ToolStation.ToolStationBlock;
import com.quattage.mechano.content.block.Upgrade.UpgradeBlock;
import com.simibubi.create.content.kinetics.BlockStressDefaults;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.eventbus.api.IEventBus;


//This is where blocks go to get registered
public class MechanoBlocks {
    public static CreateRegistrate REGISTRATE = Mechano.REGISTRATE.creativeModeTab(() -> MechanoGroup.PRIMARY);

    public static final BlockEntry<UpgradeBlock> FORGE_UPGRADE = REGISTRATE.block("tool_forge", UpgradeBlock::new)
        .initialProperties(Material.METAL)
        .properties(props -> props
            .sound(SoundType.NETHERITE_BLOCK)
            .noOcclusion()
        )
        .item()
        .transform(customItemModel())
        .register();

    public static final BlockEntry<InductorBlock> INDUCTOR = REGISTRATE.block("inductor", InductorBlock::new)
        .initialProperties(Material.METAL)
        .properties(props -> props
            .sound(SoundType.NETHERITE_BLOCK)
        )
        .item()
        .transform(customItemModel())
        .register();

    public static final BlockEntry<ToolStationBlock> TOOL_STATION = REGISTRATE.block("tool_station", ToolStationBlock::new)
        .initialProperties(Material.WOOD)
        .properties(props -> props
            .sound(SoundType.WOOD)
            .noOcclusion()
        )
        .item()
        .transform(customItemModel())
        .register();

    public static final BlockEntry<RotorBlock> ROTOR = REGISTRATE.block("rotor", RotorBlock::new)
        .initialProperties(Material.METAL)
        .properties(props -> props
            .sound(SoundType.NETHERITE_BLOCK)
            .color(MaterialColor.COLOR_ORANGE)
            .noOcclusion()
        )
        .transform(BlockStressDefaults.setImpact(12.0))
        .item()
        .transform(customItemModel())
        .register();

    public static void register(IEventBus event) {
        Mechano.logReg("blocks");

    }
}
