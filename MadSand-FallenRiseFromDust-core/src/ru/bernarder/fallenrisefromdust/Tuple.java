package ru.bernarder.fallenrisefromdust;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Tuple<T1, T2> {
	T1 l;
	T2 r;

	Tuple(T1 l, T2 r) {
		set(l, r);
	}

	Tuple<T1, T2> set(T1 l, T2 r) {
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
}
