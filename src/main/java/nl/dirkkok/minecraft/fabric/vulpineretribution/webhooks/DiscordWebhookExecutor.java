package nl.dirkkok.minecraft.fabric.vulpineretribution.webhooks;

import de.raik.webhook.WebhookBuilder;
import nl.dirkkok.minecraft.fabric.vulpineretribution.ModConfig;

import java.io.IOException;

public class DiscordWebhookExecutor extends WebhookExecutor {
    @Override
    public void execute(ModConfig.WebhookSettings settings, String playerName, String entityName) throws IOException {
        WebhookBuilder builder = new WebhookBuilder(settings.url);
        builder.content(playerName + " has killed a " + entityName + "!");
        builder.build().execute();
    }
}
