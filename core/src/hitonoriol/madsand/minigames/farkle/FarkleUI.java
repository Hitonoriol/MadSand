package hitonoriol.madsand.minigames.farkle;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.Container;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.minigames.CardGameUI;
import hitonoriol.madsand.util.TimeUtils;
import hitonoriol.madsand.util.Utils;

public class FarkleUI extends CardGameUI {
	private Farkle farkle = new Farkle();

	private static Table helpTable = createCombinationsTable();
	private Label scoreLabel = new Label("0", Gui.skin);
	private Table gameContainer = new Table();
	private Table diceContainer = new Table(Gui.skin);
	private Table binTable = new Table(Gui.skin), handTable = new Table(Gui.skin), scoreTable = new Table(Gui.skin);
	private Label roundLabel = new Label("", Gui.skin), roundScoreLabel;

	private TextButton shuffleButton = new TextButton("Shuffle Dice", Gui.skin);
	private TextButton endRoundButton = new TextButton("End Round", Gui.skin);

	private int bank;

	private final int PAD = 5;
	private final int DICE_CONT_WIDTH = 375;
	private final int SCORE_WIDTH = 150;
	private final int GAME_HEIGHT = Gui.FONT_S * (Farkle.MAX_ROUNDS + 3);

	public FarkleUI() {
		setTitle("Farkle");
		skipLine();
		Gui.setFontSize(roundLabel, Gui.FONT_L);
		Gui.setFontSize(scoreLabel, Gui.FONT_L);
		add(roundLabel).row();
		add(scoreLabel).row();
		add(new Label("(score " + Farkle.WINNING_SCORE + " to win)", Gui.skin)).row();

		gameContainer.add(scoreTable)
				.size(SCORE_WIDTH, GAME_HEIGHT)
				.padLeft(PAD).padRight(PAD);
		gameContainer.add(diceContainer)
				.size(DICE_CONT_WIDTH, Die.HEIGHT * 2 + Gui.FONT_S * 2)
				.padRight(PAD);
		diceContainer.add("Table:").row();
		diceContainer.add(binTable).size(DICE_CONT_WIDTH, Die.HEIGHT).padBottom(25).row();
		diceContainer.add("Your hand:").row();
		diceContainer.add(handTable).size(DICE_CONT_WIDTH, Die.HEIGHT).row();
		gameContainer.add(helpTable)
				.size(175, GAME_HEIGHT)
				.padRight(PAD);
		add(gameContainer).padBottom(35).row();

		clearScoreTable();
		scoreTable.align(Align.topLeft);
		scoreTable.setBackground(Gui.darkBackgroundSizeable);
		binTable.setBackground(Gui.darkBackgroundSizeable);
		handTable.setBackground(Gui.darkBackgroundSizeable);
		add(betLabel).row();
		addButton(betButton).row();
		addButton(shuffleButton).row();
		addButton(endRoundButton).row();
		addCloseButton().row();

		betLabel.setVisible(false);
		shuffleButton.setVisible(false);
		endRoundButton.setVisible(false);
		updateRoundLabel();

		Gui.setAction(betButton, () -> showBetDialog(bet -> {
			bank = bet;
			startGame();
		}));

		Gui.setAction(shuffleButton, () -> {
			farkle.subRound();
			update();
			checkForFarkle();
		});

		Gui.setAction(endRoundButton, () -> {
			if (farkle.bonusThrowAvailable()) {
				Gui.drawOkDialog("Bonus Round",
						"Cool, you created combinations with all your dice. Here's your bonus round!");
				farkle.bonusRound();
				update();
				checkForFarkle();
			} else {
				farkle.endRound();
				startRound();
			}
		});
	}

	@Override
	protected void startGame() {
		super.startGame();
		betLabel.setVisible(true);
		clearScoreTable();
		MadSand.player().delItem(currency, bank);
		shuffleButton.setVisible(true);
		setScoreLabel(0);
		farkle.startGame();
		startRound();
	}

	private void clearScoreTable() {
		scoreTable.clear();
		scoreTable.add("Round scores:").row();
	}

	@Override
	protected void endGame() {
		super.endGame();
		betLabel.setVisible(false);
		binTable.clear();
		handTable.clear();
		updateScores();
		if (farkle.gameWon()) {
			float multiplier = ((float) farkle.getScore() / (float) Farkle.WINNING_SCORE) + 1f;
			bank *= multiplier;
			Gui.drawOkDialog("You win!",
					"Congratulations! You scored " + farkle.getScore() + " points and won " + bank + " " + currencyName
							+ "!");
			MadSand.player().addItem(currency, bank);
		} else
			Gui.drawOkDialog("You Lose!",
					"You failed to score at least " + Farkle.WINNING_SCORE + " and lost " + bank + " " + currencyName
							+ ".");
		hideGameButtons();
	}

