package nl.dirkkok.minecraft.fabric.vulpineretribution;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;

public final class EntityEvents {
    public static final Event<AfterTotemAttempt> AFTER_TOTEM_ATTEMPT = EventFactory.createArrayBacked(AfterTotemAttempt.class, callbacks -> (entity, damageSource, succeeded) -> {
        for (AfterTotemAttempt callback : callbacks) {
            callback.afterTotemAttempt(entity, damageSource, succeeded);
        }
    });

    @FunctionalInterface
    public interface AfterTotemAttempt {
        void afterTotemAttempt(Entity entity, DamageSource damageSource, boolean succeeded);
    }

    private EntityEvents() { }
}
