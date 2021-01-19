package moze_intel.projecte.network.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import moze_intel.projecte.config.CustomEMCParser;
import moze_intel.projecte.network.commands.argument.NSSItemArgument;
import moze_intel.projecte.network.commands.parser.NSSItemParser.NSSItemResult;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;

public class SetEmcCMD {

	public static LiteralArgumentBuilder<CommandSource> register() {
		return Commands.literal("setemc")
				.requires(cs -> cs.hasPermissionLevel(4))
				.then(Commands.argument("emc", LongArgumentType.longArg(0, Long.MAX_VALUE))
						.then(Commands.argument("item", new NSSItemArgument())
								.executes(ctx -> setEmc(ctx, NSSItemArgument.getNSS(ctx, "item"), LongArgumentType.getLong(ctx, "emc"))))
						.executes(ctx -> setEmc(ctx, RemoveEmcCMD.getHeldStack(ctx), LongArgumentType.getLong(ctx, "emc"))));

	}

	private static int setEmc(CommandContext<CommandSource> ctx, NSSItemResult stack, long emc) {
		String toSet = stack.getStringRepresentation();
		CustomEMCParser.addToFile(toSet, emc);
		ctx.getSource().sendFeedback(new TranslationTextComponent("pe.command.set.success", toSet, emc), true);
		ctx.getSource().sendFeedback(new TranslationTextComponent("pe.command.reload.notice"), true);
		return Command.SINGLE_SUCCESS;
	}
}