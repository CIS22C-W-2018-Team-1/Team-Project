package edu.deanza.cis22c.w2018.team1.swing.util;

import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;

/**
 * A class for use with the ContextMenu. Could perhaps be generalized
 * further in the future.
 *
 * @param   <C>   the context type
 *
 * @author Dimitriye Danilovic
 */
public class ContextActionEvent<C> {
	private ActionEvent e;
	private Point2D pos;
	private C context;

	public ContextActionEvent(ActionEvent e, Point2D pos, C context) {
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

	public C getContext() {
		return context;
	}
}
