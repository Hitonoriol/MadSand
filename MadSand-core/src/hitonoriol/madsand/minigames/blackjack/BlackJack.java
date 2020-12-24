package hitonoriol.madsand.minigames.blackjack;

import hitonoriol.madsand.minigames.Deck;
import hitonoriol.madsand.minigames.Hand;

public class BlackJack {
	public static int BLACKJACK = 21;
	public static int DEALER_STOP_SCORE = 17;

	Deck deck = new Deck();

	Hand player = new BlackJackHand();
	Hand dealer = new BlackJackHand();
	GameResult gameResult;
	int bank;

	public void startGame(int bet) {
		gameResult = GameResult.None;
		bank = bet;
		deck.create();
		player.clear();
		dealer.clear();

		dealer.draw(deck);
		dealer.draw(deck).hide();

		player.draw(deck).draw(deck);
		checkScores(null);
	}

	public void play(PlayerAction action) {
		if (action == PlayerAction.Hit)
			player.draw(deck);

		boolean lastCardVisible;
		if ((lastCardVisible = !dealer.lastCard().isVisible()) && action == PlayerAction.Stand)
			dealer.setVisible();
		else if (!lastCardVisible)
			dealer.draw(deck);

		checkScores(action);
	}

	private void checkScores(PlayerAction action) {
		int playerScore = player.evaluate();
		int dealerScore = dealer.evaluate();

		if (playerScore > BLACKJACK)
			endGame(GameResult.Bust);

		else if (playerScore == BLACKJACK)
			endGame(GameResult.BlackJack);

		else if (dealerScore == BLACKJACK)
			endGame(GameResult.DealerWin);

		else if (dealerScore >= DEALER_STOP_SCORE) {
			if (playerScore > dealerScore || dealerScore > BLACKJACK)
				endGame(GameResult.Win);
			else if (playerScore == dealerScore)
				endGame(GameResult.Push);
			else if (action == PlayerAction.Stand)
				endGame(GameResult.DealerWin);
		}
	}

	private void endGame(GameResult result) {
		gameResult = result;
		if (result == GameResult.BlackJack)
			bank *= 2.5;
		else if (result == GameResult.Win)
			bank *= 2;
		else if (result != GameResult.Push)
			bank = 0;
	}

	public boolean gameEnded() {
		return gameResult != GameResult.None;
	}

	public static enum PlayerAction {
		Hit, Stand
	}

	public static enum GameResult {
		None, Win, DealerWin, Bust, Push, BlackJack;

		public boolean isWin() {
			return this == Win || this == BlackJack;
		}
	}
}
