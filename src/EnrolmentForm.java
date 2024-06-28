

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.io.*;

public class EnrolmentForm extends Frame implements ActionListener {

    private static final long serialVersionUID = 1L;
    TextField nameInput, fnameInput, phoneInput, seatNoInput, docUpload;
    CheckboxGroup genderGroup;
    Choice shiftChoice; // Dropdown for Shift
    Button submitButton, uploadButton;
    Label statusLabel;
    FileDialog fileDialog;
    File selectedFile;

    public EnrolmentForm() {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        setLayout(null);

        Label formLabel = new Label("ENROLMENT FORM");
        formLabel.setBounds(325, 20, 200, 50);
        add(formLabel);

        add(new Label("Name")).setBounds(20, 100, 150, 25);
        nameInput = new TextField(20);
        nameInput.setBounds(200, 100, 200, 25);
        add(nameInput);

        add(new Label("Father name")).setBounds(20, 150, 150, 25);
        fnameInput = new TextField(20);
        fnameInput.setBounds(200, 150, 200, 25);
        add(fnameInput);

        add(new Label("Mobile number")).setBounds(20, 200, 150, 25);
        phoneInput = new TextField(20);
        phoneInput.setBounds(200, 200, 200, 25);
        add(phoneInput);

        genderGroup = new CheckboxGroup();
        add(new Label("Gender")).setBounds(20, 250, 150, 25);
        add(new Checkbox("Male", genderGroup, true)).setBounds(200, 250, 150, 25);
        add(new Checkbox("Female", genderGroup, false)).setBounds(400, 250, 150, 25);

        add(new Label("Shift")).setBounds(20, 300, 150, 25);
        shiftChoice = new Choice();
        shiftChoice.add("Morning");
        shiftChoice.add("Afternoon");
        shiftChoice.add("Both");
        shiftChoice.setBounds(200, 300, 150, 25);
        add(shiftChoice);

        add(new Label("Seat number")).setBounds(410, 300, 100, 25);
        seatNoInput = new TextField(20);
        seatNoInput.setBounds(510, 300, 100, 25);
        add(seatNoInput);

        add(new Label("Document upload")).setBounds(20, 350, 150, 25);
        uploadButton = new Button("Upload");
        uploadButton.setBounds(200, 350, 150, 25);
        add(uploadButton);
        fileDialog = new FileDialog(this, "Select a file", FileDialog.LOAD);
        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileDialog.setVisible(true);
                String directory = fileDialog.getDirectory();
                String fileName = fileDialog.getFile();
                if (directory != null && fileName != null) {
                    selectedFile = new File(directory, fileName);

                    statusLabel.setText("Selected file: " + fileName);
                    statusLabel.setBounds(150, 400, 600, 25);
                }
            }
        });

        submitButton = new Button("Submit");
        submitButton.setBounds(325, 500, 150, 25);
        add(submitButton);

        statusLabel = new Label();
        add(statusLabel);

        submitButton.addActionListener(this);

        setTitle("Enrolment Form");
        setSize(700, 700);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent evt) {
        String name = nameInput.getText();
        String father = fnameInput.getText();
        String phone = phoneInput.getText();
        String gender = genderGroup.getSelectedCheckbox().getLabel().toLowerCase();
        String shift = shiftChoice.getSelectedItem();
        String seat = seatNoInput.getText();

        if (name.isEmpty()) {
            statusLabel.setText("Name cannot be empty.");
            statusLabel.setBounds(150, 450, 300, 25);
            return;
        }
        if (selectedFile == null) {
            statusLabel.setText("Please upload a file.");
            return;
        }
        String filePath = selectedFile.getPath().toLowerCase();
        if (!filePath.endsWith(".pdf") && !filePath.endsWith(".jpg") && !filePath.endsWith(".jpeg") && !filePath.endsWith(".png")) {
            statusLabel.setText("Please upload a PDF or image file (JPG, JPEG, PNG, PDF).");
            return;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/SSCM", "root", "_cryponymous_");

            String sql = "INSERT INTO candidate (c_name, father_name, c_mobile, gender, shift, seat_no, doc_upload, isActive) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, name);
            statement.setString(2, father);
            statement.setString(3, phone);
            statement.setString(4, gender);
            statement.setString(5, shift);
            statement.setString(6, seat);
            FileInputStream inputStream = new FileInputStream(selectedFile);
            statement.setBlob(7, inputStream);
            statement.setBoolean(8, true);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int c_id = generatedKeys.getInt(1);
                    statusLabel.setText("Row inserted successfully! Your candidate ID is: " + c_id);
                    statusLabel.setBounds(150, 450, 600, 25);
                }
            }

            inputStream.close();
            conn.close();
        } catch (ClassNotFoundException | SQLException | IOException ex) {
            ex.printStackTrace();
            statusLabel.setText("Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        new EnrolmentForm();
    }
}
