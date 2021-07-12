package hitonoriol.madsand.minigames.blackjack;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Timer;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.map.object.MapObject;
import hitonoriol.madsand.minigames.Card;
import hitonoriol.madsand.minigames.CardGameUI;
import hitonoriol.madsand.minigames.Hand;
import hitonoriol.madsand.minigames.blackjack.BlackJack.GameResult;
import hitonoriol.madsand.minigames.blackjack.BlackJack.PlayerAction;

public class BlackJackUI extends CardGameUI {
	BlackJack blackjack = new BlackJack();

	int PAD = 5;
	Label resultLabel = new Label("", Gui.skin);
	Table playerCards = new Table(Gui.skin), dealerCards = new Table(Gui.skin);
	Table buttonTable = new Table();
	TextButton hitButton = new TextButton("Hit", Gui.skin), standButton = new TextButton("Stand", Gui.skin);

	public BlackJackUI(MapObject object) {
		super(object);
		super.setTitle("BlackJack");
		super.skipLine();

		super.add("Dealer").padBottom(PAD).row();
		super.add(dealerCards).size(Card.WIDTH * 7, Card.HEIGHT).row();
		super.skipLine();
		super.skipLine();

		super.add(MadSand.player().stats.name).padBottom(PAD).row();
		super.add(betLabel).row();
		super.skipLine();
		super.add(playerCards).height(Card.HEIGHT).padBottom(PAD).row();

		buttonTable.defaults().size(Gui.BTN_WIDTH, Gui.BTN_HEIGHT).pad(5);
		buttonTable.add(hitButton);
		buttonTable.add(standButton);
		super.add(buttonTable).row();
		super.add(resultLabel).row();
		super.add(betButton).size(Gui.BTN_WIDTH, Gui.BTN_HEIGHT).row();
		super.skipLine();
		super.addCloseButton().row();
		stopGame();

		Gui.setAction(betButton, () -> startGame());
		Gui.setAction(hitButton, () -> play(PlayerAction.Hit));
		Gui.setAction(standButton, () -> Timer.instance().scheduleTask(new Timer.Task() {
			@Override
			public void run() {
				play(PlayerAction.Stand);
				buttonTable.setVisible(false);
				if (blackjack.gameEnded())
					this.cancel();
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

	protected void endGame() {
		super.endGame();
		MadSand.player().addItem(currency, blackjack.bank);
		buttonTable.setVisible(false);
		GameResult result = blackjack.gameResult;
		String resultStr = "";
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

	protected void startGame() {
		super.startGame();
		stopGame();
		super.showBetDialog(bet -> {
			if (bet > 0) {
				MadSand.player().inventory.delItem(currency, bet);
				blackjack.startGame(bet);
				setBetText(bet);
				buttonTable.setVisible(true);
				refreshHands();

				if (blackjack.gameEnded())
					endGame();
			}
		});
	}

	private void refreshHands() {
		refreshHand(blackjack.player, playerCards);
		refreshHand(blackjack.dealer, dealerCards);
	}

	private void refreshHand(Hand hand, Table table) {
		if (hand.cards.isEmpty())
			hand.cards.add(Card.nullCard);

		table.clear();
		Table cardTable = new Table();
		for (Card card : hand.cards)
			cardTable.add(new Image(card.getTexture())).size(Card.WIDTH, Card.HEIGHT).pad(5);
		table.add(cardTable).row();
		table.add("Score: " + hand.evaluate()).row();
	}
}
