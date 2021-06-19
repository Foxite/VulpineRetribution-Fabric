package nl.dirkkok.minecraft.fabric.vulpineretribution;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class VulpineRetribution implements ModInitializer {
	private static final boolean DEBUG = true;

	public EntityType<?> getTargetEntityType() {
		// In order to test this mod properly I would have to kill a fox. So I just assumed that it works properly based on the fact it works in dev.
		// Snowy foxes and red foxes are the same entity, with a different Type (enum, see source code) so no need to include something like SnowyFoxEntity.
		if (DEBUG) {
			return EntityType.CHICKEN;
		} else {
			return EntityType.FOX;
		}
	}

	@Override
	public void onInitialize() {
		ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((ServerWorld world, Entity entity, LivingEntity killedEntity) -> {
			if (entity instanceof PlayerEntity && killedEntity.getType() == getTargetEntityType()) {
				smiteEntity(entity);
			}
		});
	}

	private void smiteEntity(Entity target) {
		LightningEntity bolt = new LightningEntity(EntityType.LIGHTNING_BOLT, target.getEntityWorld());
		bolt.setCosmetic(true); // Because we explicitly kill the player (regardless of current health or armor)
		bolt.setPos(target.getX(), target.getY(), target.getZ());
		target.getCommandSource().getWorld().spawnEntity(bolt);

		// Pros:
		// - Will display an appropriate death message.
		// Cons:
		// - Will not kill creative mode players or players with OP mod armor.
		// - Totems of undying will let you get away with it.
		// I could use both, but then someone with a totem would lose the totem while still being killed, which I don't want.
		//target.damage(SmiteDamageSource.INSTANCE, 9001);

		target.kill();
	}
}