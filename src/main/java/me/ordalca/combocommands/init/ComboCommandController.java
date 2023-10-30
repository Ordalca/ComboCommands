package me.ordalca.combocommands.init;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.command.PixelmonCommandUtils;
import com.pixelmonmod.pixelmon.api.events.battles.CatchComboEvent;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.api.storage.TransientData;
import com.pixelmonmod.pixelmon.storage.playerData.CaptureCombo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Optional;

public class ComboCommandController {
    @SubscribeEvent
    public void onPlayerLogsIn(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            String message = "You currently have no catch combo.";
            if(serverPlayer.getPersistentData().contains("combo")) {
                CompoundNBT comboNBT = serverPlayer.getPersistentData().getCompound("combo");
                String species = comboNBT.contains("species")?comboNBT.getString("species"):"";
                int count = comboNBT.contains("count")?comboNBT.getInt("count"):0;
                if (! (species.equals("") || count == 0)) {
                    boolean comboExists = ComboCommandController.setCombo(serverPlayer, species,count);
                    if (comboExists && count > 0) {
                        message = "Current Combo: "+count+" "+species+".";
                    }
                }
            }
            PixelmonCommandUtils.sendMessage(serverPlayer, message);
        }
    }

    @SubscribeEvent
    public void onPlayerLogsOut(PlayerEvent.PlayerLoggedOutEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            CaptureCombo captureCombo = StorageProxy.getParty(serverPlayer).transientData.captureCombo;
            Species currentCombo = captureCombo.getCurrentSpecies();
            if (currentCombo != null) {
                updateComboData(serverPlayer, currentCombo.getName(), captureCombo.getCurrentCombo());
            } else {
                updateComboData(serverPlayer, "", 0);
            }
        }
    }

    @SubscribeEvent
    public void comboIncreased(CatchComboEvent.ComboIncrement event) {
        ServerPlayerEntity player = event.getPlayer();
        String name = event.getComboSpecies().getName();
        int count = event.getCombo();
        updateComboData(player, name, count);
    }
    public void updateComboData(ServerPlayerEntity player, String name, int count) {
        CompoundNBT combo = new CompoundNBT();
        combo.putString("species", name);
        combo.putInt("count", count);
        player.getPersistentData().put("combo", combo);
    }

    public static boolean setCombo(ServerPlayerEntity player, String species, int count) {
        Optional<Species> check = PixelmonSpecies.fromNameOrDex(species);
        if (check.isPresent()) {
            Species found = check.get();
            TransientData transientData = StorageProxy.getParty(player).transientData;
            WritableCaptureCombo captureCombo;
            if (transientData.captureCombo instanceof WritableCaptureCombo) {
                captureCombo = (WritableCaptureCombo) transientData.captureCombo;
                captureCombo.lastCapture = found;
                captureCombo.captureCount = count;
                captureCombo.refreshThresholdIndex();
            } else {
                transientData.captureCombo = new WritableCaptureCombo(found, count);
            }
            Pixelmon.EVENT_BUS.post(new CatchComboEvent.ComboIncrement(player, found, count));
        } else {
            PixelmonCommandUtils.sendMessage(player, "Could not find species \"" + species + "\"");
        }
        return check.isPresent();
    }
}
