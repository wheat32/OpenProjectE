package moze_intel.projecte.gameObjs.container;

import javax.annotation.Nonnull;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.gameObjs.container.slots.SlotPredicates;
import moze_intel.projecte.gameObjs.container.slots.ValidatedSlot;
import moze_intel.projecte.gameObjs.tiles.RelayMK3Tile;
import moze_intel.projecte.utils.ContainerHelper;
import moze_intel.projecte.utils.GuiHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandler;

public class RelayMK3Container extends RelayMK1Container {

	public static RelayMK3Container fromNetwork(int windowId, PlayerInventory invPlayer, PacketBuffer buf) {
		return new RelayMK3Container(windowId, invPlayer, (RelayMK3Tile) GuiHandler.getTeFromBuf(buf));
	}

	public RelayMK3Container(int windowId, PlayerInventory invPlayer, RelayMK3Tile relay) {
		super(ObjHandler.RELAY_MK3_CONTAINER, windowId, invPlayer, relay);
	}

	@Override
	void initSlots(PlayerInventory invPlayer) {
		IItemHandler input = tile.getInput();
		IItemHandler output = tile.getOutput();

		//Burn slot
		this.addSlot(new ValidatedSlot(input, 0, 104, 58, SlotPredicates.RELAY_INV));

		int counter = input.getSlots() - 1;
		//Inventory Buffer
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 5; j++) {
				this.addSlot(new ValidatedSlot(input, counter--, 28 + i * 18, 18 + j * 18, SlotPredicates.RELAY_INV));
			}
		}

		//Klein star charge
		this.addSlot(new ValidatedSlot(output, 0, 164, 58, SlotPredicates.EMC_HOLDER));

		ContainerHelper.addPlayerInventory(this::addSlot, invPlayer, 26, 113);
	}

	@Nonnull
	@Override
	public ItemStack transferStackInSlot(@Nonnull PlayerEntity player, int slotIndex) {
		Slot slot = this.getSlot(slotIndex);

		if (slot == null || !slot.getHasStack()) {
			return ItemStack.EMPTY;
		}

		ItemStack stack = slot.getStack();
		ItemStack newStack = stack.copy();

		if (slotIndex < 22) {
			if (!this.mergeItemStack(stack, 22, this.inventorySlots.size(), true)) {
				return ItemStack.EMPTY;
			}
			slot.onSlotChanged();
		} else if (!this.mergeItemStack(stack, 0, 21, false)) {
			return ItemStack.EMPTY;
		}
		if (stack.isEmpty()) {
			slot.putStack(ItemStack.EMPTY);
		} else {
			slot.onSlotChanged();
		}
		return slot.onTake(player, newStack);
	}

	@Override
	public boolean canInteractWith(@Nonnull PlayerEntity player) {
		return player.world.getBlockState(tile.getPos()).getBlock() == ObjHandler.relayMK3
			   && player.getDistanceSq(tile.getPos().getX() + 0.5, tile.getPos().getY() + 0.5, tile.getPos().getZ() + 0.5) <= 64.0;
	}
}