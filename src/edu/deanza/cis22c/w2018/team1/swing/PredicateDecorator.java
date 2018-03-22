package edu.deanza.cis22c.w2018.team1.swing;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class PredicateDecorator<I, O> implements Function<I, Optional<O>> {
	private Predicate<I> predicate;
	private Optional<O> style;

	public PredicateDecorator(Predicate<I> predicate, O style) {
		this.predicate = predicate;
		this.style = Optional.of(style);
	}

	@Override
	public Optional<O> apply(I i) {
		if (predicate.test(i)) {
			return style;
		}
		return Optional.empty();
	}
}
