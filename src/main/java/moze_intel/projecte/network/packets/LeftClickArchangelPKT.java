package moze_intel.projecte.network.packets;

import java.util.function.Supplier;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.gameObjs.items.rings.ArchangelSmite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class LeftClickArchangelPKT {

	public static void encode(LeftClickArchangelPKT msg, PacketBuffer buf) {
	}

	public static LeftClickArchangelPKT decode(PacketBuffer buf) {
		return new LeftClickArchangelPKT();
	}

	public static class Handler {

		public static void handle(LeftClickArchangelPKT message, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				PlayerEntity player = ctx.get().getSender();
				ItemStack main = player.getHeldItemMainhand();
				if (!main.isEmpty() && main.getItem() == ObjHandler.angelSmite) {
					((ArchangelSmite) ObjHandler.angelSmite).fireVolley(main, player);
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}
}