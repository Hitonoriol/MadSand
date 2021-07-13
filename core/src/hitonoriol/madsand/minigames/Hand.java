package hitonoriol.madsand.minigames;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Hand {
	public List<Card> cards = new ArrayList<>();

	public Hand draw(Deck deck) {
		cards.add(deck.nextCard());
		return this;
	}

	public void hide() {
		lastCard().hide();
	}

	public void setVisible() {
		lastCard().setVisible();
	}

	public Card lastCard() {
		return cards.get(cards.size() - 1);
	}

	public void clear() {
		cards.clear();
	}

	public List<Card> createSortedList() {
		List<Card> sortedList = new ArrayList<>(cards);
		Collections.sort(sortedList);
		return sortedList;
	}

	public abstract int evaluate();

	public static String getCardListString(List<Card> cards) {
		return String.format("{%s}",
				cards.stream()
						.map(card -> card.toString())
						.collect(Collectors.joining(" ")));
	}

	@Override
	public String toString() {
		return getCardListString(cards);
	}
}
