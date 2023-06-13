package hitonoriol.madsand.minigames.blackjack;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Timer;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.GuiSkin;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.map.object.MapObject;
import hitonoriol.madsand.minigames.Card;
import hitonoriol.madsand.minigames.CardGameUI;
import hitonoriol.madsand.minigames.Hand;
import hitonoriol.madsand.minigames.blackjack.BlackJack.GameResult;
import hitonoriol.madsand.minigames.blackjack.BlackJack.PlayerAction;

public class BlackJackUI extends CardGameUI {
	BlackJack blackjack = new BlackJack();

	int PAD = 5;
	Label resultLabel = Widgets.label("");
	Table playerCards = Widgets.table(), dealerCards = Widgets.table();
	Table buttonTable = Widgets.table();
	TextButton hitButton = Widgets.button("Hit"), standButton = Widgets.button("Stand");

	private static float GAME_WIDTH = Card.WIDTH * 7, GAME_HEIGHT = Card.HEIGHT * 1.5f;

	public BlackJackUI(MapObject object) {
		super(object);
		super.setTitle("BlackJack");
		skipLine();
		super.add(betLabel).row();
		super.skipLine();

		super.add("Dealer").padBottom(PAD).row();
		super.add(dealerCards).size(GAME_WIDTH, GAME_HEIGHT).row();
		super.skipLine().padBottom(PAD);

		super.add("Your hand").padBottom(PAD).row();
		super.add(playerCards).size(GAME_WIDTH, GAME_HEIGHT).padBottom(PAD).row();

		playerCards.setBackground(GuiSkin.darkBackground());
		dealerCards.setBackground(GuiSkin.darkBackground());

		buttonTable.defaults().size(Gui.BTN_WIDTH, Gui.BTN_HEIGHT).pad(5);
		buttonTable.add(hitButton);
		buttonTable.add(standButton);
		super.add(buttonTable).row();
		super.add(resultLabel).row();
		skipLine();
		super.add(betButton).size(Gui.BTN_WIDTH, Gui.BTN_HEIGHT).row();
		super.skipLine();
		super.addCloseButton().row();
		stopGame();

		Gui.setAction(betButton, this::startGame);
		Gui.setAction(hitButton, () -> play(PlayerAction.Hit));
		Gui.setAction(standButton, () -> Timer.instance().scheduleTask(new Timer.Task() {
			@Override
			public void run() {
				play(PlayerAction.Stand);
				buttonTable.setVisible(false);
				if (blackjack.gameEnded())
					cancel();
			}
		}, 0, 0.85f));
	}

	public BlackJackUI() {
		this(null);
	}

	private void play(PlayerAction action) {
		blackjack.play(action);
		refreshHands();
		if (blackjack.gameEnded())
			endGame();
	}

	@Override
	protected void endGame() {
		super.endGame();
		MadSand.player().addItem(currency, blackjack.bank);
		buttonTable.setVisible(false);
		var result = blackjack.gameResult;
		var resultStr = "";
		if (result == GameResult.BlackJack || result == GameResult.Bust)
			resultStr += result.name() + ": ";

		if (result.isWin())
			resultStr += "You win " + blackjack.bank + " " + currencyName + "s!";
		else if (result == GameResult.Push)
			resultStr = "Draw game!";
		else
			resultStr = "You lose!";

		resultLabel.setText(resultStr);
	}

	private void stopGame() {
		buttonTable.setVisible(false);
		setBetText(0);
		blackjack.player.clear();
		blackjack.dealer.clear();
		refreshHands();
		resultLabel.setText("");
	}

	@Override
	protected void startGame() {
		super.startGame();
		stopGame();
		super.showBetDialog(bet -> {
			if (bet > 0) {
				MadSand.player().inventory.delItem(currency, bet);
				blackjack.startGame(bet);
				buttonTable.setVisible(true);
				refreshHands();

				if (blackjack.gameEnded())
					endGame();
			}
		});
	}

	@Override
	protected void noMoney() {
		super.noMoney();
		closeButton.setVisible(true);
	}

	private void refreshHands() {
		refreshHand(blackjack.player, playerCards);
		refreshHand(blackjack.dealer, dealerCards);
	}

	private void refreshHand(Hand hand, Table table) {
		if (hand.cards.isEmpty())
			hand.cards.add(Card.nullCard);

		table.clear();
		var cardTable = Widgets.table();
		for (Card card : hand.cards)
			cardTable.add(new Image(card.getTexture())).size(Card.WIDTH, Card.HEIGHT).pad(5);
		table.add(cardTable).row();
		table.add("Score: " + hand.evaluate()).row();
	}
}
