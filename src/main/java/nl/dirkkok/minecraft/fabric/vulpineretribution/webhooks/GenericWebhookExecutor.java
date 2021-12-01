package nl.dirkkok.minecraft.fabric.vulpineretribution.webhooks;

import com.google.gson.JsonObject;
import nl.dirkkok.minecraft.fabric.vulpineretribution.ModConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class GenericWebhookExecutor extends WebhookExecutor {
    @Override
    public void execute(ModConfig.WebhookSettings settings, String playerName, String entityName) throws IOException {
        URL url = new URL(settings.url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // https://stackoverflow.com/a/7020054
        if (settings.basicAuthUsername != null && settings.basicAuthPassword != null && settings.basicAuthUsername.length() > 0 && settings.basicAuthPassword.length() > 0) {
            String encoded = Base64.getEncoder().encodeToString((settings.basicAuthUsername + ":" + settings.basicAuthPassword).getBytes(StandardCharsets.UTF_8));
            connection.setRequestProperty("Authorization", "Basic " + encoded);
        }
        JsonObject data = new JsonObject();
        data.addProperty("player", playerName);
        data.addProperty("target", entityName);

        connection.setRequestMethod("POST");
        OutputStream os = connection.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os);
        osw.write(data.toString());
        connection.disconnect();
    }
}
