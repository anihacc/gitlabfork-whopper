package nonet.vibes81.whopper;

import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import nonet.vibes81.whopper.blocks.BlockWhopper;
import nonet.vibes81.whopper.blocks.TileWhopper;
import nonet.vibes81.whopper.util.Ref;

public class ModBlocks {

    @GameRegistry.ObjectHolder("whopper:whopper")
    public static BlockWhopper blockWhopper;

    @SideOnly(Side.CLIENT)
    public static void initModels() {
        blockWhopper.initModel();

    }

    @SuppressWarnings("deprecation")
    public static void register(IForgeRegistry<Block> registry) {

        registry.register(new BlockWhopper());
        GameRegistry.registerTileEntity(TileWhopper.class, Ref.MOD_ID + ":whopper");
    }
}