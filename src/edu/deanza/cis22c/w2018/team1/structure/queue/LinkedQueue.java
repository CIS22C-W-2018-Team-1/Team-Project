package edu.deanza.cis22c.w2018.team1.structure.queue;/** * A class that implements the ADT queue by using a chain of nodes * that has both head and tail references. * * @author Frank M. Carrano * @author Timothy M. Henry * @version 4.0 * UPDATED by C. Lee-Klawender * NOTE: the LinkedQueue class includes the Node class as an inner class */public class LinkedQueue<T> implements QueueInterface<T> {	private Node frontNode; // References node at front of queue	private Node backNode;  // References node at back of queue	private int count = 0;	public LinkedQueue() {		frontNode = null;		backNode = null;	} // end default constructor	public boolean enqueue(T newEntry) {		if (frontNode == null) {			frontNode = new Node(newEntry);			backNode = frontNode;		} else {			backNode.setNextNode(new Node(newEntry));			backNode = backNode.getNextNode();		}		++count;		return true;	} // end enqueue	public T peekFront() {		if (isEmpty())			return null;		else			return frontNode.getData();	} // end getFront	public T dequeue() {		T front = peekFront();		if (count > 0) {			frontNode = frontNode.getNextNode();			if (frontNode == null) {				backNode = null;			}			--count;		}		return front;	} // end dequeue	public boolean isEmpty() {		return frontNode == null;	} // end isEmpty	public int size() {		return count;	}	private class Node {		private T data; // Entry in queue		private Node next; // Link to next node		private Node(T dataPortion) {			data = dataPortion;			next = null;		} // end constructor		private Node(T dataPortion, Node linkPortion) {			data = dataPortion;			next = linkPortion;		} // end constructor		private T getData() {			return data;		} // end getData		private void setData(T newData) {			data = newData;		} // end setData		private Node getNextNode() {			return next;		} // end getNextNode		private void setNextNode(Node nextNode) {			next = nextNode;		} // end setNextNode	} // end Node} // end Linkedqueue