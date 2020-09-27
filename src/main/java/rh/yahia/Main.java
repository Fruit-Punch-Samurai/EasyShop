package rh.yahia;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Main extends Application {

    private File serialFile = new File(System.getProperty("user.home") + "\\Documents\\Serial.txt");
    private File fileDirectory = new File(System.getProperty("user.home") + File.separator + "Documents" + File.separator + "EasyShop");
    private File logFile = new File(System.getProperty("user.home") + "\\Documents\\EasyShop\\EasyShop log.txt");
    private File demoFile = new File(System.getProperty("user.home") + "\\Documents\\demo.txt");

    @Override
    public void start(Stage primaryStage) throws Exception {
        if (logFile.delete()) {
            System.out.println("logFile deleted");
        }
        if (fileDirectory.mkdirs() & logFile.createNewFile()) {
            writeLogFile("Directory created");
        }

        if (isDemoStillWorking() & isMotherboardIDTrue()) {
            Parent root;
            try {
                root = FXMLLoader.load(getClass().getResource("/rh.yahia/Counter.fxml"));
                Stage stage = new Stage();
                stage.setTitle("EasyShop-Caisse");
                stage.setScene(new Scene(root, 450, 450));
                stage.setMaximized(true);
                stage.show();
                stage.getIcons().add(new Image(this.getClass().getResourceAsStream("/rh.yahia/EasyShop Logo.png")));
            } catch (IOException e) {
                writeLogFile(e.toString());
            }

        } else if (connectAndVerifySerial()) {
            try {
                Parent root;
                root = FXMLLoader.load(getClass().getResource("/rh.yahia/Counter.fxml"));
                Stage stage = new Stage();
                stage.setTitle("EasyShop-Caisse");
                stage.setScene(new Scene(root, 450, 450));
                stage.setMaximized(true);
                stage.show();
                stage.getIcons().add(new Image(this.getClass().getResourceAsStream("/rh.yahia/EasyShop Logo.png")));
            } catch (IOException e) {
                writeLogFile(e.toString());
            }

        } else {
            Parent root = FXMLLoader.load(getClass().getResource("/rh.yahia/SecurityMessageLayout.fxml"));
            primaryStage.setTitle("EasyShop-Activation");
            primaryStage.setScene(new Scene(root, -1, -1));
            primaryStage.show();
            primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream("/rh.yahia/EasyShop Logo.png")));
        }
    }



    public static void main(String[] args) {
        launch(args);
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


    private String getFileSerial() {
        try {
            Scanner scanner = new Scanner(serialFile);
            String fileSerial = null;
            if (scanner.hasNextLine()) {
                fileSerial = scanner.nextLine();
            }
            return fileSerial;
        } catch (IOException e) {
            writeLogFile(e.toString());
        }
        return null;
    }


    private boolean isMotherboardIDTrue() {
        String localString = getHashedSerial();
        if (localString != null) {
            return getHashedSerial().equals(getFileSerial());
        }
        return false;
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


    private boolean isDemoStillWorking() {
        try {
            if (demoFile.exists()) {
                if (demoFile.canWrite()) {
                    if (serialFile.delete()) {
                        writeLogFile("serialFile deleted");
                    }
                    return false;
                }

                Scanner scanner = new Scanner(demoFile);
                if (scanner.hasNextLine()) {
                    String localString = scanner.nextLine();

                    if (localString.equals("acfda976b0a525debe111a899144b489c3683d03")) {
                        return true;
                    }
                    SimpleDateFormat timeStamp = new SimpleDateFormat("dd/MM/yyyy");
                    Date dNow = new Date();

                    Date date = timeStamp.parse(localString);
                    Date nowDate = timeStamp.parse(timeStamp.format(dNow));

                    System.out.println("Current Date: " + nowDate + "\n" + "Demo date: " + date);


                    if (nowDate.after(date)) {
                        if (serialFile.delete()) {
                            writeLogFile("serialFile deleted");
                        }
                        return false;
                    }

                } else {
                    if (serialFile.delete()) {
                        writeLogFile("serialFile deleted");
                    }
                    return false;
                }
            } else if (!demoFile.exists()) {
                if (serialFile.delete()) {
                    writeLogFile("serialFile deleted");
                }
                return false;
            }
        } catch (Exception e) {
            writeLogFile(e.toString());
            if (serialFile.delete()) {
                writeLogFile("serialFile deleted");
            }
        }
        return true;
    }


    private boolean connectAndVerifySerial() {
        Connection conn = null;
        try {
            String url = "jdbc:sqlite:H:\\Serials.db";
            String sql = "SELECT Serial "
                    + "FROM SerialsAndKeys WHERE Serial = " + "'" + getHashedSerial() + "'";

            conn = DriverManager.getConnection(url);
            System.out.println("conn succeeded");
            writeLogFile("conn succeeded");

            PreparedStatement st = conn.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            if (rs.getString("Serial").equals(getHashedSerial())) {
                if (demoFile.delete() || serialFile.delete()) {
                    writeLogFile("Serial verified");
                }
                writeDemoFile();
                writeSerialFile();
                makeDemoFileHiddenAndNonWritable();

                return true;
            }

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
        return false;
    }

    private void writeDemoFile() {
        try {
            FileWriter fileWriter = new FileWriter(demoFile);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write("acfda976b0a525debe111a899144b489c3683d03");
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


}
