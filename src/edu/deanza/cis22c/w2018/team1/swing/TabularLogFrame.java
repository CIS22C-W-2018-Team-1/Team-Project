package edu.deanza.cis22c.w2018.team1.swing;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.StringJoiner;

/**
 * Logs data in tabular form and writes it to CSV when the user requests it.
 *
 * @author Dimitriye Danilovic
 */
public class TabularLogFrame extends JFrame {
	private String[] colNames;
	private List<String[]> logList;
	private AbstractTableModel model;

	public TabularLogFrame(String title, String[] colNames, List<String[]> logList) {
		super(title);
		this.colNames = colNames;
		this.logList = logList;

		BorderLayout layout = new BorderLayout();
		setLayout(layout);

		model = new AbstractTableModel() {
			@Override
			public int getRowCount() {
				return logList.size();
			}

			@Override
			public int getColumnCount() {
				return colNames.length;
			}

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				return logList.get(rowIndex)[columnIndex];
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return false;
			}
		};

		JTable table = new JTable(model);

		FontMetrics metrics = table.getFontMetrics(table.getFont());
		int fontHeight = metrics.getHeight();
		table.setRowHeight( (int) (fontHeight * 1.2) );

		TableColumnModel colModel = table.getColumnModel();
		for (int col = 0; col < colNames.length; col++) {
			colModel.getColumn(col).setHeaderValue(colNames[col]);
		}

		JScrollPane scrollPane = new JScrollPane(table);

		add(scrollPane, BorderLayout.CENTER);

		JPanel menuBar = new JPanel();
		JButton save = new JButton(new ImageIcon(
				TabularLogFrame.class.getResource("/edu/deanza/cis22c/w2018/team1/resource/floppy.gif")));
		save.addActionListener(this::saveListener);
		menuBar.add(save);

		add(menuBar, BorderLayout.NORTH);

		pack();
	}

	private void saveListener(ActionEvent e) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter("CSV log", "csv"));
		int ret = chooser.showSaveDialog(this);

		if (ret == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();

			try (Writer writer = new BufferedWriter(new FileWriter(file))) {
				writer.write(toCSVRow(colNames) + "\r\n");
				// CSV files by standard need a CRLF for line ending

				for (String[] line: logList) {
					writer.write(toCSVRow(line) + "\r\n");
				}
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(this, ex.getLocalizedMessage());
			}
		}
	}

	private static String toCSVRow(String[] line) {
		StringJoiner joiner = new StringJoiner(",");
		for (String field: line) {
			joiner.add(csvEscape(field));
		}
		return joiner.toString();
	}

	/**
	 * CSV requires that fields with special characters be quoted
	 * with "double quotes", and that any literal double quotes
	 * be escaped by doubling them.
	 * @param   field   the field to escape
	 * @return   the valid CSV field
	 */
	private static String csvEscape(String field) {
		return field.matches(".*[\\r\\n,\"].*")
				? '"' + field.replaceAll("\"", "\"\"") + '"'
				: field;

	}

	public void notifyDataChanged() {
		model.fireTableDataChanged();
	}
}
