package io.github.crepper710.air_conditioning.block.air_conditioning;

import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;

public class AirConditioningRenderer extends KineticTileEntityRenderer {
    public AirConditioningRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {
        if (!Backend.getInstance().canUseInstancing(te.getLevel())) {
            Direction direction = (Direction)te.getBlockState().getValue(BlockStateProperties.FACING);
            IVertexBuilder vb = buffer.getBuffer(RenderType.cutoutMipped());
            int lightBehind = WorldRenderer.getLightColor(te.getLevel(), te.getBlockPos().relative(direction.getOpposite()));
            int lightInFront = WorldRenderer.getLightColor(te.getLevel(), te.getBlockPos().relative(direction));
            SuperByteBuffer shaftHalf = PartialBufferer.getFacing(AllBlockPartials.SHAFT_HALF, te.getBlockState(), direction.getOpposite());
            SuperByteBuffer fanInner = PartialBufferer.getFacing(AllBlockPartials.ENCASED_FAN_INNER, te.getBlockState(), direction.getOpposite());
            float time = AnimationTickHolder.getRenderTime(te.getLevel());
            float speed = te.getSpeed() * 5.0F;
            if (speed > 0.0F) {
                speed = MathHelper.clamp(speed, 80.0F, 1280.0F);
            }

            if (speed < 0.0F) {
                speed = MathHelper.clamp(speed, -1280.0F, -80.0F);
            }

            float angle = time * speed * 3.0F / 10.0F % 360.0F;
            angle = angle / 180.0F * 3.1415927F;
            standardKineticRotationTransform(shaftHalf, te, lightBehind).renderInto(ms, vb);
            kineticRotationTransform(fanInner, te, direction.getAxis(), angle, lightInFront).renderInto(ms, vb);
        }
    }
}
