package nl.dirkkok.minecraft.fabric.vulpineretribution.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import nl.dirkkok.minecraft.fabric.vulpineretribution.EntityEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "tryUseTotem", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void onTryUseTotem(DamageSource source, CallbackInfoReturnable<Boolean> ci) {
        //noinspection ConstantConditions

        EntityEvents.AFTER_TOTEM_ATTEMPT.invoker().afterTotemAttempt((LivingEntity) (Object) this, source, ci.getReturnValue());
    }
}


