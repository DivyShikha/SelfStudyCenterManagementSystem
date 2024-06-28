import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.awt.event.*;

public class CandidateDetailsWithFilter extends JFrame {


    private static final String DATABASE_URL = "jdbc:mysql://127.0.0.1:3306/SSCM"; // Replace with your database URL
    private static final String DATABASE_USER = "root"; // Replace with your username
    private static final String DATABASE_PASSWORD = "_cryponymous_"; // Replace with your password

    JTable table = new JTable();
    JButton allButton = new JButton(" All ");
    JButton activeCandidatesButton = new JButton(" Active ");
    JButton inactiveCandidatesButton = new JButton(" Inactive ");

    public CandidateDetailsWithFilter() {

        setTitle("Candidate Details");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        table.setModel(fetchDataFromDatabase("SELECT * FROM candidate"));


        addButtonColumns();

        JScrollPane scrollPane = new JScrollPane(table);

        allButton.addActionListener(e -> refreshTable("SELECT * FROM candidate"));
        activeCandidatesButton.addActionListener(e -> refreshTable("SELECT * FROM candidate WHERE isActive = TRUE"));
        inactiveCandidatesButton.addActionListener(e -> refreshTable("SELECT * FROM candidate WHERE isActive = FALSE"));

        JPanel panel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(allButton);
        buttonPanel.add(activeCandidatesButton);
        buttonPanel.add(inactiveCandidatesButton);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        add(panel);

        setVisible(true);
    }

    private DefaultTableModel fetchDataFromDatabase(String query) {
        DefaultTableModel tableModel = new DefaultTableModel();

        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {


            int columnCount = resultSet.getMetaData().getColumnCount();
            String[] columnNames = new String[columnCount + 2]; // +2 for the button columns
            for (int i = 1; i <= columnCount; i++) {
                columnNames[i - 1] = resultSet.getMetaData().getColumnName(i);
            }
            columnNames[columnCount] = "Action"; // Name for the action button column
            columnNames[columnCount + 1] = "Update"; // Name for the update button column
            tableModel.setColumnIdentifiers(columnNames);


            while (resultSet.next()) {
                Object[] rowData = new Object[columnCount + 2]; // +2 for the button columns
                for (int i = 0; i < columnCount; i++) {
                    if (resultSet.getMetaData().getColumnName(i + 1).equals("isActive")) {
                        rowData[i] = resultSet.getBoolean("isActive") ? "Active" : "Inactive";
                    } else {
                        rowData[i] = resultSet.getObject(i + 1);
                    }
                }
                rowData[columnCount] = resultSet.getBoolean("isActive") ? "Delete" : "Reactivate"; // Action button text
                rowData[columnCount + 1] = "Update"; // Button text for update
                tableModel.addRow(rowData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tableModel;
    }


    class ButtonRenderer extends JButton implements TableCellRenderer {

        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }


    class ButtonEditor extends DefaultCellEditor {

        private JButton button;
        private String label;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    String id = table.getValueAt(row, 0).toString();
                    if (label.equals("Delete")) {
                        setRowInactiveInDatabase(id);
                    } else if (label.equals("Reactivate")) {
                        setRowActiveInDatabase(id);
                    } else if (label.equals("Update")) {
                        openUpdateWindow(id);
                    }
                    refreshTable("SELECT * FROM candidate");
                }
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }

        private void setRowInactiveInDatabase(String id) {
            try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
                 PreparedStatement preparedStatement = connection.prepareStatement("UPDATE candidate SET isActive = FALSE WHERE c_id = ?")) {
                preparedStatement.setInt(1, Integer.parseInt(id));
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Row set to inactive successfully.");
                } else {
                    System.out.println("No row found with the given ID.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        private void setRowActiveInDatabase(String id) {
            try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
                 PreparedStatement preparedStatement = connection.prepareStatement("UPDATE candidate SET isActive = TRUE WHERE c_id = ?")) {
                preparedStatement.setInt(1, Integer.parseInt(id));
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Row reactivated successfully.");
                } else {
                    System.out.println("No row found with the given ID.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        private void openUpdateWindow(String id) {
            JFrame updateFrame = new JFrame("Update Candidate");
            updateFrame.setSize(400, 300);
            updateFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            updateFrame.setLayout(new GridLayout(0, 2));


            try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM candidate WHERE c_id = ?")) {
                preparedStatement.setInt(1, Integer.parseInt(id));
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                        if (resultSet.getMetaData().getColumnName(i).equals("c_id") || resultSet.getMetaData().getColumnName(i).equals("isActive")) {
                            continue; // Skip c_id and isActive columns
                        }
                        updateFrame.add(new JLabel(resultSet.getMetaData().getColumnName(i)));
                        JTextField textField = new JTextField(resultSet.getString(i));
                        textField.setName(resultSet.getMetaData().getColumnName(i)); // Set name for easy access later
                        updateFrame.add(textField);
                    }
                    JButton saveButton = new JButton("Save");
                    saveButton.addActionListener(e -> {

                        saveUpdatedData(id, updateFrame.getContentPane());
                        updateFrame.dispose();
                        refreshTable("SELECT * FROM candidate");
                    });
                    updateFrame.add(saveButton);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            updateFrame.setVisible(true);
        }

        private void saveUpdatedData(String id, Container contentPane) {
            Component[] components = contentPane.getComponents();
            try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
                 PreparedStatement preparedStatement = connection.prepareStatement(
                         "UPDATE candidate SET c_name=?, father_name=?, c_mobile=?, shift=?, seat_no=?, doc_upload=?, gender=? WHERE c_id=?")) {

                int parameterIndex = 1;
                for (Component component : components) {
                    if (component instanceof JTextField) {
                        JTextField textField = (JTextField) component;
                        preparedStatement.setString(parameterIndex++, textField.getText());
                    }
                }
                preparedStatement.setInt(parameterIndex, Integer.parseInt(id));
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Row updated successfully.");
                } else {
                    System.out.println("No row found with the given ID.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void refreshTable(String query) {
        table.clearSelection();
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        table.setModel(fetchDataFromDatabase(query));
        addButtonColumns();
        table.clearSelection();
    }

    private void addButtonColumns() {
        TableColumn actionButtonColumn = table.getColumnModel().getColumn(table.getColumnCount() - 2);
        actionButtonColumn.setCellRenderer(new ButtonRenderer());
        actionButtonColumn.setCellEditor(new ButtonEditor(new JCheckBox()));

        TableColumn updateButtonColumn = table.getColumnModel().getColumn(table.getColumnCount() - 1);
        updateButtonColumn.setCellRenderer(new ButtonRenderer());
        updateButtonColumn.setCellEditor(new ButtonEditor(new JCheckBox()));
    }

    public static void main(String[] args) {
        // Load MySQL JDBC driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        // Create and display the JFrame
        new CandidateDetailsWithFilter();
    }
}

