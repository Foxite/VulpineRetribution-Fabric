package nl.dirkkok.minecraft.fabric.vulpineretribution.webhooks;

import nl.dirkkok.minecraft.fabric.vulpineretribution.ModConfig;

import java.io.IOException;

public abstract class WebhookExecutor {
    public abstract void execute(ModConfig.WebhookSettings settings, String playerName, String entityName) throws IOException;
}
