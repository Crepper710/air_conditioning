package io.github.crepper710.air_conditioning.block;

import com.simibubi.create.repack.registrate.util.entry.TileEntityEntry;
import io.github.crepper710.air_conditioning.AirConditioning;
import io.github.crepper710.air_conditioning.block.air_conditioning.AirConditioningRenderer;
import io.github.crepper710.air_conditioning.block.air_conditioning.AirConditioningTileEntity;
import io.github.crepper710.air_conditioning.block.air_conditioning.FanInstance;

public class ModTiles {
    public static final TileEntityEntry<AirConditioningTileEntity> AIR_CONDITIONING;

    public static void register() {}

    static {
        AIR_CONDITIONING = AirConditioning.registrate().tileEntity("air_conditioning", AirConditioningTileEntity::new)
                .instance(() -> FanInstance::new)
                .validBlocks(ModBlocks.AIR_CONDITIONING)
                .renderer(() -> AirConditioningRenderer::new)
                .register();
    }
}
