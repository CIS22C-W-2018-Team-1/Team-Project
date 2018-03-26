package edu.deanza.cis22c.w2018.team1.swing;

import edu.deanza.cis22c.w2018.team1.structure.Pair;
import edu.deanza.cis22c.w2018.team1.swing.util.ContextActionEvent;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A right click menu which can vary its contents based on the
 * context and passes the context on to the triggered menu items
 *
 * @param   <C>   the type for the context
 *
 * @author Dimitriye Danilovic
 */
public class ContextMenu<C> {
	private Point mousePos = new Point();
	private C currentContext;

	private JPopupMenu popupMenu;
	private Function<Point2D, C> contextSupplier;
	private List<Pair<Predicate<C>, JMenuItem>> menuItems;

	public ContextMenu() {
		popupMenu = new JPopupMenu();
		contextSupplier = (p) -> null;
		menuItems = new ArrayList<>();
	}

	/**
	 * The mouse adapter which triggers the popup menu when appropriate,
	 * i.e. on right click on most systems.
	 */
	private MouseListener popupAdapter = new MouseAdapter() {
		private void displayRightClickMenu(MouseEvent e) {
			mousePos = e.getPoint();

			currentContext = contextSupplier.apply(mousePos);

			popupMenu.removeAll();

			menuItems.forEach((p) -> {
				if (p.getLeft().test(currentContext)) {
					popupMenu.add(p.getRight());
				}
			});

			popupMenu.show((JComponent) e.getSource(), e.getX(), e.getY());
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				displayRightClickMenu(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				displayRightClickMenu(e);
			}
		}
	};

	public MouseListener getTriggerListener() {
		return popupAdapter;
	}

	public JMenuItem addMenuItem(Predicate<C> shouldShow, Consumer<ContextActionEvent<? extends C>> action) {
		JMenuItem menuItem = new JMenuItem();

		// Wrap the ActionEvent with context data before passing to the passed listener.
		menuItem.addActionListener((e) -> action.accept(new ContextActionEvent<C>(e, this.mousePos, this.currentContext)));
		menuItems.add(new Pair<>(shouldShow, menuItem));

		return menuItem;
	}

	/**
	 * Sets the function which is used to retrieve the
	 * context based on mouse position. Note that this need not,
	 * and in fact likely should not be a pure function.
	 *
	 * @param   contextSupplier   the context supplier
	 */
	public void setContextSupplier(Function<Point2D, C> contextSupplier) {
		this.contextSupplier = contextSupplier;
	}
}
