package hitonoriol.madsand.gfx;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.math3.random.RandomDataGenerator;

import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gfx.Effects.TextureEffect;
import hitonoriol.madsand.util.Utils;

public class ConditionalEffects<T extends Item> {
	private Map<Predicate<T>, Function<T, TextureEffect>> effects = new LinkedHashMap<>(3);
	private RandomDataGenerator itemRandom = new RandomDataGenerator();
	private Predicate<T> noCondition = item -> true;

	private ConditionalEffects() {}

	public void apply(T item) {
		itemRandom.reSeed(item.hashCode());
		effects.forEach((itemPredicate, effect) -> {
			if (itemPredicate.test(item)) {
				item.applyEffects(processor -> processor.addEffect(effect.apply(item)));
				Utils.out("Woo! Applied fx to %s", item);
			}
		});
		item.applyEffects(TextureProcessor::applyEffects);
	}

	public ConditionalEffects<T> addEffect(Predicate<T> predicate, Function<T, TextureEffect> effectEntry) {
		effects.put(predicate, effectEntry);
		return this;
	}

	public ConditionalEffects<T> addEffect(Function<T, TextureEffect> effectEntry) {
		return addEffect(noCondition, effectEntry);
	}

	public RandomDataGenerator random() {
		return itemRandom;
	}

	public static <T extends Item> ConditionalEffects<T> create(Consumer<ConditionalEffects<T>> initializer) {
		var condFx = new ConditionalEffects<T>();
		initializer.accept(condFx);
		return condFx;
	}
}
