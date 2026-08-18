package anchor_wfx;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Shipment {

    private static int idCounter = 1;

    public enum Status {
        PENDING_DEPARTURE("Pending Departure"),
        DEPARTED("Departed"),
        IN_TRANSIT("In Transit"),
        ARRIVED("Arrived"),
        CUSTOMS_CLEARED("Customs Cleared"),
        DELIVERED("Delivered"),
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
            return PENDING_DEPARTURE;
        }

        public static Status fromName(String dbValue) {
            if (dbValue == null) {
                return PENDING_DEPARTURE;
            }
            for (Status s : values()) {
                if (s.name().equalsIgnoreCase(dbValue)) {
                    return s;
                }
            }
            return PENDING_DEPARTURE;
        }

    }

    private final IntegerProperty id;
    private final IntegerProperty bookingId;
    private IntegerProperty vesselId;
    private IntegerProperty routeId;
    private IntegerProperty containerId;
    private final ObjectProperty<Status> status;
    private final ObjectProperty<LocalDate> departureDate;
    private final ObjectProperty<LocalDate> arrivalDate;
    private final List<ShipmentMilestone> milestones;

    public Shipment(int bookingId, int vesselId, int routeId, int containerId,
            Status status, LocalDate departureDate, LocalDate arrivalDate) {
        this.id = new SimpleIntegerProperty(idCounter++);
        this.bookingId = new SimpleIntegerProperty(bookingId);
        this.vesselId = new SimpleIntegerProperty(vesselId);
        this.routeId = new SimpleIntegerProperty(routeId);
        this.containerId = new SimpleIntegerProperty(containerId);
        this.status = new SimpleObjectProperty<>(status);
        this.departureDate = new SimpleObjectProperty<>(departureDate);
        this.arrivalDate = new SimpleObjectProperty<>(arrivalDate);
        this.milestones = new ArrayList<>();
    }

    public Shipment(int id, int bookingId, int vesselId, int routeId, int containerId,
            Status status, LocalDate departureDate, LocalDate arrivalDate) {
        this.id = new SimpleIntegerProperty(id);
        this.bookingId = new SimpleIntegerProperty(bookingId);
        this.vesselId = new SimpleIntegerProperty(vesselId);
        this.routeId = new SimpleIntegerProperty(routeId);
        this.containerId = new SimpleIntegerProperty(containerId);
        this.status = new SimpleObjectProperty<>(status);
        this.departureDate = new SimpleObjectProperty<>(departureDate);
        this.arrivalDate = new SimpleObjectProperty<>(arrivalDate);
        this.milestones = new ArrayList<>();
    }

    public static void resetCounter(int next) {
        idCounter = next;
    }

    public int getId() {
        return id.get();
    }

    public int getBookingId() {
        return bookingId.get();
    }

    public int getVesselId() {
        return vesselId.get();
    }

    public int getRouteId() {
        return routeId.get();
    }

    public int getContainerId() {
        return containerId.get();
    }

    public Status getStatus() {
        return status.get();
    }

    public String getStatusDisplay() {
        return status.get().getDisplay();
    }

    public LocalDate getDepartureDate() {
        return departureDate.get();
    }

    public LocalDate getArrivalDate() {
        return arrivalDate.get();
    }

    public List<ShipmentMilestone> getMilestones() {
        return milestones;
    }

    public void setVesselId(int v) {
        vesselId.set(v);
    }

    public void setRouteId(int v) {
        routeId.set(v);
    }

    public void setContainerId(int v) {
        containerId.set(v);
    }

    public void setStatus(Status v) {
        status.set(v);
    }

    public void setDepartureDate(LocalDate v) {
        departureDate.set(v);
    }

    public void setArrivalDate(LocalDate v) {
        arrivalDate.set(v);
    }

    public void addMilestone(ShipmentMilestone m) {
        milestones.add(m);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public IntegerProperty bookingIdProperty() {
        return bookingId;
    }

    public IntegerProperty vesselIdProperty() {
        return vesselId;
    }

    public IntegerProperty routeIdProperty() {
        return routeId;
    }

    public IntegerProperty containerIdProperty() {
        return containerId;
    }

    public ObjectProperty<Status> statusProperty() {
        return status;
    }

    public ObjectProperty<LocalDate> departureDateProperty() {
        return departureDate;
    }

    public ObjectProperty<LocalDate> arrivalDateProperty() {
        return arrivalDate;
    }
}
