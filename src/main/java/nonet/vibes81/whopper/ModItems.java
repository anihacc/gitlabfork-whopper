package nonet.vibes81.whopper;

import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import nonet.vibes81.whopper.blocks.BlockWhopper;

public class ModItems {

    @SideOnly(Side.CLIENT)
    public static void initModels() {
    }

    public static void register(IForgeRegistry<Item> registry) {

        //Block items
       registry.register(new ItemBlock(ModBlocks.blockWhopper).setRegistryName(BlockWhopper.BLOCK_WHOPPER));
    }
}