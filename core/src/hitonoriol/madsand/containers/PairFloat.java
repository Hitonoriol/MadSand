package hitonoriol.madsand.containers;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PairFloat { // jus a container for them float coords
	public float x, y;

	public PairFloat(float x, float y) {
		set(x, y);
	}

	public PairFloat(PairFloat pair) {
		this(pair.x, pair.y);
	}

	public PairFloat() {
		this(0, 0);
	}

	public PairFloat set(float x, float y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public PairFloat set(PairFloat pair) {
		return set(pair.x, pair.y);
	}

	public PairFloat add(float x, float y) {
		this.x += x;
		this.y += y;
		return this;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(84589, 41203).append(x).append(y).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PairFloat))
			return false;
		if (obj == this)
			return true;

		PairFloat rhs = (PairFloat) obj;
		return new EqualsBuilder().append(x, rhs.x).append(y, rhs.y).isEquals();
	}
}
