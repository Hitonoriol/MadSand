package hitonoriol.madsand.minigames.blackjack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
	public static int MAX_CARDS = 52;
	private List<Card> cards = new ArrayList<>();

	public Deck() {
		create();
	}

	public void create() {
		if (cards.size() == MAX_CARDS)
			return;

		if (!cards.isEmpty())
			cards.clear();

		for (Card.Suit suit : Card.Suit.values())
			for (int rank = Card.Rank.MIN_VALUE; rank <= Card.Rank.MAX_VALUE; ++rank)
				cards.add(new Card(rank, suit));

		shuffle();
	}

	private void shuffle() {
		Collections.shuffle(cards);
	}

	public Card nextCard() {
		return cards.remove(0);
	}
}
