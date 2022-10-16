package io.github.crepper710.air_conditioning.networking;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import io.github.crepper710.air_conditioning.AirConditioning;
import io.github.crepper710.air_conditioning.networking.packets.ConfigureAirConditioningTile;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public enum Packets {
    CONFIGURE_AIR_CONDITIONING_TILE(ConfigureAirConditioningTile.class, ConfigureAirConditioningTile::new, NetworkDirection.PLAY_TO_SERVER);

    public static final ResourceLocation CHANNEL_NAME = new ResourceLocation(AirConditioning.MOD_ID, "network");
    public static final String NETWORK_VERSION = new ResourceLocation(AirConditioning.MOD_ID, "1").toString();
    public static SimpleChannel channel;
    private final Packets.LoadedPacket<?> packet;

    <T extends SimplePacketBase> Packets(Class<T> type, Function<PacketBuffer, T> factory, NetworkDirection direction) {
        this.packet = new Packets.LoadedPacket<>(type, factory, direction);
    }

    public static void registerPackets() {
        NetworkRegistry.ChannelBuilder channelBuilder = NetworkRegistry.ChannelBuilder.named(CHANNEL_NAME);
        channelBuilder = channelBuilder.serverAcceptedVersions(NETWORK_VERSION::equals);
        channel = channelBuilder.clientAcceptedVersions(NETWORK_VERSION::equals).networkProtocolVersion(() -> NETWORK_VERSION).simpleChannel();
        Packets[] packets = values();
        int var1 = packets.length;
        for (Packets packet : packets) {
            packet.packet.register();
        }

    }

    private static class LoadedPacket<T extends SimplePacketBase> {
        private static int index = 0;
        BiConsumer<T, PacketBuffer> encoder;
        Function<PacketBuffer, T> decoder;
        BiConsumer<T, Supplier<NetworkEvent.Context>> handler;
        Class<T> type;
        NetworkDirection direction;

        private LoadedPacket(Class<T> type, Function<PacketBuffer, T> factory, NetworkDirection direction) {
            this.encoder = SimplePacketBase::write;
            this.decoder = factory;
            this.handler = SimplePacketBase::handle;
            this.type = type;
            this.direction = direction;
        }

        private void register() {
            Packets.channel.messageBuilder(this.type, index++, this.direction).encoder(this.encoder).decoder(this.decoder).consumer(this.handler).add();
        }
    }
}
