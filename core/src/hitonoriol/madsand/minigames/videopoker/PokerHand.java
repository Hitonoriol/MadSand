package hitonoriol.madsand.minigames.videopoker;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.text.WordUtils;

import hitonoriol.madsand.minigames.Card;
import hitonoriol.madsand.minigames.Deck;
import hitonoriol.madsand.minigames.Hand;
import hitonoriol.madsand.util.Utils;

public class PokerHand extends Hand {
	static int CARDS = 5;
	public Combination combination = Combination.HIGH_CARD;

	private boolean isFlush() {
		for (int i = 0; i < CARDS - 1; ++i)
			if (cards.get(i).suit != cards.get(i + 1).suit)
				return false;
		return true;
	}

	private boolean isStraight() {
		var sortedCards = super.createSortedList();
		Utils.dbg("Sorted hand: %s", Hand.getCardListString(sortedCards));
		for (int i = 0; i < CARDS - 1; ++i)
			if (Math.abs(sortedCards.get(i).rank - sortedCards.get(i + 1).rank) != 0)
				return false;
		return true;
	}

	private boolean isStraightFlush() {
		return isFlush() && isStraight();
	}

	private boolean isRoyalFlush() {
		if (!isFlush())
			return false;
		for (Card card : cards)
			if (card.rank < 10)
				return false;
		return true;
	}

	private Combination getDuplicateCombination() {
		Map<Integer, Integer> dups = new HashMap<>();
		for (Card card : cards)
			dups.put(card.rank, dups.getOrDefault(card.rank, 0) + 1);

		if (dups.containsValue(3) && dups.containsValue(2))
			return Combination.FULL_HOUSE;

		if (dups.containsValue(4))
			return Combination.FOUR_OF_A_KIND;

		if (dups.containsValue(3))
			return Combination.THREE_OF_A_KIND;

		int pairs = 0;
		for (Entry<Integer, Integer> entry : dups.entrySet())
			if (entry.getValue() == 2)
				++pairs;
		if (pairs == 2)
			return Combination.TWO_PAIR;

		if (dups.containsValue(2))
			return Combination.PAIR;

		return Combination.HIGH_CARD;
	}

	@Override
	public int evaluate() {
		Utils.dbg("Evaluating poker hand...");
		var dupComb = getDuplicateCombination();
		if (isRoyalFlush())
			set(Combination.ROYAL_FLUSH);

		else if (isStraightFlush())
			set(Combination.STRAIGHT_FLUSH);

		else if (dupComb == Combination.FOUR_OF_A_KIND || dupComb == Combination.FULL_HOUSE)
			set(dupComb);

		else if (isFlush())
			set(Combination.FLUSH);

		else if (isStraight())
			set(Combination.STRAIGHT);

		else
			set(dupComb);

		Utils.dbg("Hand: %s | Combination: %s", this, combination);
		return 0;
	}

	private void set(Combination combination) {
		this.combination = combination;
	}

	public PokerHand drawAll(Deck deck) {
		if (!cards.isEmpty())
			super.clear();

		for (int i = 0; i < CARDS; ++i)
			super.draw(deck);
		return this;
	}

	public PokerHand draw(Deck deck, int pos) {
		cards.add(pos, deck.nextCard());
		cards.remove(pos + 1);
		return this;
	}

	public enum Combination {
		HIGH_CARD(0),
		PAIR(0.95f),
		TWO_PAIR(1.25f),
		THREE_OF_A_KIND(1.5f),
		STRAIGHT(2f),
		FLUSH(2.5f),
		FULL_HOUSE(3.5f),
		FOUR_OF_A_KIND(5),
		STRAIGHT_FLUSH(5),
		ROYAL_FLUSH(6);

		public static int COMBINATIONS = 10;

		float multiplier;

		Combination(float multiplier) {
			this.multiplier = multiplier;
		}

		public String getName() {
			return WordUtils.capitalizeFully(name().replace("_", " "));
		}

		public String getWinCoefStr() {
			return "x" + multiplier;
		}
	}

}
