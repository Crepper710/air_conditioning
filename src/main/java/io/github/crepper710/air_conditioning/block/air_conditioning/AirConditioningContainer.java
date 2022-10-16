package io.github.crepper710.air_conditioning.block.air_conditioning;

import com.simibubi.create.repack.registrate.util.entry.ContainerEntry;
import io.github.crepper710.air_conditioning.AirConditioning;
import io.github.crepper710.air_conditioning.block.ModBlocks;
import io.github.crepper710.air_conditioning.containers.ModContainers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class AirConditioningContainer extends Container {
    private final AirConditioningTileEntity tileEntity;
    private final PlayerEntity playerEntity;
    private final IItemHandler playerInventory;


    public AirConditioningContainer(ContainerType<?> type, int id, PlayerInventory inv, PacketBuffer extraData) {
        super(type, id);
        playerEntity = inv.player;
        this.playerInventory = new InvWrapper(inv);
        ClientWorld world = Minecraft.getInstance().level;
        TileEntity tileEntity = world.getBlockEntity(extraData.readBlockPos());
        if (tileEntity instanceof AirConditioningTileEntity) {
            this.tileEntity = (AirConditioningTileEntity) tileEntity;
            this.tileEntity.handleUpdateTag(tileEntity.getBlockState(), extraData.readNbt());
            init();
        } else {
            this.tileEntity = null;
        }
    }

    public AirConditioningContainer(ContainerType<?> type, int id, PlayerInventory inv, AirConditioningTileEntity te) {
        super(type, id);
        this.playerEntity = inv.player;
        this.playerInventory = new InvWrapper(inv);
        this.tileEntity = te;
        init();
    }

    public static AirConditioningContainer create(int id, PlayerInventory inv, AirConditioningTileEntity te) {
        return new AirConditioningContainer(ModContainers.AIR_CONDITIONING.get(), id, inv, te);
    }

    protected void init() {
        if (tileEntity != null) {
            tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
                addSlot(new SlotItemHandler(h, 0, 8, 18));
                addSlot(new SlotItemHandler(h, 1, 26, 18));
                addSlot(new SlotItemHandler(h, 2, 8, 36));
                addSlot(new SlotItemHandler(h, 3, 26, 36));

                addSlot(new SlotItemHandler(h, 4, 134, 18));
                addSlot(new SlotItemHandler(h, 5, 152, 18));
                addSlot(new SlotItemHandler(h, 6, 134, 36));
                addSlot(new SlotItemHandler(h, 7, 152, 36));
            });
        }

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerEntity.inventory, col + row * 9 + 9, 8 + col * 18, 68 + row * 18));
            }
        }

        for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot) {
            this.addSlot(new Slot(playerEntity.inventory, hotbarSlot, 8 + hotbarSlot * 18, 126));
        }
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return stillValid(IWorldPosCallable.create(tileEntity.getLevel(), tileEntity.getBlockPos()), player, ModBlocks.AIR_CONDITIONING.get());
    }

    public AirConditioningTileEntity getTileEntity() {
        return tileEntity;
    }
}
