package hitonoriol.madsand.minigames.blackjack;

import java.util.ArrayList;
import java.util.List;

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

	public abstract int evaluate();
}
