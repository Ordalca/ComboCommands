package me.ordalca.combocommands;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.command.PixelmonCommandUtils;
import com.pixelmonmod.pixelmon.api.events.battles.CatchComboEvent;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.api.storage.TransientData;
import com.pixelmonmod.pixelmon.storage.playerData.CaptureCombo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Optional;

public class ComboCommandController {
    @SubscribeEvent
    public void onPlayerLogsIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            String message = "You currently have no catch combo.";
            if(serverPlayer.getPersistentData().contains("combo")) {
                CompoundTag comboNBT = serverPlayer.getPersistentData().getCompound("combo");
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
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PlayerPartyStorage storage = StorageProxy.getPartyNow(serverPlayer);
            if (storage != null) {
                CaptureCombo captureCombo = storage.transientData.captureCombo;
                Species currentCombo = captureCombo.getCurrentSpecies();
                if (currentCombo != null) {
                    updateComboData(serverPlayer, currentCombo.getName(), captureCombo.getCurrentCombo());
                } else {
                    updateComboData(serverPlayer, "", 0);
                }
            }
        }
    }

    @SubscribeEvent
    public void comboIncreased(CatchComboEvent.ComboIncrement event) {
        ServerPlayer player = event.getPlayer();
        String name = event.getComboSpecies().getName();
        int count = event.getCombo();
        updateComboData(player, name, count);
    }
    public void updateComboData(ServerPlayer player, String name, int count) {
        CompoundTag combo = new CompoundTag();
        combo.putString("species", name);
        combo.putInt("count", count);
        player.getPersistentData().put("combo", combo);
    }

    public static boolean setCombo(ServerPlayer player, String species, int count) {
        Optional<Species> check = PixelmonSpecies.fromNameOrDex(species);
        if (check.isPresent()) {
            Species found = check.get();
            TransientData transientData = getDataForPlayer(player);
            if (transientData == null) {
                PixelmonCommandUtils.sendMessage(player, "Could not find player data for "+player);
            }
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

    public static TransientData getDataForPlayer(ServerPlayer player) {
        PlayerPartyStorage storage = StorageProxy.getPartyNow(player);
        if (storage != null) {
            return storage.transientData;
        } else {
            return null;
        }
    }
}
