package moze_intel.projecte.gameObjs.container.slots.transmutation;

import java.math.BigInteger;
import javax.annotation.Nonnull;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class SlotConsume extends SlotItemHandler {

	private final TransmutationInventory inv;

	public SlotConsume(TransmutationInventory inv, int par2, int par3, int par4) {
		super(inv, par2, par3, par4);
		this.inv = inv;
	}

	@Override
	public void putStack(@Nonnull ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}

		inv.addEmc(BigInteger.valueOf(EMCHelper.getEmcSellValue(stack)).multiply(BigInteger.valueOf(stack.getCount())));
		this.onSlotChanged();
		inv.handleKnowledge(stack);
	}

	@Override
	public boolean isItemValid(@Nonnull ItemStack stack) {
		return EMCHelper.doesItemHaveEmc(stack) || stack.getItem() == ObjHandler.tome;
	}
}