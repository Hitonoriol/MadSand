package hitonoriol.madsand.containers;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Tuple<T1, T2> {
	public T1 l;
	public T2 r;

	public Tuple(T1 l, T2 r) {
		set(l, r);
	}

	public Tuple() {
		set(null, null);
	}

	public Tuple<T1, T2> set(T1 l, T2 r) {
		this.l = l;
		this.r = r;
		return this;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(211, 233).append(l).append(r).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Tuple))
			return false;
		if (obj == this)
			return true;

		@SuppressWarnings("unchecked")
		Tuple<T1, T2> rhs = (Tuple<T1, T2>) obj;
		return new EqualsBuilder().append(l, rhs.l).append(r, rhs.r).isEquals();
	}

	public static Tuple<Integer, Double> makeTuple(int key, double val) {
		return new Tuple<Integer, Double>(key, val);
	}

	public static Tuple<Integer, String> makeTuple(int key, String val) {
		return new Tuple<Integer, String>(key, val);
	}
}
