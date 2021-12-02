package nl.dirkkok.minecraft.fabric.vulpineretribution;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = VulpineRetribution.MODID)
public class ModConfig implements ConfigData {
    public String targetEntity = "chicken";
    public boolean destroyItems;
    public boolean kickPlayer;
    public boolean banPlayer;
    public int banDurationMinutes;
    public boolean executeWebhook;
    public boolean aggressiveBlaming;
    public double maxBlamingDistance;
    public boolean discountTotem;
    public boolean explain;
    public WebhookSettings webhookSettings = new WebhookSettings();

    public static class WebhookSettings {
        public WebhookType type = WebhookType.DISCORD;
        public String url = "";
        public String basicAuthUsername;
        public String basicAuthPassword;

        public enum WebhookType {
            GENERIC,
            DISCORD
        }
    }
}
