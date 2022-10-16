package io.github.crepper710.air_conditioning.networking.packets;

import com.simibubi.create.foundation.networking.TileEntityConfigurationPacket;
import io.github.crepper710.air_conditioning.block.air_conditioning.AirConditioningTileEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ConfigureAirConditioningTile extends TileEntityConfigurationPacket<AirConditioningTileEntity> {
    private double targetTemperature;

    public ConfigureAirConditioningTile(PacketBuffer buffer) {
        super(buffer);
    }

    public ConfigureAirConditioningTile(BlockPos pos, double targetTemperature) {
        super(pos);
        this.targetTemperature = targetTemperature;
    }

    @Override
    protected void writeSettings(PacketBuffer packetBuffer) {
        packetBuffer.writeDouble(this.targetTemperature);
    }

    @Override
    protected void readSettings(PacketBuffer packetBuffer) {
        this.targetTemperature = packetBuffer.readDouble();
    }

    @Override
    protected void applySettings(AirConditioningTileEntity airConditioningTileEntity) {
        airConditioningTileEntity.setTargetTemperature(this.targetTemperature);
    }
}
