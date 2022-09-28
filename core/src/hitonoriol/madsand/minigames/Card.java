package hitonoriol.madsand.minigames;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import hitonoriol.madsand.gamecontent.Textures;

public class Card implements Comparable<Card> {
	public static Card nullCard = new Card(0, null).hide();

	public static final int WIDTH = 81, HEIGHT = 117;
	private static TextureRegion[][] cards = Textures.getTexture("gui/cards").split(WIDTH, HEIGHT);

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

	@Override
	public int compareTo(Card o) {
		return Integer.compare(this.rank, o.rank);
	}

	public TextureRegion getTexture() {
		if (!isVisible())
			return cards[Card.SUITS][0];

		return cards[suit.ordinal()][rank - Card.Rank.MIN_VALUE];

	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Card))
			return false;
		if (obj == this)
			return true;

		Card rhs = (Card) obj;
		return new EqualsBuilder().append(suit, rhs.suit).append(rank, rhs.rank).isEquals();
	}

	@Override
	public String toString() {
		return String.format("[%s(%d) of %s]",
				rank <= 10 ? rank : Rank.byValue(rank),
				rank,
				suit.name());
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

			return vals[value - 10];
		}
	}
}
