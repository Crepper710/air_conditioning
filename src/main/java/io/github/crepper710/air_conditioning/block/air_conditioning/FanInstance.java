package io.github.crepper710.air_conditioning.block.air_conditioning;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileInstance;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.foundation.render.AllMaterialSpecs;

import io.github.crepper710.air_conditioning.block.air_conditioning.AirConditioningTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

//copy from com.simibubi.create.content.contraptions.components.fan.FanInstance

public class FanInstance extends KineticTileInstance<AirConditioningTileEntity> {

    protected final RotatingData shaft;
    protected final RotatingData fan;
    final Direction direction;
    private final Direction opposite;

    public FanInstance(MaterialManager<?> modelManager, AirConditioningTileEntity tile) {
        super(modelManager, tile);

        direction = blockState.getValue(FACING);

        opposite = direction.getOpposite();
        shaft = getRotatingMaterial().getModel(AllBlockPartials.SHAFT_HALF, blockState, opposite).createInstance();
        fan = modelManager.defaultCutout()
                .material(AllMaterialSpecs.ROTATING)
                .getModel(AllBlockPartials.ENCASED_FAN_INNER, blockState, opposite)
                .createInstance();

        setup(shaft);
        setup(fan, getFanSpeed());
    }

    private float getFanSpeed() {
        float speed = tile.getSpeed() * 5;
        if (speed > 0)
            speed = MathHelper.clamp(speed, 80, 64 * 20);
        if (speed < 0)
            speed = MathHelper.clamp(speed, -64 * 20, -80);
        return speed;
    }

    @Override
    public void update() {
        updateRotation(shaft);
        updateRotation(fan, getFanSpeed());
    }

    @Override
    public void updateLight() {
        BlockPos behind = pos.relative(opposite);
        relight(behind, shaft);

        BlockPos inFront = pos.relative(direction);
        relight(inFront, fan);
    }

    @Override
    public void remove() {
        shaft.delete();
        fan.delete();
    }
}
