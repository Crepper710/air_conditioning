package io.github.crepper710.air_conditioning.block.air_conditioning;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.logistics.block.chute.ChuteTileEntity;
import com.simibubi.create.repack.registrate.util.entry.TileEntityEntry;
import io.github.crepper710.air_conditioning.AirConditioning;
import io.github.crepper710.air_conditioning.AirConditioningConfig;
import io.github.crepper710.air_conditioning.block.ModBlocks;
import io.github.crepper710.air_conditioning.utils.MathUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AirConditioningTileEntity extends KineticTileEntity implements INamedContainerProvider, ITickableTileEntity {
    private final ItemStackHandler itemHandler = createHandler();
    private final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);

    private int coolingFuel = 0;
    private int heatingFuel = 0;
    private double targetTemperature = 0;

    public AirConditioningTileEntity(TileEntityType<? extends AirConditioningTileEntity> type) {
        super(type);
    }

    @Override
    protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
        itemHandler.deserializeNBT(compound.getCompound("inv"));
        this.coolingFuel = compound.getInt("coolingFuel");
        this.heatingFuel = compound.getInt("heatingFuel");
        this.targetTemperature = compound.getDouble("targetTemperature");
        super.fromTag(state, compound, clientPacket);
    }

    @Override
    protected void write(CompoundNBT compound, boolean clientPacket) {
        compound.put("inv", itemHandler.serializeNBT());
        compound.putInt("coolingFuel", coolingFuel);
        compound.putInt("heatingFuel", heatingFuel);
        compound.putDouble("targetTemperature", targetTemperature);
        super.write(compound, clientPacket);
    }

    private ItemStackHandler createHandler() {
        return new ItemStackHandler(8) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                switch (slot) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        return AirConditioningConfig.getAcceptedCoolingFuels().containsKey(stack.getItem().getRegistryName().toString());
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        return AirConditioningConfig.getAcceptedHeatingFuels().containsKey(stack.getItem().getRegistryName().toString());
                    default:
                        return false;
                }
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return handler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onSpeedChanged(float prevSpeed) {
        super.onSpeedChanged(prevSpeed);
        updateChute();
    }

    public void updateChute() {
        Direction direction = getBlockState().getValue(AirConditioningBlock.FACING);
        if (!direction.getAxis()
                .isVertical())
            return;
        TileEntity poweredChute = level.getBlockEntity(worldPosition.relative(direction));
        if (!(poweredChute instanceof ChuteTileEntity))
            return;
        ChuteTileEntity chuteTE = (ChuteTileEntity) poweredChute;
        if (direction == Direction.DOWN)
            chuteTE.updatePull();
        else
            chuteTE.updatePush(1);
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("c_cs_air_conditioning.gui.title.air_conditioning");
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
        return AirConditioningContainer.create(id, inv, this);
    }

    @Override
    public void tick() {
        if (this.getSpeed() != 0) {
            if (coolingFuel >= AirConditioningConfig.getCoolingFuelPerTick()) {
                coolingFuel -= AirConditioningConfig.getCoolingFuelPerTick();
            }
            if (heatingFuel >= AirConditioningConfig.getHeatingFuelPerTick()) {
                heatingFuel -= AirConditioningConfig.getHeatingFuelPerTick();
            }
        }
        tryToFuel();
        super.tick();
    }

    public double getCurrentMaxCoolingAmount() {
        return MathUtils.binaryDigits(Math.round(this.getSpeed())) * AirConditioningConfig.getCoolingAmountPerRpmDigit();
    }

    public boolean canCool() {
        return coolingFuel >= AirConditioningConfig.getCoolingFuelPerTick();
    }

    public double getCurrentMaxHeatingAmount() {
        return MathUtils.binaryDigits(Math.round(this.getSpeed())) * AirConditioningConfig.getHeatingAmountPerRpmDigit();
    }

    public boolean canHeat() {
        return heatingFuel >= AirConditioningConfig.getHeatingFuelPerTick();
    }

    public boolean isActive() {
        return this.getSpeed() != 0;
    }

    private void tryToFuel() {
        if (coolingFuel != AirConditioningConfig.getMaxCoolingFuel()) {
            tryToFuelCooling(0);
            tryToFuelCooling(1);
            tryToFuelCooling(2);
            tryToFuelCooling(3);
        }

        if (heatingFuel != AirConditioningConfig.getMaxHeatingFuel()) {
            tryToFuelHeating(4);
            tryToFuelHeating(5);
            tryToFuelHeating(6);
            tryToFuelHeating(7);
        }
    }

    private void tryToFuelCooling(int slot) {
        ItemStack stack = this.itemHandler.getStackInSlot(slot);
        String name = stack.getItem().getRegistryName().toString();
        if (AirConditioningConfig.getAcceptedCoolingFuels().containsKey(name) && !stack.isEmpty()) {
            int amount = AirConditioningConfig.getAcceptedCoolingFuels().getOrDefault(name, 0);
            while (stack.getCount() != 0 && coolingFuel + amount <= AirConditioningConfig.getMaxCoolingFuel()) {
                stack.shrink(1);
                coolingFuel += amount;
            }
        }
    }

    private void tryToFuelHeating(int slot) {
        ItemStack stack = this.itemHandler.getStackInSlot(slot);
        String name = stack.getItem().getRegistryName().toString();
        if (AirConditioningConfig.getAcceptedHeatingFuels().containsKey(name) && !stack.isEmpty()) {
            int amount = AirConditioningConfig.getAcceptedHeatingFuels().getOrDefault(name, 0);
            while ((!stack.isEmpty()) && heatingFuel + amount <= AirConditioningConfig.getMaxHeatingFuel()) {
                stack.shrink(1);
                heatingFuel += amount;
            }
        }
    }

    public int getCoolingFuel() {
        return coolingFuel;
    }

    public int getHeatingFuel() {
        return heatingFuel;
    }

    public double getTargetTemperature() {
        return targetTemperature;
    }

    public void setTargetTemperature(double targetTemperature) {
        this.targetTemperature = targetTemperature;
        this.networkDirty = true;
    }
}
