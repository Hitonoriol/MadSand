package hitonoriol.madsand.minigames.farkle;

import hitonoriol.madsand.containers.Container;

public class Farkle {
	private int round;
	private int score, cumulativeScore;
	private DiceBin diceBin = new DiceBin();
	private FarkleHand diceHand = new FarkleHand();
	public static final int WINNING_SCORE = 5000;
	public static final int MIN_ROUND_SCORE = 300;
	public static final int MAX_ROUNDS = 10;

	public void startGame() {
		round = 0;
		score = 0;
	}

	public void startRound() {
		++round;
		cumulativeScore = 0;
		diceBin.fill();
		diceHand.clear();
	}

	/* Re-shuffle leftover dice */
	private void subRound(boolean isBonusRound) {
		cumulativeScore = evaluateRound();
		diceHand.clear();

		if (!isBonusRound)
			diceBin.shuffle();

		if (isFarkle())
			cumulativeScore = 0;
	}

	public void subRound() {
		subRound(false);
	}

	public void endRound() {
		score += evaluateRound();
	}

	public void bonusRound() {
		diceBin.fill();
		if (isFarkle())
			endRound();
		subRound();
	}

	/* Farkle = no combinations available */
	public boolean isFarkle() {
		return FarkleHand.evaluate(diceBin.getDice()) == 0;
	}

	void moveDie(Container<Die> from, Container<Die> to, Die die) {
		from.remove(die);
		to.add(die);
	}

	public void selectDie(Die die) {
		moveDie(diceBin, diceHand, die);
	}

	public void deselectDie(Die die) {
		moveDie(diceHand, diceBin, die);
	}

	public int evaluateRound() {
		return diceHand.getScore() + cumulativeScore;
	}

	public boolean bonusThrowAvailable() {
		return diceBin.isEmpty() && !diceHand.hasUnusedDice();
	}

	/* Means that current round can be ended */
	public boolean isRoundValid() {
		return evaluateRound() >= MIN_ROUND_SCORE;
	}

	public boolean gameEnded() {
		return round >= MAX_ROUNDS;
	}

	public int getRoundNumber() {
		return round;
	}

	public int getScore() {
		return score;
	}

	public DiceBin getDiceBin() {
		return diceBin;
	}

	public FarkleHand getDiceHand() {
		return diceHand;
	}

	public boolean gameWon() {
		return score >= WINNING_SCORE;
	}
}
