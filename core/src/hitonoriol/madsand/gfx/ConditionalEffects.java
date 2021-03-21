package hitonoriol.madsand.gfx;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import hitonoriol.madsand.Utils;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gfx.Effects.TextureEffect;

public class ConditionalEffects<T extends Item> {
	private Map<Predicate<T>, TextureEffect> effects = new HashMap<>(3);
	private Random itemRandom = Utils.random;

	public ConditionalEffects(BiConsumer<Map<Predicate<T>, TextureEffect>, Random> initializer) {
		initializer.accept(effects, itemRandom);
	}

	public void apply(T item) {
		itemRandom = item.itemRandom();
		effects.forEach((itemPredicate, effect) -> {
			if (itemPredicate.test(item)) {
				item.applyEffects(processor -> processor.addEffect(effect));
				Utils.out("Woo! Applied fx " + effect + " to " + item.name);
			}
		});
		item.applyEffects(processor -> processor.applyEffects());
	}
}
