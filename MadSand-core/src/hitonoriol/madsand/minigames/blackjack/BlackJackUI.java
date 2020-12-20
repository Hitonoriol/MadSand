package hitonoriol.madsand.minigames.blackjack;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Timer;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.gui.dialogs.SliderDialog;
import hitonoriol.madsand.map.MapObject;
import hitonoriol.madsand.minigames.blackjack.BlackJack.GameResult;
import hitonoriol.madsand.minigames.blackjack.BlackJack.PlayerAction;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.world.World;

public class BlackJackUI extends GameDialog {
	BlackJack blackjack = new BlackJack();
	static TextureRegion[][] cards;

	int currency = Globals.getInt(Globals.CURRENCY);
	int CARD_WIDTH = 81, CARD_HEIGHT = 117;
	int BTN_WIDTH = 150, BTN_HEIGHT = 30;
	int PAD = 5;

	MapObject object;
	Label betLabel = new Label("", Gui.skin);
	Label resultLabel = new Label("", Gui.skin);
	Table playerCards = new Table(Gui.skin), dealerCards = new Table(Gui.skin);
	Table buttonTable = new Table();
	TextButton hitButton = new TextButton("Hit", Gui.skin), standButton = new TextButton("Stand", Gui.skin);
	TextButton betButton = new TextButton("Place Bet", Gui.skin);

	private BlackJackUI(Stage stage) {
		super(stage);
		if (cards == null)
			cards = TextureRegion.split(Resources.loadTexture("misc/cards.png"), 81, 117);
	}

	public BlackJackUI(MapObject object) {
		this(Gui.overlay);
		this.object = object;
		super.setTitle("BlackJack");
		super.skipLine();

		super.add("Dealer").padBottom(PAD).row();
		super.add(dealerCards).size(CARD_WIDTH * 6, CARD_HEIGHT).row();
		super.skipLine();
		super.skipLine();

		super.add(World.player.stats.name).padBottom(PAD).row();
		super.add(betLabel).row();
		super.skipLine();
		super.add(playerCards).height(CARD_HEIGHT).padBottom(PAD).row();

		buttonTable.defaults().size(BTN_WIDTH, BTN_HEIGHT).pad(5);
		buttonTable.add(hitButton);
		buttonTable.add(standButton);
		super.add(buttonTable).row();
		super.add(resultLabel).row();
		super.add(betButton).size(BTN_WIDTH, BTN_HEIGHT).row();
		super.skipLine();
		super.addCloseButton().row();
		stopGame();

		betButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (World.player.inventory.hasItem(currency, 1))
					startGame();
				else
					Gui.drawOkDialog("You don't have any money!", Gui.overlay);
			}
		});

		hitButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				play(PlayerAction.Hit);
			}
		});

		standButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Timer.instance().scheduleTask(new Timer.Task() {
					@Override
					public void run() {
						play(PlayerAction.Stand);
						buttonTable.setVisible(false);
						if (blackjack.gameEnded()) 
							this.cancel();
					}
				}, 0, 0.85f);
			}
		});
	}

	private void play(PlayerAction action) {
		blackjack.play(action);
		refreshHands();
		if (blackjack.gameEnded())
			endGame();
	}

	private void endGame() {
		World.player.addItem(currency, blackjack.bank);
		buttonTable.setVisible(false);
		betButton.setVisible(true);
		GameResult result = blackjack.gameResult;
		String resultStr;
		if (result.isWin())
			resultStr = "You win!";
		else if (result == GameResult.Push)
			resultStr = "Draw game!";
		else
			resultStr = "You lose!";

		resultLabel.setText(result.name() + ": " + resultStr);
	}

	private void stopGame() {
		buttonTable.setVisible(false);
		betButton.setVisible(true);
		setBetText(0);
		blackjack.player.clear();
		blackjack.dealer.clear();
		refreshHands();
		resultLabel.setText("");
	}

	private void startGame() {
		stopGame();
		SliderDialog betDialog = new SliderDialog(
				World.player.inventory.getItem(currency).quantity);

		ChangeListener gameStart = new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				int bet = betDialog.getSliderValue();
				if (bet > 0) {
					World.player.inventory.delItem(currency, bet);
					blackjack.startGame(bet);
					setBetText(bet);
					buttonTable.setVisible(true);
					betButton.setVisible(false);
					refreshHands();

					if (blackjack.gameEnded())
						endGame();
				}
				betDialog.remove();
			}
		};

		betDialog.setSliderTitle("Place your bet:")
				.setOnUpdateText(ItemProp.getItemName(currency) + "s")
				.setConfirmListener(gameStart)
				.setTitle("Bet")
				.show();
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
			cardTable.add(new Image(getCardTexture(card))).size(CARD_WIDTH, CARD_HEIGHT).pad(5);
		table.add(cardTable).row();
		table.add("Score: " + hand.evaluate()).row();
	}

	public void setBetText(int bet) {
		betLabel.setText("Bet: " + bet);
	}

	public TextureRegion getCardTexture(Card card) {
		if (!card.isVisible())
			return cards[Card.SUITS][0];

		return cards[card.suit.ordinal()][card.rank - Card.Rank.MIN_VALUE];

	}
}
