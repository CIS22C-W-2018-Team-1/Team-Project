package edu.deanza.cis22c.w2018.team1.swing;

import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;

public class ContextActionEvent<E> {
	private ActionEvent e;
	private Point2D pos;
	private E context;

	public ContextActionEvent(ActionEvent e, Point2D pos, E context) {
		this.e = e;
		this.pos = pos;
		this.context = context;
	}

	public ActionEvent getActionEvent() {
		return e;
	}

	public Point2D getLocation() {
		return pos;
	}

	public E getContext() {
		return context;
	}
}
