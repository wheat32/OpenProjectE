package moze_intel.projecte.gameObjs.items.armor;

import com.google.common.collect.Multimap;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import moze_intel.projecte.gameObjs.items.IFlightProvider;
import moze_intel.projecte.gameObjs.items.IStepAssister;
import moze_intel.projecte.utils.ClientKeyHelper;
import moze_intel.projecte.utils.Constants;
import moze_intel.projecte.utils.PEKeybind;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class GemFeet extends GemArmorBase implements IFlightProvider, IStepAssister {

	private static final UUID MODIFIER = UUID.randomUUID();

	public GemFeet(Properties props) {
		super(EquipmentSlotType.FEET, props);
	}

	public static boolean isStepAssistEnabled(ItemStack stack) {
		return stack.getTag() != null && stack.getTag().contains(Constants.NBT_KEY_STEP_ASSIST) && stack.getTag().getBoolean(Constants.NBT_KEY_STEP_ASSIST);
	}

	public void toggleStepAssist(ItemStack boots, PlayerEntity player) {
		boolean value;
		CompoundNBT bootsTag = boots.getOrCreateTag();
		if (bootsTag.contains(Constants.NBT_KEY_STEP_ASSIST)) {
			bootsTag.putBoolean(Constants.NBT_KEY_STEP_ASSIST, !bootsTag.getBoolean(Constants.NBT_KEY_STEP_ASSIST));
			value = bootsTag.getBoolean(Constants.NBT_KEY_STEP_ASSIST);
		} else {
			//If we don't have the tag count that as it already being "false"
			bootsTag.putBoolean(Constants.NBT_KEY_STEP_ASSIST, true);
			value = true;
		}
		player.sendMessage(new TranslationTextComponent("pe.gem.stepassist_tooltip").appendText(" ")
				.appendSibling(new TranslationTextComponent(value ? "pe.gem.enabled" : "pe.gem.disabled").applyTextStyle(value ? TextFormatting.GREEN : TextFormatting.RED)));
	}

	private static boolean isJumpPressed() {
		return DistExecutor.runForDist(() -> () -> Minecraft.getInstance().gameSettings.keyBindJump.isKeyDown(), () -> () -> false);
	}

	@Override
	public void onArmorTick(ItemStack stack, World world, PlayerEntity player) {
		if (!world.isRemote) {
			ServerPlayerEntity playerMP = (ServerPlayerEntity) player;
			playerMP.fallDistance = 0;
		} else {
			if (!player.abilities.isFlying && isJumpPressed()) {
				player.setMotion(player.getMotion().add(0, 0.1, 0));
			}
			if (!player.onGround) {
				if (player.getMotion().getY() <= 0) {
					player.setMotion(player.getMotion().mul(1, 0.9, 1));
				}
				if (!player.abilities.isFlying) {
					if (player.moveForward < 0) {
						player.setMotion(player.getMotion().mul(0.9, 1, 0.9));
					} else if (player.moveForward > 0 && player.getMotion().lengthSquared() < 3) {
						player.setMotion(player.getMotion().mul(1.1, 1, 1.1));
					}
				}
			}
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltips, ITooltipFlag flags) {
		tooltips.add(new TranslationTextComponent("pe.gem.feet.lorename"));
		tooltips.add(new TranslationTextComponent("pe.gem.stepassist.prompt", ClientKeyHelper.getKeyName(PEKeybind.ARMOR_TOGGLE)));

		boolean enabled = isStepAssistEnabled(stack);
		tooltips.add(new TranslationTextComponent("pe.gem.stepassist_tooltip").appendText(" ")
				.appendSibling(new TranslationTextComponent(enabled ? "pe.gem.enabled" : "pe.gem.disabled").applyTextStyle(enabled ? TextFormatting.GREEN : TextFormatting.RED)));
	}

	@Nonnull
	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(@Nonnull EquipmentSlotType slot, ItemStack stack) {
		if (slot != EquipmentSlotType.FEET) {
			return super.getAttributeModifiers(slot, stack);
		}
		Multimap<String, AttributeModifier> attributes = super.getAttributeModifiers(slot, stack);
		attributes.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(MODIFIER, "Armor modifier", 1.0, Operation.MULTIPLY_TOTAL).setSaved(false));
		return attributes;
	}

	@Override
	public boolean canProvideFlight(ItemStack stack, ServerPlayerEntity player) {
		return player.getItemStackFromSlot(EquipmentSlotType.FEET) == stack;
	}

	@Override
	public boolean canAssistStep(ItemStack stack, ServerPlayerEntity player) {
		return player.getItemStackFromSlot(EquipmentSlotType.FEET) == stack && isStepAssistEnabled(stack);
	}
}