package nl.dirkkok.minecraft.fabric.vulpineretribution;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.ConfigSerializer;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import nl.dirkkok.minecraft.fabric.vulpineretribution.webhooks.DiscordWebhookExecutor;
import nl.dirkkok.minecraft.fabric.vulpineretribution.webhooks.GenericWebhookExecutor;
import nl.dirkkok.minecraft.fabric.vulpineretribution.webhooks.WebhookExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Objects;

public class VulpineRetribution implements ModInitializer {
	public static final String MODID = "vulpineretribution";
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	@Override
	public void onInitialize() {
		AutoConfig.register(ModConfig.class, (config, aClass) -> new GsonConfigSerializer<>(config, aClass, new GsonBuilder().setPrettyPrinting().serializeNulls().create()));
		ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

		WebhookExecutor webhookExecutor;
		if (config.executeWebhook && config.webhookSettings != null && config.webhookSettings.url != null && config.webhookSettings.url.length() > 0) {
			webhookExecutor = switch (config.webhookSettings.type) {
				case GENERIC -> new GenericWebhookExecutor();
				case DISCORD -> new DiscordWebhookExecutor();
			};
		} else {
			webhookExecutor = null;
		}

		ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((ServerWorld world, Entity entity, LivingEntity killedEntity) -> {
			if (entity instanceof ServerPlayerEntity player && killedEntity.getType().getUntranslatedName().equals(config.targetEntity)) {
				LOGGER.atInfo().log("Player {} killed a {}", entity.getName(), killedEntity.getType().getUntranslatedName());

				if (config.destroyItems) {
					player.getInventory().clear();
				}

				smiteEntity(player);

				if (config.banPlayer) {
					banPlayer(player);
				} else if (config.kickPlayer) {
					kickPlayer(player);
				}

				if (webhookExecutor != null) {
					try {
						webhookExecutor.execute(config.webhookSettings, player.getEntityName(), killedEntity.getType().getUntranslatedName());
					} catch (IOException ex) {
						LOGGER.atError().log("Error executing " + config.webhookSettings.type + " webhook: " + ex);
					}
				}
			}
		});
	}

	private void banPlayer(ServerPlayerEntity player) {
		player.getCommandSource().getServer().getPlayerManager().getUserBanList().add(new BannedPlayerEntry(player.getGameProfile()));
		kickPlayer(player);
	}

	private void kickPlayer(ServerPlayerEntity player) {
		player.getCommandSource().getServer().getPlayerManager().remove(player);
	}

	private void smiteEntity(Entity target) {
		LightningEntity bolt = new LightningEntity(EntityType.LIGHTNING_BOLT, target.getEntityWorld());
		bolt.setCosmetic(true); // Because we explicitly kill the player (regardless of current health or armor)
		bolt.setPos(target.getX(), target.getY(), target.getZ());
		target.getCommandSource().getWorld().spawnEntity(bolt);
		target.kill();
	}
}
