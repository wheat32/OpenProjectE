package moze_intel.projecte.events;

import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.ObjHandler;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PECore.MODID, value = Dist.CLIENT)
public class PlayerRender {

	@SubscribeEvent
	public static void onFOVUpdateEvent(FOVUpdateEvent evt) {
		if (!evt.getEntity().getItemStackFromSlot(EquipmentSlotType.FEET).isEmpty() && evt.getEntity().getItemStackFromSlot(EquipmentSlotType.FEET).getItem() == ObjHandler.gemFeet) {
			evt.setNewfov(evt.getFov() - 0.4F);
		}
	}
}