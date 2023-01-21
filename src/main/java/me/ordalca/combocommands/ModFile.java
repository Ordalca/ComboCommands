package me.ordalca.combocommands;

import com.pixelmonmod.pixelmon.Pixelmon;
import me.ordalca.combocommands.init.ComboCommand;
import me.ordalca.combocommands.init.ComboCommandController;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ModFile.MOD_ID)
@Mod.EventBusSubscriber(modid = ModFile.MOD_ID)
public class ModFile {

    public static final String MOD_ID = "combocommands";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    private static ModFile instance;

    public ModFile() {
        instance = this;

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);

        MinecraftForge.EVENT_BUS.register(this);
        Pixelmon.EVENT_BUS.register(this);

        ComboCommandController listener = new ComboCommandController();
        MinecraftForge.EVENT_BUS.register(listener);
        Pixelmon.EVENT_BUS.register(listener);


    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Loaded ComboCommand mod");
    }

    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event) {
        new ComboCommand(event.getDispatcher());
    }


    public static ModFile getInstance() {
        return instance;
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
