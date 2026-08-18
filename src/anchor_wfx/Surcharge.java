package anchor_wfx;

import javafx.beans.property.*;

/**
 * Surcharge model for ANCHOR ADS — Billings & Documentation module.
 */
public class Surcharge {

    private static int idCounter = 1;

    private final IntegerProperty id;
    private final StringProperty  name;
    private final DoubleProperty  defaultAmount;

    public Surcharge(String name, double defaultAmount) {
        this.id            = new SimpleIntegerProperty(idCounter++);
        this.name          = new SimpleStringProperty(name);
        this.defaultAmount = new SimpleDoubleProperty(defaultAmount);
    }

    public Surcharge(int id, String name, double defaultAmount) {
        this.id            = new SimpleIntegerProperty(id);
        this.name          = new SimpleStringProperty(name);
        this.defaultAmount = new SimpleDoubleProperty(defaultAmount);
    }

    public static void resetCounter(int next) { idCounter = next; }

    public int    getId()            { return id.get(); }
    public String getName()          { return name.get(); }
    public double getDefaultAmount() { return defaultAmount.get(); }

    public void setName(String v)          { name.set(v); }
    public void setDefaultAmount(double v) { defaultAmount.set(v); }

    public IntegerProperty idProperty()            { return id; }
    public StringProperty  nameProperty()          { return name; }
    public DoubleProperty  defaultAmountProperty() { return defaultAmount; }

    @Override public String toString() { return name.get(); }
}