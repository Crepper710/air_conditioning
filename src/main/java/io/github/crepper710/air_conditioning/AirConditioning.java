package io.github.crepper710.air_conditioning;

import com.simibubi.create.content.AllSections;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.repack.registrate.util.NonNullLazyValue;
import com.simibubi.create.repack.registrate.util.nullness.NonnullType;
import dev.momostudios.coldsweat.api.event.core.TempModifierRegisterEvent;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.api.util.TempHelper;
import io.github.crepper710.air_conditioning.block.ModBlocks;
import io.github.crepper710.air_conditioning.block.ModTiles;
import io.github.crepper710.air_conditioning.block.air_conditioning.AirConditioningBlock;
import io.github.crepper710.air_conditioning.block.air_conditioning.AirConditioningContainer;
import io.github.crepper710.air_conditioning.block.air_conditioning.AirConditioningTempModifier;
import io.github.crepper710.air_conditioning.block.air_conditioning.AirConditioningTileEntity;
import io.github.crepper710.air_conditioning.containers.ModContainers;
import io.github.crepper710.air_conditioning.networking.Packets;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

@Mod(AirConditioning.MOD_ID)
public class AirConditioning {
    public static final String MOD_ID = "c_cs_air_conditioning"; // c = create; cs = cold sweat
    private static final NonNullLazyValue<CreateRegistrate> registrate = CreateRegistrate.lazy(MOD_ID);

    public AirConditioning() {
        ModBlocks.register();
        ModTiles.register();
        ModContainers.register();
        AirConditioningConfig.setup();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(AirConditioning::init);
        MinecraftForge.EVENT_BUS.addListener(AirConditioning::registerTempModifier);
        MinecraftForge.EVENT_BUS.addListener(AirConditioning::serverTick);
    }

    public static @NonnullType CreateRegistrate registrate() {
        return registrate.get();
    }

    private static void init(FMLCommonSetupEvent event) {
        Packets.registerPackets();
    }

    private static void registerTempModifier(TempModifierRegisterEvent event) {
        event.register(AirConditioningTempModifier.INSTANCE);
    }

    private static void serverTick(TickEvent.ServerTickEvent event) {
        ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().forEach((player) -> TempHelper.replaceModifier(player, AirConditioningTempModifier.INSTANCE, Temperature.Type.WORLD));
    }
}
