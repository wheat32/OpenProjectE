package moze_intel.projecte.gameObjs.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.container.AlchBagContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class AlchBagScreen extends PEContainerScreen<AlchBagContainer> {

	private static final ResourceLocation texture = new ResourceLocation(PECore.MODID, "textures/gui/alchchest.png");

	public AlchBagScreen(AlchBagContainer container, PlayerInventory invPlayer, ITextComponent title) {
		super(container, invPlayer, title);
		this.xSize = 255;
		this.ySize = 230;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		RenderSystem.color4f(1, 1, 1, 1);
		Minecraft.getInstance().textureManager.bindTexture(texture);
		blit(guiLeft, guiTop, 0, 0, xSize, ySize);
	}
}