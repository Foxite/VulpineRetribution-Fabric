package nl.dirkkok.minecraft.fabric.vulpineretribution;

import com.google.gson.GsonBuilder;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import nl.dirkkok.minecraft.fabric.vulpineretribution.webhooks.DiscordWebhookExecutor;
import nl.dirkkok.minecraft.fabric.vulpineretribution.webhooks.GenericWebhookExecutor;
import nl.dirkkok.minecraft.fabric.vulpineretribution.webhooks.WebhookExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Date;

public class VulpineRetribution implements ModInitializer {
	public static final String MODID = "vulpineretribution";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	private ModConfig m_Config;

	@Override
	public void onInitialize() {
		AutoConfig.register(ModConfig.class, (config, aClass) -> new GsonConfigSerializer<>(config, aClass, new GsonBuilder().setPrettyPrinting().serializeNulls().create()));
		m_Config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

		WebhookExecutor webhookExecutor;
		if (m_Config.executeWebhook && m_Config.webhookSettings != null && m_Config.webhookSettings.url != null && m_Config.webhookSettings.url.length() > 0) {
			webhookExecutor = switch (m_Config.webhookSettings.type) {
				case GENERIC -> new GenericWebhookExecutor();
				case DISCORD -> new DiscordWebhookExecutor();
			};
		} else {
			webhookExecutor = null;
		}

		EntityEvents.AFTER_TOTEM_ATTEMPT.register((Entity killedEntity, DamageSource source, boolean succeeded) -> {
			if (!(m_Config.discountTotem && succeeded) && killedEntity.getType().getUntranslatedName().equals(m_Config.targetEntity)) {
				Entity perp = null;
				boolean definitive = true;
				if (source.getAttacker() instanceof ServerPlayerEntity player_) {
					perp = player_;
				} else if (m_Config.aggressiveBlaming) {
					perp = (ServerPlayerEntity) killedEntity.getWorld().getClosestPlayer(killedEntity, m_Config.maxBlamingDistance);
					definitive = false;
				} else {
					perp = source.getAttacker();
				}

				if (perp == null) {
					return;
				}

				LOGGER.atInfo().log("Player {} killed a {}", perp.getName(), killedEntity.getType().getUntranslatedName());

				if (perp instanceof ServerPlayerEntity player) {
					StringBuilder message = null;

					if (m_Config.explain) {
						message = new StringBuilder();
						if (!definitive) {
							message.append("In their rage");
						} else {
							message.append("As punishment for your crimes");
						}

						message.append(", you have been smitten by the gods");
					}

					if (m_Config.destroyItems) {
						player.getInventory().clear();
						if (message != null) {
							message.append(" and your items have been shattered");
						}
					}

					if (message != null) {
						message.append(".");

						player.sendMessage(new LiteralText(message.toString()).setStyle(Style.EMPTY.withColor(0xFF0000)), false);
					}
				}

				smiteEntity(perp);

				if (perp instanceof ServerPlayerEntity player) {
					if (m_Config.banPlayer) {
						banPlayer(player);
					} else if (m_Config.kickPlayer) {
						kickPlayer(player);
					}
				}

				if (webhookExecutor != null) {
					try {
						webhookExecutor.execute(m_Config.webhookSettings, perp.getEntityName(), killedEntity.getType().getUntranslatedName());
					} catch (IOException ex) {
						LOGGER.atError().log("Error executing " + m_Config.webhookSettings.type + " webhook: " + ex);
					}
				}
			}
		});
	}

	private void banPlayer(@NotNull ServerPlayerEntity player) {
		Date expiration;
		if (m_Config.banDurationMinutes == 0) {
			expiration = null;
		} else {
			expiration = new Date(new Date().getTime() + m_Config.banDurationMinutes * 60000L);
		}
		player.getCommandSource().getServer().getPlayerManager().getUserBanList().add(new BannedPlayerEntry(player.getGameProfile(), new Date(), "Vulpine Retribution", expiration, ">:("));
		kickPlayer(player);
	}

	private void kickPlayer(@NotNull ServerPlayerEntity player) {
		player.networkHandler.disconnect(Text.of(">:("));
	}

	private void smiteEntity(Entity target) {
		LightningEntity bolt = new LightningEntity(EntityType.LIGHTNING_BOLT, target.getEntityWorld());
		bolt.setCosmetic(true); // Because we explicitly kill the player (regardless of current health or armor)
		bolt.setPos(target.getX(), target.getY(), target.getZ());
		target.getCommandSource().getWorld().spawnEntity(bolt);
		target.kill();
	}
}
