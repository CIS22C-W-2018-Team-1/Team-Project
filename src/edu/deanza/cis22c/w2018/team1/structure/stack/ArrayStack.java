package edu.deanza.cis22c.w2018.team1.structure.stack;import java.util.Arrays;/** * A class of stacks whose entries are stored in an array. * * @author Frank M. Carrano * @author Timothy M. Henry * @version 4.0 * UPDATED by C. Lee-Klawender * UPDATED by D. Danilovic */public class ArrayStack<T> implements StackInterface<T> {	private T[] stack;    // Array of stack entries	private int topIndex; // Index of top entry	private static final int DEFAULT_CAPACITY = 10;	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8; // Taken from ArrayList	/**	 * Constructs a new {@code ArrayStack} with the default capacity.	 */	public ArrayStack() {		this(DEFAULT_CAPACITY);	}	/**	 * Constructs a new {@code ArrayStack} with enough capacity to hold	 * at least {@code initialCapacity} elements	 * @param   initialCapacity   the desired initial capacity	 */	public ArrayStack(int initialCapacity) {		// The cast is safe because the new array contains null entries		@SuppressWarnings("unchecked")		T[] tempStack = (T[]) new Object[Math.max(initialCapacity, DEFAULT_CAPACITY)];		stack = tempStack;		topIndex = -1;	}	/**	 * Increases the capacity of this {@code ArrayStack} instance, if	 * necessary, to ensure that it can hold at least the number of elements	 * specified by the minimum capacity argument.	 *	 * @param   capacity   the desired minimum capacity	 */	public void ensureCapacity(int capacity) { // adapted from ArrayList implementation		int oldCapacity = stack.length;		if (oldCapacity - capacity > 0) { return; }		int newCapacity = oldCapacity + (oldCapacity >> 1); // newCapacity is 1.5 * oldCapacity		if (newCapacity - capacity < 0) // check for overflow			newCapacity = capacity;		if (newCapacity - MAX_ARRAY_SIZE > 0)			newCapacity = hugeCapacity(capacity);		// capacity is usually close to size, so this is a win:		stack = Arrays.copyOf(stack, newCapacity);	}	private static int hugeCapacity(int minCapacity) {		if (minCapacity < 0) // overflow			throw new OutOfMemoryError();		return (minCapacity > MAX_ARRAY_SIZE) ?				Integer.MAX_VALUE :				MAX_ARRAY_SIZE;	}	@Override	public boolean push(T newEntry) {		ensureCapacity(stack.length + 1);		stack[topIndex + 1] = newEntry;		topIndex++;		return true;	}	@Override	public T peek() {		if (isEmpty())			return null;		else			return stack[topIndex];	}	@Override	public T pop() {		if (isEmpty())			return null;		else {			T top = stack[topIndex];			stack[topIndex] = null;			topIndex--;			return top;		}	}	@Override	public boolean isEmpty() {		return topIndex == -1;	}	@Override	public int size() {		return topIndex + 1;	}} // end ArrayStack