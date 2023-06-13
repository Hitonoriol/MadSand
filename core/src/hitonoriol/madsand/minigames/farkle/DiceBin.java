package hitonoriol.madsand.minigames.farkle;

import java.util.ArrayList;
import java.util.List;

import hitonoriol.madsand.containers.Container;
import hitonoriol.madsand.util.Functional;

public class DiceBin implements Container<Die> {
	public static final int MAX_DICE = 6;
	private List<Die> dice = new ArrayList<>();

	public void shuffle() {
		dice.forEach(Die::roll);
	}

	public void fill() {
		if (!isEmpty())
			clear();

		Functional.repeat(MAX_DICE, this::addDie);
	}

	public List<Die> getDice() {
		return dice;
	}

	@Override
	public boolean add(Die die) {
		return dice.add(die);
	}

	@Override
	public boolean remove(Die die) {
		return dice.remove(die);
	}

	public boolean isEmpty() {
		return dice.isEmpty();
	}

	public void clear() {
		dice.clear();
	}

	public void addDie() {
		dice.add(new Die());
	}
}
