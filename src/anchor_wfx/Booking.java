package anchor_wfx;

import javafx.beans.property.*;
import java.time.LocalDate;

/**
 * Booking model for ANCHOR ADS — Bookings & Shipments module.
 */
public class Booking {

    private static int idCounter = 1;

    // ── Status Enum ────────────────────────────────────────────────────────
    public enum Status {
        PENDING("Pending"),
        CONFIRMED("Confirmed"),
        CONVERTED("Converted"),
        VOIDED("Voided"),
        CANCELLED("Cancelled");

        private final String display;

        Status(String d) {
            this.display = d;
        }

        public String getDisplay() {
            return display;
        }

        public static Status fromDisplay(String d) {
            for (Status s : values()) {
                if (s.display.equals(d)) {
                    return s;
                }
            }
            return PENDING;
        }

        public static Status fromName(String dbValue) {
            if (dbValue == null) {
                return PENDING;
            }
            for (Status s : values()) {
                if (s.name().equalsIgnoreCase(dbValue)) {
                    return s;
                }
            }
            return PENDING;
        }
    }

    private final IntegerProperty id;
    private final IntegerProperty shipperId;
    private final IntegerProperty consigneeId;
    private final IntegerProperty cargoId;
    private final ObjectProperty<Status> status;
    private final StringProperty notes;
    private final ObjectProperty<LocalDate> createdDate;

    // ── New booking constructor (auto-increment ID) ────────────────────────
    public Booking(int shipperId, int consigneeId, int cargoId,
            Status status, String notes) {
        this.id = new SimpleIntegerProperty(idCounter++);
        this.shipperId = new SimpleIntegerProperty(shipperId);
        this.consigneeId = new SimpleIntegerProperty(consigneeId);
        this.cargoId = new SimpleIntegerProperty(cargoId);
        this.status = new SimpleObjectProperty<>(status);
        this.notes = new SimpleStringProperty(notes);
        this.createdDate = new SimpleObjectProperty<>(LocalDate.now());
    }

    // ── Edit constructor (preserve existing ID) ────────────────────────────
    public Booking(int id, int shipperId, int consigneeId, int cargoId,
            Status status, String notes, LocalDate createdDate) {
        this.id = new SimpleIntegerProperty(id);
        this.shipperId = new SimpleIntegerProperty(shipperId);
        this.consigneeId = new SimpleIntegerProperty(consigneeId);
        this.cargoId = new SimpleIntegerProperty(cargoId);
        this.status = new SimpleObjectProperty<>(status);
        this.notes = new SimpleStringProperty(notes);
        this.createdDate = new SimpleObjectProperty<>(createdDate);
    }

    public static void resetCounter(int next) {
        idCounter = next;
    }

    // ── Getters ────────────────────────────────────────────────────────────
    public int getId() {
        return id.get();
    }

    public int getShipperId() {
        return shipperId.get();
    }

    public int getConsigneeId() {
        return consigneeId.get();
    }

    public int getCargoId() {
        return cargoId.get();
    }

    public Status getStatus() {
        return status.get();
    }

    public String getStatusDisplay() {
        return status.get().getDisplay();
    }

    public String getNotes() {
        return notes.get();
    }

    public LocalDate getCreatedDate() {
        return createdDate.get();
    }

    // ── Setters ────────────────────────────────────────────────────────────
    public void setShipperId(int v) {
        shipperId.set(v);
    }

    public void setConsigneeId(int v) {
        consigneeId.set(v);
    }

    public void setCargoId(int v) {
        cargoId.set(v);
    }

    public void setStatus(Status v) {
        status.set(v);
    }

    public void setNotes(String v) {
        notes.set(v);
    }

    // ── Properties ─────────────────────────────────────────────────────────
    public IntegerProperty idProperty() {
        return id;
    }

    public IntegerProperty shipperIdProperty() {
        return shipperId;
    }

    public IntegerProperty consigneeIdProperty() {
        return consigneeId;
    }

    public IntegerProperty cargoIdProperty() {
        return cargoId;
    }

    public ObjectProperty<Status> statusProperty() {
        return status;
    }

    public StringProperty notesProperty() {
        return notes;
    }

    public ObjectProperty<LocalDate> createdDateProperty() {
        return createdDate;
    }
}
