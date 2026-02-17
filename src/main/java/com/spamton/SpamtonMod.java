package com.spamton;

import com.spamton.command.SpamtonCommands;
import com.spamton.entity.SpamtonDamageHandler;
import com.spamton.entity.SpamtonTickHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpamtonMod implements ModInitializer {

    public static final String MOD_ID = "spamton";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        SpamtonConfig.load();
        SpamtonCommands.register();
        SpamtonDamageHandler.register();
        SpamtonTickHandler.register();
        registerWorldLoadFallback();
        LOGGER.info("Spamton mod initialized.");
    }

    private void registerWorldLoadFallback() {
        ServerWorldEvents.LOAD.register((server, world) -> {
            SpamtonTickHandler.onWorldLoad(world);
        });
    }
}
