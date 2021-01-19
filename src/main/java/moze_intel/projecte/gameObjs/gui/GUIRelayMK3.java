package moze_intel.projecte.gameObjs.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.container.RelayMK3Container;
import moze_intel.projecte.utils.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GUIRelayMK3 extends PEContainerScreen<RelayMK3Container> {

	private static final ResourceLocation texture = new ResourceLocation(PECore.MODID, "textures/gui/relay3.png");

	public GUIRelayMK3(RelayMK3Container container, PlayerInventory invPlayer, ITextComponent title) {
		super(container, invPlayer, title);
		this.xSize = 212;
		this.ySize = 194;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int var1, int var2) {
		this.font.drawString(I18n.format("pe.relay.mk3"), 38, 6, 0x404040);
		this.font.drawString(Constants.EMC_FORMATTER.format(container.emc.get()), 125, 39, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		RenderSystem.color4f(1, 1, 1, 1);
		Minecraft.getInstance().textureManager.bindTexture(texture);

		blit(guiLeft, guiTop, 0, 0, xSize, ySize);

		//Emc bar progress
		int progress = (int) ((double) container.emc.get() / container.tile.getMaximumEmc() * Constants.MAX_CONDENSER_PROGRESS);
		blit(guiLeft + 105, guiTop + 6, 30, 195, progress, 10);

		//Klein start bar progress. Max is 30.
		progress = (int) (container.getKleinChargeProgress() * 30);
		blit(guiLeft + 153, guiTop + 82, 0, 195, progress, 10);

		//Burn Slot bar progress. Max is 30.
		progress = (int) (container.getInputBurnProgress() * 30);
		blit(guiLeft + 101, guiTop + 82, 0, 195, progress, 10);
	}
}