package anchor_wfx;

import javafx.beans.property.*;

/**
 * Shipping route model for ANCHOR ADS — Manage Vessels & Fleet module.
 */
public class Route {

    private static int idCounter = 1;

    private final IntegerProperty id;
    private final IntegerProperty originPortId;
    private final IntegerProperty destinationPortId;
    private final IntegerProperty transitDays;

    // ── New route constructor ──────────────────────────────────────────────
    public Route(int originPortId, int destinationPortId, int transitDays) {
        this.id                = new SimpleIntegerProperty(idCounter++);
        this.originPortId      = new SimpleIntegerProperty(originPortId);
        this.destinationPortId = new SimpleIntegerProperty(destinationPortId);
        this.transitDays       = new SimpleIntegerProperty(transitDays);
    }

    // ── Edit constructor ───────────────────────────────────────────────────
    public Route(int id, int originPortId, int destinationPortId, int transitDays) {
        this.id                = new SimpleIntegerProperty(id);
        this.originPortId      = new SimpleIntegerProperty(originPortId);
        this.destinationPortId = new SimpleIntegerProperty(destinationPortId);
        this.transitDays       = new SimpleIntegerProperty(transitDays);
    }

    public static void resetCounter(int next) { idCounter = next; }

    // ── Getters ────────────────────────────────────────────────────────────
    public int getId()                { return id.get(); }
    public int getOriginPortId()      { return originPortId.get(); }
    public int getDestinationPortId() { return destinationPortId.get(); }
    public int getTransitDays()       { return transitDays.get(); }

    // ── Setters ────────────────────────────────────────────────────────────
    public void setOriginPortId(int v)      { originPortId.set(v); }
    public void setDestinationPortId(int v) { destinationPortId.set(v); }
    public void setTransitDays(int v)       { transitDays.set(v); }

    // ── Properties ─────────────────────────────────────────────────────────
    public IntegerProperty idProperty()                { return id; }
    public IntegerProperty originPortIdProperty()      { return originPortId; }
    public IntegerProperty destinationPortIdProperty() { return destinationPortId; }
    public IntegerProperty transitDaysProperty()       { return transitDays; }
}