package moze_intel.projecte.gameObjs.container.inventory;

import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

public class MercurialEyeInventory implements IItemHandlerModifiable {

	public final ItemStack invItem;
	private final IItemHandlerModifiable compose;

	public MercurialEyeInventory(ItemStack stack) {
		this.invItem = stack;
		this.compose = (IItemHandlerModifiable) stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(NullPointerException::new);
	}

	@Override
	public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
		compose.setStackInSlot(slot, stack);
	}

	@Override
	public int getSlots() {
		return compose.getSlots();
	}

	@Nonnull
	@Override
	public ItemStack getStackInSlot(int slot) {
		return compose.getStackInSlot(slot);
	}

	@Nonnull
	@Override
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
		if (stack == invItem) {
			return stack; // Cannot put the bag into itself
		}
		return compose.insertItem(slot, stack, simulate);
	}

	@Nonnull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return compose.extractItem(slot, amount, simulate);
	}

	@Override
	public int getSlotLimit(int slot) {
		return compose.getSlotLimit(slot);
	}

	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
		return compose.isItemValid(slot, stack);
	}
}