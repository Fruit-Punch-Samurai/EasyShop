package rh.yahia;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Product {
    private SimpleIntegerProperty prPrice;
    private SimpleDoubleProperty prAchatPrice;
    private SimpleStringProperty prCode;
    private SimpleStringProperty prName;
    private SimpleIntegerProperty prQuantity = new SimpleIntegerProperty(1);
    private SimpleIntegerProperty prStock = new SimpleIntegerProperty(0);

    Product(String code, String name, Double achatPrice, Integer price, Integer stock) {
        prCode = new SimpleStringProperty(code);
        prName = new SimpleStringProperty(name);
        prPrice = new SimpleIntegerProperty(price);
        prAchatPrice = new SimpleDoubleProperty(achatPrice);
        prStock = new SimpleIntegerProperty(stock);
    }

    Product(String name, Integer price, Integer quantity) {
        prName = new SimpleStringProperty(name);
        prPrice = new SimpleIntegerProperty(price);
        prQuantity = new SimpleIntegerProperty(quantity);
    }

    Product(String code, String name, Double achatPrice, Integer price) {
        prCode = new SimpleStringProperty(code);
        prName = new SimpleStringProperty(name);
        prAchatPrice = new SimpleDoubleProperty(achatPrice);
        prPrice = new SimpleIntegerProperty(price);
    }

    String getName() {
        return prName.get();
    }

    Integer getPrStock() {
        return prStock.get();
    }

    Integer getPrice() {
        return prPrice.get();
    }

    String getCode() {
        return prCode.get();
    }

    Integer getPrQuantity() {
        return prQuantity.get();
    }

    Double getPrAchatPrice() {
        return prAchatPrice.get();
    }

    public SimpleIntegerProperty prQuantityProperty() {
        return prQuantity;
    }

    public SimpleIntegerProperty prStockProperty() {
        return prStock;
    }

    public SimpleIntegerProperty prPriceProperty() {
        return prPrice;
    }

    public SimpleStringProperty prCodeProperty() {
        return prCode;
    }

    public SimpleStringProperty prNameProperty() {
        return prName;
    }

    public SimpleDoubleProperty prAchatPriceProperty() {
        return prAchatPrice;
    }

    public void setPrAchatPrice(int prAchatPrice) {
        this.prAchatPrice.set(prAchatPrice);
    }

    void setPrQuantity(int prQuantity) {
        this.prQuantity.set(prQuantity);
    }

    void setPrPrice(int prPrice) {
        this.prPrice.set(prPrice);
    }

    void setPrCode(String prCode) {
        this.prCode.set(prCode);
    }

    void setPrName(String prName) {
        this.prName.set(prName);
    }

    void setPrStock(int prStock) {
        this.prStock.set(prStock);
    }

}
