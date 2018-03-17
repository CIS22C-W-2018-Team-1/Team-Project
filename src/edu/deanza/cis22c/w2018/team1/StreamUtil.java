package edu.deanza.cis22c.w2018.team1;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamUtil {
	public static <T> Stream<T> fromIterator(Iterator<T> iter) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iter, Spliterator.ORDERED), false);
	}
}
