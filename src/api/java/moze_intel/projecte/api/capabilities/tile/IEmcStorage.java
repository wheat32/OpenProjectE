package moze_intel.projecte.api.capabilities.tile;

import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

/**
 * This interface represents a capability for tiles that want to support storing, providing, or receiving EMC.
 *
 * The contract of this interface is only the above statement However, ProjectE implements an "active-push" system, where providers automatically send EMC to acceptors.
 * You are recommended to follow this convention.
 *
 * This is exposed through the Capability system.
 *
 * Acquire an instance of this using {@link net.minecraft.tileentity.TileEntity#getCapability(Capability, Direction)}.
 *
 * @author williewillus
 */
public interface IEmcStorage {

	/**
	 * Like {@link net.minecraftforge.fluids.capability.IFluidHandler.FluidAction} except for EMC in general
	 */
	enum EmcAction {
		EXECUTE,
		SIMULATE;

		public boolean execute() {
			return this == EXECUTE;
		}

		public boolean simulate() {
			return this == SIMULATE;
		}

		public EmcAction combine(boolean execute) {
			return get(execute && execute());
		}

		public static EmcAction get(boolean execute) {
			return execute ? EXECUTE : SIMULATE;
		}
	}

	/**
	 * Gets the current amount of EMC in this IEMCStorage
	 *
	 * @return The current EMC stored
	 */
	long getStoredEmc();

	/**
	 * Gets the maximum amount of EMC this IEMCStorage is allowed to contain
	 *
	 * @return The maximum EMC allowed
	 *
	 * @implNote This value should never be zero
	 */
	long getMaximumEmc();

	/**
	 * Helper method to get the amount of EMC this {@link IEmcStorage} needs to become full.
	 *
	 * @return The amount of EMC this {@link IEmcStorage} needs.
	 */
	default long getNeededEmc() {
		return Math.max(0, getMaximumEmc() - getStoredEmc());
	}

	/**
	 * Checks if this {@link IEmcStorage} is full.
	 *
	 * @return True if it is full
	 */
	default boolean hasMaxedEmc() {
		return getStoredEmc() >= getMaximumEmc();
	}

	/**
	 * Extract, at most, the given amount of EMC
	 *
	 * @param toExtract The maximum amount to extract
	 * @param action    The action to perform, either {@link EmcAction#EXECUTE} or {@link EmcAction#SIMULATE}
	 *
	 * @return The amount actually extracted
	 *
	 * @implNote It is expected that if a negative value for {@code toExtract} is given, you act as if {@link #insertEmc(long, EmcAction)} was called instead.
	 */
	long extractEmc(long toExtract, EmcAction action);

	/**
	 * Accept, at most, the given amount of EMC from the given side
	 *
	 * @param toAccept The maximum amount to accept
	 * @param action   The action to perform, either {@link EmcAction#EXECUTE} or {@link EmcAction#SIMULATE}
	 *
	 * @return The amount actually accepted
	 *
	 * @implNote It is expected that if a negative value for {@code toAccept} is given, you act as if {@link #extractEmc(long, EmcAction)} was called instead.
	 */
	long insertEmc(long toAccept, EmcAction action);

	/**
	 * Checks if this {@link IEmcStorage} is a relay.
	 *
	 * @return True if this {@link IEmcStorage} should be considered to be a relay.
	 *
	 * @implNote If two neighboring {@link IEmcStorage}s are both relays, ProjectE's blocks (relays) will not try send Emc to each other. This is to prevent thrashing.
	 */
	default boolean isRelay() {
		return false;
	}
}