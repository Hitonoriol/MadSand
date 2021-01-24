package hitonoriol.madsand.minigames.blackjack;

import hitonoriol.madsand.minigames.Card;
import hitonoriol.madsand.minigames.Hand;

public class BlackJackHand extends Hand {

	static int ACE_SCORE = 11;

	@Override
	public int evaluate() {
		int score = 0;
		for (Card card : super.cards) {
			if (!card.isVisible())
				continue;

			if (card.rank <= 10)
				score += card.rank;
			else if (card.rank > 10 && card.rank < Card.Rank.MAX_VALUE)
				score += 10;
			else {
				if (score + ACE_SCORE > BlackJack.BLACKJACK)
					++score;
				else
					score += ACE_SCORE;
			}
		}
		return score;
	}

}
