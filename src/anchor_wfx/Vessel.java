package anchor_wfx;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Vessel {

    private static int idCounter = 1;

    // ── Status Enum (maps to DB enum: active / docked / under_maintenance) ─
    public enum Status {
        ACTIVE           ("Active"),
        DOCKED           ("Docked"),
        UNDER_MAINTENANCE("Under Maintenance");

        private final String display;
        Status(String d) { this.display = d; }
        public String getDisplay() { return display; }

        public static Status fromDisplay(String d) {
            for (Status s : values()) if (s.display.equals(d)) return s;
            return ACTIVE;
        }
        
        public static Status fromDb(String dbValue) {
            switch (dbValue.toLowerCase()) {
                case "active": return ACTIVE;
                case "docked": return DOCKED;
                case "under_maintenance": return UNDER_MAINTENANCE;
                default: return ACTIVE;
            }
        }
        
        public String toDbValue() {
            switch (this) {
                case ACTIVE: return "active";
                case DOCKED: return "docked";
                case UNDER_MAINTENANCE: return "under_maintenance";
                default: return "active";
            }
        }
    }

    private final IntegerProperty        id;
    private final StringProperty         name;
    private final StringProperty         vesselType;
    private final DoubleProperty         capacityWeight;
    private final DoubleProperty         capacityVolume;
    private final ObjectProperty<Status> status;
    private final StringProperty         registrationNumber;
    private final ObservableList<Crew> assignedCrew = FXCollections.observableArrayList();

    // ── New vessel constructor (auto-increment ID) ─────────────────────────
    public Vessel(String name, String vesselType,
                  double capacityWeight, double capacityVolume,
                  Status status, String registrationNumber) {
        this.id                 = new SimpleIntegerProperty(idCounter++);
        this.name               = new SimpleStringProperty(name);
        this.vesselType         = new SimpleStringProperty(vesselType);
        this.capacityWeight     = new SimpleDoubleProperty(capacityWeight);
        this.capacityVolume     = new SimpleDoubleProperty(capacityVolume);
        this.status             = new SimpleObjectProperty<>(status);
        this.registrationNumber = new SimpleStringProperty(registrationNumber);
    }

    // ── Edit constructor (preserve existing ID) ────────────────────────────
    public Vessel(int id, String name, String vesselType,
                  double capacityWeight, double capacityVolume,
                  Status status, String registrationNumber) {
        this.id                 = new SimpleIntegerProperty(id);
        this.name               = new SimpleStringProperty(name);
        this.vesselType         = new SimpleStringProperty(vesselType);
        this.capacityWeight     = new SimpleDoubleProperty(capacityWeight);
        this.capacityVolume     = new SimpleDoubleProperty(capacityVolume);
        this.status             = new SimpleObjectProperty<>(status);
        this.registrationNumber = new SimpleStringProperty(registrationNumber);
    }

    public static void resetCounter(int next) { idCounter = next; }

    // ── Getters ────────────────────────────────────────────────────────────
    public int    getId()                 { return id.get(); }
    public String getName()               { return name.get(); }
    public String getVesselType()         { return vesselType.get(); }
    public double getCapacityWeight()     { return capacityWeight.get(); }
    public double getCapacityVolume()     { return capacityVolume.get(); }
    public Status getStatus()             { return status.get(); }
    public String getStatusDisplay()      { return status.get().getDisplay(); }
    public String getRegistrationNumber() { return registrationNumber.get(); }
    public ObservableList<Crew> getAssignedCrew() { return assignedCrew; }
    public String getCrewNames() {
        if (assignedCrew.isEmpty()) return "No Crew";
        return assignedCrew.stream().map(Crew::getName).reduce((a, b) -> a + ", " + b).orElse("");
    }
    public int getCrewCount() { return assignedCrew.size(); }
    

    // ── Setters ────────────────────────────────────────────────────────────
    public void setName(String v)               { name.set(v); }
    public void setVesselType(String v)         { vesselType.set(v); }
    public void setCapacityWeight(double v)     { capacityWeight.set(v); }
    public void setCapacityVolume(double v)     { capacityVolume.set(v); }
    public void setStatus(Status v)             { status.set(v); }
    public void setRegistrationNumber(String v) { registrationNumber.set(v); }
    public void setAssignedCrew(List<Crew> crewList) {
        this.assignedCrew.setAll(crewList);
    }

    // ── Properties ─────────────────────────────────────────────────────────
    public IntegerProperty        idProperty()                 { return id; }
    public StringProperty         nameProperty()               { return name; }
    public StringProperty         vesselTypeProperty()         { return vesselType; }
    public DoubleProperty         capacityWeightProperty()     { return capacityWeight; }
    public DoubleProperty         capacityVolumeProperty()     { return capacityVolume; }
    public ObjectProperty<Status> statusProperty()             { return status; }
    public StringProperty         registrationNumberProperty() { return registrationNumber; }
}