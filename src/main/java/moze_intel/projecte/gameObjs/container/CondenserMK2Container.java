package moze_intel.projecte.gameObjs.container;

import javax.annotation.Nonnull;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.gameObjs.container.slots.SlotCondenserLock;
import moze_intel.projecte.gameObjs.container.slots.SlotPredicates;
import moze_intel.projecte.gameObjs.container.slots.ValidatedSlot;
import moze_intel.projecte.gameObjs.tiles.CondenserMK2Tile;
import moze_intel.projecte.utils.ContainerHelper;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.GuiHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandler;

public class CondenserMK2Container extends CondenserContainer {

	public CondenserMK2Container(int windowId, PlayerInventory invPlayer, CondenserMK2Tile condenser) {
		super(ObjHandler.CONDENSER_MK2_CONTAINER, windowId, invPlayer, condenser);
	}

	public static CondenserMK2Container fromNetwork(int windowId, PlayerInventory invPlayer, PacketBuffer buf) {
		return new CondenserMK2Container(windowId, invPlayer, (CondenserMK2Tile) GuiHandler.getTeFromBuf(buf));
	}

	@Override
	protected void initSlots(PlayerInventory invPlayer) {
		this.addSlot(new SlotCondenserLock(boxedLockInfo, 0, 12, 6));

		IItemHandler input = tile.getInput();
		IItemHandler output = tile.getOutput();

		//Condenser Inventory
		//Inputs
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 6; j++) {
				this.addSlot(new ValidatedSlot(input, j + i * 6, 12 + j * 18, 26 + i * 18, s -> SlotPredicates.HAS_EMC.test(s) && !tile.isStackEqualToLock(s)));
			}
		}

		//Outputs
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 6; j++) {
				this.addSlot(new ValidatedSlot(output, j + i * 6, 138 + j * 18, 26 + i * 18, s -> false));
			}
		}

		ContainerHelper.addPlayerInventory(this::addSlot, invPlayer, 48, 154);
	}

	@Nonnull
	@Override
	public ItemStack transferStackInSlot(@Nonnull PlayerEntity player, int slotIndex) {
		if (slotIndex == 0) {
			return ItemStack.EMPTY;
		}

		Slot slot = this.getSlot(slotIndex);

		if (slot == null || !slot.getHasStack()) {
			return ItemStack.EMPTY;
		}

		ItemStack stack = slot.getStack();
		ItemStack newStack = stack.copy();

		if (slotIndex <= 84) {
			if (!this.mergeItemStack(stack, 85, 120, false)) {
				return ItemStack.EMPTY;
			}
		} else if (!EMCHelper.doesItemHaveEmc(stack) || !this.mergeItemStack(stack, 1, 42, false)) {
			return ItemStack.EMPTY;
		}

		if (stack.isEmpty()) {
			slot.putStack(ItemStack.EMPTY);
		} else {
			slot.onSlotChanged();
		}
		return slot.onTake(player, stack);
	}

	@Override
	public boolean canInteractWith(@Nonnull PlayerEntity player) {
		return player.world.getBlockState(tile.getPos()).getBlock() == ObjHandler.condenserMk2
			   && player.getDistanceSq(tile.getPos().getX() + 0.5, tile.getPos().getY() + 0.5, tile.getPos().getZ() + 0.5) <= 64.0;
	}
}