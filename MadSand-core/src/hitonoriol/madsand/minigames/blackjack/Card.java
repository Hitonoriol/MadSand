package hitonoriol.madsand.minigames.blackjack;

public class Card {
	public static Card nullCard = new Card(0, null).hide();

	public int rank;
	public Suit suit;
	private boolean visible = true;

	public Card(int rank, Suit suit) {
		this.rank = rank;
		this.suit = suit;
	}

	public boolean isVisible() {
		return visible;
	}

	public Card setVisible() {
		visible = true;
		return this;
	}

	public Card hide() {
		visible = false;
		return this;
	}

	public static int SUITS = 4;

	public static enum Suit {
		HEARTS,
		DIAMONDS,
		CLUBS,
		SPADES;
	}

	public static enum Rank {
		NUMBER, // 2-10
		JACK, // 11
		QUEEN, // 12
		KING, // 13
		ACE; // 14

		public static int MIN_VALUE = 2, MAX_VALUE = 14;

		private static final Rank vals[] = Rank.values();

		public static Rank byValue(int value) {
			if (value < 11)
				return Rank.NUMBER;

			return vals[value - 11];
		}
	}
}
