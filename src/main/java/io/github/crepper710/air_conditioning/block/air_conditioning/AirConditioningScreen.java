package io.github.crepper710.air_conditioning.block.air_conditioning;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.gui.AbstractSimiContainerScreen;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.config.ClientSettingsConfig;
import dev.momostudios.coldsweat.util.math.CSMath;
import io.github.crepper710.air_conditioning.AirConditioning;
import io.github.crepper710.air_conditioning.AirConditioningConfig;
import io.github.crepper710.air_conditioning.networking.Packets;
import io.github.crepper710.air_conditioning.networking.packets.ConfigureAirConditioningTile;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class AirConditioningScreen extends AbstractSimiContainerScreen<AirConditioningContainer> {
    private static final ResourceLocation AIR_CONDITIONING_TEXTURES = new ResourceLocation(AirConditioning.MOD_ID, "textures/gui/air_conditioning.png");

    private final boolean celsius = ClientSettingsConfig.getInstance().celsius();
    private final AirConditioningTileEntity te;
    private double targetTemperature;
    private boolean increaseButtonPressed, decreaseButtonPressed;

    public AirConditioningScreen(AirConditioningContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        this.te = container.getTileEntity();
        this.targetTemperature = te.getTargetTemperature();
    }

    @Override
    protected void init() {
        setWindowSize(176, 150);
        super.init();
    }

    @Override
    protected void renderWindow(MatrixStack matrixStack, int i, int i1, float v) {
        if (this.minecraft != null) {
            this.minecraft.getTextureManager().bind(AIR_CONDITIONING_TEXTURES);
            matrixStack.pushPose();
            matrixStack.translate(leftPos, topPos, 0);
            this.blit(matrixStack, 0, 0, 0, 0, 176, 150);
            try {
                int cooling = (int) ((te.getCoolingFuel() / ((double) AirConditioningConfig.getMaxCoolingFuel())) * 36);
                this.blit(matrixStack, 47, 17 + (36 - cooling), 176, 0, 8, cooling);
            } catch (ArithmeticException ignored) {}
            try {
                int heating = (int) ((te.getHeatingFuel() / ((double) AirConditioningConfig.getMaxHeatingFuel())) * 36);
                this.blit(matrixStack, 121, 17 + (36 - heating), 184, 0, 8, heating);
            } catch (ArithmeticException ignored) {}
            int x = i - leftPos;
            int y = i1 - topPos;
            {
                int off = 0;
                if (increaseButtonPressed){
                    off += 10;
                } else if (x >= 109 && x <= 116 && y >= 17 && y <= 21) {
                    off += 5;
                }
                this.blit(matrixStack, 109, 17, 192, off, 8, 5);
            }
            {
                int off = 0;
                if (decreaseButtonPressed){
                    off += 10;
                } else if (x >= 109 && x <= 116 && y >= 22 && y <= 26) {
                    off += 5;
                }
                this.blit(matrixStack, 109, 22, 192, off + 15, 8, 5);
            }
            drawTextMaxWidth(matrixStack, String.format("%.2f \u00B0%c", CSMath.convertUnits(targetTemperature, Temperature.Units.MC, celsius ? Temperature.Units.C : Temperature.Units.F, true), celsius ? 'C' : 'F'), 60, 18, 48, -1);
            drawTextMaxWidth(matrixStack, "Max change:", 60, 28, 56, -1);
            drawTextMaxWidth(matrixStack, String.format("+%.2f", CSMath.convertUnits(te.getCurrentMaxHeatingAmount(), Temperature.Units.MC, celsius ? Temperature.Units.C : Temperature.Units.F, true)), 60, 38, 27, 0xFFFF8000);
            drawTextMaxWidth(matrixStack, String.format("+%.2f", CSMath.convertUnits(te.getCurrentMaxCoolingAmount(), Temperature.Units.MC, celsius ? Temperature.Units.C : Temperature.Units.F, true)), 89, 38, 27, 0xFF00FFFF);
//            this.font.draw(matrixStack, String.format("%.2f \u00B0%c", CSMath.convertUnits(targetTemperature, Temperature.Units.MC, celsius ? Temperature.Units.C : Temperature.Units.F, true), celsius ? 'C' : 'F'), 59, 17, -1);
            this.font.draw(matrixStack, this.title, 8, 6, 4210752);
            this.font.draw(matrixStack, this.inventory.getDisplayName(), 8, 57, 4210752);
            matrixStack.popPose();
        }
    }

    protected void drawTextMaxWidth(MatrixStack matrixStack, String text, int x, int y, int maxWidth, int color) {
        if (this.font.width(text) <= maxWidth) {
            this.font.draw(matrixStack, text, x, y, color);
        } else {
            matrixStack.pushPose();
            float scale = maxWidth;
            scale /= this.font.width(text);
            matrixStack.translate(x, y, 0);
            matrixStack.scale(scale, scale, 1);
            this.font.draw(matrixStack, text, 0, 0, color);
            matrixStack.popPose();
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        double x_ = x - leftPos;
        double y_ = y - topPos;
        if (x_ >= 109 && x_ <= 116 && y_ >= 17 && y_ <= 21) {
            changeTemperature(1);
            increaseButtonPressed = true;
            return true;
        }
        if (x_ >= 109 && x_ <= 116 && y_ >= 22 && y_ <= 26) {
            changeTemperature(-1);
            decreaseButtonPressed = true;
            return true;
        }
        return super.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        increaseButtonPressed = decreaseButtonPressed = false;
        return super.mouseReleased(x, y, button);
    }

    private void changeTemperature(double temperatureOffset) {
        double targetTemperature = this.targetTemperature;
        targetTemperature = CSMath.convertUnits(targetTemperature, Temperature.Units.MC, celsius ? Temperature.Units.C : Temperature.Units.F, true);
        targetTemperature = Math.round(targetTemperature);
        targetTemperature += temperatureOffset;
        targetTemperature = CSMath.convertUnits(targetTemperature, celsius ? Temperature.Units.C : Temperature.Units.F, Temperature.Units.MC, true);
        setTargetTemperature(targetTemperature);
    }

    private int lastModification = -1;

    private void setTargetTemperature(double targetTemperature) {
        lastModification = 0;
        this.targetTemperature = targetTemperature;
    }

    @Override
    public void tick() {
        if (this.lastModification >= 0) {
            ++this.lastModification;
        }

        if (this.lastModification >= 15) {
            this.lastModification = -1;
            Packets.channel.sendToServer(new ConfigureAirConditioningTile(this.te.getBlockPos(), this.targetTemperature));
        }
        super.tick();
    }

    @Override
    public void removed() {
        Packets.channel.sendToServer(new ConfigureAirConditioningTile(this.te.getBlockPos(), this.targetTemperature));
        super.removed();
    }
}
