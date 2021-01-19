package moze_intel.projecte.events;

import java.util.EnumSet;
import java.util.Set;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.gameObjs.container.AlchBagContainer;
import moze_intel.projecte.gameObjs.items.AlchemicalBag;
import moze_intel.projecte.gameObjs.items.IFireProtector;
import moze_intel.projecte.handlers.InternalAbilities;
import moze_intel.projecte.handlers.InternalTimers;
import moze_intel.projecte.utils.PlayerHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

@Mod.EventBusSubscriber(modid = PECore.MODID)
public class TickEvents {

	@SubscribeEvent
	public static void playerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			event.player.getCapability(ProjectEAPI.ALCH_BAG_CAPABILITY).ifPresent(provider -> {
				Set<DyeColor> colorsChanged = EnumSet.noneOf(DyeColor.class);
				for (DyeColor color : getBagColorsPresent(event.player)) {
					IItemHandler inv = provider.getBag(color);
					for (int i = 0; i < inv.getSlots(); i++) {
						ItemStack current = inv.getStackInSlot(i);
						if (!current.isEmpty()) {
							current.getCapability(ProjectEAPI.ALCH_BAG_ITEM_CAPABILITY).ifPresent(alchBagItem -> {
								if (alchBagItem.updateInAlchBag(inv, event.player, current)) {
									colorsChanged.add(color);
								}
							});
						}
					}
				}

				for (DyeColor e : colorsChanged) {
					if (event.player.openContainer instanceof AlchBagContainer) {
						ItemStack heldItem = event.player.getHeldItem(((AlchBagContainer) event.player.openContainer).hand);
						if (heldItem.getItem() instanceof AlchemicalBag && ((AlchemicalBag) heldItem.getItem()).color == e) {
							// Do not sync if this color is open, the container system does it for us
							// and we'll stay out of its way.
							continue;
						}
					}
					provider.sync(e, (ServerPlayerEntity) event.player);
				}
			});

			if (!event.player.getEntityWorld().isRemote) {
				event.player.getCapability(InternalAbilities.CAPABILITY).ifPresent(InternalAbilities::tick);
				event.player.getCapability(InternalTimers.CAPABILITY).ifPresent(InternalTimers::tick);
				if (event.player.isBurning() && shouldPlayerResistFire((ServerPlayerEntity) event.player)) {
					event.player.extinguish();
				}
			}
		}
	}

	public static boolean shouldPlayerResistFire(ServerPlayerEntity player) {
		for (ItemStack stack : player.inventory.armorInventory) {
			if (!stack.isEmpty() && stack.getItem() instanceof IFireProtector && ((IFireProtector) stack.getItem()).canProtectAgainstFire(stack, player)) {
				return true;
			}
		}
		for (int i = 0; i <= 8; i++) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (!stack.isEmpty() && stack.getItem() instanceof IFireProtector && ((IFireProtector) stack.getItem()).canProtectAgainstFire(stack, player)) {
				return true;
			}
		}
		IItemHandler curios = PlayerHelper.getCurios(player);
		if (curios != null) {
			for (int i = 0; i < curios.getSlots(); i++) {
				ItemStack stack = curios.getStackInSlot(i);
				if (!stack.isEmpty() && stack.getItem() instanceof IFireProtector && ((IFireProtector) stack.getItem()).canProtectAgainstFire(stack, player)) {
					return true;
				}
			}
		}
		return false;
	}

	private static Set<DyeColor> getBagColorsPresent(PlayerEntity player) {
		Set<DyeColor> bagsPresent = EnumSet.noneOf(DyeColor.class);
		player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(inv -> {
			for (int i = 0; i < inv.getSlots(); i++) {
				ItemStack stack = inv.getStackInSlot(i);
				if (!stack.isEmpty() && stack.getItem() instanceof AlchemicalBag) {
					bagsPresent.add(((AlchemicalBag) stack.getItem()).color);
				}
			}
		});
		return bagsPresent;
	}
}