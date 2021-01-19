package moze_intel.projecte.gameObjs.container;

import javax.annotation.Nonnull;
import moze_intel.projecte.emc.FuelMapper;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.gameObjs.container.slots.SlotGhost;
import moze_intel.projecte.gameObjs.container.slots.SlotPredicates;
import moze_intel.projecte.gameObjs.container.slots.ValidatedSlot;
import moze_intel.projecte.gameObjs.tiles.CollectorMK1Tile;
import moze_intel.projecte.utils.ContainerHelper;
import moze_intel.projecte.utils.GuiHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IntReferenceHolder;
import net.minecraftforge.items.IItemHandler;

public class CollectorMK1Container extends PEContainer {

	public final CollectorMK1Tile tile;
	public final IntReferenceHolder sunLevel = IntReferenceHolder.single();
	public final BoxedLong emc = new BoxedLong();
	private final IntReferenceHolder kleinChargeProgress = IntReferenceHolder.single();
	private final IntReferenceHolder fuelProgress = IntReferenceHolder.single();
	public final BoxedLong kleinEmc = new BoxedLong();

	public static CollectorMK1Container fromNetwork(int windowId, PlayerInventory playerInv, PacketBuffer buf) {
		return new CollectorMK1Container(windowId, playerInv, (CollectorMK1Tile) GuiHandler.getTeFromBuf(buf));
	}

	public CollectorMK1Container(int windowId, PlayerInventory invPlayer, CollectorMK1Tile collector) {
		this(ObjHandler.COLLECTOR_MK1_CONTAINER, windowId, invPlayer, collector);
	}

	protected CollectorMK1Container(ContainerType<? extends CollectorMK1Container> type, int windowId, PlayerInventory invPlayer, CollectorMK1Tile collector) {
		super(type, windowId);
		this.longFields.add(emc);
		this.intFields.add(sunLevel);
		this.intFields.add(kleinChargeProgress);
		this.intFields.add(fuelProgress);
		this.longFields.add(kleinEmc);
		this.tile = collector;
		initSlots(invPlayer);
	}

	void initSlots(PlayerInventory invPlayer) {
		IItemHandler aux = tile.getAux();
		IItemHandler main = tile.getInput();

		//Klein Star Slot
		this.addSlot(new ValidatedSlot(aux, CollectorMK1Tile.UPGRADING_SLOT, 124, 58, SlotPredicates.COLLECTOR_INV));

		int counter = main.getSlots() - 1;
		//Fuel Upgrade storage
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 4; j++) {
				this.addSlot(new ValidatedSlot(main, counter--, 20 + i * 18, 8 + j * 18, SlotPredicates.COLLECTOR_INV));
			}
		}

		//Upgrade Result
		this.addSlot(new ValidatedSlot(aux, CollectorMK1Tile.UPGRADE_SLOT, 124, 13, SlotPredicates.COLLECTOR_INV));

		//Upgrade Target
		this.addSlot(new SlotGhost(aux, CollectorMK1Tile.LOCK_SLOT, 153, 36, SlotPredicates.COLLECTOR_LOCK));

		ContainerHelper.addPlayerInventory(this::addSlot, invPlayer, 8, 84);
	}

	@Nonnull
	@Override
	public ItemStack slotClick(int slotID, int button, @Nonnull ClickType flag, PlayerEntity player) {
		if (slotID >= 0) {
			Slot slot = getSlot(slotID);
			if (slot instanceof SlotGhost && !slot.getStack().isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
				return ItemStack.EMPTY;
			}
		}
		return super.slotClick(slotID, button, flag, player);
	}

	@Override
	public void detectAndSendChanges() {
		emc.set(tile.getStoredEmc());
		sunLevel.set(tile.getSunLevel());
		kleinChargeProgress.set((int) (tile.getItemChargeProportion() * 8000));
		fuelProgress.set((int) (tile.getFuelProgress() * 8000));
		kleinEmc.set(tile.getItemCharge());
		super.detectAndSendChanges();
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

		if (slotIndex <= 10) {
			if (!this.mergeItemStack(stack, 11, 46, false)) {
				return ItemStack.EMPTY;
			}
		} else if (slotIndex <= 46) {
			if (!FuelMapper.isStackFuel(stack) || FuelMapper.isStackMaxFuel(stack) || !this.mergeItemStack(stack, 1, 8, false)) {
				return ItemStack.EMPTY;
			}
		} else {
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
		return player.world.getBlockState(tile.getPos()).getBlock() == ObjHandler.collectorMK1
			   && player.getDistanceSq(tile.getPos().getX() + 0.5, tile.getPos().getY() + 0.5, tile.getPos().getZ() + 0.5) <= 64.0;
	}

	public double getKleinChargeProgress() {
		return kleinChargeProgress.get() / 8000.0;
	}

	public double getFuelProgress() {
		return fuelProgress.get() / 8000.0;
	}
}