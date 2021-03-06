package edu.deanza.cis22c.w2018.team1.structure;

import java.util.Objects;

public class Pair<L, R> {
	private L left;
	private R right;

	public Pair(L left, R right) {
		this.left  = left;
		this.right = right;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Pair<?, ?> pair = (Pair<?, ?>) o;
		return Objects.equals(left, pair.left) &&
		       Objects.equals(left, pair.right);
	}

	@Override
	public int hashCode() {
		return Objects.hash(left, right);
	}

	@Override
	public String toString() {
		return "Pair(" + left + ", " + right + ')';
	}

	public L getLeft() {
		return left;
	}

	public void setLeft(L left) {
		this.left = left;
	}

	public R getRight() {
		return right;
	}

	public void setRight(R right) {
		this.right = right;
	}
}
