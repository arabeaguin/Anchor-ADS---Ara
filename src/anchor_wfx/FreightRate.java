package anchor_wfx;

import javafx.beans.property.*;

/**
 * FreightRate model for ANCHOR ADS — Billings & Documentation module.
 */
public class FreightRate {

    private static int idCounter = 1;

    private final IntegerProperty id;
    private final StringProperty  name;
    private final DoubleProperty  baseAmount;

    public FreightRate(String name, double baseAmount) {
        this.id         = new SimpleIntegerProperty(idCounter++);
        this.name       = new SimpleStringProperty(name);
        this.baseAmount = new SimpleDoubleProperty(baseAmount);
    }

    public FreightRate(int id, String name, double baseAmount) {
        this.id         = new SimpleIntegerProperty(id);
        this.name       = new SimpleStringProperty(name);
        this.baseAmount = new SimpleDoubleProperty(baseAmount);
    }

    public static void resetCounter(int next) { idCounter = next; }

    public int    getId()         { return id.get(); }
    public String getName()       { return name.get(); }
    public double getBaseAmount() { return baseAmount.get(); }

    public void setName(String v)       { name.set(v); }
    public void setBaseAmount(double v) { baseAmount.set(v); }

    public IntegerProperty idProperty()         { return id; }
    public StringProperty  nameProperty()       { return name; }
    public DoubleProperty  baseAmountProperty() { return baseAmount; }

    @Override public String toString() { return name.get(); }
}