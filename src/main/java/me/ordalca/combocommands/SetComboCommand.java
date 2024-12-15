package me.ordalca.combocommands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pixelmonmod.api.registry.RegistryValue;
import com.pixelmonmod.pixelmon.api.command.PixelmonCommandUtils;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.util.helpers.NumberHelper;
import com.pixelmonmod.pixelmon.command.PixelCommand;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.*;

public class SetComboCommand extends PixelCommand {
	public SetComboCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
		super(dispatcher, "setcombo", "/setcombo <count> <species>", 4);
	}

	@Override
	public void execute(CommandSourceStack sender, String[] args) throws CommandRuntimeException, CommandSyntaxException {
		if (args.length == 2) {
			ServerPlayer player = PixelmonCommandUtils.requireEntityPlayer(sender);
			if (player == null) {
				PixelmonCommandUtils.endCommand("argument.entity.notfound.player", sender);
			}

			OptionalInt count = NumberHelper.parseInt(args[0]);
			if (count.isEmpty()) {
				PixelmonCommandUtils.endCommand("pixelmon.command.general.invalid");
			}

			String speciesName = null;

			Optional<RegistryValue<Species>> target = PixelmonSpecies.get(args[1]);
			if (target.isPresent()) {
				speciesName = target.get().getValueUnsafe().getStrippedName();
			} else {
				PixelmonCommandUtils.endCommand("pixelmon.command.general.invalid");
			}

			if (ComboCommandController.setCombo(player, speciesName, count.getAsInt())) {
				PixelmonCommandUtils.sendMessage(player, "Set "+player.getName().getString()+"'s combo for "+speciesName+" to "+count.getAsInt());
			}
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, CommandSourceStack sender, String[] args, BlockPos pos) throws CommandSyntaxException {
		return switch (args.length) {
			case 0 -> super.getTabCompletions(server, sender, args, pos);
			case 2 -> PixelmonCommandUtils.tabCompletePokemon();
			default -> Collections.emptyList();
		};
	}
}