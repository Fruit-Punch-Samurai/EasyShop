package rh.yahia;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class Controller implements Initializable {

    private ObservableList<Product> prArrayList = FXCollections.observableArrayList();
    private ObservableList<Achat> ventesList = FXCollections.observableArrayList();
    private ObservableList<Achat> achatsList = FXCollections.observableArrayList();

    //Files variables/////////////////////////
    private String fileDirectory = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "EasyShop";
    private File productsDirectory = new File(fileDirectory);
    private File productsFile = new File(fileDirectory + "\\listN1.txt");
    private File ventesHistoriqueFile = new File(System.getProperty("user.home") + "\\Documents\\EasyShop\\Historique des ventes.txt");
    private File ventesFile = new File(System.getProperty("user.home") + "\\Documents\\EasyShop\\ventes.txt");
    private File achatsHistoriqueFile = new File(System.getProperty("user.home") + "\\Documents\\EasyShop\\Historique des achats.txt");
    private File achatsFile = new File(System.getProperty("user.home") + "\\Documents\\EasyShop\\Achats.txt");
    private File passwordFile = new File(System.getProperty("user.home") + "\\Documents\\EasyShop\\EasyShop data.txt");
    private File logFile = new File(System.getProperty("user.home") + "\\Documents\\EasyShop\\EasyShop log.txt");
    /////////////////////////////////////

    //TextFormatter code:
    private Pattern digitPattern = Pattern.compile("\\d*");
    private Pattern doubleDigitPattern = Pattern.compile("\\d*\\.?\\d*?");
    private Pattern negativeDigitPattern = Pattern.compile("-?\\d*");

    private UnaryOperator<TextFormatter.Change> digitFilter = change -> {
        if (digitPattern.matcher(change.getControlNewText()).matches()) {
            return change;
        }
        return null;
    };
    private UnaryOperator<TextFormatter.Change> doubleDigitFilter = change -> {
        if (doubleDigitPattern.matcher(change.getControlNewText()).matches()) {
            return change;
        }
        return null;
    };
    private UnaryOperator<TextFormatter.Change> negativeDigitFilter = change -> {
        if (negativeDigitPattern.matcher(change.getControlNewText()).matches()) {
            return change;
        }
        return null;
    };

    private TextFormatter priceFieldFormatter = new TextFormatter(digitFilter);
    private TextFormatter codeFieldFormatter = new TextFormatter(digitFilter);
    private TextFormatter stockFieldFormatter = new TextFormatter(negativeDigitFilter);
    private TextFormatter achatPriceFormatter = new TextFormatter(doubleDigitFilter);

    private TextFormatter modifyCodeFormatter = new TextFormatter(digitFilter);
    private TextFormatter modifyPriceFormatter = new TextFormatter(digitFilter);
    private TextFormatter modifyAchatPriceFormatter = new TextFormatter(doubleDigitFilter);
    ////////////////////////////////////


    @FXML
    TableColumn<Achat, String> profitsDateClmn, lossesDateClmn;
    @FXML
    TableColumn<Product, String> prNameClmn;
    @FXML
    TableColumn<Product, Integer> prPriceClmn, prAchatPriceClmn, prStockClmn;
    @FXML
    TableColumn<Achat, Integer> lossesCostClmn, profitsCostClmn;
    @FXML
    TableColumn<Product, Long> prCodeClmn;
    @FXML
    PasswordField newPassField, newPassFieldConfirmation;
    @FXML
    Button addBtn, saveNewPassBtn, addStockBtn, saveModifBtn;
    @FXML
    TextField nameField, priceField, codeField, achatPriceField, stockField, modifyName, modifyAchatPrice, modifyPrice, modifyCode;
    @FXML
    TableView<Product> prTable;
    @FXML
    TableView<Achat> profitsTable, lossesTable;
    @FXML
    TextArea deletedItemsField, historiqueTextArea, achatHistoriqueTextArea;
    @FXML
    ChoiceBox<String> stockChoiceBox;
    @FXML
    Label profitsTextArea, eventsLabel, lossesTextArea, montantTextArea, infoLabel;
    @FXML
    VBox vbox, editingBox, addBox, stockBox, detailedParametersBox, detailsBox, bigV;
    @FXML
    VBox newPassBox;
    @FXML
    FlowPane flowPane;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //create files, directories, historique and Arrays
        createFilesAndDirectories(productsDirectory, productsFile, ventesFile, achatsHistoriqueFile);
        getAchatsAndVentesAndProductsArrays();
        getAchatsAndVentesHistorique();
        ////////////////////////////////////////////////////////


        createTables();
        synchronize();
        writeAchatsAndVentesFileForMonth(getAchatArray(), getVentesArray());

        detailedParametersBox.getChildren().remove(newPassBox);
        detailedParametersBox.getChildren().remove(detailsBox);
        vbox.getChildren().setAll(editingBox);
        vbox.getChildren().setAll(stockBox);
        vbox.getChildren().setAll(addBox);
        shortcutsGroupingMethod();
    }


    @FXML
    void addEvent() {
        try {
            DecimalFormat df = new DecimalFormat("#.00");
            if (nameField.getText().isBlank()) {
                nameField.requestFocus();
            } else if (achatPriceField.getText().isBlank()) {
                achatPriceField.requestFocus();
            } else if (priceField.getText().isBlank()) {
                priceField.requestFocus();
            } else if (codeField.getText().isBlank() || codeField.getText().toCharArray().length != 13) {
                codeField.requestFocus();
            } else if (!compareNames(nameField.getText())) {
                eventAlert("Nom déjà pris");
                nameField.requestFocus();
            } else if (!compareCodes(codeField.getText())) {
                eventAlert("Code barre déjà pris");
                codeField.requestFocus();
            } else if (compareCodes(codeField.getText()) & compareNames(nameField.getText())) {
                nameField.setText(nameField.getText().trim());
                Product newPr = new Product(codeField.getText(), nameField.getText(), Double.parseDouble(df.format(Double.parseDouble(achatPriceField.getText())).replace(",", ".")), Integer.parseInt(priceField.getText()));
                writeLogFile(nameField.getText() + " " + achatPriceField.getText() + " " + priceField.getText() + " " + codeField.getText());
                prArrayList.add(newPr);
                synchronize();
                nameField.requestFocus();
                writePrFile();
                eventAlert("Nouveau produit: \"" + nameField.getText() + "\" vient d'être ajouté");
                prTable.requestFocus();
                prTable.getSelectionModel().select(newPr);
            }
        } catch (NumberFormatException e) {
            writeLogFile(e.toString());
        }

    }


    //deleting methods variables
    private Product deletedItem;
    private int deletedItemIndex;

    @FXML
    void deleteEvent() {
        try {
            if (!prArrayList.isEmpty()) {
                Product selectedItem = prTable.getSelectionModel().getSelectedItem();
                deletedItem = selectedItem;
                deletedItemIndex = prArrayList.indexOf(deletedItem);
                deletedItemsField.setText("N: " + selectedItem.getName() + " || PA: " + selectedItem.getPrAchatPrice() + " DA || P: " + selectedItem.getPrice() + " DA || CB: " + selectedItem.getCode() + " || S: " + selectedItem.getPrStock());
                prTable.getItems().remove(selectedItem);
                synchronize();
                writePrFile();
                eventAlert("Produit supprimé");
            }
        } catch (NullPointerException e) {
            writeLogFile(e.toString());
        }
    }


    @FXML
    void cancelDelete() {
        if (deletedItem != null) {
            if (compareCodes(deletedItem.getCode()) & compareNames(deletedItem.getName())) {
                prArrayList.add(deletedItemIndex, deletedItem);
                deletedItemsField.clear();
                deletedItem = null;
                writePrFile();
                prTable.getSelectionModel().select(deletedItemIndex);
                eventAlert("Suppression annulé");
                synchronize();
            }
        }
    }


    @FXML
    void addStock() {
        try {
            if (!stockField.getText().isBlank() & Integer.parseInt(stockField.getText()) != 0) {
                Product selectedItem = getStockChoice();
                int addedStock = Integer.parseInt(stockField.getText());
                selectedItem.setPrStock(selectedItem.getPrStock() + addedStock);

                writeAchatHistorique(selectedItem);
                writeAchatFile();
                writePrFile();
                addStockOptimisingMethods();
                synchronize();

                if (addedStock == 1) {
                    eventAlert(addedStock + " unité de \"" + selectedItem.getName() + "\" a été ajoutée");
                } else {
                    eventAlert(addedStock + " unités de \"" + selectedItem.getName() + "\" ont été ajoutées");
                }
                stockChoiceBox.requestFocus();
            }
        } catch (IndexOutOfBoundsException | NullPointerException | NumberFormatException e) {
            writeLogFile(e.toString());
        }
    }


    //modifying methods variables
    private Product selectedItem;
    private int selectedItemIndex;

    @FXML
    private void getModifyBox() {
        try {
            selectedItem = prTable.getSelectionModel().getSelectedItem();
            selectedItemIndex = prArrayList.indexOf(selectedItem);
            prArrayList.remove(selectedItem);
            modifyName.setText(selectedItem.getName());
            modifyAchatPrice.setText(String.valueOf(selectedItem.getPrAchatPrice()));
            modifyPrice.setText(String.valueOf(selectedItem.getPrice()));
            modifyCode.setText(selectedItem.getCode());
            vbox.getChildren().setAll(editingBox);
            prTable.setDisable(true);
            flowPane.setDisable(true);
        } catch (NullPointerException e) {
            writeLogFile(e.toString());
        }
    }


    @FXML
    void saveModifications() {
        DecimalFormat df = new DecimalFormat("#.00");
        if (modifyName.getText().isBlank()) {
            modifyName.requestFocus();
        } else if (modifyAchatPrice.getText().isBlank()) {
            modifyAchatPrice.requestFocus();
        } else if (modifyPrice.getText().isBlank()) {
            modifyPrice.requestFocus();
        } else if (modifyCode.getText().isBlank() || modifyCode.getText().toCharArray().length != 13) {
            modifyCode.requestFocus();
        } else if (!compareNames(modifyName.getText())) {
            eventAlert("Nom déjà pris");
        } else if (!compareCodes(modifyCode.getText())) {
            eventAlert("Code barre déjà pris");
        } else if (compareCodes(modifyCode.getText()) || compareNames(modifyName.getText())) {
            Product localProduct = new Product(modifyCode.getText(), modifyName.getText(), Double.parseDouble(df.format(Double.parseDouble(modifyAchatPrice.getText())).replace(",", ".")), Integer.parseInt(modifyPrice.getText()), selectedItem.getPrStock());
            prArrayList.add(selectedItemIndex, localProduct);
            prTable.setDisable(false);
            flowPane.setDisable(false);
            writePrFile();
            getAchatsAndVentesAndProductsArrays();
            prTable.getSelectionModel().select(selectedItemIndex);
            hideEditingBox();
            synchronize();
            eventAlert("Modification terminé");
            selectedItem = null;
        }
    }

    @FXML
    void cancelModify() {
        if (!prArrayList.contains(selectedItem)) {
            prArrayList.add(selectedItemIndex, selectedItem);
            prTable.getSelectionModel().select(selectedItemIndex);
            eventAlert("");
        }
        hideEditingBox();
    }

    @FXML
    void saveNewPass() {
        try {
            if (newPassField.getText().isBlank()) {
                newPassField.requestFocus();
            } else if (newPassFieldConfirmation.getText().isBlank()) {
                newPassFieldConfirmation.requestFocus();
            } else if (newPassFieldConfirmation.getText().equals(newPassField.getText())) {
                if (passwordFile.exists()) {
                    if (passwordFile.delete()) {
                        writeLogFile("PassFile deleted");
                    }
                }
                if (passwordFile.createNewFile()) {
                    writeLogFile("PassFile created");
                }
                hashPassword();
                makePassFileHidden();
                newPassField.clear();
                newPassFieldConfirmation.clear();
                detailedParametersBox.getChildren().remove(newPassBox);
                successAlert("Mot de passe enregistré avec succès");
            } else if (!newPassFieldConfirmation.getText().equals(newPassField.getText())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Veuillez verifier votre mot de passe");
                alert.initModality(Modality.APPLICATION_MODAL);
                alert.showAndWait();
            }
        } catch (IOException e) {
            writeLogFile(e.toString());
        }

    }


    @FXML
    void cancelNewPass() {
        newPassField.clear();
        detailedParametersBox.getChildren().remove(newPassBox);
    }


    @FXML
    void showPassModifyingBox() {
        if (!detailedParametersBox.getChildren().contains(newPassBox)) {
            detailedParametersBox.getChildren().remove(detailsBox);
            detailedParametersBox.getChildren().add(newPassBox);
        }
    }

    //parameters variables
    private final String string1 = "Cette action effacera votre historique mensuel ( l'onglet \"Historique\" ne sera pas affecté)";
    private final String string2 = "Cette action effacera tout l'historique de commerce";
    private final String string3 = "Cette action effacera la liste des produits et l'historique de commerce";

    @FXML
    void showDetailsBox1() {
        if (!detailedParametersBox.getChildren().contains(detailsBox)) {
            detailedParametersBox.getChildren().add(detailsBox);
            detailedParametersBox.getChildren().remove(newPassBox);

        }
        infoLabel.setText(string1);
    }


    @FXML
    void showDetailsBox2() {
        if (!detailedParametersBox.getChildren().contains(detailsBox)) {
            detailedParametersBox.getChildren().add(detailsBox);
            detailedParametersBox.getChildren().remove(newPassBox);
        }
        infoLabel.setText(string2);
    }


    @FXML
    void showDetailsBox3() {
        if (!detailedParametersBox.getChildren().contains(detailsBox)) {
            detailedParametersBox.getChildren().add(detailsBox);
            detailedParametersBox.getChildren().remove(newPassBox);


        }
        infoLabel.setText(string3);
    }


    @FXML
    void doParameterAction() {
        switch (infoLabel.getText()) {
            case string1:
                deleteMonthHist();
                hideParameterBox();
                successAlert("Historique mensuel effacé avec succès");
                break;
            case string2:
                deleteAllHist();
                hideParameterBox();
                successAlert("Historique effacé avec succès");
                break;
            case string3:
                reinitializer();
                hideParameterBox();
                successAlert("Réinitialisation terminé");
                break;
            default:
                writeLogFile("Error in doParameterAction method");
        }
    }

    @FXML
    void hideParameterBox() {
        detailedParametersBox.getChildren().remove(detailsBox);
    }

    private void deleteMonthHist() {
        try {
            FileWriter fileWriter = new FileWriter(achatsFile);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write("");

            FileWriter fileWriter1 = new FileWriter(ventesFile);
            BufferedWriter bufferedWriter1 = new BufferedWriter(fileWriter1);
            bufferedWriter1.write("");
            getAchatsAndVentesAndProductsArrays();
            synchronize();
        } catch (IOException e) {
            writeLogFile(e.toString());
        }
    }

    private void deleteAllHist() {
        try {
            FileWriter fileWriter = new FileWriter(ventesHistoriqueFile);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write("");

            FileWriter fileWriter1 = new FileWriter(achatsHistoriqueFile);
            BufferedWriter bufferedWriter1 = new BufferedWriter(fileWriter1);
            bufferedWriter1.write("");

            getAchatsAndVentesHistorique();
            synchronize();
        } catch (IOException e) {
            writeLogFile(e.toString());
        }
    }


    private void reinitializer() {
        try {
            FileWriter fileWriter = new FileWriter(ventesHistoriqueFile);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write("");

            FileWriter fileWriter4 = new FileWriter(achatsHistoriqueFile);
            BufferedWriter bufferedWriter4 = new BufferedWriter(fileWriter4);
            bufferedWriter4.write("");

            FileWriter fileWriter1 = new FileWriter(achatsFile);
            BufferedWriter bufferedWriter1 = new BufferedWriter(fileWriter1);
            bufferedWriter1.write("");

            FileWriter fileWriter2 = new FileWriter(ventesFile);
            BufferedWriter bufferedWriter2 = new BufferedWriter(fileWriter2);
            bufferedWriter2.write("");

            FileWriter fileWriter3 = new FileWriter(productsFile);
            BufferedWriter bufferedWriter3 = new BufferedWriter(fileWriter3);
            bufferedWriter3.write("");

            getAchatsAndVentesAndProductsArrays();
            getAchatsAndVentesHistorique();
            synchronize();

        } catch (IOException e) {
            writeLogFile(e.toString());
        }
    }


    @FXML
    private void getStockBox() {
        vbox.getChildren().setAll(stockBox);
    }

    @FXML
    private void getAddBox() {
        vbox.getChildren().setAll(addBox);
    }

    //change focus when ENTER is clicked/////////////

    //////////////////////////////////////////////

    private void createFilesAndDirectories(File directory, File textFile, File ventesFile, File achatsFile) {

        try {
            if (directory.mkdirs()) {
                writeLogFile("Directory created");
            } else {
                writeLogFile("Directory not created");
            }
            if (textFile.createNewFile() & ventesFile.createNewFile() & achatsFile.createNewFile()) {
                writeLogFile("File created");
            } else {
                writeLogFile("File not created");
            }
        } catch (IOException e) {
            writeLogFile(e.toString());
        }
    }


    private void synchronize() {
        ControllerCounter controllerCounter = new ControllerCounter();
        controllerCounter.getPrArray().setAll(prArrayList);

        ObservableList<String> localArray = FXCollections.observableArrayList();
        DecimalFormat df = new DecimalFormat("#.00");

        int i = stockChoiceBox.getSelectionModel().getSelectedIndex();
        for (Product product : prArrayList
        ) {
            localArray.add(product.getName());
        }
        stockChoiceBox.setItems(localArray);
        stockChoiceBox.getSelectionModel().select(i);

        double profits = 0.00;
        for (Achat achat : profitsTable.getItems()) {
            profits = profits + achat.getFinalProfitForTable();
        }
        if (profits == 0) {
            profitsTextArea.setText("0 DA");
        } else {
            profitsTextArea.setText(df.format(profits).replace(",", ".") + " DA");
        }


        double losses = 0.00;
        for (
                Achat achat : lossesTable.getItems()) {
            losses = losses + achat.getFinalProfitForTable();
        }

        if (losses == 0) {
            lossesTextArea.setText("0 DA");
        } else {
            lossesTextArea.setText(df.format(losses).replace(",", ".") + " DA");
        }

        double montant = profits - losses;
        if (montant == 0) {
            montantTextArea.setText("0 DA");
        } else {
            montantTextArea.setText(df.format(montant).replace(",", ".") + " DA");
        }
        if (montant > 0) {
            montantTextArea.setTextFill(Color.valueOf("#1fd340"));
        } else if (montant < 0) {
            montantTextArea.setTextFill(Color.RED);
        } else {
            montantTextArea.setTextFill(Color.BLACK);
        }
        profitsTable.refresh();
        lossesTable.refresh();
    }


    private void getAchatsAndVentesHistorique() {
        try {
            achatHistoriqueTextArea.clear();
            Scanner scanner = new Scanner(achatsHistoriqueFile, StandardCharsets.UTF_8);
            while (scanner.hasNextLine()) {
                achatHistoriqueTextArea.appendText(scanner.nextLine());
                achatHistoriqueTextArea.appendText("\n");
            }

            historiqueTextArea.clear();
            Scanner historiqueFileScanner = new Scanner(ventesHistoriqueFile, StandardCharsets.UTF_8);
            while (historiqueFileScanner.hasNext()) {
                historiqueTextArea.appendText(historiqueFileScanner.nextLine());
                historiqueTextArea.appendText("\n");
            }

        } catch (IOException e) {
            writeLogFile(e.toString());
        }
    }


    private void writeAchatFile() {
        try {
            FileWriter fileWriter = new FileWriter(achatsFile, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            String timeStamp = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
            Double localDouble = getStockChoice().getPrAchatPrice() * Integer.parseInt(stockField.getText());
            DecimalFormat df = new DecimalFormat("#.00");

            bufferedWriter.write(timeStamp + " " + df.format(localDouble).replace(",", "."));
            bufferedWriter.newLine();

            bufferedWriter.close();
        } catch (IOException e) {
            writeLogFile(e.toString());
        }
    }


    private void writeAchatsAndVentesFileForMonth(ObservableList<Achat> achatsArrayList, ObservableList<Achat> ventesArrayList) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(achatsFile), StandardCharsets.UTF_8));

            for (Achat achat : achatsArrayList
            ) {
                bufferedWriter.write(achat.getAchatDate() + " " + achat.getFinalProfitForTable());
                bufferedWriter.newLine();
            }
            bufferedWriter.close();


            BufferedWriter bufferedWriter1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ventesFile), StandardCharsets.UTF_8));

            for (Achat achat : ventesArrayList) {
                bufferedWriter1.write(achat.getAchatDate() + " " + achat.getFinalProfitForTable());
                bufferedWriter1.newLine();
            }
            bufferedWriter1.close();


        } catch (IOException e) {
            writeLogFile(e.toString());
        }

    }


    private void getAchatsAndVentesAndProductsArrays() {
        try {
            Scanner scanner = new Scanner(achatsFile, StandardCharsets.UTF_8);
            Scanner scanner2 = new Scanner(ventesFile, StandardCharsets.UTF_8);
            Scanner prFileScanner = new Scanner(productsFile, StandardCharsets.UTF_8);

            ventesList.clear();
            achatsList.clear();
            prArrayList.clear();

            while (scanner2.hasNext()) {
                Achat localAchat = new Achat(scanner2.next(), Double.parseDouble(scanner2.next()));
                if (verifyDate(localAchat).equals(new SimpleDateFormat("MM/yyyy").format(Calendar.getInstance().getTime()))) {
                    ventesList.add(localAchat);
                }
            }

            while (scanner.hasNext()) {
                Achat localAchat = new Achat(scanner.next(), Double.parseDouble(scanner.next()));

                if (verifyDate(localAchat).equals(new SimpleDateFormat("MM/yyyy").format(Calendar.getInstance().getTime()))) {
                    achatsList.add(localAchat);
                }
            }
            try {

                while (prFileScanner.hasNext()) {
                    String localCode = prFileScanner.next();
                    prFileScanner.skip(prFileScanner.nextLine());
                    String localName = prFileScanner.nextLine();
                    prArrayList.add(new Product(localCode, localName, Double.parseDouble(prFileScanner.next()), prFileScanner.nextInt(), prFileScanner.nextInt()));
                }
            } catch (NoSuchElementException e) {
                writeLogFile(e.toString());
            }

        } catch (IOException e) {
            writeLogFile(e.toString());
        }
    }


    private void createTables() {
        profitsDateClmn.setCellValueFactory(new PropertyValueFactory<>("achatDate"));
        profitsCostClmn.setCellValueFactory(new PropertyValueFactory<>("finalProfitForTable"));
        //noinspection unchecked
        profitsTable.getColumns().setAll(profitsDateClmn, profitsCostClmn);
        profitsTable.setItems(ventesList);

        lossesDateClmn.setCellValueFactory(new PropertyValueFactory<>("achatDate"));
        lossesCostClmn.setCellValueFactory(new PropertyValueFactory<>("finalProfitForTable"));
        //noinspection unchecked
        lossesTable.getColumns().setAll(lossesDateClmn, lossesCostClmn);
        lossesTable.setItems(achatsList);

        prNameClmn.setCellValueFactory(new PropertyValueFactory<>("prName"));
        prPriceClmn.setCellValueFactory(new PropertyValueFactory<>("prPrice"));
        prCodeClmn.setCellValueFactory(new PropertyValueFactory<>("prCode"));
        prAchatPriceClmn.setCellValueFactory(new PropertyValueFactory<>("prAchatPrice"));
        prStockClmn.setCellValueFactory(new PropertyValueFactory<>("prStock"));
        //noinspection unchecked
        prTable.getColumns().setAll(prNameClmn, prAchatPriceClmn, prPriceClmn, prCodeClmn, prStockClmn);
        prTable.setItems(prArrayList);

    }


    private void writePrFile() {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(productsFile), StandardCharsets.UTF_8));

            if (!prArrayList.isEmpty()) {
                for (Product product : prArrayList) {
                    bw.write(product.getCode() + "\n" + product.getName() + "\n" + product.getPrAchatPrice() + " " + product.getPrice() + " " + product.getPrStock());
                    bw.newLine();
                }
                bw.close();
            }
        } catch (IOException e) {
            writeLogFile(e.toString());
        }
    }


    private void writeAchatHistorique(Product selectedItem) {
        try {
            String timeStamp = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(achatsHistoriqueFile, true), StandardCharsets.UTF_8));

            bufferedWriter.write("Stock ajouté le: " + timeStamp + " ");
            bufferedWriter.newLine();
            bufferedWriter.write("N: " + selectedItem.getName() + " PA: " + selectedItem.getPrAchatPrice() + " DA" + " Q: " + Integer.parseInt(stockField.getText()) + " Total: " + selectedItem.getPrAchatPrice() * Integer.parseInt(stockField.getText()) + " DA" + "\n");
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (IOException e) {
            writeLogFile(e.toString());
        }

    }


    private Product getStockChoice() {
        if (!prArrayList.isEmpty()) {
            for (Product product : prArrayList) {
                if (stockChoiceBox.getSelectionModel().getSelectedItem().equals(product.getName())) {
                    return product;
                }
            }
        }
        return prArrayList.get(1);
    }

    //compare the achat or vente month to the current month...
    private String verifyDate(Achat achat) {
        String localDate = achat.getAchatDate();
        StringBuilder stringBuilder = new StringBuilder(localDate);
        stringBuilder.delete(0, 3);
        localDate = stringBuilder.toString();
        return localDate;
    }

    private ObservableList<Achat> getAchatArray() {
        return lossesTable.getItems();
    }

    private ObservableList<Achat> getVentesArray() {
        return profitsTable.getItems();
    }

    private void addStockOptimisingMethods() {
        try {
            achatHistoriqueTextArea.clear();
            Scanner scanner = new Scanner(achatsHistoriqueFile, StandardCharsets.UTF_8);
            while (scanner.hasNextLine()) {
                achatHistoriqueTextArea.appendText(scanner.nextLine());
                achatHistoriqueTextArea.appendText("\n");

                Scanner prFileScanner = new Scanner(productsFile, StandardCharsets.UTF_8);
                Scanner scanner1 = new Scanner(achatsFile, StandardCharsets.UTF_8);
                achatsList.clear();
                prArrayList.clear();

                while (scanner1.hasNext()) {
                    Achat localAchat = new Achat(scanner1.next(), Double.parseDouble(scanner1.next()));

                    if (verifyDate(localAchat).equals(new SimpleDateFormat("MM/yyyy").format(Calendar.getInstance().getTime()))) {
                        achatsList.add(localAchat);
                    }
                }
                while (prFileScanner.hasNext()) {
                    String localCode = prFileScanner.next();
                    prFileScanner.skip(prFileScanner.nextLine());
                    String localName = prFileScanner.nextLine();
                    prArrayList.add(new Product(localCode, localName, Double.parseDouble(prFileScanner.next()), prFileScanner.nextInt(), prFileScanner.nextInt()));
                }
            }
        } catch (IOException e) {
            writeLogFile(e.toString());
        }
    }

    private Boolean compareCodes(String productCode) {
        for (Product product : prArrayList) {
            if (productCode.equals(product.getCode())) {
                return false;
            }
        }
        return true;
    }

    private Boolean compareNames(String productName) {
        for (Product product : prArrayList) {
            if (productName.equals(product.getName())) {
                return false;
            }
        }
        return true;
    }

    private void hideEditingBox() {
        flowPane.setDisable(false);
        prTable.setDisable(false);
        modifyName.clear();
        modifyPrice.clear();
        modifyCode.clear();
        modifyAchatPrice.clear();
        vbox.getChildren().setAll(addBox);
    }

    private void shortcutsGroupingMethod() {

        nameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                achatPriceField.requestFocus();
            }
        });

        achatPriceField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                priceField.requestFocus();
            }
        });

        priceField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                codeField.requestFocus();
            }
        });

        codeField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                addBtn.requestFocus();
            }
        });

        modifyName.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                modifyAchatPrice.requestFocus();
            }
        });

        modifyPrice.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                modifyCode.requestFocus();
            }
        });

        modifyAchatPrice.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                modifyPrice.requestFocus();
            }
        });

        modifyCode.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                saveModifBtn.requestFocus();
            }
        });

        prTable.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.DELETE) {
                deleteEvent();
            } else if (e.getCode() == KeyCode.ENTER) {
                if (vbox.getChildren().contains(stockBox)) {
                    stockChoiceBox.requestFocus();
                } else {
                    nameField.requestFocus();
                }
            }
        });

        stockChoiceBox.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                stockField.requestFocus();
            }
        });

        stockField.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                addStockBtn.requestFocus();
            }
        });

        bigV.setOnKeyPressed(e -> eventsLabel.setText(""));

        newPassField.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                newPassFieldConfirmation.requestFocus();
            }
        });

        newPassFieldConfirmation.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                saveNewPassBtn.requestFocus();
            }
        });
        ///////////////////////////////////////////

        priceField.setTextFormatter(priceFieldFormatter);

        achatPriceField.setTextFormatter(achatPriceFormatter);

        codeField.setTextFormatter(codeFieldFormatter);

        stockField.setTextFormatter(stockFieldFormatter);

        modifyPrice.setTextFormatter(modifyPriceFormatter);

        modifyAchatPrice.setTextFormatter(modifyAchatPriceFormatter);

        modifyCode.setTextFormatter(modifyCodeFormatter);

        nameField.setTextFormatter(new TextFormatter<>((change) -> {
            change.setText(change.getText().toUpperCase());
            return change;
        }));

        modifyName.setTextFormatter(new TextFormatter<>((change) -> {
            change.setText(change.getText().toUpperCase());
            return change;
        }));
    }

    private void makePassFileHidden() {
        Path localPath = Paths.get(passwordFile.getAbsolutePath());
        try {
            Files.setAttribute(localPath, "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
        } catch (IOException e) {
            writeLogFile(e.toString());
        }

    }


    private void hashPassword() {
        try {
            if (!newPassField.getText().isBlank()) {
                String password = newPassField.getText();
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


                FileWriter fileWriter = new FileWriter(passwordFile);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(sb.toString());
                bufferedWriter.close();
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            writeLogFile(e.toString());
        }
    }


    private void writeLogFile(String string) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, true), StandardCharsets.UTF_8));
            bufferedWriter.newLine();
            bufferedWriter.write("\n" + string);
            bufferedWriter.close();
        } catch (IOException e) {
            writeLogFile(e.toString());
        }
    }


    private void successAlert(String string) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Succès");
        alert.setHeaderText(string);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }


    private void eventAlert(String string) {
        eventsLabel.setText(string);
    }

}