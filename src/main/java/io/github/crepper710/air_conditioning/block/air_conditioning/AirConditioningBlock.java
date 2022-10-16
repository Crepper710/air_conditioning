package io.github.crepper710.air_conditioning.block.air_conditioning;

import com.simibubi.create.content.contraptions.base.DirectionalKineticBlock;
import com.simibubi.create.content.logistics.block.chute.AbstractChuteBlock;
import com.simibubi.create.foundation.block.BlockStressDefaults;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.data.BlockStateGen;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.repack.registrate.util.entry.BlockEntry;
import io.github.crepper710.air_conditioning.AirConditioning;
import io.github.crepper710.air_conditioning.block.ModTiles;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class AirConditioningBlock extends DirectionalKineticBlock implements ITE<AirConditioningTileEntity> {
    public AirConditioningBlock(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType use(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockRayTraceResult blockRayTraceResult) {
        if (world.isClientSide)
            return ActionResultType.SUCCESS;

        withTileEntityDo(world, blockPos, te -> NetworkHooks.openGui((ServerPlayerEntity) player, te, te::sendToContainer));
        return ActionResultType.SUCCESS;
    }

    protected void openContainer(World p_220089_1_, BlockPos p_220089_2_, PlayerEntity p_220089_3_) {
        TileEntity tileentity = p_220089_1_.getBlockEntity(p_220089_2_);
        if (tileentity instanceof AirConditioningTileEntity) {
            p_220089_3_.openMenu((INamedContainerProvider)tileentity);
        }

    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return ModTiles.AIR_CONDITIONING.create();
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState p_196243_4_, boolean p_196243_5_) {
        if (state.hasTileEntity() && (state.getBlock() != p_196243_4_.getBlock() || !p_196243_4_.hasTileEntity())) {
            withTileEntityDo(world, pos, AirConditioningTileEntity::updateChute);
            world.removeBlockEntity(pos);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();

        BlockState placedOn = world.getBlockState(pos.relative(face.getOpposite()));
        BlockState placedOnOpposite = world.getBlockState(pos.relative(face));
        if (AbstractChuteBlock.isChute(placedOn))
            return defaultBlockState().setValue(FACING, face.getOpposite());
        if (AbstractChuteBlock.isChute(placedOnOpposite))
            return defaultBlockState().setValue(FACING, face);

        Direction preferredFacing = getPreferredFacing(context);
        if (preferredFacing == null)
            preferredFacing = context.getNearestLookingDirection();
        return defaultBlockState().setValue(FACING, context.getPlayer() != null && context.getPlayer()
                .isShiftKeyDown() ? preferredFacing : preferredFacing.getOpposite());
    }

    @Override
    public BlockState updateAfterWrenched(BlockState newState, ItemUseContext context) {
        return newState;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING)
                .getAxis();
    }

    @Override
    public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(FACING)
                .getOpposite();
    }

    @Override
    public boolean showCapacityWithAnnotation() {
        return true;
    }

    @Override
    public Class<AirConditioningTileEntity> getTileEntityClass() {
        return AirConditioningTileEntity.class;
    }

}
