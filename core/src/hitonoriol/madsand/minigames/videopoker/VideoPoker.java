package hitonoriol.madsand.minigames.videopoker;

import java.util.ArrayList;

import hitonoriol.madsand.minigames.Deck;

public class VideoPoker {
	Deck deck = new Deck();
	PokerHand hand = new PokerHand();
	int bank;
	private ArrayList<Integer> heldCards = new ArrayList<>(PokerHand.CARDS);

	public void startGame(int bet) {
		deck.create();
		heldCards.clear();
		hand.clear();
		hand.drawAll(deck);
		bank = bet;
		hand.evaluate();
	}

	public void holdCard(int idx) {
		heldCards.add(idx);
	}

	public void removeHeldCard(int idx) {
		heldCards.remove(Integer.valueOf(idx));
	}

	public boolean isHeld(int idx) {
		return heldCards.contains(idx);
	}

	public void draw() {
		for (int i = 0; i < PokerHand.CARDS; ++i) {
			if (heldCards.contains(i))
				continue;
			hand.draw(deck, i);
		}
		endGame();
	}

	public void endGame() {
		hand.evaluate();
		bank *= hand.combination.multiplier;
	}
}
