package anchor_wfx;

import java.time.LocalDate;

/**
 * Represents a logged milestone event on a Shipment.
 */
public class ShipmentMilestone {

    // ── Milestone Type Enum ────────────────────────────────────────────────
    public enum Type {
        BOOKING_CONFIRMED("Booking Confirmed"),
        DEPARTED         ("Departed"),
        ARRIVED          ("Arrived"),
        CUSTOMS_CLEARED  ("Customs Cleared"),
        DELIVERED        ("Delivered");

        private final String display;
        Type(String d) { this.display = d; }
        public String getDisplay() { return display; }

        public static Type fromDisplay(String d) {
            for (Type t : values()) if (t.display.equals(d)) return t;
            return BOOKING_CONFIRMED;
        }
    }

    private final Type      type;
    private final LocalDate date;
    private final String    remarks;

    public ShipmentMilestone(Type type, LocalDate date, String remarks) {
        this.type    = type;
        this.date    = date;
        this.remarks = remarks;
    }

    // ── Getters ────────────────────────────────────────────────────────────
    public Type      getType()        { return type; }
    public String    getTypeDisplay() { return type.getDisplay(); }
    public LocalDate getDate()        { return date; }
    public String    getRemarks()     { return remarks; }
}