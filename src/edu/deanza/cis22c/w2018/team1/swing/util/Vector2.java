package edu.deanza.cis22c.w2018.team1.swing.util;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Objects;

/**
 * A useful 2d math class.
 *
 * @author Dimitriye Danilovic
 */
public class Vector2 {
	private double x, y;

	public Vector2(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Vector2(Point p) {
		this.x = p.getX();
		this.y = p.getY();
	}

	public Vector2(Point2D p) {
		this.x = p.getX();
		this.y = p.getY();
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double magnitude() {
		return Math.sqrt(x * x + y * y);
	}

	public Vector2 plus(Vector2 o) {
		return new Vector2(x + o.x, y + o.y);
	}

	public Vector2 plus(Point2D p) {
		return plus(new Vector2(p));
	}

	public Vector2 minus(Vector2 o) {
		return new Vector2(x - o.x, y - o.y);
	}

	public Vector2 minus(Point2D p) {
		return minus(new Vector2(p));
	}

	public Vector2 times(double d) {
		return new Vector2(d * x, d * y);
	}

	public Vector2 normalized() {
		return times(1 / magnitude());
	}

	public Point asPoint() {
		return new Point((int) x, (int) y);
	}

	/**
	 * Linearly interpolates from this to the
	 * parameter vector by a factor of alpha
	 *
	 * @param   o       the vector to lerp with
	 * @param   alpha   an aliasing value
	 *
	 * @return   the result vector
	 */
	public Vector2 lerp(Vector2 o, double alpha) {
		return times(1 - alpha).plus(o.times(alpha));
	}

	public Vector2 lerp(Point2D p, double alpha) {
		return lerp(new Vector2(p), alpha);
	}

	public Vector2 applyTransform(AffineTransform tx) {
		Point2D p = new Point2D.Double(x, y);
		tx.deltaTransform(p, p);
		return new Vector2(p);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Vector2 vector2 = (Vector2) o;
		return Double.compare(vector2.x, x) == 0 &&
				Double.compare(vector2.y, y) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public String toString() {
		return String.format("Vector2(%f, %f)", x, y);
	}
}
