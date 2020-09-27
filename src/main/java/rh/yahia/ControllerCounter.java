package rh.yahia;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class ControllerCounter implements Initializable {

    private ObservableList<Product> caisseTableArrayList = FXCollections.observableArrayList();
    private static ObservableList<Product> caisseFileArrayList = FXCollections.observableArrayList();
    private ObservableList<String> prListViewArray = FXCollections.observableArrayList();

    private File prFile = new File(System.getProperty("user.home") + "\\Documents\\EasyShop\\listN1.txt");
    private File ventesHistorique = new File(System.getProperty("user.home") + "\\Documents\\EasyShop\\Historique des ventes.txt");
    private File ventesFile = new File(System.getProperty("user.home") + "\\Documents\\EasyShop\\Ventes.txt");
    private File achatsFile = new File(System.getProperty("user.home") + "\\Documents\\EasyShop\\Achats.txt");
    private File passwordFile = new File(System.getProperty("user.home") + "\\Documents\\EasyShop\\EasyShop data.txt");
    private File logFile = new File(System.getProperty("user.home") + "\\Documents\\EasyShop\\EasyShop log.txt");


    //TextFormatter code//////////////////
    private Pattern digitPattern = Pattern.compile("\\d*");
    private UnaryOperator<TextFormatter.Change> filter = change -> {
        if (digitPattern.matcher(change.getControlNewText()).matches()) {
            return change;
        }
        return null;
    };
    private TextFormatter codeFieldFormatter = new TextFormatter(filter);
    private TextFormatter changeFieldFormatter = new TextFormatter(filter);
    private TextFormatter nonListeFieldFormatter = new TextFormatter(filter);
    //////////////////////////////////////////

    @FXML
    Button searchBtn;
    @FXML
    ToggleButton settingsToggleBtn;
    @FXML
    TableView<Product> caisseTable;
    @FXML
    TableColumn<Product, String> prNameClmn;
    @FXML
    ListView<String> prListView;
    @FXML
    TableColumn<Product, Integer> prPriceClmn;
    @FXML
    TableColumn<Product, Long> prQuantityClmn;
    @FXML
    TextField listViewSearchTextField, nonListeField, keepTheChangeTextField;
    @FXML
    PasswordField passTextField;
    @FXML
    Label montantTextArea, montantTextAreaN2, changeTextArea, detailsLabel;
    @FXML
    HBox bigHBox;
    @FXML
    Group passwordGroup;
    @FXML
    Accordion accordion;
    @FXML
    TitledPane detailsPane, searchPane;
    @FXML
    RadioButton codeRadio, nameRadio;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        //Create files////////////////////////////////////
        try {
            if (ventesHistorique.createNewFile() & ventesFile.createNewFile() & achatsFile.createNewFile()) {
                writeLogFile("Files created");
            } else {
                writeLogFile("Files not created");
            }


        } catch (IOException e) {
            writeLogFile(e.toString());
        }

        /////////////////////////////////////////////////

        //Read data from the products file//////////////////
        getPrArrayFromFile();
        ///////////////////////////////////////////////////

        prNameClmn.setCellValueFactory(new PropertyValueFactory<>("prName"));
        prPriceClmn.setCellValueFactory(new PropertyValueFactory<>("prPrice"));
        prQuantityClmn.setCellValueFactory(new PropertyValueFactory<>("prQuantity"));
        //noinspection unchecked
        caisseTable.getColumns().setAll(prNameClmn, prPriceClmn, prQuantityClmn);
        caisseTable.setItems(caisseTableArrayList);

        refresh();

        shortcutsGroupingMethod();

        accordion.setExpandedPane(searchPane);

    }


    @FXML
    void writeDetails() {
        try {
            writeDetails(caisseTable.getSelectionModel().getSelectedItem());
        } catch (NullPointerException e) {
            writeLogFile(e.toString());
        }
    }


    @FXML
    void deleteEvent() {
        try {
            Product selectedItem = caisseTable.getSelectionModel().getSelectedItem();
            selectedItem.setPrQuantity(1);
            caisseTableArrayList.remove(selectedItem);
            calculate();
        } catch (NullPointerException e) {
            writeLogFile(e.toString());
        }
    }


    @FXML
    void saveAchatEvent() {
        if (!caisseTableArrayList.isEmpty() & Integer.parseInt(montantTextArea.getText()) != 0) {
            writeHistorique();
            calculateStock();
            writeVente();
            caisseTableArrayList.clear();
            for (Product product : caisseFileArrayList) {
                product.setPrQuantity(1);
            }
        }
    }


    @FXML
    void askForPassword() {
        if (settingsToggleBtn.isSelected()) {
            passwordGroup.setVisible(true);
            passTextField.requestFocus();
        } else {
            passwordGroup.setVisible(false);
            passTextField.clear();
        }
    }


    @FXML
    void verifyPassword() {
        try {
            String password = "12345678912340";
            try {
                if (passwordFile.createNewFile()) {
                    writeLogFile("PassFile created");
                    Path localPath = Paths.get(passwordFile.getAbsolutePath());
                    Files.setAttribute(localPath, "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
                }
            } catch (IOException e) {
                writeLogFile(e.toString());
            }
            Scanner scanner = new Scanner(passwordFile, StandardCharsets.UTF_8);
            if (!passwordFile.exists() || !scanner.hasNextLine()) {
                if (passTextField.getText().equals(password)) {
                    settingsToggleBtn.setSelected(false);
                    passwordGroup.setVisible(false);
                    passTextField.clear();
                    settingsEvent();
                }
            } else if (verifyHashedPassword()) {
                settingsToggleBtn.setSelected(false);
                passwordGroup.setVisible(false);
                passTextField.clear();
                settingsEvent();
            }
        } catch (IOException e) {
            writeLogFile(e.toString());

        }
    }


    @FXML
    void refresh() {
        listViewSearchTextField.clear();
        synchronizeListView();
        prListViewArray.sort(Comparator.comparing(String::new));
    }


    @FXML
    void getChange() {
        try {
            if (!keepTheChangeTextField.getText().equals("")) {
                int theChange = Integer.parseInt(keepTheChangeTextField.getText()) - Integer.parseInt(montantTextArea.getText());
                changeTextArea.setText(theChange + " DA");
            }
        } catch (NumberFormatException e) {
            writeLogFile(e.toString());
        }
    }


    //searching methods
    @FXML
    void search() {
        if (nameRadio.isSelected() & !listViewSearchTextField.getText().isBlank()) {
            listViewSearch();
        } else if (codeRadio.isSelected() & !listViewSearchTextField.getText().isBlank()) {
            listViewCodeSearch();
        }
    }

    private void listViewSearch() {
        ObservableList<String> searchingArray = FXCollections.observableArrayList();
        for (Product product : caisseFileArrayList) {
            if (product.getName().contains(listViewSearchTextField.getText())) {
                searchingArray.add(product.getName());
                prListView.setItems(searchingArray);
            } else if (listViewSearchTextField.getText().equals("")) {
                prListView.setItems(prListViewArray);
            }
        }
    }


    private void listViewCodeSearch() {
        ObservableList<String> searchingArray = FXCollections.observableArrayList();
        for (Product product : caisseFileArrayList) {
            if (product.getCode().equals(listViewSearchTextField.getText())) {
                searchingArray.add(product.getName());
                prListView.setItems(searchingArray);
            } else if (listViewSearchTextField.getText().equals("")) {
                prListView.setItems(prListViewArray);
            }
        }
    }


    @FXML
    void addNonListe() {
        try {
            if (!nonListeField.getText().isBlank() & Integer.parseInt(nonListeField.getText()) != 0) {
                Product product = new Product("Pas de code barre", "Article non listé", 0.0, Integer.parseInt(nonListeField.getText()), 0);
                caisseTableArrayList.add(product);
                selectNewProduct(product);
                calculate();
            }
        } catch (NumberFormatException e) {
            writeLogFile(e.toString());
        }
    }


    @FXML
    private void settingsEvent() {
        try {
            Scene root = new Scene(FXMLLoader.load(getClass().getResource("/rh.yahia/sample.fxml")));
            Stage ggg = new Stage();
            ggg.setTitle("EasyShop-Gestion");
            ggg.setScene(root);
            ggg.initModality(Modality.APPLICATION_MODAL);
            ggg.show();
            ggg.getIcons().add(new Image(this.getClass().getResourceAsStream("/rh.yahia/EasyShop Logo.png")));
            ggg.setMaximized(true);
            ggg.setOnCloseRequest(e -> {
                caisseTableArrayList.clear();
                getPrArrayFromFile();
                calculate();
                refresh();
            });
        } catch (IOException e) {
            writeLogFile(e.toString());
        }
    }


    @FXML
    private void subtractQuan() {
        changeQuantity(-1);
    }

    @FXML
    void addQuantity() {
        changeQuantity(+1);
        calculate();
    }


    private void calculate() {
        int finalPrice = 0;
        for (Product product : caisseTableArrayList) {
            finalPrice = finalPrice + (product.getPrice() * product.getPrQuantity());
        }
        montantTextArea.setText(String.valueOf(finalPrice));
        if (Integer.parseInt(montantTextArea.getText()) > 0) {
            montantTextArea.setTextFill(Color.valueOf("#1fd340"));
            montantTextAreaN2.setTextFill(Color.valueOf("#1fd340"));
        } else if (Integer.parseInt(montantTextArea.getText()) < 0) {
            montantTextArea.setTextFill(Color.RED);
            montantTextAreaN2.setTextFill(Color.RED);
        } else {
            montantTextAreaN2.setTextFill(Color.BLACK);
            montantTextArea.setTextFill(Color.BLACK);

        }
    }

    @FXML
    private void fromListToTable() {
        try {
            for (Product product : caisseFileArrayList) {
                if (prListView.getSelectionModel().getSelectedItem().equals(product.getName())) {
                    if (caisseTableArrayList.contains(product)) {
                        product.setPrQuantity(product.getPrQuantity() + 1);
                        selectNewProduct(product);
                        calculate();
                        break;
                    }
                    caisseTableArrayList.add(product);
                    selectNewProduct(product);
                    calculate();
                }
            }
        } catch (NullPointerException e) {
            writeLogFile(e.toString());
        }
    }

    private void writeHistorique() {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ventesHistorique, true), StandardCharsets.UTF_8));

            String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(Calendar.getInstance().getTime());
            bufferedWriter.write("Vente effectué le: " + timeStamp);
            for (Product product : caisseTableArrayList) {
                if (product.getPrQuantity() != 0) {
                    bufferedWriter.newLine();
                    bufferedWriter.write("N: " + product.getName() + " P: " + product.getPrice() + " DA" + " Q: " + product.getPrQuantity());
                }
            }
            bufferedWriter.newLine();
            bufferedWriter.write("Total: " + montantTextArea.getText() + " DA");
            bufferedWriter.newLine();
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (IOException e) {
            writeLogFile(e.toString());
        }
    }

    private void writeVente() {
        try {
            Achat achat = new Achat(caisseTable);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ventesFile, true), StandardCharsets.UTF_8));

            bufferedWriter.write(achat.getAchatDate());
            bufferedWriter.write(" " + achat.getFinalProfit());
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (IOException e) {
            writeLogFile(e.toString());
        }
    }

    private void calculateStock() {
        try {
//            FileWriter fileWriter = new FileWriter(prFile);
//            PrintWriter printWriter = new PrintWriter(fileWriter);
            BufferedWriter printWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(prFile), StandardCharsets.UTF_8));

            for (Product product : caisseTableArrayList) {
                for (Product pr : caisseFileArrayList) {
                    if (pr.getCode().equals(product.getCode())) {
                        pr.setPrStock(pr.getPrStock() - product.getPrQuantity());
                    }
                }
            }
            for (Product pro : caisseFileArrayList) {
                printWriter.write(pro.getCode() + "\n" + pro.getName() + "\n" + pro.getPrAchatPrice() + " " + pro.getPrice() + " " + pro.getPrStock());
                printWriter.newLine();
            }
            printWriter.close();

        } catch (IOException e) {
            writeLogFile(e.toString());
        }
    }

    ObservableList getPrArray() {
        return caisseFileArrayList;
    }

    private void synchronizeListView() {
        ObservableList<String> localArray = FXCollections.observableArrayList();
        for (Product product : caisseFileArrayList) {
            localArray.add(product.getName());
        }
        prListViewArray.clear();
        prListViewArray.setAll(localArray);
        prListView.setItems(prListViewArray);
        prListView.refresh();
    }

    private void changeQuantity(int change) {
        try {
            Product selectedItem = caisseTable.getSelectionModel().getSelectedItem();
            selectedItem.setPrQuantity(selectedItem.getPrQuantity() + change);
            calculate();
        } catch (NullPointerException e) {
            writeLogFile(e.toString());
        }
    }


    private void getPrArrayFromFile() {
        try {
            Scanner prFileScanner = new Scanner(prFile, StandardCharsets.UTF_8);
            caisseFileArrayList.clear();
            while (prFileScanner.hasNext()) {
                String localCode = prFileScanner.next();
                prFileScanner.skip(prFileScanner.nextLine());
                String localName = prFileScanner.nextLine();
                caisseFileArrayList.add(new Product(localCode, localName, Double.parseDouble(prFileScanner.next()), prFileScanner.nextInt(), prFileScanner.nextInt()));
            }
        } catch (IOException | NoSuchElementException e) {
            writeLogFile(e.toString());
        }
    }

    //shortcutsGroupingMethods variables
    private String localString = "";

    private void shortcutsGroupingMethod() {
        //radioButtons methods
        ToggleGroup toggleGroup = new ToggleGroup();
        nameRadio.setToggleGroup(toggleGroup);
        nameRadio.setSelected(true);
        codeRadio.setToggleGroup(toggleGroup);

        nameRadio.setOnAction(e -> {
            if (codeRadio.isSelected()) {
                listViewSearchTextField.setTextFormatter(codeFieldFormatter);
            } else if (nameRadio.isSelected()) {
                listViewSearchTextField.clear();
                listViewSearchTextField.setTextFormatter(new TextFormatter<>((change) -> {
                    change.setText(change.getText().toUpperCase());
                    return change;
                }));
            }
        });

        codeRadio.setOnAction(e -> {
            if (codeRadio.isSelected()) {
                listViewSearchTextField.setTextFormatter(codeFieldFormatter);
                listViewSearchTextField.clear();
            } else if (nameRadio.isSelected()) {
                listViewSearchTextField.setTextFormatter(new TextFormatter<>((change) -> {
                    change.setText(change.getText().toUpperCase());
                    return change;
                }));
            }
        });
        ///////////////////

        nonListeField.setTextFormatter(nonListeFieldFormatter);
        keepTheChangeTextField.setTextFormatter(changeFieldFormatter);

        prListView.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                fromListToTable();
            }
        });

        prListView.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY & e.getClickCount() == 2) {
                fromListToTable();
            }
        });

        keepTheChangeTextField.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                getChange();
            }
        });

        bigHBox.setOnMouseClicked(e -> montantTextArea.requestFocus());


        bigHBox.setOnKeyReleased(e -> {
                    if (e.getCode() == KeyCode.F12) {
                        saveAchatEvent();
                    } else if (e.getCode() == KeyCode.F10) {
                        accordion.setExpandedPane(searchPane);
                        nonListeField.requestFocus();
                    } else if (e.getCode() == KeyCode.F11) {
                        accordion.setExpandedPane(searchPane);
                        listViewSearchTextField.requestFocus();
                    } else if (e.getCode().isDigitKey()) {
                        localString = localString + e.getText();
                    }
                    if (localString.length() > 13) {
                        StringBuilder stringBuilder = new StringBuilder(localString);
                        localString = stringBuilder.delete(0, 1).toString();
                        writeLogFile(localString);
                    }

//needs to modify textFields
                    if (!listViewSearchTextField.isFocused() & !keepTheChangeTextField.isFocused() & !nonListeField.isFocused() & e.getCode().isDigitKey()) {
                        for (Product product : caisseFileArrayList) {
                            if (localString.equals(product.getCode())) {
                                if (caisseTableArrayList.contains(product)) {
                                    product.setPrQuantity(product.getPrQuantity() + 1);
                                    localString = "";
                                    calculate();
                                    selectNewProduct(product);
                                    break;
                                }
                                caisseTableArrayList.add(product);
                                localString = "";
                                selectNewProduct(product);
                                calculate();
                                break;
                            }
                        }
                    }
                }
        );


        caisseTable.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DELETE) {
                deleteEvent();
            }
            if (e.getCode() == KeyCode.ADD) {
                changeQuantity(1);
            }
            if (e.getCode() == KeyCode.SUBTRACT) {
                changeQuantity(-1);
            }
            if (e.getCode() == KeyCode.ENTER) {
                Product localProduct = caisseTable.getSelectionModel().getSelectedItem();
                writeDetails(localProduct);
            }
            if (e.getCode() == KeyCode.BACK_SPACE) {
                accordion.setExpandedPane(searchPane);
                prListView.requestFocus();
            }
        });

        listViewSearchTextField.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER & !listViewSearchTextField.getText().matches("")) {
                search();
            }
        });

        listViewSearchTextField.setTextFormatter(new TextFormatter<>((change) -> {
            change.setText(change.getText().toUpperCase());
            return change;
        }));
    }

    private boolean verifyHashedPassword() {
        try {
            if (!passTextField.getText().isBlank()) {
                String password = passTextField.getText();
                String algorithm = "SHA";

                byte[] plainText = password.getBytes();

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

                Scanner scanner = new Scanner(passwordFile, StandardCharsets.UTF_8);
                String storedPassword = null;
                if (scanner.hasNextLine()) {
                    storedPassword = scanner.nextLine();
                }
                if (sb.toString().equals(storedPassword)) {
                    return true;
                }

            }
        } catch (NoSuchAlgorithmException | IOException e) {
            writeLogFile(e.toString());
        }
        return false;
    }

    private void writeLogFile(String string) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, true), StandardCharsets.UTF_8));

            bufferedWriter.newLine();
            bufferedWriter.write(string);
            bufferedWriter.close();
        } catch (IOException e) {
            writeLogFile(e.toString());
        }
    }

    private void selectNewProduct(Product product) {
        caisseTable.requestFocus();
        caisseTable.getSelectionModel().select(caisseTableArrayList.indexOf(product));
    }

    private void writeDetails(Product product) {
        try {
            detailsLabel.setText("Nom: " + product.getName() + "\n" + "Prix: " + product.getPrice() + " DA" + "\n" + "Code barre: " + product.getCode() + "\n" + "Stock: " + product.getPrStock() + " Unités");
            accordion.setExpandedPane(detailsPane);
        } catch (NullPointerException e) {
            writeLogFile(e.toString());
        }
    }


}