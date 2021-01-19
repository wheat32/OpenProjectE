package moze_intel.projecte.api.capabilities;

import java.math.BigInteger;
import java.util.Set;
import javax.annotation.Nonnull;
import moze_intel.projecte.api.ItemInfo;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;

/**
 * This interface defines the contract for some object that exposes transmutation knowledge through the Capability system.
 *
 * Acquire an instance of this using {@link net.minecraft.entity.Entity#getCapability(Capability, Direction)}.
 */
public interface IKnowledgeProvider extends INBTSerializable<CompoundNBT> {

	/**
	 * @return Whether the player has the "tome" flag set, meaning all knowledge checks automatically return true
	 */
	boolean hasFullKnowledge();

	/**
	 * @param fullKnowledge Whether the player has the "tome" flag set, meaning all knowledge checks automatically return true
	 */
	void setFullKnowledge(boolean fullKnowledge);

	/**
	 * Clears all knowledge. Additionally, clears the "tome" flag.
	 */
	void clearKnowledge();

	/**
	 * @param stack The stack to query
	 *
	 * @return Whether the player has transmutation knowledge for this stack
	 *
	 * @implNote This method defaults to making sure the stack is not empty and then wrapping the stack into an {@link ItemInfo} and calling {@link
	 * #hasKnowledge(ItemInfo)}
	 */
	default boolean hasKnowledge(@Nonnull ItemStack stack) {
		return !stack.isEmpty() && hasKnowledge(ItemInfo.fromStack(stack));
	}

	/**
	 * @param info The {@link ItemInfo} to query
	 *
	 * @return Whether the player has transmutation knowledge for this {@link ItemInfo}
	 */
	boolean hasKnowledge(@Nonnull ItemInfo info);

	/**
	 * @param stack The stack to add to knowledge
	 *
	 * @return Whether the operation was successful
	 *
	 * @implNote This method defaults to making sure the stack is not empty and then wrapping the stack into an {@link ItemInfo} and calling {@link
	 * #addKnowledge(ItemInfo)}
	 */
	default boolean addKnowledge(@Nonnull ItemStack stack) {
		return !stack.isEmpty() && addKnowledge(ItemInfo.fromStack(stack));
	}

	/**
	 * @param info The {@link ItemInfo} to add to knowledge
	 *
	 * @return Whether the operation was successful
	 */
	boolean addKnowledge(@Nonnull ItemInfo info);

	/**
	 * @param stack The stack to remove from knowledge
	 *
	 * @return Whether the operation was successful
	 *
	 * @implNote This method defaults to making sure the stack is not empty and then wrapping the stack into an {@link ItemInfo} and calling {@link
	 * #removeKnowledge(ItemInfo)}
	 */
	default boolean removeKnowledge(@Nonnull ItemStack stack) {
		return !stack.isEmpty() && removeKnowledge(ItemInfo.fromStack(stack));
	}

	/**
	 * @param info The {@link ItemInfo} to remove from knowledge
	 *
	 * @return Whether the operation was successful
	 */
	boolean removeKnowledge(@Nonnull ItemInfo info);

	/**
	 * @return An unmodifiable but live view of the knowledge list.
	 */
	@Nonnull
	Set<ItemInfo> getKnowledge();

	/**
	 * @return The player's input and lock slots
	 */
	@Nonnull
	IItemHandler getInputAndLocks();

	/**
	 * @return The emc in this player's transmutation tablet network
	 */
	BigInteger getEmc();

	/**
	 * @param emc The emc to set in this player's transmutation tablet network
	 */
	void setEmc(BigInteger emc);

	/**
	 * @param player The player to sync to.
	 */
	void sync(@Nonnull ServerPlayerEntity player);
}