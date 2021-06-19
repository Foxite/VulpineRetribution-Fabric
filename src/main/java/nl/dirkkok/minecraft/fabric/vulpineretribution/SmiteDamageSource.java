package nl.dirkkok.minecraft.fabric.vulpineretribution;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.text.Text;

public class SmiteDamageSource extends DamageSource {
	public static final DamageSource INSTANCE = new SmiteDamageSource("smite");

	protected SmiteDamageSource(String name) {
		super(name);
	}

	@Override
	public Text getDeathMessage(LivingEntity entity) {
		return Text.of(entity.getEntityName() + " was smitten");
	}
}
