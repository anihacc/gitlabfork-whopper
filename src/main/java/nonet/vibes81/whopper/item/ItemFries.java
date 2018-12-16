package nonet.vibes81.whopper.item;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemFood;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import nonet.vibes81.whopper.util.Ref;

public class ItemFries extends ItemFood {

    public ItemFries(int amount, float saturation, boolean isWolfFood) {
        super(amount, saturation, isWolfFood);
        
        setTranslationKey(Ref.MOD_ID + ".fries");
        setRegistryName(new ResourceLocation(Ref.MOD_ID, "fries"));
        setCreativeTab(CreativeTabs.FOOD);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

}
