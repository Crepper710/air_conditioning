package io.github.crepper710.air_conditioning.block;

import com.simibubi.create.content.AllSections;
import com.simibubi.create.foundation.block.BlockStressDefaults;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.ModelGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.repack.registrate.util.entry.BlockEntry;
import io.github.crepper710.air_conditioning.AirConditioning;
import io.github.crepper710.air_conditioning.block.air_conditioning.AirConditioningBlock;
import io.github.crepper710.air_conditioning.item.ModItemGroups;
import net.minecraft.client.renderer.RenderType;

public class ModBlocks {
    private static final CreateRegistrate REGISTRATE = AirConditioning.registrate().itemGroup(() -> ModItemGroups.DEFAULT);

    public static final BlockEntry<AirConditioningBlock> AIR_CONDITIONING;

    public static void register() {}

    static {
        REGISTRATE.startSection(AllSections.KINETICS);
        AIR_CONDITIONING = REGISTRATE.block("air_conditioning", AirConditioningBlock::new)
                .initialProperties(SharedProperties::stone)
                .blockstate(BlockStateGen.directionalBlockProvider(true))
                .addLayer(() -> RenderType::cutoutMipped)
                .transform(BlockStressDefaults.setImpact(1.0))
                .item()
                .transform(ModelGen.customItemModel())
                .register();
    }
}
