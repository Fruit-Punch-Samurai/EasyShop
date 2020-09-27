package rh.yahia;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Achat {
    private SimpleStringProperty achatDate;
    private ObservableList<Product> achatsArray;
    private Double finalProfit;
    private SimpleDoubleProperty finalProfitForTable;

    public Achat(TableView<Product> table) {
        achatDate = new SimpleStringProperty(new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime()));
        ObservableList<Product> prArray = FXCollections.observableArrayList();
        prArray.setAll(table.getItems());
        this.achatsArray = prArray;
        finalProfit = 0.00;
        for (Product product : achatsArray) {
            finalProfit += (product.getPrice() * product.getPrQuantity());
        }
        finalProfitForTable = new SimpleDoubleProperty(finalProfit);
    }

    public Achat(String date, Integer finalPrice) {
        achatDate = new SimpleStringProperty(date);
        finalProfitForTable = new SimpleDoubleProperty(finalPrice);
    }

    public Achat(String date, Double finalPrice) {
        achatDate = new SimpleStringProperty(date);
        finalProfitForTable = new SimpleDoubleProperty(finalPrice);
    }


    public String getAchatDate() {
        return achatDate.get();
    }

    public SimpleStringProperty achatDateProperty() {
        return achatDate;
    }

    public void setAchatDate(String achatDate) {
        this.achatDate.set(achatDate);
    }

    public ObservableList<Product> getAchatsArray() {
        return achatsArray;
    }

    public void setAchatsArray(ObservableList<Product> achatsArray) {
        this.achatsArray = achatsArray;
    }

    public Double getFinalProfit() {
        return finalProfit;
    }

    public void setFinalProfit(Double finalProfit) {
        this.finalProfit = finalProfit;
    }

    public Double getFinalProfitForTable() {
        return finalProfitForTable.get();
    }

    public SimpleDoubleProperty finalProfitForTableProperty() {
        return finalProfitForTable;
    }

    public void setFinalProfitForTable(int finalProfitForTable) {
        this.finalProfitForTable.set(finalProfitForTable);
    }
}