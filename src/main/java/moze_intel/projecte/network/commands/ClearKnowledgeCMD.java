package moze_intel.projecte.network.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.KnowledgeClearPKT;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class ClearKnowledgeCMD {

	public static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("clearknowledge")
				.requires(cs -> cs.hasPermissionLevel(4))
				.then(Commands.argument("targets", EntityArgument.players())
						.executes(cs -> execute(cs, EntityArgument.getPlayers(cs, "targets"))));
	}

	private static int execute(CommandContext<CommandSource> ctx, Collection<ServerPlayerEntity> targets) {
		CommandSource source = ctx.getSource();
		for (ServerPlayerEntity player : targets) {
			player.getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY).ifPresent(IKnowledgeProvider::clearKnowledge);
			PacketHandler.sendTo(new KnowledgeClearPKT(), player);
			source.sendFeedback(new TranslationTextComponent("pe.command.clearknowledge.success", player.getDisplayName()), true);

			if (player != source.getEntity()) {
				player.sendMessage(new TranslationTextComponent("pe.command.clearknowledge.notify", source.getDisplayName()).applyTextStyle(TextFormatting.RED));
			}
		}
		return targets.size();
	}
}