	private void startRound() {
		if (farkle.gameEnded()) {
			endGame();
			return;
		}
		farkle.startRound();
		scoreTable.add(roundScoreLabel = new Label("", Gui.skin)).row();
		update();

		checkForFarkle();
	}

	private Image createDieButton(Container<Die> moveFrom, Container<Die> moveTo, Die die) {
		Image dieButton = new Image(die.getTexture());
		Gui.setClickAction(dieButton, () -> {
			farkle.moveDie(moveFrom, moveTo, die);
			update();
		});
		return dieButton;
	}

	private void update() {
		binTable.clear();
		handTable.clear();
		DiceBin bin = farkle.getDiceBin();
		FarkleHand hand = farkle.getDiceHand();

		farkle.getDiceBin().getDice().forEach(die -> binTable.add(createDieButton(bin, hand, die)));
		farkle.getDiceHand().getDice().forEach(die -> handTable.add(createDieButton(hand, bin, die)));
		updateScores();
		if (hand.hasUnusedDice())
			hideGameButtons();
		else {
			endRoundButton.setVisible(farkle.isRoundValid());
			shuffleButton.setVisible(true);
		}

		if (bin.isEmpty())
			shuffleButton.setVisible(false);
	}

	private void updateScores() {
		setScoreLabel(farkle.getScore());
		setRoundScoreLabel(farkle.evaluateRound());
		updateRoundLabel();
	}

	private void hideGameButtons() {
		endRoundButton.setVisible(false);
		shuffleButton.setVisible(false);
	}

	private boolean checkForFarkle() {
		if (!farkle.isFarkle())
			return false;

		Gui.drawOkDialog("Farkle!", "Oops, no combinations avalilable. Better luck next time!");
		hideGameButtons();
		TimeUtils.scheduleTask(() -> startRound(), 1);
		return true;
	}

	private void updateRoundLabel() {
		roundLabel.setText("Round " + farkle.getRoundNumber() + "/" + Farkle.MAX_ROUNDS);
	}

	private void setScoreLabel(int score) {
		scoreLabel.setText(Utils.str(score));
	}

	private void setRoundScoreLabel(int roundScore) {
		roundScoreLabel.setText(farkle.getRoundNumber() + ". " + roundScore);
	}

	private static Table createHelpEntry(FarkleHand hand) {
		Table entry = new Table(Gui.skin);
		entry.align(Align.topLeft);
		hand.getDice().forEach(die -> entry.add(new Image(die.getTexture()))
				.size(Die.WIDTH / 2, Die.HEIGHT / 2)
				.align(Align.left));
		entry.add("=" + hand.getScore());
		hand.clear();
		return entry;
	}

	private static Table createCombinationsTable() {
		Table table = new Table(Gui.skin), combTable = new Table(Gui.skin);
		table.setBackground(Gui.darkBackgroundSizeable);
		table.align(Align.topLeft);
		table.add("Combinations:").row();
		combTable.defaults().align(Align.left);

		FarkleHand hand = new FarkleHand();
		Gui.skipLine(combTable);
		combTable.add("Single:").row();
		combTable.add(createHelpEntry(hand.set(1))).row();
		combTable.add(createHelpEntry(hand.set(5))).row();

		Gui.skipLine(combTable);
		combTable.add("Three of a kind:").row();
		for (int die = Die.MIN_VALUE; die <= Die.MAX_VALUE; ++die)
			combTable.add(createHelpEntry(hand.set(die, die, die))).row();

		Gui.skipLine(combTable);
		combTable.add("Three pair:").row();
		combTable.add(createHelpEntry(hand.set(1, 1, 2, 2, 3, 3))).row();

		Gui.skipLine(combTable);
		combTable.add("Straight:").row();
		combTable.add(createHelpEntry(hand.set(1, 2, 3, 4, 5, 6))).row();

		Gui.skipLine(combTable);
		combTable.add("X of a kind:").row();
		combTable.add(createHelpEntry(hand.set(5, 5, 5))).row();
		combTable.add(createHelpEntry(hand.set(5, 5, 5, 5))).row();
		combTable.add(createHelpEntry(hand.set(5, 5, 5, 5, 5))).row();
		combTable.add(createHelpEntry(hand.set(5, 5, 5, 5, 5, 5))).row();
		table.add(new AutoFocusScrollPane(combTable));
		return table;
	}
}
