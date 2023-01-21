package me.ordalca.combocommands.init;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.config.PixelmonConfigProxy;
import com.pixelmonmod.pixelmon.api.events.battles.CatchComboEvent;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.storage.playerData.CaptureCombo;
import net.minecraft.entity.player.ServerPlayerEntity;


public class WritableCaptureCombo extends CaptureCombo {
    public int captureCount;
    public Species lastCapture;

    WritableCaptureCombo(Species species, int count) {
        this.captureCount = count;
        this.lastCapture = species;
    }

    @Override
    public void onCapture(ServerPlayerEntity player, Species species) {
        if (PixelmonConfigProxy.getBattle().isAllowCatchCombo()) {
            if (this.lastCapture == species) {
                ++this.captureCount;
            } else {
                this.captureCount = 1;
            }

            this.lastCapture = species;
            Pixelmon.EVENT_BUS.post(new CatchComboEvent.ComboIncrement(player, this.lastCapture, this.captureCount));
        } else {
            this.clearCombo();
        }
    }
    @Override
    public int getCurrentCombo() {
        return this.captureCount;
    }
    @Override
    public Species getCurrentSpecies() {
        return this.lastCapture;
    }
    @Override
    public void clearCombo() {
        this.lastCapture = null;
        this.captureCount = 0;
    }
    @Override
    public int getCurrentThreshold() {
        for(int i = 0; i < PixelmonConfigProxy.getBattle().getCatchComboThresholds().size(); ++i) {
            if (this.captureCount <= (Integer)PixelmonConfigProxy.getBattle().getCatchComboThresholds().get(i)) {
                return i;
            }
        }

        return PixelmonConfigProxy.getBattle().getCatchComboThresholds().size();
    }
}
