package rh.yahia;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ResourceBundle;

public class SecurityController implements Initializable {

    private File serialFile = new File(System.getProperty("user.home") + "\\Documents\\Serial.txt");
    private File logFile = new File(System.getProperty("user.home") + "\\Documents\\EasyShop\\EasyShop log.txt");
    private File demoFile = new File(System.getProperty("user.home") + "\\Documents\\demo.txt");


    @FXML
    PasswordField activationCodeField, demoField;
    @FXML
    RadioButton saveSerialRadio;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }


    @FXML
    void activate() {
        try {
            if (activationCodeField.getText().equals("goku")) {
                saveSerialRadio.setSelected(true);
            } else if (activationCodeField.getText().equals("3602610")) {
                if (serialFile.exists()) {
                    if (serialFile.delete()) {
                        writeLogFile("SerialFile deleted");
                    }
                }

                if (saveSerialRadio.isSelected()) {
                    connectAndSaveSerial();
                }
                if (serialFile.createNewFile()) {
                    writeLogFile("Serial File created");
                }

                if (!demoField.getText().isBlank()) {
                    if (demoFile.delete()) {
                        writeLogFile("DemoFile deleted");
                    } else if (demoFile.createNewFile()) {
                        writeLogFile("DemoFile created");
                    }
                    if (demoField.getText().equals("AccessGranted")) {
                        writeDemoFile("acfda976b0a525debe111a899144b489c3683d03");
                        makeDemoFileHiddenAndNonWritable();
                    } else {
                        writeDemoFile(demoField.getText());
                        makeDemoFileHiddenAndNonWritable();
                    }
                }
                writeSerialFile();
                makeSerialFileHidden();

                Parent root;
                root = FXMLLoader.load(getClass().getResource("/rh.yahia/Counter.fxml"));
                Stage stage = new Stage();
                stage.setTitle("EasyShop-Caisse");
                stage.setScene(new Scene(root, 450, 450));
                stage.setMaximized(true);
                stage.show();
                closeCurrentStage();
            }
        } catch (IOException e) {
            writeLogFile(e.toString());
        }
    }


    private String serial() {
        String result = "";
        try {
            File file = File.createTempFile("realhowto", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new FileWriter(file);
            String vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"
                    + "Set colItems = objWMIService.ExecQuery _ \n"
                    + "   (\"Select * from Win32_BaseBoard\") \n"
                    + "For Each objItem in colItems \n"
                    + "    Wscript.Echo objItem.SerialNumber \n"
                    + "    exit for  ' do the first cpu only! \n"
                    + "Next \n";
            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
            BufferedReader input =
                    new BufferedReader
                            (new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result = result.concat(line);
            }
            input.close();
        } catch (Exception e) {
            writeLogFile(e.toString());
        }
        return result.trim();
    }


    private void closeCurrentStage() {
        Stage stage = (Stage) activationCodeField.getScene().getWindow();
        stage.close();
    }

    private String getHashedSerial() {
        try {
            String serial = serial();
            String algorithm = "SHA";

            byte[] plainText = serial.getBytes();

            MessageDigest md = MessageDigest.getInstance(algorithm);

            md.reset();
            md.update(plainText);

            byte[] encodedPassword = md.digest();

            StringBuilder sb = new StringBuilder();
            for (byte b : encodedPassword) {
                if ((b & 0xff) < 0x10) {
                    sb.append("0");
                }

                sb.append(Long.toString(b & 0xff, 16));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            writeLogFile(e.toString());
        }
        return null;
    }

    private void makeSerialFileHidden() {
        Path localPath = Paths.get(serialFile.getAbsolutePath());
        try {
            Files.setAttribute(localPath, "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
        } catch (IOException e) {
            writeLogFile(e.toString());
        }

    }


    private void makeDemoFileHiddenAndNonWritable() {
        Path localPath = Paths.get(demoFile.getAbsolutePath());
        try {
            Files.setAttribute(localPath, "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
            if (demoFile.setWritable(false) & demoFile.setReadOnly()) {
                writeLogFile("DemoFile is non-writable");
            }
        } catch (IOException e) {
            writeLogFile(e.toString());
        }

    }


    private void writeLogFile(String string) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(logFile, true));
            bufferedWriter.newLine();
            bufferedWriter.write("\n" + string);
            bufferedWriter.close();
        } catch (IOException e) {
            writeLogFile(e.toString());
        }
    }

    private void writeDemoFile(String string) {
        try {
            FileWriter fileWriter = new FileWriter(demoFile);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(string);
            bufferedWriter.close();
        } catch (IOException e) {
            writeLogFile(e.toString());
        }
    }

    private void writeSerialFile() {
        try {
            FileWriter fileWriter = new FileWriter(serialFile);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(getHashedSerial());
            printWriter.close();
        } catch (IOException e) {
            writeLogFile(e.toString());
        }
    }


    private void connectAndSaveSerial() {
        Connection conn = null;
        try {
            String url = "jdbc:sqlite:H:\\Serials.db";
            String sql = "CREATE TABLE IF NOT EXISTS SerialsAndKeys (\n" +
                    "Serial STRING UNIQUE NOT NULL);";

            conn = DriverManager.getConnection(url);
            System.out.println("conn succeeded");
            writeLogFile("conn succeeded");

            Statement st = conn.createStatement();
            st.execute(sql);
            st.close();

            String sqlInsert = "INSERT INTO SerialsAndKeys(Serial) VALUES(?)";
            PreparedStatement pstmt = conn.prepareStatement(sqlInsert);
            pstmt.setString(1, getHashedSerial());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            writeLogFile("conn failed");
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
                writeLogFile("SQLException");
            }
        }
    }

}