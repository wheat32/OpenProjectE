package moze_intel.projecte.capability;

import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class ItemCapabilityWrapper implements ICapabilitySerializable<CompoundNBT> {

	private final ItemCapability<?>[] capabilities;
	private final ItemStack itemStack;

	public ItemCapabilityWrapper(ItemStack stack, List<Supplier<ItemCapability<?>>> capabilities) {
		itemStack = stack;
		this.capabilities = new ItemCapability<?>[capabilities.size()];
		for (int i = 0; i < capabilities.size(); i++) {
			ItemCapability<?> cap = capabilities.get(i).get();
			this.capabilities[i] = cap;
			cap.setWrapper(this);
		}
	}

	public ItemCapabilityWrapper(ItemStack stack, ItemCapability<?>... capabilities) {
		itemStack = stack;
		this.capabilities = capabilities;
		for (ItemCapability<?> cap : this.capabilities) {
			cap.setWrapper(this);
		}
	}

	protected ItemStack getItemStack() {
		return itemStack;
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
		for (ItemCapability<?> cap : capabilities) {
			if (capability == cap.getCapability()) {
				return cap.getLazyCapability().cast();
			}
		}
		return LazyOptional.empty();
	}

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT serializedNBT = new CompoundNBT();
		for (ItemCapability<?> cap : capabilities) {
			if (cap instanceof IItemCapabilitySerializable) {
				IItemCapabilitySerializable serializableCap = (IItemCapabilitySerializable) cap;
				serializedNBT.put(serializableCap.getStorageKey(), serializableCap.serializeNBT());
			}
		}
		return serializedNBT;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		for (ItemCapability<?> cap : capabilities) {
			if (cap instanceof IItemCapabilitySerializable) {
				IItemCapabilitySerializable serializableCap = (IItemCapabilitySerializable) cap;
				if (nbt.contains(serializableCap.getStorageKey())) {
					serializableCap.deserializeNBT(nbt.get(serializableCap.getStorageKey()));
				}
			}
		}
	}
}