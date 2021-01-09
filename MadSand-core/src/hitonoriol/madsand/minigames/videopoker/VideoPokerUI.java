package hitonoriol.madsand.minigames.videopoker;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.map.MapObject;
import hitonoriol.madsand.minigames.Card;
import hitonoriol.madsand.minigames.CardGameUI;
import hitonoriol.madsand.minigames.videopoker.PokerHand.Combination;
import hitonoriol.madsand.world.World;

public class VideoPokerUI extends CardGameUI {

	static int PAD = 5;

	VideoPoker videoPoker = new VideoPoker();

	Table cards = new Table(Gui.skin);
	Table combTable = new Table(Gui.skin);
	Label combLabel = new Label("", Gui.skin);
	TextButton drawButton = new TextButton("Draw", Gui.skin);
	TextButton betButton = new TextButton("Bet", Gui.skin);
	List<Image> cardUI = new ArrayList<>(PokerHand.CARDS);

	public VideoPokerUI(MapObject object) {
		super(object);
		super.setTitle("Video Poker");
		super.skipLine();
		super.add("Combination multipliers:").row();
		super.skipLine();
		super.add(createCombTable()).align(Align.center).row();
		super.skipLine();
		super.add(betLabel).row();
		super.skipLine();
		super.add(cards).size(Card.WIDTH * (PokerHand.CARDS + 1), Card.HEIGHT).pad(PAD).row();
		super.skipLine();
		super.add(combLabel).row();
		super.skipLine();
		super.add(betButton).size(Gui.BTN_WIDTH, Gui.BTN_HEIGHT).row();
		super.add(drawButton).size(Gui.BTN_WIDTH, Gui.BTN_HEIGHT).row();
		super.addCloseButton();

		combTable.align(Align.center);
		cards.defaults().align(Align.center);
		drawButton.setVisible(false);
		setBetText(0);
		refreshCardTable();

		betButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				showBetDialog(bet -> {
					World.player.inventory.delItem(currency, bet);
					startGame(bet);
				});
			}
		});

		drawButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				endGame();
			}
		});
	}

	protected void endGame() {
		super.endGame();
		videoPoker.draw();
		createCardImages();
		refreshCardTable();
		combLabel.setText(combLabel.getText() + ": You win " + videoPoker.bank + " " + currencyName + "s!");
		World.player.addItem(currency, videoPoker.bank);
		drawButton.setVisible(false);
		betButton.setVisible(true);
	}

	private void startGame(int bet) {
		super.startGame();
		setBetText(bet);
		drawButton.setVisible(true);
		betButton.setVisible(false);
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
