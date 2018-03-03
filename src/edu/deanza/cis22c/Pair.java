package edu.deanza.cis22c;

import java.util.Objects;

public class Pair<L, R> {
	public L first;
	public R second;

	public Pair(L x, R y) {
		first = x;
		second = y;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Pair<?, ?> pair = (Pair<?, ?>) o;
		return Objects.equals(first, pair.first) &&
		       Objects.equals(second, pair.second);
	}

	@Override
	public int hashCode() {
		return Objects.hash(first, second);
	}
}