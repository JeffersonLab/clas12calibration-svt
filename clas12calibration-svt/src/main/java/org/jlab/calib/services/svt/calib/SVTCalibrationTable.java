package org.jlab.calib.services.svt.calib;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;

public class SVTCalibrationTable extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JTable table;
	private JCheckBox rowCheck;
	private static JTextArea output;

	public SVTCalibrationTable() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		table = new JTable(new SVTCalibrationTableModel());
		table.setPreferredScrollableViewportSize(new Dimension(500, 70));
		table.setFillsViewportHeight(true);
		table.getSelectionModel().addListSelectionListener(new RowListener());
		DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
		renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

		add(new JScrollPane(table));

		output = new JTextArea(5, 40);
		output.setEditable(false);
		add(new JScrollPane(output));
	}

	public JTable getTable() {
		return table;
	}

	public void actionPerformed(ActionEvent event) {
		output.append("actionPerformed ");
		String command = event.getActionCommand();
		if ("Row Selection" == command) {
			table.setRowSelectionAllowed(rowCheck.isSelected());
		}
		rowCheck.setSelected(table.getRowSelectionAllowed());
	}

	public void printout(String str) {
		output.append(str);
		output.append("\n");
	}

	public static void println(String str) {
		output.append(str);
		output.append("\n");
	}

	private void outputSelection() {
		output.append("Row:");
		for (int c : table.getSelectedRows()) {
			output.append(String.format(" %d", c));
		}
		output.append("\n");
	}

	private class RowListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent event) {
			if (event.getValueIsAdjusting()) {
				return;
			}
			outputSelection();
		}
	}

	class SVTCalibrationTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private String[] columnNames = { "Channel", "Sector", "Layer", "Chip", "Status", "ENC, e", "Gain, mv/fC",
				"Offset, mV", "Vt_50, mV", "Threshold, e" };
		final int ncols = 10;
		final int nrows = 128;
		private Object[][] data = new Object[nrows][ncols];

		SVTCalibrationTableModel() {
			for (int j = 1; j < ncols; ++j) {
				for (int i = 0; i < nrows; ++i) {
					data[i][j] = new String("");
					data[i][0] = new Integer(i);
				}
			}
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.length;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		@SuppressWarnings("unchecked")
		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		public void setValueAt(Object value, int row, int col) {
			data[row][col] = value;
			fireTableCellUpdated(row, col);
		}

	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	static void createAndShowTable() {
		// Disable boldface controls.
		UIManager.put("swing.boldMetal", Boolean.FALSE);

		// Create and set up the window.
		JFrame frame = new JFrame("SVT Channel Calibration Table");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create and set up the content pane.
		SVTCalibrationTable newContentPane = new SVTCalibrationTable();
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowTable();
			}
		});
	}
}
