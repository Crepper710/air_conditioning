package io.github.crepper710.air_conditioning.block.air_conditioning;

import com.google.common.collect.Lists;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import io.github.crepper710.air_conditioning.AirConditioning;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class AirConditioningTempModifier extends TempModifier {
    public static final AirConditioningTempModifier INSTANCE = new AirConditioningTempModifier();

    private AirConditioningTempModifier() {

    }

    @Override
    protected Function<Temperature, Temperature> calculate(PlayerEntity playerEntity) {
        AxisAlignedBB aabb = new AxisAlignedBB(-7, -7, -7, 7, 7, 7).move(-0.5, -0.5, -0.5).move(playerEntity.position());
        int minX = MathHelper.floor((aabb.minX) / 16.0D);
        int maxX = MathHelper.ceil((aabb.maxX) / 16.0D);
        int minZ = MathHelper.floor((aabb.minZ) / 16.0D);
        int maxZ = MathHelper.ceil((aabb.maxZ) / 16.0D);
        List<AirConditioningTileEntity> list = Lists.newArrayList();
        AbstractChunkProvider abstractchunkprovider = playerEntity.level.getChunkSource();

        for(int x = minX; x < maxX; ++x) {
            for(int z = minZ; z < maxZ; ++z) {
                Chunk chunk = abstractchunkprovider.getChunk(x, z, false);
                if (chunk != null) {
                    chunk.getBlockEntities().forEach((pos, te) -> {
                        if (aabb.contains(pos.getX(), pos.getY(), pos.getZ()) && te instanceof AirConditioningTileEntity) {
                            AirConditioningTileEntity airConditioningTileEntity = (AirConditioningTileEntity) te;
                            if (airConditioningTileEntity.isActive()) {
                                list.add((AirConditioningTileEntity) te);
                            }
                        }
                    });
                }
            }
        }

        AirConditioningTileEntity te = list
                .stream()
                .min(Comparator.comparingDouble(o -> playerEntity.position().subtract(0.5, 0.5, 0.5).distanceToSqr(o.getBlockPos().getX(), o.getBlockPos().getY(), o.getBlockPos().getZ())))
                .orElse(null);

        return temperature -> {
            if (te != null) {
                if (te.getTargetTemperature() > temperature.get()) {
                    if (te.canHeat()) {
                        temperature.set(Math.min(te.getTargetTemperature(), temperature.get() + te.getCurrentMaxHeatingAmount()));
                    }
                } else {
                    if (te.canCool()) {
                        temperature.set(Math.max(te.getTargetTemperature(), temperature.get() - te.getCurrentMaxCoolingAmount()));
                    }
                }
            }
            return temperature;
        };
    }

    @Override
    public String getID() {
        return AirConditioning.MOD_ID + ":air_conditioning";
    }
}
