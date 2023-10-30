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
    public int currentThresholdIndex;

    WritableCaptureCombo(Species species, int count) {
        this.captureCount = count;
        this.lastCapture = species;
        this.refreshThresholdIndex();
    }

    @Override
    public void onCapture(ServerPlayerEntity player, Species species) {
        if (PixelmonConfigProxy.getBattle().isAllowCatchCombo()) {
            if (this.lastCapture == species) {
                ++this.captureCount;
            } else {
                this.captureCount = 1;
            }

            this.refreshThresholdIndex();
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
        this.currentThresholdIndex = 0;
    }
    @Override
    public int getCurrentThreshold() {
        return currentThresholdIndex;
    }
    @Override
    public int refreshThresholdIndex() {
        for(int i = 0; i < PixelmonConfigProxy.getBattle().getCatchComboThresholds().size(); i++) {
            if(this.captureCount <= PixelmonConfigProxy.getBattle().getCatchComboThresholds().get(i)) {
                return currentThresholdIndex = i;
            }
        }
        return currentThresholdIndex = PixelmonConfigProxy.getBattle().getCatchComboThresholds().size();
    }
    @Override
    public float getBaseExpBonus() {
        return PixelmonConfigProxy.getBattle().getCatchComboExpBonuses().size() > 0 ? PixelmonConfigProxy.getBattle().getCatchComboExpBonuses().get(0) : 1F;
    }
    @Override
    public float getExpBouns() {
        return PixelmonConfigProxy.getBattle().getCatchComboExpBonuses().size() > currentThresholdIndex ? PixelmonConfigProxy.getBattle().getCatchComboExpBonuses().get(currentThresholdIndex) : 1F;
    }
    @Override
    public float getBaseShinyModifier() {
        return PixelmonConfigProxy.getBattle().getCatchComboShinyModifiers().size() > 0 ? PixelmonConfigProxy.getBattle().getCatchComboShinyModifiers().get(0) : 1F;
    }
    @Override
    public float getShinyModifier() {
        return PixelmonConfigProxy.getBattle().getCatchComboShinyModifiers().size() > currentThresholdIndex ? PixelmonConfigProxy.getBattle().getCatchComboShinyModifiers().get(currentThresholdIndex) : 1F;
    }
    @Override
    public int getBasePerfIVCount() {
        return PixelmonConfigProxy.getBattle().getCatchComboPerfectIVs().size() > 0 ? PixelmonConfigProxy.getBattle().getCatchComboPerfectIVs().get(0) : 0;
    }

    @Override
    public int getPerfIVCount() {
        return PixelmonConfigProxy.getBattle().getCatchComboPerfectIVs().size() > currentThresholdIndex ? PixelmonConfigProxy.getBattle().getCatchComboPerfectIVs().get(currentThresholdIndex) : 0;
    }
}
