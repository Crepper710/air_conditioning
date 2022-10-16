package io.github.crepper710.air_conditioning.item;

import io.github.crepper710.air_conditioning.AirConditioning;
import io.github.crepper710.air_conditioning.block.ModBlocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class ModItemGroups {
    public static final ItemGroup DEFAULT = new ItemGroup(ItemGroup.getGroupCountSafe(), AirConditioning.MOD_ID + ".default") {
        @Override
        public ItemStack makeIcon() {
            return ModBlocks.AIR_CONDITIONING.asStack();
        }
    };
}
