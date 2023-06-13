package hitonoriol.madsand.minigames.farkle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.mutable.MutableInt;

import hitonoriol.madsand.containers.Container;
import hitonoriol.madsand.util.Functional;

public class FarkleHand implements Container<Die> {
	private List<Die> dice = new ArrayList<>();
	private int score;

	public FarkleHand set(int... diceValues) {
		for (int die : diceValues)
			add(new Die(die));
		return this;
	}

	@Override
	public boolean add(Die die) {
		boolean added = dice.add(die);
		update();
		return added;
	}

	@Override
	public boolean remove(Die die) {
		boolean removed = dice.remove(die);
		update();
		return removed;
	}

	private void update() {
		score = evaluate(dice);
	}

	private static final int singleOneScore = 100, singleFiveScore = 50;
	private static final int threePairScore = 750;
	private static final int straightScore = 1750;
	private static final int tripleScores[] = { 1000, 200, 300, 400, 500, 600 };

	public static int evaluate(List<Die> dice) {
		var score = new MutableInt(0);
		var diceMap = createDiceMap(dice);

		score.add(evaluateFullHandCombinations(diceMap));
		if (score.getValue() > 0)
			return score.getValue();

		Functional.repeat(Die.MAX_VALUE, dieValue -> {
			++dieValue;
			int dups = diceMap.getOrDefault(dieValue, 0);
			if (dups == 0)
				return;

			if (dups < 3) {
				if (dieValue == 1)
					score.add(singleOneScore * dups);
				else if (dieValue == 5)
					score.add(singleFiveScore * dups);
			} else
				score.add(tripleScores[dieValue - 1] * (dups - 2));
		});

		return score.getValue();
	}

	private static int evaluateFullHandCombinations(Map<Integer, Integer> diceMap) {
		if (diceMap.size() == DiceBin.MAX_DICE) // Each die value is present in hand exactly once -> 1, 2, 3, 4, 5, 6
			return straightScore;

		if (diceMap.size() != 3)
			return 0;

		boolean threePair = true;
		for (int dups : diceMap.values())
			if (dups != 2) {
				threePair = false;
				break;
			}
		if (threePair)
			return threePairScore;

		return 0;
	}

	private static Map<Integer, Integer> createDiceMap(List<Die> dice) {
		Map<Integer, Integer> diceMap = new HashMap<>();
		dice.forEach(die -> diceMap.put(die.getValue(), diceMap.getOrDefault(die.getValue(), 0) + 1));
		return diceMap;
	}

	public int getScore() {
		return score;
	}

	public void clear() {
		dice.clear();
		score = 0;
	}

	public List<Die> getDice() {
		return dice;
	}

	public boolean hasUnusedDice() {
		var diceMap = createDiceMap(dice);

		if (evaluateFullHandCombinations(diceMap) > 0)
			return false;

		for (int dieValue = 1; dieValue < Die.MAX_VALUE + 1; ++dieValue) {
			int dups = diceMap.getOrDefault(dieValue, 0);
			if (dups == 0)
				continue;

			if (dieValue != 1 && dieValue != 5 && dups < 3)
				return true;
		}
		return false;
	}
}
