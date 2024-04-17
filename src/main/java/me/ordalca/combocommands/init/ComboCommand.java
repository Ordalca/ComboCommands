package me.ordalca.combocommands.init;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pixelmonmod.pixelmon.api.command.PixelmonCommandUtils;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.command.PixelCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.List;
import java.util.Locale;


public class ComboCommand extends PixelCommand {
    public ComboCommand(CommandDispatcher<CommandSource> dispatcher) {
        super(dispatcher, "combo", "/combo [player] <pokemon> <count>", 4);
    }
    @Override
    public void execute(CommandSource sender, String[] args) throws CommandException, CommandSyntaxException {
        args = PixelmonCommandUtils.setupCommandTargets(this, sender, args, 0);
        if (args.length < 3) {
            sender.sendSuccess(PixelmonCommandUtils.format(TextFormatting.RED, "pixelmon.command.general.invalid", new Object[0]), false);
            PixelmonCommandUtils.endCommand(this.getUsage(sender), new Object[0]);
        }
        ServerPlayerEntity player;
        int argIdx = 0;
        if (PixelmonSpecies.has(args[0].toLowerCase(Locale.ROOT)) && ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByName(args[0]) == null) {
             player = PixelmonCommandUtils.requireEntityPlayer(sender);
        } else {
            player = PixelmonCommandUtils.requireEntityPlayer(args[argIdx]);
            argIdx+=1;
        }
        if (player == null) {
            PixelmonCommandUtils.endCommand("argument.entity.notfound.player", new Object[]{args[0]});
        }
        String species = args[argIdx++];
        int count = PixelmonCommandUtils.requireInt(args[argIdx],"pixelmon.command.general.invalid");
        boolean success = ComboCommandController.setCombo(player, species, count);
        if (success){
            PixelmonCommandUtils.sendMessage(player, "You have a catch combo of "+count+" "+species+".");
        } else {
            PixelmonCommandUtils.sendMessage(sender, "Catch combo setting failed.");
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, CommandSource sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return PixelmonCommandUtils.tabCompleteUsernames();
        } else if (args.length == 2) {
            return PixelmonCommandUtils.tabCompletePokemon();
        } else {
            return Lists.newArrayList();
        }
    }
}
