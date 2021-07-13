package hitonoriol.madsand.minigames.videopoker;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.map.object.MapObject;
import hitonoriol.madsand.minigames.Card;
import hitonoriol.madsand.minigames.CardGameUI;
import hitonoriol.madsand.minigames.videopoker.PokerHand.Combination;

public class VideoPokerUI extends CardGameUI {

	static int PAD = 5;

	VideoPoker videoPoker = new VideoPoker();

	Table cards = new Table(Gui.skin);
	Table combTable = new Table(Gui.skin);
	Label combLabel = new Label("", Gui.skin);
	TextButton drawButton = new TextButton("Draw", Gui.skin);
	List<Image> cardUI = new ArrayList<>(PokerHand.CARDS);

	private static int GAME_WIDTH = Card.WIDTH * (PokerHand.CARDS + 1);

	public VideoPokerUI(MapObject object) {
		super(object);
		super.setTitle("Video Poker");
		super.skipLine();
		super.add(betLabel).row();
		super.skipLine();
		super.add(createCombTable()).width(GAME_WIDTH).align(Align.center).row();
		super.add(cards).size(GAME_WIDTH, Card.HEIGHT * 1.5f).pad(PAD).row();
		super.skipLine();
		super.add(combLabel).row();
		super.skipLine();
		addButton(betButton).size(Gui.BTN_WIDTH, Gui.BTN_HEIGHT).row();
		addButton(drawButton).size(Gui.BTN_WIDTH, Gui.BTN_HEIGHT).row();
		super.addCloseButton();

		combTable.align(Align.center);
		cards.setBackground(Gui.darkBackgroundSizeable);
		cards.defaults().align(Align.center);
		drawButton.setVisible(false);
		setBetText(0);
		refreshCardTable();

		Gui.setAction(betButton, () -> {
			showBetDialog(bet -> {
				MadSand.player().inventory.delItem(currency, bet);
				startGame(bet);
			});
		});

		Gui.setAction(drawButton, () -> endGame());
	}

	public VideoPokerUI() {
		this(null);
	}

	protected void endGame() {
		super.endGame();
		videoPoker.draw();
		createCardImages();
		refreshCardTable();
		combLabel.setText(combLabel.getText() + ": You win " + videoPoker.bank + " " + currencyName + "s!");
		MadSand.player().addItem(currency, videoPoker.bank);
		drawButton.setVisible(false);
	}

	private void startGame(int bet) {
		super.startGame();
		drawButton.setVisible(true);
		videoPoker.startGame(bet);
		createCardImages();

		for (Card card : videoPoker.hand.cards) {
			int idx = videoPoker.hand.cards.indexOf(card);
			cardUI.get(idx).addListener(new ClickListener(Buttons.LEFT) {
				public void clicked(InputEvent event, float x, float y) {
					if (!videoPoker.isHeld(idx))
						videoPoker.holdCard(idx);
					else
						videoPoker.removeHeldCard(idx);
					refreshCardTable();
				}
			});
		}
		refreshCardTable();
	}

	private void createCardImages() {
		cardUI.clear();
		for (Card card : videoPoker.hand.cards)
			cardUI.add(new Image(card.getTexture()));
	}

	private void refreshCardTable() {
		cards.clear();
		if (cardUI.isEmpty()) {
			cards.add(new Image(Card.nullCard.getTexture()));
			return;
		}

		for (Image cardImg : cardUI)
			cards.add(cardImg).size(Card.WIDTH, Card.HEIGHT).pad(PAD);
		cards.row();

		for (int i = 0; i < PokerHand.CARDS; ++i)
			cards.add(createHeldLbl(i)).width(Card.WIDTH).pad(PAD);

		combLabel.setText(videoPoker.hand.combination.getName());
	}

	private Table createCombTable() {
		combTable.clear();
		combTable.pad(PAD);
		combTable.add("Combination multipliers:").align(Align.center).colspan(4).row();
		Gui.skipLine(combTable);
		combTable.setBackground(Gui.darkBackgroundSizeable);
		int opposite = Combination.COMBINATIONS / 2;
		Combination combinations[] = Combination.values();
		Cell<Label> lastCell;
		for (int i = 0; i < opposite; ++i) {
			for (int j = i; j < i + opposite + 1; j += opposite) {
				combTable.add(combinations[j].getName() + ": ").width(150);
				lastCell = combTable.add(combinations[j].getWinCoefStr()).width(50);
				if (i == j)
					lastCell.padRight(35);
			}
			combTable.row();
		}
		return combTable;
	}

	private Label createHeldLbl(int cardIdx) {
		Label label = new Label(videoPoker.isHeld(cardIdx) ? "Held" : "", Gui.skin);
		label.setAlignment(Align.center);
		return label;
	}

}
