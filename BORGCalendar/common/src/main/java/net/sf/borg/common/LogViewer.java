/*
 * written by google gemini
 */
package net.sf.borg.common;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LogViewer extends JFrame {
	
	private static final long serialVersionUID = -6270066756808515613L;

	private class LogRowRenderer extends DefaultTableCellRenderer {
	    private static final long serialVersionUID = -3073125084127463837L;

		@Override
	    public Component getTableCellRendererComponent(JTable table, Object value, 
	            boolean isSelected, boolean hasFocus, int row, int column) {
	        
	        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

	        // Get the log type from the first column (index 0)
	        String type = table.getValueAt(row, 0).toString();

	        if (isSelected) {
	            c.setBackground(table.getSelectionBackground());
	            c.setForeground(table.getSelectionForeground());
	        } else {
	            // Apply colors based on type
	            switch (type) {
	                case "ERROR" -> {
	                    c.setBackground(new Color(255, 210, 210)); // Soft Red
	                    c.setForeground(Color.RED.darker());
	                }
	                case "UPDATE" -> {
	                    c.setBackground(new Color(210, 255, 210)); // Soft Green
	                    c.setForeground(Color.GREEN.darker());
	                }
	                case "WARN" -> {
	                    c.setBackground(new Color(255, 245, 200)); // Soft Yellow
	                    c.setForeground(new Color(150, 100, 0));  // Dark Orange/Brown
	                }
	                case "INFO" -> {
	                    c.setBackground(Color.WHITE);
	                    c.setForeground(Color.BLACK);
	                }
	                default -> {
	                    c.setBackground(Color.WHITE);
	                    c.setForeground(Color.BLACK);
	                }
	            }
	        }
	        return c;
	    }
	}
	
	public static class LogEntry {
		public final static String INFO = "INFO";
		public final static String UPDATE = "UPDATE";
		public final static String WARN = "WARN";
		public final static String ERROR = "ERROR";
		private String type; // INFO, WARN, ERROR
	    private String message;

	    public LogEntry(String type, String message) {
	        this.type = type;
	        this.message = message;
	    }

	    public String getType() { return type; }
	    public String getMessage() { return message; }
	}

    public LogViewer(List<LogEntry> logs) {
        setTitle("Log Viewer");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //setLocationRelativeTo(null);

        // 1. Define Table Columns
        String[] columnNames = {"Type", "Description (Click for details)"};
        
        // 2. Load data into the Table Model
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;

			@Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        for (LogEntry log : logs) {
            model.addRow(new Object[]{log.getType(), log.getMessage()});
        }

        // 3. Setup JTable and JScrollPane
        JTable table = new JTable(model);
        LogRowRenderer renderer = new LogRowRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // 4. Add Selection Listener for the Detail Window
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { // Ensure event only fires once
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    showDetailWindow(logs.get(selectedRow));
                }
            }
        });
        
       
    }

    private void showDetailWindow(LogEntry log) {
        JFrame detailFrame = new JFrame("Log Detail: " + log.getType());
        detailFrame.setSize(400, 300);
        
        JTextArea textArea = new JTextArea(log.getMessage());
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        detailFrame.add(new JScrollPane(textArea));
        detailFrame.setVisible(true);
        //detailFrame.setLocationRelativeTo(this);
    }

    public static void main(String[] args) {
        // Sample Data
        List<LogEntry> sampleLogs = List.of(
            new LogEntry("INFO", "System started successfully at 09:00 AM."),
            new LogEntry("WARN", "Memory usage exceeding 80% threshold."),
            new LogEntry("ERROR", "NullPointerException in DatabaseConnector.java:42. Connection failed."),
            new LogEntry("UPDATE", "User 'Admin' logged in from IP 192.168.1.1.")
        );

        SwingUtilities.invokeLater(() -> {
            new LogViewer(sampleLogs).setVisible(true);
        });
    }
}