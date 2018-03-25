package edu.deanza.cis22c.w2018.team1.swing.util;

import javax.swing.event.MouseInputListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.LinkedList;
import java.util.function.BiConsumer;

public class OrderedMouseListener implements MouseInputListener {
	private LinkedList<Object> listeners;

	public OrderedMouseListener() {
		listeners = new LinkedList<>();
	}

	public void addListener(MouseInputListener mouseInputListener) {
		listeners.addFirst(mouseInputListener);
	}

	public void addListener(MouseListener listener) {
		listeners.addFirst(listener);
	}

	public void addListener(MouseMotionListener listener) {
		listeners.addFirst(listener);
	}

	public boolean removeListener(MouseInputListener mouseInputListener) {
		return listeners.remove(mouseInputListener);
	}

	public boolean removeListener(MouseListener listener) {
		return listeners.remove(listener);
	}

	public boolean removeListener(MouseMotionListener listener) {
		return listeners.remove(listener);
	}

	private void dispatchMotion(BiConsumer<MouseMotionListener, MouseEvent> c, MouseEvent e) {
		for (Object listener: listeners) {
			if (listener instanceof MouseMotionListener) {
				c.accept((MouseMotionListener) listener, e);
				if (e.isConsumed()) { break; }
			}
		}
	}

	private void dispatchButton(BiConsumer<MouseListener, MouseEvent> c, MouseEvent e) {
		for (Object listener: listeners) {
			if (listener instanceof MouseListener) {
				c.accept((MouseListener) listener, e);
				if (e.isConsumed()) { break; }
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		dispatchButton(MouseListener::mouseClicked, e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		dispatchButton(MouseListener::mousePressed, e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		dispatchButton(MouseListener::mouseReleased, e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		dispatchButton(MouseListener::mouseEntered, e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		dispatchButton(MouseListener::mouseExited, e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		dispatchMotion(MouseMotionListener::mouseDragged, e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		dispatchMotion(MouseMotionListener::mouseMoved, e);
	}
}
