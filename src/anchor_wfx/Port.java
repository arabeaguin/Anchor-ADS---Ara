package anchor_wfx;

import javafx.beans.property.*;

/**
 * Port model for ANCHOR ADS — Manage Vessels & Fleet module.
 */
public class Port {

    private static int idCounter = 1;

    private final IntegerProperty id;
    private final StringProperty  name;
    private final StringProperty  country;
    private final StringProperty  city;

    // ── New port constructor ───────────────────────────────────────────────
    public Port(String name, String country, String city) {
        this.id      = new SimpleIntegerProperty(idCounter++);
        this.name    = new SimpleStringProperty(name);
        this.country = new SimpleStringProperty(country);
        this.city    = new SimpleStringProperty(city);
    }

    // ── Edit constructor ───────────────────────────────────────────────────
    public Port(int id, String name, String country, String city) {
        this.id      = new SimpleIntegerProperty(id);
        this.name    = new SimpleStringProperty(name);
        this.country = new SimpleStringProperty(country);
        this.city    = new SimpleStringProperty(city);
    }

    public static void resetCounter(int next) { idCounter = next; }

    // ── Getters ────────────────────────────────────────────────────────────
    public int    getId()      { return id.get(); }
    public String getName()    { return name.get(); }
    public String getCountry() { return country.get(); }
    public String getCity()    { return city.get(); }

    // ── Setters ────────────────────────────────────────────────────────────
    public void setName(String v)    { name.set(v); }
    public void setCountry(String v) { country.set(v); }
    public void setCity(String v)    { city.set(v); }

    // ── Properties ─────────────────────────────────────────────────────────
    public IntegerProperty idProperty()      { return id; }
    public StringProperty  nameProperty()    { return name; }
    public StringProperty  countryProperty() { return country; }
    public StringProperty  cityProperty()    { return city; }

    /** Used in ComboBox displays throughout the UI */
    @Override
    public String toString() {
        return name.get() + " — " + city.get() + ", " + country.get();
    }
}