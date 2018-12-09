package nonet.vibes81.whopper.blocks;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import nonet.vibes81.whopper.util.Ref;

public class GuiWhopper extends GuiContainer {

    private static final ResourceLocation background = new ResourceLocation(Ref.MOD_ID, "textures/gui/whopper.png");
    private TileWhopper blockWhopper;

    GuiWhopper(TileWhopper tileEntity, ContainerWhopper container) {
        super(container);

        blockWhopper = tileEntity;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(background);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
        String customName = blockWhopper.getDisplayName().getFormattedText();
        fontRenderer.drawString(customName,guiLeft + 6, guiTop + 6,  0x404040, false);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }
}